package bpel.operations;
import bpel.Log;

/**
 * A sequence simply runs all of it's children in order
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Sequence extends InternalNode
{
		  public void action( int id )
		  {
					 String logFunction = "Sequence.action";
					 Log.logEntry( id, logFunction);
					 for(int i = 0; i < children.size(); i++)
					 {	
								children.elementAt(i).execute(id);
					 }
					 Log.logExit( id, logFunction);
		  }

		  public Sequence( String oname, int odepth )
		  {
					 super(oname, "Sequence", odepth);
		  }
}
