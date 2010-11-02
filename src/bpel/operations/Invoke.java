package bpel.operations;
import bpel.calls.*;
import bpel.database.*;
import bpel.parse.YamlParser;
import java.util.*;
import bpel.Log;

/**
 * Invoke node, calls an external service.  TODO: Cut out XML-RPC support for now, since I wasn't using a standard library
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Invoke extends LeafNode 
{
		  private CallType cType;
		  private String rtstcType;
		  private String inVar;
		  private String outVar;
		  private String uriCall;

		  // SOAP specific
		  private String action;
		  private String responseVariables;
		  private String namespaceUri;
		  private String namespacePrefix;
		  private String destination;
		  private String responseName;
		  private String attachmentName;

		  /**
			* The type of calls that can be associated with an invoke
			*/
		  public enum CallType
		  {
					 REST,
					 SOAP,
					 RPC,
					 RSS
		  }

		  /**
			* Actually do the invokation, note that RTSTC services are somewhat odd:
			* 	 * A RTSTC service is just another copy of this BPEL engine running in a different location
			*   * The advantage is that you don't have to send data back and forth, the current state of execution is passed on
			*   * The benefit is only visible if the services are closer and the network inbetween is slower
			*   * Also assumes that the amount of data to pass through is much greater than the BPEL and current state
			*   * Even more benefit could be had by dynamically cutting down on variables and process parts no longer used
			*       *  IE I stored variable X and used it 3 calls later, but don't need it again, so scrub it from the next call
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			*/
		  public void action(int id)
		  {
					 String s = "";
					 String logFunction = "Invoke.action";
					 Log.logEntry( id, logFunction);
					 if( !"no".equals(rtstcType) ) 
					 {
								try
								{
										  String current = "";
										  String parents = "";
										  String variables = "";
										  BpelResultSet rs = null;

										  // Stop the remaining execution
										  BpelDatabase.update("UPDATE bpel_process SET state = \'stop\' WHERE id = " + id);

										  // Grab the current node 
										  rs = BpelDatabase.query(
																"SELECT currentType, currentName FROM bpel_process WHERE id=" + id);
										  rs.next();
										  current = rs.getString("currentType") + "::" + rs.getString("currentName");
										  
										  // Grab the parent nodes
										  rs = BpelDatabase.query(
																"SELECT type, name FROM current_parents WHERE id=" + id);
										  while(rs.next())
										  {
													 parents += rs.getString("type") + "::" + rs.getString("name") + ";";
										  }

										  // Grab the variables
										  rs = BpelDatabase.query(
																"SELECT type, name, value FROM variables WHERE id=" + id);
										  while(rs.next())
										  {
													 variables += rs.getString("type") + "::" + rs.getString("name") + 
																   "::" + rs.getString("value") + ";";
										  }

										  uriCall += "?bpel_file=" + rtstcType + "&parents=" + parents + "&variables=" + 
													    variables + "&current=" + current;

										  // Cut out newlines and other carp  TODO: Think on this :p
										  uriCall = uriCall.replaceAll("\n", "");
										  uriCall = uriCall.replaceAll("!", "");
										  uriCall = uriCall.replaceAll(" ", "");

								} catch (Exception e) {
										  System.err.println(e);
								}
					 }

					 switch( cType )
					 {
								case REST:
								case RSS:
								    System.err.println("URI_CALL REST: " + uriCall);
									 s = HttpCall.call(uriCall);
					             //System.out.println("Received REST: " + s );

									 // If RTSTC we will not store the variable
									 if( !"NONE".equals(outVar) && "no".equals(rtstcType) ) Variable.update(id, outVar, s);
									 if( !"NONE".equals(outVar) && "no".equals(rtstcType) && "YAML".equals(Variable.type( id, outVar ) ) )
									 {
												YamlParser.parse(id, outVar, s);
									 }
									 break;
							   case SOAP:
							       String finalDestination = "";
									 finalDestination = finalDestination.replaceAll( "\n", "");
									 finalDestination = finalDestination.replaceAll( " ", "");
									 System.err.println( uriCall + ":" + finalDestination );
									 // TODO: Fix variable handling to be the same amoungst all calls
									 System.err.println( SoapCall.call(uriCall, action, responseVariables, namespaceUri, namespacePrefix, finalDestination, responseName, attachmentName) );
									 break;
								case RPC:
									 String vType = "";
									 String vValue = ""; 
									 if(!"NONE".equals(inVar)) vType = Variable.type(id, inVar);
								    System.err.println("URI_CALL RPC: " + uriCall);
									 if( "YAML".equals(vType) )
									 {
												// Go through and find variables starting with VarName.
												BpelResultSet rs = BpelDatabase.query("SELECT name FROM variables WHERE id=" + id 
																	 + " AND name LIKE \"" + inVar + ".%\";");
												try
												{
														  while( rs.next() )
														  {
																	 String tmpVarName = rs.getString("name");
																	 vValue += tmpVarName.substring(tmpVarName.indexOf(".") + 1) + 
																				"=" + Variable.retrieve(id,tmpVarName) + "&";
														  }
														  vValue = vValue.substring( 0, vValue.length() - 1 );

														  // Strip newlines and whitespace
														  vValue = vValue.replaceAll( "\n", "");
														  vValue = vValue.replaceAll( " ", "");
												} catch ( Exception e ) {
														  System.err.println(e);
												}
									 } else if (!"NONE".equals(inVar)) {
												vValue = Variable.retrieve(id, inVar);
												vValue = vValue.replaceAll( "\n", "");
												vValue = vValue.replaceAll( " ", "");
									 }
									 Log.logEvent(-1, "uri: " + uriCall);
					             s = HttpCall.call(uriCall + "?" + vValue, null) ; // TODO fix this back to POST data
									 System.err.println("OUTPUT: " + s);
					           //  System.out.println("Received RPC: " + s);

									 // If RTSTC we will not store the variable
									 if( "no".equals(rtstcType) ) Variable.update(id, outVar, s);
									 if( "YAML".equals(Variable.type( id, outVar ) ) )
									 {
												YamlParser.parse(id, outVar, s);
									 }
									 break;
					 }
					 Variable.update( id, inVar, s );
					 Log.logExit( id, logFunction );
		  }

		  public Invoke( String oname, int odepth, String type, String in, String out, String uri, String rtstc, String soapAction, String soapResponseVariables, String soapNamespaceUri, String soapNamespacePrefix, String soapDestination, String soapResponseName, String soapAttachmentName )
		  {
					 super(oname, "Invoke", odepth);
					 String tmpType = type.toUpperCase();

					 inVar = in;
					 outVar = out;
					 uriCall = uri;
					 rtstcType = rtstc;

					 // SOAP Specific
					 action = soapAction;
					 responseVariables = soapResponseVariables;
					 namespaceUri = soapNamespaceUri;
					 namespacePrefix = soapNamespacePrefix;
					 destination = soapDestination;
					 responseName = soapResponseName;
					 attachmentName = soapAttachmentName;
					 
					 if( "REST".equals(type) )
								cType = CallType.REST;
					 else if ( "SOAP".equals(type) )
								cType = CallType.SOAP;
					 else if ( "RPC".equals(type) )
								cType = CallType.RPC;
					 else if ( "RSS".equals(type) )
								cType = CallType.RSS;
					 else {
								System.err.println("Unsupported invoke type!");
								System.exit(-1);
					 }

		  }
}
