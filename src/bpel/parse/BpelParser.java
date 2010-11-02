package bpel.parse;
import bpel.database.*;
import bpel.operations.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import bpel.Log;

/**
 * Parse BPEL into a tree of executable nodes
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class BpelParser extends XmlParser
{
	private Operation root = null;
	private int bpelId = 0;
	private boolean newProc = false;

	private String getTextValue(Node n)
	{
			  String logFunction = "BpelParser.getTextValue";
			  Log.logEntry( -1, logFunction);
			  if( n == null) return "";
			  NodeList text = n.getChildNodes();
			  if(text == null || text.getLength() < 1)
			  {
						 System.err.println("No text within " + n.getNodeName());
						 System.exit(-1);
			  }
			  Log.logExit( -1, logFunction, text.item(0).getNodeValue() );
			  return text.item(0).getNodeValue();
	}

	/**
	 * Display the xml for the current node and the parents
	 *
	 * @param id The id of the bpel process of the variable
	 * @return string containing XML
	 */
   public String displayXml(int id)
	{
			  String ret = "";
			  if(root != null) ret = root.displayXml(id);
			  return ret;
	}

	/**
	 * Display the entire tree starting at the root node
	 *
	 * @param id The id of the bpel process of the variable
	 * @return string containing HTML code for displaying
	 */
	
   public String display(int id)
	{
			  String ret = "";
			  if(root != null) ret = root.display(id);			  
			  return ret;
	}
	 public String displayRaph(int id)
	{
			  String ret = "";
			  if(root != null) ret = root.displayRaph(id);
			  root.yloc = 0;
			  return ret;
	}

	/**
	 * Execute the tree starting at the root node,
	 * The Id must have been properly set in the constructor
	 */
   public void execute()
	{
			  if(root != null) root.execute(bpelId);
	}

   private String getAttribute(String operation, String name, NamedNodeMap attrs)
	{
			  String logFunction = "BpelParser.getAttribute";
			  Log.logEntry( -1, logFunction);
			  Node n = attrs.getNamedItem(name);
			  if ( n == null )
			  {
			     System.err.println( operation + " is missing necessary attribute "
									    + name + "!");
			     System.exit(-1);
			  }

			  Log.logExit( -1, logFunction, n.getNodeValue() );
			  return n.getNodeValue();
	}

	/**
	 * This does all the real parsing work.  
	 * Given a parser node it will check the name and build the appropriate operations from there
	 *
	 * @param n Current node to parse
	 * @param depth The indentation level we are at, usefull for displaying
	 * @param parent The parser element above this one, since we need to add children as we go
	 * @return The ParserElement that was created during the parsing
	 */
   protected ParserElement doAction( Node n, int depth, ParserElement parent )
	{
			  String logFunction = "BpelParser.ParserElement";
			  Log.logEntry( -1, logFunction);
			  NamedNodeMap attrs = n.getAttributes();
			  ParserElement current = null;
			  String nodeName = n.getNodeName();
			  String nameAttr = "";
			  boolean addChild = true; // Process and variables are the only non-child nodes 

			  // Most elements have a name attribute
			  // treat the ones that don't as special cases
			  if( ! (
						"variables".equals(nodeName) ||
					   "copy".equals(nodeName) ||
					   "condition".equals(nodeName) ||
					   "from".equals(nodeName) ||
					   "to".equals(nodeName) ||
					   "until".equals(nodeName) ||
						"for".equals(nodeName) )
				 ) 
			  {
						 nameAttr = getAttribute(n.getNodeName(), "name", attrs);
			  } else {
						 if( !"copy".equals(nodeName) ) addChild = false;
           }

			  if( "process".equals(n.getNodeName()) )
			  {
			          root = new BpelProcess(nameAttr, depth);
						 current = root;
						 addChild = false;
			  } else if ( "sequence".equals( n.getNodeName()) ) {
						 current = new Sequence(nameAttr, depth);
			  } else if ( "flow".equals( n.getNodeName()) ) {
						 current = new Flow(nameAttr, depth);
			  } else if ( "flownumber".equals( n.getNodeName()) ) {
						 int flownum = 0;
						 try
						 {
						    flownum=Integer.parseInt(getAttribute(n.getNodeName(),"number",attrs));
						 } catch (Exception e) {
							 System.out.println("Bad number for flownumber");
						 }
						 current = new FlowNumber(nameAttr, depth, flownum);
			  } else if ( "if".equals( n.getNodeName()) ) {
						 current = new If(nameAttr, depth);
			  } else if ( "mainif".equals( n.getNodeName()) || "elseif".equals( n.getNodeName()) ) {
						 NodeList children = n.getChildNodes();
						 String condStr = "";
						 for( int i = 0; i < children.getLength(); i++)
						 {
									if( "condition".equals(children.item(i).getNodeName()))
									{
											  condStr = getTextValue( children.item(i) );
									}
						 }

						 if( "mainif".equals( n.getNodeName() ) )
						    current = new MainIf(nameAttr, depth, condStr);
						 else if( "elseif".equals( n.getNodeName() ) )
						    current = new ElseIf(nameAttr, depth, condStr);
			  } else if ( "else".equals( n.getNodeName()) ) {
						 current = new Else(nameAttr, depth);
			  } else if ( "while".equals( n.getNodeName()) ) {
						 NodeList children = n.getChildNodes();
						 String condStr = "";
						 for( int i = 0; i < children.getLength(); i++)
						 {
									if( "condition".equals(children.item(i).getNodeName()))
									{
											  condStr = getTextValue( children.item(i) );
									}
						 }
						 current = new While(nameAttr, depth, condStr);
			  } else if ( "invoke".equals( n.getNodeName()) ) {
						 String typeAttr=getAttribute(n.getNodeName(),"type",attrs);
						 String inputAttr=getAttribute(n.getNodeName(),"inputVariable",attrs);
						 String outputAttr=getAttribute(n.getNodeName(),"outputVariable",attrs);
						 String uri = getAttribute(n.getNodeName(), "uri", attrs);
						 String rtstc = getAttribute(n.getNodeName(), "rtstc", attrs);

						 // SOAP Specific variables
						 String action = null;
						 String responseVars = null;
						 String namespaceUri = null;
						 String namespacePre = null;
						 String dest = null;
						 String respName = null;
						 String attachName = null;

						 // SOAP setup
						 if( "SOAP".equals( typeAttr.toUpperCase() ) ) 
						 {
						    action = getAttribute(n.getNodeName(), "action", attrs);
						    responseVars = getAttribute(n.getNodeName(), "responseVariables", attrs);
						    namespaceUri = getAttribute(n.getNodeName(), "namespaceUri", attrs);
						    namespacePre = getAttribute(n.getNodeName(), "namespacePrefix", attrs);
						    dest = getAttribute(n.getNodeName(), "destination", attrs);
						    respName = getAttribute(n.getNodeName(), "responseName", attrs);
						    attachName = getAttribute(n.getNodeName(), "attachmentName", attrs);
						 }

						 // Very long constructor due to...SOAP
						 current = new Invoke(nameAttr,depth,typeAttr,inputAttr,outputAttr, uri, rtstc, action, 
											       responseVars, namespaceUri, namespacePre, dest, respName, attachName);
			  } else if ( "assign".equals( n.getNodeName()) ) {
						 current = new Assign(nameAttr, depth);
				}else if ( "interact".equals( n.getNodeName()) ) {
						 String server = getAttribute(n.getNodeName(), "server", attrs);
						 String message = getAttribute(n.getNodeName(), "message", attrs);
						 current = new Interact(nameAttr, server, message, depth);             
			  } else if ( "copy".equals( n.getNodeName() ) ) {
						 // Parse the copy, from, and to portions of the expression
						 NodeList children = n.getChildNodes();
						 String toStr = null;
						 String fromStr = null;

						 // The first child should be the copy node with the weird logic DOMParser uses
						 for( int i = 0; i < children.getLength(); i++)
						 {
									if( "to".equals(children.item(i).getNodeName()))
									{
											  NamedNodeMap toAttrs = children.item(i).getAttributes();
											  toStr = getAttribute(children.item(i).getNodeName(), "variable", toAttrs);
									} else if( "from".equals(children.item(i).getNodeName())) {
											  fromStr = getTextValue(children.item(i));
									} else if ( !"#text".equals(children.item(i).getNodeName())) {
											  System.err.println("Invalid tag " + children.item(i).getNodeName() + " in copy!");
											  System.exit(-1);
									}
						 }
						 current = new Copy(depth, toStr, fromStr);
			  } else if ( "wait".equals( n.getNodeName()) ) {
						 NodeList children = n.getChildNodes();
						 String timeStr = "";
						 for( int i = 0; i < children.getLength(); i++)
						 {
									if("for".equals(children.item(i).getNodeName()) || "until".equals(children.item(i).getNodeName()))
									{
											  timeStr = getTextValue( children.item(i) );
									} else if ( !"#text".equals(children.item(i).getNodeName())) {
											  System.err.println("Invalid tag " + children.item(i).getNodeName() + " in wait!");
											  System.exit(-1);
									}
						 }
						 current = new Wait(nameAttr, depth, timeStr);
			  } else if ( "empty".equals( n.getNodeName()) ) {
						 current = new Empty(nameAttr, depth, "TODO");
			  } else if ( "variable".equals( n.getNodeName()) ) {
						 String typeAttr = getAttribute(n.getNodeName(), "type", attrs);
						 addChild = false;

						 // Do not add if we are resuming
						 try{
									BpelResultSet rs = BpelDatabase.query("SELECT state FROM bpel_process WHERE id=" + bpelId);
									rs.next();
									if( "run".equals(rs.getString("state")) && newProc ) Variable.add(bpelId, nameAttr, typeAttr);
						 } catch ( Exception e ) {
									System.err.println(e);
						 }
			  }

			  if ( addChild )
			  {
  	  				    Operation op = (Operation)parent;
			          op.addChild( (Operation)current );
			  }
			  Log.logExit( -1, logFunction, "ParserElement" );
			  return current;
	}	  

	/**
	 * Parse the associated bpel text
	 */
	public void parseBpel()
	{
			  parseTree();
	}

	/**
	 * Constructor for a BpelParser 
	 *
	 * @param input The actual text of the BPEL to parse
	 * @param id The process id
	 * @param newProcess If this is a new process or not, needed to avoid recreating Variables 
	 */
   public BpelParser(String input, int id, boolean newProcess)
	{
			  super(input);
			  bpelId = id;
			  newProc = newProcess;
   }
}
