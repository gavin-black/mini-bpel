package bpel;
import bpel.database.*;
import bpel.operations.*;
import bpel.parse.*;
import bpel.service.*;
import bpel.calls.HttpCall;
import bpel.Log;

/**
 * The main class for the BPEL engine, will process a bpel XML file
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class BpelEngine
{
	 private String response;

    /**
	  * Retrieve the response from execution
	  *
	  * @return response
	  */
	 public String getResponse()
	 {
				String logFunction = "BpelEngine.getResponse";
				Log.logEntry( -1, logFunction);
				Log.logExit( -1, logFunction, response);
				return response;
	 }

    /**
	  * Main function that starts up a web server to accept requests
	  *
	  * @param args a string received from a web service
	  */
    public static void main(String args[])
	 {
				String logFunction = "BpelEngine.main";
				Log.logEntry( -1, logFunction);
				try
				{
						  if( args.length == 0 )
									 WebServer.newServer( WebServer.DEFAULT_PORT );
						  else if ( args.length == 1 )
									 WebServer.newServer( Integer.parseInt(args[0]) );
						  else 
									 System.err.println("Wrong number of arguments!");
				} catch( Exception e) {
						  System.err.println(e);
				}
				Log.logExit( -1, logFunction);
	 }

    /**
	  * Parse the arguments received from the web service and perform 
	  * the appropriate actions
	  * @param args the arguments passed in.  Contains either:
	  *        * bpel_file=filename
	  *        * id=number&action=display/pause/stop/start
	  *        * bpel_file=filename&parents="type::name;..."&current="type::name"&variables="name::value;..."
	  *        NOTE: bpel_file can be replaced with bpel_text
	  * @return true on success, fals otherwise
	  */
	 public boolean begin( String args )
	 {
				boolean ret = true;
				int id = 0;
				String logFunction = "BpelEngine.begin";
				Log.logEntry( id, logFunction);

				// TODO: Handle args in a much less brittle manner
				if ( args.startsWith("bpel_file=") )
				{
						  String bpelUrl = args.substring("bpel_file=".length(), 
												( args.indexOf("&") >= 0 ? args.indexOf("&") : args.length() ) );
			           String bpel = HttpCall.call(bpelUrl);
						  if ( "ERROR".equals(bpel) )
						  { 
									 id = -1;
									 ret = false;
						  } else {
									 
									 // If the args contain variables= we assume it is a 
									 // dynamic passing scheme
									 if(args.indexOf("variables=") >= 0 )
									 {
										    if( args.indexOf("parents=") < 0 || 
												  args.indexOf("current=") < 0 )
											 {
															 System.err.println("Invalid call! Expected bpel_file/bpel_text, parents, current, and variables!");
															 System.exit(-1);
											 }
											 
											 RtstcParser.parseArgs(id, args);
											 System.err.println("RTSTC Call\n");
										    BpelDatabase.update("UPDATE bpel_process SET state = \'resume\' WHERE id = " + id);
									 } else if ( args.indexOf("args=") >= 0 ) {
												String[] tmp = (args.substring( args.indexOf("args=") + "args=".length() )).split(";");
												for( int i = 0; i < tmp.length; i ++ )
												{
														  bpel = bpel.replaceAll("#ARGUMENT" + i, tmp[i]);
												}
									 }
									 // TODO: Moved the following line, make sure RTSTC still works
									 id = newBpelProcess( bpel );
									 (new BpelThread(bpel, id)).start();
									 ret = action( id, "start" );
						  }
						  response = Integer.toString(id);
            } else if( args.startsWith("id=") && args.indexOf("&") != -1 ) {
						  String action = args.substring("id=".length());
						  id = Integer.parseInt( action.substring(0, action.indexOf("&")));
						  ret = action( id, action.substring(action.indexOf("action=") + "action=".length() ));
            } else if( args.startsWith("action=") ) {
						  // Get last id
						  int maxId = 0;
						  BpelResultSet rs = BpelDatabase.query("SELECT MAX(id) FROM bpel_process");
						  try
						  {
									 rs.next();
									 maxId = rs.getInt("MAX(id)");
						  } catch ( Exception e ) {
									 System.err.println(e);
						  }
						  
						  if( args.indexOf("log") > 0 )
						  {
									 response = "";//action(maxId, "log");
						  } else if( args.indexOf("setVar") > 0 ) {
									 ret = action(maxId, args.substring(args.indexOf("setVar")));
						  } else if( args.indexOf("getVar") > 0 ) {
									 ret = action(maxId, args.substring(args.indexOf("getVar")));
						  } else if( args.indexOf("pause") > 0 ) {
									 ret = action(maxId, "pause");
						  } else if( args.indexOf("resume") > 0 ) {
									 ret = action(maxId, "resume");
						  } else if( args.indexOf("getBpel") > 0 ) {
									 ret = action(maxId, "getBpel");
						  } else if( args.indexOf("displayText") > 0 ) {
									 ret = action(maxId, "displayText");
							} else if( args.indexOf("displayRaph") > 0 ) {
									 ret = action(maxId, "displayRaph");
						  } else if( args.indexOf("displayXml") > 0 ) {
									 ret = action(maxId, "displayXml");
						  } else if( args.indexOf("getId") > 0 ) {
									 response = "" + maxId;
						  }
				} else {
						  System.err.println("Invalid arguments!");
						  ret = false;
				}

				Log.logExit( id, logFunction, ret);
				return ret;
	 }

    /**
	  * Add a new bpel process to the database
	  * @param bpel The BPEL text
	  * @return id on success, -1 otherwise
	  */
	 public int newBpelProcess(String bpel)
	 {
			  String logFunction = "BpelEngine.newBpelProcess";
			  int id = 0;
			  Log.logEntry( id, logFunction);
			  BpelResultSet rs;
			  
			  // Add a new instance into the bpel database
			  BpelDatabase.update("INSERT INTO bpel_process(state, currentType, currentName, bpel) values (\'run\', \'process\', \'start\', \'" + bpel + "\');");
			  rs = BpelDatabase.query("SELECT MAX(id) FROM bpel_process");
			  try
			  {
						 rs.next();
						 id = rs.getInt("MAX(id)");
			  } catch ( Exception e ) {
			  			 System.err.println(e);
			  }

			  Log.logExit( id, logFunction, id);
			  return id;	
	 }


    /**
	  * Handle an execution action (bpel/pause/resume/log/display/current)
	  * @param id The process id to effect
	  * @param action Either pause, stop, start, or display a bpel process
	  * @return true on success, false otherwise
	  */
	  public boolean action(int id, String action)
	  {
			    String logFunction = "BpelEngine.newBpelProcess";
			    Log.logEntry( id, logFunction);
				 boolean ret = false;
				 BpelResultSet rs;
				 String bpel = "";
				 String curState = "";
				 rs = BpelDatabase.query("SELECT COUNT(id) FROM bpel_process WHERE id=" + id);
				 try
				 {
							rs.next();
							if ( rs.getInt("COUNT(id)") == 0 )
							{
									  response = "ERROR: No such bpel process " + id + "!";
									  Log.logExit( id, logFunction, false);
									  return false; 
							}
							rs = BpelDatabase.query("SELECT currentType, currentName, state FROM bpel_process WHERE id=" + id);
							rs.next();
							curState = rs.getString("state");
				 } catch ( Exception e ) {
			  			   System.err.println(e);
				 }

				 if( "displayXml".equals(action) ) // Spit out an XML list
				 {
							// Retrieve BPEL from database
							rs = BpelDatabase.query("SELECT bpel FROM bpel_process WHERE id=" + id);
							try
							{
									  rs.next();
									  bpel = rs.getString("bpel");
							} catch ( Exception e ) {
									  System.err.println(e);
							}
							BpelParser p = new BpelParser(bpel, id, false);
							p.parseBpel();
							response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
							response += "<root>\n";
							response += p.displayXml(id);
							response += "</root>\n";
							ret = true;
				 }
				 else if( "displayText".equals(action) ) // Spit out an HTML page of the current execution
				 {
								// Retrieve BPEL from database
							rs = BpelDatabase.query("SELECT bpel FROM bpel_process WHERE id=" + id);
							try
							{
									  rs.next();
									  bpel = rs.getString("bpel");
							} catch ( Exception e ) {
									  System.err.println(e);
							}
							BpelParser p = new BpelParser(bpel, id, false);
							p.parseBpel();
							response = "<meta http-equiv=\"refresh\" content=\"1\"/>";
							response += p.display(id);
							ret = true;
				 }
				 else if( "displayRaph".equals(action) ) // Spit out an HTML page of the current execution
				 {
							// Retrieve BPEL from database
							rs = BpelDatabase.query("SELECT bpel FROM bpel_process WHERE id=" + id);
							try
							{
									  rs.next();
									  bpel = rs.getString("bpel");
							} catch ( Exception e ) {
									  System.err.println(e);
							}
							BpelParser p = new BpelParser(bpel, id, false);
							p.parseBpel();
							
							response = "<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"+
              "<script src=\"http://mm134605-pc.mitre.org:8080/bpel_files/raphael.js\" type=\"text/javascript\" charset=\"utf-8\"></script>"+							
              "<script src=\"http://mm134605-pc.mitre.org:8080/bpel_files/dummy2.js\" type=\"text/javascript\" charset=\"utf-8\"></script>"+
              "<script src=\"http://mm134605-pc.mitre.org:8080/bpel_files/jquery.js\" type=\"text/javascript\" charset=\"utf-8\"></script>" +
              "<script type=\"text/javascript\">"+
              "ca_boxes = [";

							response += p.displayRaph(id);
							response = response.substring(0, response.lastIndexOf("["));							
							
							response+="];</script><body onload=\"draggingBoxes({divName:'minibpmn',windowWidth:1500,windowHeight:2000,boxes:ca_boxes, arrows:ca_arrows, colors:ca_colors});\"><div id=\"minibpmn\"></div></body></html>";
							ret = true;
				 } else if ( "pause".equals(action) ) { // Stop the executing thread to resume later
							if( "complete".equals(curState) )
									  response = "ERROR: Can't pause a completed process!";
							else if( "stop".equals(curState) )
									  response = "ERROR: Can't pause a stopped process!";
							else {
									  BpelDatabase.update("UPDATE bpel_process SET state = \'stop\' WHERE id = " + id);
									  response = "" + id;
									  ret = true;
							}
				 } else if ( "resume".equals(action) ) { // Resume a paused thread
							if( "complete".equals(curState) )
									  response = "ERROR: Can't resume a completed process!";
							else if( !"stop".equals(curState) )
									  response = "ERROR: Can't resume a running process!";
							else 
							{
									  BpelDatabase.update("UPDATE bpel_process SET state = \'resume\' WHERE id = " + id);
									  // Retrieve BPEL from database
									  rs = BpelDatabase.query("SELECT bpel FROM bpel_process WHERE id=" + id);
									  try
									  {
												 rs.next();
												 bpel = rs.getString("bpel");
									          (new BpelThread(bpel, id)).start();
												 response = "" + id;
												 ret = true;
									  } catch ( Exception e ) {
												 System.err.println(e);
									  }
							}
				 } else if ( "getBpel".equals(action) ) { // Retrieve raw BPEL for a running process
							// Retrieve BPEL from database
							rs = BpelDatabase.query("SELECT bpel FROM bpel_process WHERE id=" + id);
							try
							{
									  rs.next();
									  response = rs.getString("bpel");
							        ret = true;
							} catch ( Exception e ) {
									  System.err.println(e);
							}
				 } else if ( action.startsWith("setVar") ) { // Set a variable in the process
							int locName = action.indexOf("&name=");
							int locVar = action.indexOf("&value=");
							if( locName < 0 || locVar < 0 )  response = "ERROR: setVar action requires a name and a value";
							else 
							{
									  String varName = action.substring(locName + "&name=".length(), locVar );
									  String varValue = action.substring(locVar + "&value=".length() );
									  if( !Variable.exists(id, varName) ) response = "ERROR: " + varName + " does not exist";
									  else
									  {
												 Variable.update(id, varName, varValue);
												 response = varName + " successfully set to " + varValue;
												 ret = true;
									  }
							}
				 } else if ( action.startsWith("getVar") ) { // Get a variable in the process
							int locName = action.indexOf("&name=");
							if( locName < 0 )  response = "ERROR: getVar action requires a name";
							else 
							{
									  String varName = action.substring(locName + "&name=".length());
									  if( !Variable.exists(id, varName) ) response = "ERROR: " + varName + " does not exist";
									  else
									  {
												 response = Variable.retrieve(id, varName);
												 ret = true;
									  }
							}
							ret = true;
				 } else if ( "start".equals(action) ) { // Starting a new process
							ret = true;
				 } else if ( action.startsWith("log") ) { // Get the log for a process
							// If there is a since then only retrieve those
							int locSince = action.indexOf("&since=");
							if( locSince < 0 )
							{
									  response = Log.retrievePublic( id );
							} else {

							}
							ret = true;
				 } else { // Invalid action
							response = "ERROR: Invalid action \'" + action + 
									     "\' expected: resume,pause,display,getVar,setVar,log, or getBpel";
				 }
				 Log.logExit( id, logFunction, ret);
				 return ret;
	  }
}
