package bpel.operations;
import bpel.database.Variable;
import bpel.Log;
import bpel.evaluation.*;

/**
 * The copy bpel operation
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Copy extends LeafNode 
{
		  String fromStr = "";
		  String toStr = "";

		  /**
			* Hide the displaying of the copy operators, since they are nameless
			*
			* @param id The id of the bpel process
			*/
		  public String display(int id)
		  {
					 //return super.display(id);
					 return "";
		  }

		  /**
			* Perform the actual assignment
			*
			* @param id The id of the bpel process
			*/
		  public void action(int id)
		  {
					String logFunction = "Copy.action";
					Log.logEntry( id, logFunction);
					Evaluation e = new Evaluation();
					Log.logEvent( id, "FROM: " + fromStr);
					Log.logEvent( id, "TO: " + toStr);
					
					String evalRet[] = e.eval(id, fromStr);
					Variable.update(id, toStr, evalRet[0]);
					String x = Variable.retrieve(id, toStr);
					Log.logExit( id, logFunction);
		  }

		  /**
			* Constructor for making an Copy operation
			*
			* @param oname The name of the operation
			* @param odepth The current indentation level
			* @param to contains the variable name to store the result
			* @param from contains the expression to evaluate for assignment
			*/
		  public Copy( int odepth, String to, String from )
		  {
					 super("", "Copy", odepth);
					 fromStr = from;
					 toStr = to;
		  }
}
