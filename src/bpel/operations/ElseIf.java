package bpel.operations;
import bpel.Log;
import bpel.evaluation.*;

/**
 * ElseIf operation 
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class ElseIf extends InternalNode
{
		  private String comp = "";

		  /**
			* Evaluate whether or not to enter this branch
			*
			* @param id The current process
			* @return true if we should enter, false otherwise
			*/
		  public boolean evalCondition(int id)
		  {
					 String logFunction = "ElseIf.evalCondition";
					 Log.logEntry( id, logFunction);
					 Evaluation e = new Evaluation();
					 String evalRet[] = e.eval(id,comp);
					 boolean ret = ("boolean".equals(evalRet[1]) && "1".equals(evalRet[0]));
					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }

		  /**
			* Execute the children of this operation.  Does not check the condition.
			*
			* @param id The current process
			*/
		  public void action( int id )
		  {
					 String logFunction = "ElseIf.action";
					 Log.logEntry( id, logFunction);
					 for(int i = 0; i < children.size(); i++)
					 { 
								children.elementAt(i).execute(id);
					 }
					 Log.logExit( id, logFunction);
		  }

		  /**
			* Normal ElseIf constructor
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			* @param comparison The evaluation to decide whether or not to go into this branch
			*/
		  public ElseIf( String oname, int odepth, String comparison )
		  {
					 super(oname, "ElseIf", odepth);
					 comp = comparison;
		  }

		  /**
			* ElseIf constructor, used by classes that extend ElseIf
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			* @param comparison The evaluation to decide whether or not to go into this branch
			* @param inheritType The type of the operation
			*/
		  public ElseIf( String oname, int odepth, String comparison, String inheritType )
		  {
					 super(oname, inheritType, odepth);
					 comp = comparison;
		  }
}
