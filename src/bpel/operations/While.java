package bpel.operations;
import bpel.Log;
import bpel.database.*;
import bpel.evaluation.*;

/**
 * Basic loop functionality
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class While extends InternalNode
{
		  private String comp = "";
		  Evaluation e = new Evaluation();

		  public void action( int id )
		  {
					 String logFunction = "While.action";
					 Log.logEntry( id, logFunction);
					 String evalRet[] = e.eval(id,comp);
					 while( "boolean".equals(evalRet[1]) && "1".equals(evalRet[0]))
					 {
								for(int i = 0; i < children.size(); i++)
								{ 
										  children.elementAt(i).execute(id);
								}

								// Check for stop state
								try
								{
										  BpelResultSet rs = BpelDatabase.query("SELECT state FROM bpel_process WHERE id=" + id);
										  rs.next();
										  if( "stop".equals(rs.getString("state")) ) break;
								} catch ( Exception e ) {
										  System.err.println(e);
								}

	 				         evalRet = e.eval(id,comp);
					 }
					 Log.logExit( id, logFunction);
		  }

		  public While( String oname, int odepth, String comparison )
		  {
					 super(oname, "While", odepth);
					 comp = comparison;
		  }

}
