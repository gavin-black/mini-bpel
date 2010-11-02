package bpel.operations;
import bpel.Log;

/**
 * Else operation, simply an ElseIf that always has a true condition
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Else extends ElseIf 
{
		  /**
			* The conditional for Else, which is always true
			*
			* @param id The id of the bpel process
			* @return true, since else acts as an always true ElseIf
			*/
		  public boolean evalCondition(int id)
		  {
					 String logFunction = "Else.evalCondition";
					 Log.logEntry( id, logFunction);
					 Log.logExit( id, logFunction, true);
					 return true;
		  }

		  /**
			* Else constructor, calls ElseIf
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			*/
		  public Else( String oname, int odepth )
		  {
					 super(oname, odepth, "", "Else");
		  }
}
