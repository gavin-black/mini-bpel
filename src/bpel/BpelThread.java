package bpel;
import bpel.parse.BpelParser;
import bpel.Log;

/**
 * Spawn off a new parser and have it execute in it's own thread
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class BpelThread extends Thread
{
		  private String tBpel;
		  private int tId;

		  /**
			* Run the thread, should not be called directly, will go when start() is called
			*/
		  public void run()
		  {
					 String logFunction = "BpelThread.run";
					 Log.logEntry( tId, logFunction);
					 BpelParser p = new BpelParser(tBpel, tId, true);
					 p.parseBpel();
					 p.execute();
					 Log.logExit( tId, logFunction );
		  }
		  
		  /**
			* Constructor to setup the thread
			*
			* @param bpel The string containing the full bpel
			* @param id The id of the current process
			*/
		  public BpelThread( String bpel, int id )
		  {
					 super();
					 String logFunction = "BpelThread_Constructor";
					 Log.logEntry( id, logFunction);
					 tBpel = bpel;
					 tId = id;
					 Log.logExit( id, logFunction );
		  }
}
