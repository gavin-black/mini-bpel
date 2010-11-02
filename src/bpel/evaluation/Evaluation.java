package bpel.evaluation;
import bpel.database.Variable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Evaluation of XPath expressions.
 *
 * @author Mike Brenner
 * @author <a href="mailto:mikeb@mitre.org">gblack@mitre.org</a>
 */
public class Evaluation {
	private Variable junkVar = new Variable();
	private Ket junkKet = new Ket("i0");
	private int                magicNumber = -1;
	private String             eut      = "";
	private int                eut_upto = -1;
	private int                eut_length = -1;
	private LinkedList<String> code   = new LinkedList<String>();
	private LinkedList<String> stack  = new LinkedList<String>();
	private boolean            tracing= false; // Trace top level flow
	private boolean            testing= false; // Trace flow of execution
	private String             token  = "what!";

	public final String[] eval(int magicNumber, String expressionUnderTest){
		this.magicNumber = magicNumber;
		this.eut=expressionUnderTest+"~";
		code   = new LinkedList<String>();
		stack  = new LinkedList<String>();
		eut_length = eut.length();
		eut_upto=0;
		getNextToken();
		getExpr();
		String[] combinedResult = execute();
		String type = combinedResult[1];
		if      ("i".equals(type)) type="int";
		else if ("s".equals(type)) type="string";
		else if ("b".equals(type)) type="boolean";
		else if ("f".equals(type)) type="float";
		else if ("d".equals(type)) type="dateTime";
		else if ("p".equals(type)) type="duration";
		else if ("y".equals(type)) type="ybinary";
		else if ("x".equals(type)) type="xml";
		combinedResult[1] = type;
		return combinedResult;
	}
	private final char lexGet(){
		if (eut_upto >= eut_length)
			stop (true, 1, "lexGet: eut_upto="+eut_upto+ " but eut_length is only " + eut_length);
		return eut.charAt(eut_upto);
	}
	private final void getNextToken(){
		token=consume();
	}
	private final String consume(){
		char c = lexGet();
		while (-1 != "  \t\n\r".indexOf(c)){
			eut_upto++; c=lexGet(); // Skip unquoted white space.
		}
		if ('~' == c){
			if (eut_upto != eut_length-1)
				stop (false, 1, "Try not using something other than ~ as an operator in the XPATH language. " +
					"eut_upto="+eut_upto+" eut_length="+eut_length+"eut=["+eut+"]");
			return "~";
		} else if ('$' == c){
			StringBuilder sb = new StringBuilder();
			while (-1 != "$-._0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ".indexOf(c))
				{sb.append(c); eut_upto++; c = lexGet();}
			return sb.toString();
		} else if ('\'' == c){
			StringBuilder sb = new StringBuilder(); eut_upto++; c=lexGet();
			while ('\'' != c)
				{sb.append(c); eut_upto++; c=lexGet();}
			eut_upto++; c=lexGet();
			return '\''+sb.toString()+'\'';
		} else if ('"' == c){
			StringBuilder sb = new StringBuilder(); eut_upto++; c=lexGet();
			while ('"' != c)
				{sb.append(c); eut_upto++; c=lexGet();}
			eut_upto++; c=lexGet();
			return '"'+sb.toString()+'"';
		} else if (-1 != "0123456789".indexOf(c)){
			StringBuilder sb = new StringBuilder();
			while (-1 != ".0123456789".indexOf(c)){
				sb.append(c); eut_upto++; c=lexGet();}
			return sb.toString();
		} else if (-1 != "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c)){
			StringBuilder sb = new StringBuilder();
			while (-1 != "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c))
				{sb.append(c); eut_upto++; c=lexGet();}
			return sb.toString();
		} else if ('!' == c){
			eut_upto++; c=lexGet();
			if ('=' != c)
				stop(false,1,"Try using not $x instead of ! $x in the XPATH language");
			eut_upto++;
			return "!=";
		} else if ('<' == c){
			eut_upto++; c=lexGet();
			if ('=' == c) {eut_upto++; return "<="; } else return "<";
		} else if ('>' == c){
			eut_upto++; c=lexGet();
			if ('=' == c) {eut_upto++; return ">="; } else return ">";
		} else if ('=' == c){
			eut_upto++; c=lexGet();
			if ('=' == c)
				stop (false, 2, "Try using $a=$b instead of $a==$b in the XPATH language. ");
			return "=";
		} else if ('/' == c){
			stop (false, 1,
				"Try using 5 div 2 which equals 2.5, or 5 quo 2 which equals 2, or 5 mod 2 which equals 1 in the XPATH language instead of a forward slash.");
		} else if ('&'==c){
			stop(false, 1, "Problem with this ampersand: \n" +
				"Try using ($s and $t) instead of ($s & $t); \n" +
				"Try using ($s and $t) instead of ($s && $t); \n" +
				"Try using concat($s,$t) instead of ($s & $t); \n" +
				"Try using XML character expressions in quoted strings; ]n"+
				"in the XPATH language.");
		} else if ('|'==c){
			stop(false, 1, "Try using $p or $q instead of | or || in the XPATH language.");
		} else if (-1 != "(),*+-^".indexOf(c)){
			eut_upto++;
			return c+"";
		} else if (-1 != "`.;:@#%\\_[]{}?".indexOf(c)){
			stop(false, 1, "Prohibited lexical character [" + c + "] in the XPATH language.");
		} else
			stop (false, 1, "Illegal character ["+c+"] in the XPATH language.");
		return "this can never happen";
	}
	private final void genRaw(String message){
		code.push(message);
	}
	private final void gen(String operation){
		genRaw('`'+operation);
	}
	private final void getAndExpr(){
		getOrExpr();
		getAndExprFollower();}
	private final void getAndExprFollower(){
		if ("and".equals(token))
			{getNextToken();getOrExpr();genRaw("i2");gen("and");getAndExprFollower();}
	}
	private final void getOrExpr(){getRelExpr();getOrExprFollower();}
	private final void getOrExprFollower(){
		if ("or" .equals(token))
			{getNextToken();getRelExpr();genRaw("i2");gen("or");getOrExprFollower();}
		else if ("xor".equals(token))
			{getNextToken();getRelExpr();genRaw("i2");gen("xor");getOrExprFollower();}
	}
	private final void getRelExpr(){getExprExpr();getRelExprFollower();}
	private final void getRelExprFollower(){
		if ("=" .equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("eq");getRelExprFollower();}
		else if ("!=".equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("ne");getRelExprFollower();}
		else if ("<" .equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("lt");getRelExprFollower();}
		else if ("<=".equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("le");getRelExprFollower();}
		else if (">" .equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("gt");getRelExprFollower();}
		else if (">=".equals(token))
			{getNextToken();getExprExpr();genRaw("i2");gen("ge");getRelExprFollower();}
	}
	private final void getExprExpr(){
		if ("+".equals(token))
			{getNextToken();} // Skipping a possible single preliminary + sign.
		boolean isMinusPresent=false;
		if ("-".equals(token))  // Recognizing a possible single preliminary - sign.
			{getNextToken();isMinusPresent=true;}
		getTermExpr();
		if (isMinusPresent)
			{genRaw("i1");gen("negate");}
		getExprExprFollower();
	}
	private final void getExprExprFollower(){
		if ("+" .equals(token))
			{getNextToken(); getTermExpr();genRaw("i2");gen("plus");getExprExprFollower();}
		else if ("-".equals(token))
			{getNextToken();getTermExpr();genRaw("i2");gen("minus");getExprExprFollower();}
	}
	private final void getTermExpr(){
		getFactorExpr();getTermExprFollower();
	}
	private final void getTermExprFollower(){
		if ("*" .equals(token))
			{getNextToken();getFactorExpr();genRaw("i2");gen("times");getTermExprFollower();}
		else if ("div".equals(token))
			{getNextToken();getFactorExpr();genRaw("i2");gen("div");  getTermExprFollower();}
		else if ("quo".equals(token))
			{getNextToken();getFactorExpr();genRaw("i2");gen("quo");  getTermExprFollower();}
		else if ("mod".equals(token))
			{getNextToken();getFactorExpr();genRaw("i2");gen("mod");  getTermExprFollower();}
	}
	private final void getFactorExpr(){
		getPrimary();getFactorExprFollower();
	}
	private final void getFactorExprFollower(){
		if ("^" .equals(token))
			{getNextToken(); getPrimary();genRaw("i2");gen("power");getFactorExprFollower();}
	}
	private final boolean getPrimaryFunction(String funct){
		String x=token;
		if (funct.equals(x)){
			getNextToken();
			int argCount=0;
			mustBe("("); getNextToken();
			if ( ! ")".equals(token))
				while (true){
					getExpr(); argCount++;
					x=token; if ( ! ",".equals(x)) break;
					getNextToken();
				}
			mustBe(")");
			getNextToken();
			genRaw("i"+argCount);
			gen(funct);
			return true;
		} else
			return false;
	}
	private final void getPrimary(){
		String x=token;
		if (null==x || 0==x.length())
			stop(false,2,"missing Primary");
		char x1 = x.charAt(0);
		if      ("(".equals(x)){getNextToken(); getExpr(); mustBe(")"); getNextToken();}
		else if (-1 != "0123456789".indexOf(x1)){getNextToken();genRaw("f"+x);}
		else if ('"' ==x1){getNextToken();genRaw("s" + x.substring(1,x.length()-1));}
		else if ('\''==x1){getNextToken();genRaw("s" + x.substring(1,x.length()-1));}
		else if ('$' ==x1){getNextToken();genRaw(x);}
		else if (getPrimaryFunction("abs"));
		else if (getPrimaryFunction("binary"));
		else if (getPrimaryFunction("ceiling"));
		else if (getPrimaryFunction("contains"));
		else if (getPrimaryFunction("equals"));
		else if (getPrimaryFunction("concat"));
		else if (getPrimaryFunction("false"));
		else if (getPrimaryFunction("floor"));
		else if (getPrimaryFunction("L"));
		else if (getPrimaryFunction("lang"));
		else if (getPrimaryFunction("length"));
		else if (getPrimaryFunction("negate"));
		else if (getPrimaryFunction("normalize-space"));
		else if (getPrimaryFunction("not"));
		else if (getPrimaryFunction("number"));
		else if (getPrimaryFunction("round"));
		else if (getPrimaryFunction("string"));
		else if (getPrimaryFunction("string-length"));
		else if (getPrimaryFunction("substring"));
		else if (getPrimaryFunction("substring-before"));
		else if (getPrimaryFunction("substring-after"));
		else if (getPrimaryFunction("translate"));
		else if (getPrimaryFunction("true"));
		else if (getPrimaryFunction("unparsed-entity-uri"));
		else
			stop (false, 2, "getPrimary: I do not understand the word [" + x + "]");
	}
	private final void mustBe(String token){
		String x=token;
		if (! x.equals(token))
			stop (false, 2, "Missing [" + token + "]. Instead found ["+ x +"]");
	}
	private final void getExpr(){
		getAndExpr();
	}
	private final String codePop(){
		return code.removeLast();
	}
	private final String stackPop(){
		return stack.pop();
	}
	private final StringBuilder codeImage(){
		StringBuilder s = new StringBuilder();
		s.append("CODE {");
		for (String st: code)
			s.append(st).append(",");
		s.append("} ");
		return s;
	}
	private final StringBuilder stackImage(){
		StringBuilder s = new StringBuilder();
		s.append("EXEC ").append(" [");
		for (String st: stack)
			s.append(st).append(",");
		s.append("]");
		return s;
	}
	private final void dump(String message){
		StringBuilder s = new StringBuilder().append(message).append(" ").append(codeImage()).append(" ").append(stackImage());
		tell(s.toString());
	}
	private final static void tell(String message){
		System.out.println(message);
	}
	private final double toRound(String a){
		double b=toNumber(a);
		return Math.round(b);
	}
	private final String toString(double number){
		String s = "" + number;
		if (s.endsWith(".0"))
			return s.substring(0, s.length()-2);
		return s;
	}
	private final Double toNumber(String x){
		if (null==x)
			stop (true, 3, "Bad binary null");
		double d = Double.parseDouble(x);
		return d;
	}
	private final String toBoolean(double x){
		if (0.0 == x)
			return "0";
		else
			return "1";
	}
	private final String toBoolean(String x){
		if (null==x)
			stop (true, 3, "toBinary: Bad binary null");
		if ("0".equals(x))
			return "0";
		else if ("".equals(x))
			return "0";
		else
			return "1";
	}
	private final void doBinaryNumber(String op,String[] args,String[] types){
		String value1 = args[0]; String value0 = args[1];
		double val1 = Double.parseDouble(value1); double val0 = Double.parseDouble(value0);
		double result=-1;
		if (types[0]=="b" || types[1]=="b")
			stop(false,3,"Try using the number function before doing arithmetic on booleans, in the XPATH language.");
		if (types[0]!="i" && types[0]!="f")
			if (false) System.out.println("Might have to use the number function with strings with +-* div quo mod operators");
		if (types[1]!="i" && types[1]!="f")
			if (false) System.out.println("Might have to use the number function with strings with +-* div quo mod operators");
		if      ("`plus" .equals(op)) result=val0+val1;
		else if ("`minus".equals(op)) result=val0-val1;
		else if ("`times".equals(op)) result=val0*val1;
		else if ("`power".equals(op)) result=Math.pow(val0,val1);
		else if ("`quo"  .equals(op)) result=(long) (val0/val1); // quo is the integer div
		else if ("`div"  .equals(op)) result=val0/val1;          // div is the floating point quotient (not the integer div)
		else if ("`mod"  .equals(op)) result=((long) val0) % (long) val1; // mod = n-((n quo m)*m
		else stop (true, 3, "No such binary operator["+op+"]");
		String resultAsString = toString(result);
		String typer = (-1 == resultAsString.indexOf('.'))?"i":"f";
		stack.push(typer + resultAsString);
	}
	private final void doBinaryBoolean(String op,String[] args,String[] types){
		String value1 = args[0]; String value0 = args[1];
		boolean bra1=false; boolean bra2=false;
		if ("b".equals(types[0]) || "f".equals(types[0]) || "i".equals(types[0]))
			bra1=true;
		if ("b".equals(types[1]) || "f".equals(types[1]) || "i".equals(types[1]))
			bra2=true;
		if (bra1 && bra2){ // number-number comparison
			double val1 = Double.parseDouble(value1);
			double val0 = Double.parseDouble(value0);
			double result=-1;
			if      ("`eq"   .equals(op)) result=val0==val1?1:0;
			else if ("`ne"   .equals(op)) result=val0!=val1?1:0;
			else if ("`ge"   .equals(op)) result=val0>=val1?1:0;
			else if ("`gt"   .equals(op)) result=val0>val1?1:0;
			else if ("`le"   .equals(op)) result=val0<=val1?1:0;
			else if ("`lt"   .equals(op)) result=val0<val1?1:0;
			else if ("`and"  .equals(op)) result=val0*val1;
			else if ("`or"   .equals(op)) result=(val0+val1>0)?1:0;
			else if ("`xor"  .equals(op)) result=(val0!=val1)?1:0;
			else stop (true, 3, "No such binary boolean operator["+op+"]");
			stack.push("b" + toBoolean(result));
		} else if (bra1==bra2){ // string-string comparison
			int result=-1;
			if      ("`eq"   .equals(op)) result=    value0.equals(value1)?1:0;
			else if ("`ne"   .equals(op)) result=   !value0.equals(value1)?1:0;
			else if ("`ge"   .equals(op)) result= 0< value0.compareTo(value1)?1:0;
			else if ("`gt"   .equals(op)) result= 0<=value0.compareTo(value1)?1:0;
			else if ("`le"   .equals(op)) result= 0> value0.compareTo(value1)?1:0;
			else if ("`lt"   .equals(op)) result= 0>=value0.compareTo(value1)?1:0;
			else if ("`and"  .equals(op)) stop(false,3,"Cannot do boolean logical operators on non-numeric types");
			else if ("`or"   .equals(op)) stop(false,3,"Cannot do boolean logical operators on non-numeric types");
			else if ("`xor"  .equals(op)) stop(false,3,"Cannot do boolean logical operators on non-numeric types");
			else stop (true, 3, "No such binary boolean operator["+op+"]");
			stack.push("b" + toBoolean(result));
		} else stop(false,2,"Use the number or string function to avoid mixed-type string-number operations " +
			"[ (" + types[0] + ") " + args[0] + " " + op + " (" + types[1] + ") " + args[1] + " ]" +
			"in the XPATH language.");
	}
	private final void doBuiltInOp(String op){
		String nargsAsString = stackPop();
		if (null==nargsAsString || nargsAsString.length()<2 || 'i' != nargsAsString.charAt(0))
			stop(true,3,"Bad nargs integer type ["+nargsAsString+"]");
		int nargs = Integer.parseInt(nargsAsString.substring(1));
		String[] args = new String[nargs];
		String[] types = new String[nargs];
		for (int i=0; i<nargs; i++){
			String argOriginal = stackPop();
			if (null==argOriginal || 2 > argOriginal.length())
				stop(true,3, "Empty object on the code stack");
			types[i] = argOriginal.substring(0,1);
			args[i]  = argOriginal.substring(1); // could be empty
		}
		if ("`concat".equals(op)){
				String result = "";
				for (int i=nargs-1;i>=0;i--){
					result += args[i];
				}
				stack.push('s'+result);
		} else if (0==nargs){
			if      ("`true".equals(op)){stack.push("b1");}
			else if ("`false".equals(op)){stack.push("b0");}
			else stop (false, 3, "No such 0-argument operation or function as ["+op+"]");
		} else if (1==nargs){
			if      ("`not".equals(op)){stack.push('b'+toString(1-toNumber(args[0])));}
			else if ("`abs".equals(op)){stack.push('f'+toString(Math.abs(toNumber(args[0]))));}
			else if ("`negate".equals(op)){stack.push('f'+toString(-toNumber(args[0])));}
			else if ("`binary".equals(op)){stack.push('y'+toBoolean(args[0]));}
			else if ("`ceiling".equals(op)){stack.push('f'+toString(Math.ceil(toNumber(args[0]))));}
			else if ("`floor".equals(op)){stack.push("f"+toString(Math.floor(toNumber(args[0]))));}
			else if ("`round".equals(op)){stack.push("f"+toString(Math.round(toNumber(args[0]))));}
			else if ("`lang".equals(op)){stack.push('b'+(args[0].equals("en")?"1":"0"));}
			else if ("`number".equals(op)){stack.push('f'+toString(toNumber(args[0])));}
			else if ("`string".equals(op)){stack.push('s'+args[0]);}
			else if ("`string-length".equals(op)){stack.push("i"+(args[0].length()));}
			else if ("`unparsed-entity-uri".equals(op)){stack.push('s'+"");}
			else if ("`normalize-space".equals(op)){
				stack.push('s'+args[0].trim().replaceAll("[ \t\n0B\f\r]+", " "));}
			else stop (false, 3, "No such 1-argument operation or function as ["+op+"]");
		} else if (2==nargs){
			if (-1 != "#`plus#`minus#`times#`power#`quo#`div#`mod".indexOf(op))
				doBinaryNumber(op,args,types);
			else if (-1 != "#`eq#`ne#`ge#`gt#`le#`lt#`and#`or#`xor".indexOf(op))
				doBinaryBoolean(op,args,types);
			else if ("`contains".equals(op)){
				stack.push(-1==args[1].indexOf(args[0])?"b0":"b1");
			} else if ("`equals".equals(op)){
				stack.push(args[1].equals(args[0])?"b0":"b1");
			} else if ("`substring".equals(op)){
				int start=Integer.parseInt(args[0]) - 1;
				stack.push("s" + args[1].substring(start));
			} else if ("`substring-after".equals(op)){
				int i=args[1].indexOf(args[0]);
				String answer=args[1];
				if (-1!=i)
					answer=args[1].substring(i+args[0].length());
				stack.push("s"+answer);
			} else if ("`substring-before".equals(op)){
				int i=args[1].indexOf(args[0]);
				if (-1==i)
					stack.push("s"+args[1]);
				else
					stack.push("s"+args[1].substring(0, i));
			} else {
				stop (false, 3, "No such 2-argument operation or function as ["+op+"]");
			}
		} else if (3==nargs){
			if ("`substring".equals(op)){
				int start = Integer.parseInt(args[1]) - 1;
				int length = Integer.parseInt(args[0]);
				stack.push('s'+args[2].substring(start, start+length));
			} else if ("`translate".equals(op)){
				stop(true, 4, "Must program the translate(string, pattern_chars, replacement_chars) function run-time. Call a programmer.");
			} else {
				stop(false, 3, "No such 3-argument operation or function as ["+op+"]");
			}
		} else {
			stop(true, 3, "doFunction: No built-in operator or function except concat can have more than 3 arguments ["+op+"]");
		}
	}
	private final String[] execute(){
		int errorHalt=100;
		if (tracing){dump("start");}
		while (true){
			if (0==errorHalt--)	            stop(true, 3, "Stack recursion executed more than 100 levels");
			if (code.isEmpty())             break; // normal end-of-job(stop)
			String op = codePop();
			if      ("~".equals(op))        break;
			if (null==op || 0==op.length())	stop(true,3,"Null code op");
		    char opType = op.charAt(0);
			if      ('$'==opType){
 				String varName  = op.substring(1);
				String varValue = Variable.retrieve(magicNumber,varName); // variable value
				String varType  = Variable.type(magicNumber,varName);
				if ("string".equals(varType))
					stack.push("s" + varValue);
				else if ("int".equals(varType))
					stack.push("i" + varValue);
				else if ("short".equals(varType))
					stack.push("i" + varValue);
				else if ("long".equals(varType))
					stack.push("i" + varValue);
				else if ("float".equals(varType))
					stack.push("f" + varValue);
				else if ("boolean".equals(varType))
					stack.push("b" + varValue);
				else if ("date".equals(varType))
					stack.push("d" + varValue);
				else if ("dateTime".equals(varType))
					stack.push("d" + varValue);
				else if ("time".equals(varType))
					stack.push("d" + varValue);
				else if ("xml".equals(varType))
					stack.push("x" + varValue);
				else if ("duration".equals(varType))
					stack.push("p" + varValue);
				else if ("binary64Binary".equals(varType))
					stack.push("y" + varValue);
				else if ("hexBinary".equals(varType))
					stack.push("y" + varValue);
				else if ("binary".equals(varType))
					stack.push("y" + varValue);
				else stop(true,3,"Unknown exec stack type ["+ varType +"]from Variable");
			} else if ('`'==opType) doBuiltInOp(op); // function or operator
			else if ('s'==opType) stack.push(op); // string
			else if ('f'==opType) stack.push(op); // float number
			else if ('i'==opType) stack.push(op); // integer
			else if ('b'==opType) stack.push(op); // boolean
			else if ('d'==opType) stack.push(op); // date
			else if ('p'==opType) stack.push(op); // duration
			else if ('x'==opType) stack.push(op); // xml
			else if ('L'==opType); // skip no-op
			else                             stop(true,3,"Unknown execution op type ["+opType+"]");
			if (tracing) dump("	executed "+ op);
		}
		if (1!=stack.size())
			stop (true, 3, "Extra operands on the execution stack");
		String resultCombined = stack.pop();
		if (testing)
			tell("    == " + eut + " result=[" + resultCombined + "]");
		String resultType  = resultCombined.substring(0,1);
		String resultValue = resultCombined.substring(1);
		return new String[] {resultValue, resultType};
	}
	private final static void stop(boolean programmer, int stage, String message){
		throw new IllegalArgumentException(
			(programmer?"Programmer":"User") + " Level " + stage + " Error: " + message);
	}
	private static String[][] testVectorAssigns = {
		{"$x","3","string"},      {"$xyz","5","string"}, {"$var1", "6", "string"}};
	private static String[][] testVectorsAA = {
		{"1+2", "3", "i"},        {"55+44", "99", "i"}, {"2-1","1", "i"},
		{"1-21","-20", "i"},      {"1+11", "12", "i"},
		{"1<3", "1", "b"},        {"3<1", "0", "b"},
		{"3=1", "0", "b"},        {"3!=1", "1", "b"},
		{"1<=3", "1", "b"}, 	  {"3<=1", "0", "b"},
		{"1=2 or 2=2", "1","b"},  {"1=1 or 2=2", "1", "b"},
		{"1=2 or 2=1",  "0", "b"},
		{"1=1 or 2=1",  "1", "b"},
		{"1=2 and 2=2", "0", "b"},{"1=1 and 2=2", "1", "b"},
		{"1=2 and 2=1", "0", "b"},{"1=1 and 2=1", "0", "b"},
		{"1=2 xor 2=2", "1", "b"},{"1=1 xor 2=2", "0", "b"},
		{"1=2 xor 2=1", "0", "b"},{"1=1 xor 2=1", "1", "b"},
		{"1 and 1 and 1", "1", "b"},{"1 or 1 or 1", "1", "b"},
		{"1 and 1 and 0", "0", "b"},{"1 or 1 or 0", "1", "b"},
		{"true()", "1","b"},      {"false()", "0","b"},
		{"not (3=2)", "1","b"},   {"not (3=3)", "0","b"},
		{"5 div 2", "2.5", "f"},  {"5 quo 2", "2", "i"},
		{"5 mod 2", "1", "i"},	  {"2*3*4*5", "120", "i"},
		{"1+2+3", "6", "i"},      {"2*3*4", "24", "i"},
		{"1+2-3", "0", "i"},      {"120 quo 2 quo 3 quo 4", "5", "i"},
		{"120 div 2 div 3 div 4", "5", "i"},
		{"1+3*4", "13", "i"},     {"1*3+4+2^5", "39", "i"},
		{"1-2+3", "2", "i"},      {"1-2-3", "-4", "i"},
		{"$x+1", "4", "i"},       {"$xyz+1", "6", "i"},
		{"(7)","7", "f"},         {"(1+3)*4", "16", "i"},
		{"concat('abc','efg')", "abcefg", "s"},
		{"concat('ij', 'mn','pq')","ijmnpq", "s"},
		{"concat('pq','st','uv','56')","pqstuv56", "s"},
		{"concat('a','c','e','g','j','m')","acegjm", "s"},
		{"negate (3-2)", "-1", "f"},
		{"negate(negate (3-2))", "1", "f"},
		{"binary (6)", "1", "y"},
		{"ceiling (5.1)", "6", "f"},
		{"ceiling (5.0)", "5", "f"},
		{"floor (5.1)", "5", "f"},
		{"floor (5.999999)", "5", "f"},
		{"lang('fr')", "0", "b"},
		{"lang('en')", "1", "b"},
		{"number ('5.5')", "5.5", "f"},
		{"number (true())", "1", "f"},
		{"string ('5.5')", "5.5", "s"},
		{"string (true())", "1", "s"},
		{"string-length ('5.5')", "3", "i"},
		{"string-length (false())", "1", "i"},
		{"unparsed-entity-uri ('google.com')", "", "s"},
		{"contains('abcd', 'ab')","1", "b"},
		{"contains('abcd', 'bc')","1", "b"},
		{"contains('abcd', 'cd')","1", "b"},
		{"contains('abcd', 'bd')","0", "b"},
		{"substring('abcd',2)","bcd", "s"},
		{"substring-after ('abcd','bc')","d", "s"},
		{"substring-before('abcd','bc')","a", "s"},
		{"substring('abcd',1,2)","ab", "s"},
		{"substring('abcd',1,1)","a", "s"},
		{"normalize-space ('  u  x  ')", "u x", "s"},
		{"normalize-space ('\t\tm\t\tn\t\t')", "m n", "s"},
		{"abs(-5.5)", "5.5", "f"},
		{"$var1 + 1", "7", "i"},
		{"\"foo\"", "foo", "s"},
		{"1=1","1","b"}};
	private static String[][] testVectorsA = {
		{"1=1","1","b"}};
	private static String[] testVectorsB = {"!", "&"};

	private final static void regressionTest(){
		Evaluation e = new Evaluation();
		e.tracing=false;
		int L = testVectorAssigns.length;
		Variable.clearProcess(-1);
		for (int i=0; i<L; i++){
			String[] a = testVectorAssigns[i];
			Variable.add (-1, a[0].substring(1),a[2]);
			Variable.update (-1, a[0].substring(1),a[1]);
			if (e.tracing)
				System.err.println("			Assigned " + a[0] + "= (" + a[2] + ") " + a[1] + ";");
		}
		L = testVectorsAA.length;
		for (int i=0; i<L; i++){
			String[] a = testVectorsAA[i];
			e.should (a[0], a[1], a[2]);
		}
		tell("__________________________________________________________"); e.tracing=true;
		L = testVectorsA.length;
		for (int i=0; i<L; i++){
			String[] a = testVectorsA[i];
			e.should (a[0], a[1], a[2]);
		}
		e.tracing=false;
		L = testVectorsB.length;
		for (int i=0;i<L;i++){
			e.shouldNot(testVectorsB[i]);
		}
		e.tracing=false;
	}
	private final void should(String expression, String wanted, String desiredType){
		String[] result = eval(magicNumber, expression);
		String got = result[0];
		String type  = result[1].substring(0,1);
		if (! got.equals(wanted))
			stop(true,4," Regression [" +
				expression + "] s/be [" + wanted + "] "+wanted.length()+" chars long, but got [" + got + "] " + got.length()+ " chars long, minitype=["+type+"]");
		if (! type.equals(desiredType))
			stop(true,4," Regression [" +
				expression + "] [ type s/be [" + desiredType + "] but got [" + type + "]");
	}
	private final void shouldNot(String expression){
		try {
			String[] got = eval(magicNumber, expression);
			throw new IllegalStateException("Programmer Level 5 Error: failed to catch the error in ["+
				expression+"]");
		} catch (IllegalArgumentException e){
			// We expected it to fail by thowing this exception.
		}
	}
	public final static void main (String[] args){
		regressionTest();
	}
} // returns exceptions; short circuits
