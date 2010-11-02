package bpel.operations;
import bpel.database.*;
import bpel.Log;

/**
 * End the process prematurely
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Exit extends LeafNode
{

		  /**
			* Exit node, change the execution state to 'stop'
			*
			* @param id The current process
			*/
		  public void action(int id)
		  {
					 String logFunction = "Exit.action";
					 Log.logEntry( id, logFunction);
					 BpelDatabase.update("UPDATE bpel_process SET state = \'stop\' WHERE id = " + id);
					 Log.logExit( id, logFunction);
		  }

		  /**
			* Constructor
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			*/
		  public Exit( String oname, int odepth)
		  {
					 super(oname, "Exit", odepth);
		  }
}
