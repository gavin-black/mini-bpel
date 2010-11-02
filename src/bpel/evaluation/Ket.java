package bpel.evaluation;
import bpel.database.Variable;

/**
 * More functions for evaluating XPath expressions, only used by Evaluation
 *
 * @author Mike Brenner
 * @author <a href="mailto:mikeb@mitre.org">gblack@mitre.org</a>
 */
public class Ket {
	private String type = "omega";
	private String value = "uninitialized";

	public Ket (String type, String value){
		this.type=type; this.value=value;
	}
	public Ket (String quick){
		setQuick(quick);
	}
	private static final void err (String bad){
		throw new IllegalArgumentException("Programmer Type 3.5 Error: ket omga: " + bad);
	}
	private static final void nonnull(String see){
		if (null==see)
			err("attempt to set ket to null");
	}
	private static final void nonempty (String see){
		nonnull(see);
		if (0==see.length())
			err("attempt to set numberic ket to the empty string");
	}
	public final boolean isLegalType(String t){
		nonnull(t);
		for (int i=0; i<LEGAL_TYPES.length; i++){
			if (t.equals(LEGAL_TYPES[i])) return true;
		}
		return false;
	}
	public final void setQuick (String x){
		nonnull(x);
		nonempty(x);
		type=x.substring(0,1);
		value=x.substring(1);
	}
	public final void setString (String x){
		nonnull(x);
		type="s"; value=x;
	}
	public final void setInt (long x){
		type="i"; value=x+"";
	}
	public final void setInt (String x){
		nonnull(x);
		type="i"; value="" + Long.parseLong(x);
	}
	public final void setNumber (double x){
		type="n"; value=x+"";
	}
	public final void setNumber (String x){
		nonnull(x);
		type="n"; value="" + Double.parseDouble(x);
	}
	public final void setBoolean (boolean x){
		type="b"; value=x?"0":"1";
	}
	public final void setBoolean (long x){
		type="b"; value=0==x?"0":"1";
	}
	public final void setBoolean (String x){
		nonnull(x);
		type="b"; value=("0".equals(x)||"".equals(x))?"0":"1";
	}
	public final void setBinary (String x){
		nonnull(x);
		type="y"; value=x;
	}
	public final void setOmega (String x){
		nonnull(x);
		type="o"; value=x;
	}
	public final void setXml (String x){
		nonnull(x);
		type="x"; value=x;
	}
	public final void setLabel (String x){
		nonnull(x);
		type="L"; value=x;
	}
	public final void setDate (String x){
		nonnull(x);
		type="d"; value=x;
	}
	public final void setDuration (String x){
		nonnull(x);
		type="p"; value=x;
	}
	public final String getString(){
		if ("o".equals(type))
			err("String getValue");
		return value;
	}
	public final String getQuick(){
		return type+value;
	}
	public final double getNumber(){
		if ("o".equals(type))
			err("numeric getValue");
		if ("b".equals(type))
			return Integer.parseInt(value);
		else if ("n".equals(type))
			return Double.parseDouble(value);
		else if ("s".equals(type))
			return Double.parseDouble(value);
		else if ("i".equals(type))
			return (long) Double.parseDouble(value);
		else if ("y".equals(type))
			err("I am not programmed to convert binary files to a single number");
		else if ("x".equals(type))
			err("I am not programmed to convert xml files to a single number");
		else if ("L".equals(type))
			err("I am not programmed to convert internal addresses to a single number");
		else if ("d".equals(type))
			err("I am not programmed to convert dates to a single number");
		else if ("p".equals(type))
			err("I am not programmed to convert durations to a single number");
		err("getNumber: bad type [" + type + "]");
		return -17.0;
	}
	public final long getInteger(){
		double x=getNumber(); // Types n, s, i, b will return from this.
		return (long)x;
	}
	public final boolean getBoolean(){
		if ("o".equals(type))
			err("boolean getValue");
		if ("b".equals(type))
			return !("".equals(value) || "0".equals(value));
		long x=getInteger(); // Types n, s, i will return from this.
		return 0!=x;
	}
	public final String getBinary(){
		if ("o".equals(type))
			err("floating getValue");
		if ("y".equals(type))
			return value;
		err("I am not programmed to convert type " + type + " to binary file format.");
		return "o1";
	}
	public final String getXML(){
		if ("o".equals(type))
			err("floating getValue");
		if ("x".equals(type))
			return value;
		err("Missing schema and xsl script to convert type " + type + " to xml format");
		return "o1";
	}
	public final String getLabel(){
		if ("o".equals(type))
			err("floating getValue");
		if ("L".equals(type))
			return value;
		err("Missing namespace for validating type L with schematron and water.");
		return "o1";
	}
	public final String getDate(){
		if ("o".equals(type))
			err("floating getValue");
		if ("d".equals(type))
			return value;
		err("Not currently programmed to convert type " + type + " to date (D) format");
		return "o1";
	}
	public final String getDuration(){
		if ("o".equals(type))
			err("floating getValue");
		if ("p".equals(type))
			return value;
		err("Not currently programmed to convert type " + type + " to duration (P) format");
		return "o1";
	}
	public final String getType(){
		if ("o".equals(type))
			err("getValue");
		else return type;
		return "o1";
}

	private final Ket op (String op, Ket right){
		// Ket k = new Ket();
		int y=0;
		return new Ket("a","b");
	}
	private final void regret(String x){
		err(x);
	}
	private final void test(){
		final int L = testVectors.length;
		for (int t=0; t<L; t++){
			String[] v = testVectors[t];
			Ket resultAsKet = new Ket(v[1]).op(v[0], new Ket(v[2]));
			String resultAsString = resultAsKet.getQuick();
			if (! resultAsString.equals(v[3])){
				regret("Programmer Error Level 3.5 ket regression: got [" + resultAsString +
					" for [" + v[0] + ","  + v[1] + ","  + v[2] + ","  + v[3] + "]");
			}
		}
	}
	private static String[][] testVectors = {
		{"+","s3","s2","n5"}, {"+","n3","s2","n5"},	{"+","i3","s2","n5"},
		{"+","s3","n2","n5"}, {"+","n3","n2","n5"},	{"+","i3","n2","n5"},
		{"+","s3","i2","n5"}, {"+","n3","i2","n5"},	{"+","i3","i2","i5"},

		{"-","s3","s2","n1"}, {"-","n3","s2","n1"},	{"-","i3","s2","n1"},
		{"-","s3","n2","n1"}, {"-","n3","n2","n1"},	{"-","i3","n2","n1"},
		{"-","s3","i2","n1"}, {"-","n3","i2","n1"},	{"-","i3","i2","i1"},

		{"*","s3","s2","n6"}, {"*","n3","s2","n6"},	{"*","i3","s2","n6"},
		{"*","s3","n2","n6"}, {"*","n3","n2","n6"},	{"*","i3","n2","n6"},
		{"*","s3","i2","n6"}, {"*","n3","i2","n6"},	{"*","i3","i2","i6"},

		{"+","i1","i2","i3"}};

	private String[] LEGAL_TYPES={"s","n","i","b","y","x","L","d","p","o"};
	private String[] LEGAL_OPS={"and or xor < > = != <= <= + - * div quo mod"};
}
