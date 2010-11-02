package bpel.operations;
import bpel.Log;
import bpel.evaluation.*;

/**
 * Similar to while, except it runs at least once
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class RepeatUntil extends InternalNode
{
		  private String comp = "";
		  Evaluation e = new Evaluation();

		  public void action( int id )
		  {
					 String logFunction = "RepeatUntil.action";
					 Log.logEntry( id, logFunction);
					 String evalRet[] = e.eval(id,comp);

					 do
					 {
								for(int i = 0; i < children.size(); i++)
								{ 
										  children.elementAt(i).execute(id);
								}
					        evalRet = e.eval(id,comp);
					 }
					 while( "boolean".equals(evalRet[1]) && "1".equals(evalRet[0]));
					 Log.logExit( id, logFunction);
		  }

		  public RepeatUntil( String oname, int odepth, String comparison )
		  {
					 super(oname, "While", odepth);
					 comp = comparison;
		  }
}
