package bpel.operations;
import bpel.database.Variable;
import bpel.Log;
import bpel.calls.HttpCall;

/**
 * The assign bpel operation
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Interact extends LeafNode 
{
			private String server;
			private String message;

		  /**
			* Assign is really just a renamed sequence with Copy nodes inside
			*
			* @param id The current thread
			*/
		  public void action( int id )
		  {
          String logFunction = "Interact.action";
          Log.logEntry( id, logFunction);
			
          String uri = "http://" + server + ":8080/ez_soa/core/database/set.jsp?service=input.text&key=state&value=" + message;
          String ret = "(UNDECIDED)";
          HttpCall.call(uri);
					try{ Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
          uri = "http://" + server + ":8080/ez_soa/core/database/set.jsp?service=input.button&key=state&value=(UNDECIDED)";
          HttpCall.call(uri);
					try{ Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
			 
          uri = "http://" + server + ":8080/ez_soa/core/messages/send.jsp?message_type=bpel.next.process";
          HttpCall.call(uri);
					try{ Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
          uri = "http://" + server + ":8080/ez_soa/core/database/get.jsp?service=input.button&key=state";
			    while( ret.indexOf("(UNDECIDED)") >= 0) 
			    {
						ret = HttpCall.call(uri);
					  try{ Thread.sleep(1000); } catch(Exception e) { e.printStackTrace(); }
			    }
			
					 Log.logExit( id, logFunction, "void");
		  }

		  /**
			* Constructor for making an Assign operation
			*
			* @param oname The name of the operation
			* @param odepth The current indentation level
			*/
		  public Interact( String oname, String serv, String msg, int odepth )
		  {
							super( oname, "Interact", odepth );
							server = serv;
							message = msg;
		  }

}
