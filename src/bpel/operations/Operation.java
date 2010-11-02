package bpel.operations;
import bpel.parse.ParserElement;
import bpel.database.*;
import java.util.*;
import bpel.Log;

/**
 * Top level class for all operations
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Operation extends ParserElement
{

		  public Vector<Operation> children = new Vector<Operation>();

		  /**
			* Resume execution of a stopped or discontinous process
			* Will look through the database for current parents (Internal Nodes)
			*
			* @param id The id of the bpel process to resume
			* @param cName The name of the node to resume at
			* @param cType The type of the node to resume at
			* @return true, this function is overwritten later and needs the return then
			*/
		  public boolean resumeExecution( int id, String cName, String cType )
		  {
					 String logFunction = "Operation.resumeExecution";
					 Log.logEntry( id, logFunction);
					 if( name.equals(cName) && type.equals(cType) )
					 {
								execute(id);
								Log.logExit( id, logFunction, true);
								return true;
					 }
					 for(int i = 0; i < children.size(); i++) 
					 {
								if(children.elementAt(i).resumeExecution(id, name, type))
								{
										  Log.logExit( id, logFunction, true);
										  return true;
								}
					 }

					 Log.logExit( id, logFunction, false);
					 return false;
		  }

		  /**
			* Should never run besides testing, it will simply print out the node name
			*
			* @param id The id of the bpel process
			* @return true, this function is overwritten later and needs the return then
			*/
		  public boolean execute(int id)
		  {
					 System.out.println("Operation Try: " + name);
					 return true;
		  }

		  public int getMaxLength( int id, int curDepth )
		  {
					 // Note If is a special case and has it's own overriden version
					 int ret = curDepth;
					 ret += name.length();

					 // Step through the children and get their max depth
					 for(int i = 0; i < children.size(); i++)
					 {	
								int tmp = children.elementAt(i).getMaxLength(id, 0);
								if ( tmp > ret ) ret = tmp;
					 }

					 return ret;
		  }

		  public int getBoxes( int boxId, int maxHorizontal, double depth, int parent )
		  {
					 double scale = 1/(maxHorizontal);
					 int newBoxId = boxId;
					 int isSeq = 0;
					 int childBoxId = boxId;
					 int lastChildBoxId = boxId;
					 // <id;name,width,x,y,color>
					 if( !"Sequence".equals(type) )
					    System.out.println( "<" + boxId + ";" + name + "," + scale + "," + .5 * scale+ "," + depth + ", #0000000>");
					 for(int i = 0; i < children.size(); i++)
					 {	
								childBoxId = newBoxId + 1;
								if( "Sequence".equals(children.elementAt(i).type) ) {
										  System.out.println("SequenceStart");
										  isSeq = 1;
								} 
								newBoxId = children.elementAt(i).getBoxes(childBoxId, maxHorizontal, (childBoxId) * .1, isSeq);
								isSeq = 0;
								if( "While".equals(children.elementAt(i).type) ) 
								{
										 childBoxId = newBoxId;
								} else if( "Sequence".equals(children.elementAt(i).type) ) {
										 childBoxId = newBoxId;
								}else{
										  if(parent == 1) {
										          System.out.println("SequenceFin");
													 parent = 0;
													 System.out.println("<" + (lastChildBoxId -1) + "," + newBoxId + ">");
										  }else{
													 System.out.println("<" + lastChildBoxId + "," + newBoxId + ">");
										  }
								}
								lastChildBoxId = childBoxId;
					 }

	             return newBoxId;				 
		  }

		  /**
			* Return an XML document of the current children and parents
			*
			* @param id The id of the bpel process to display
			* @return String containing raw XML
			*/
		  public String displayXml(int id)
		  {
					 String ret = "";
					 String depthString = "";
					 BpelResultSet rs;
					 boolean foundMatch = false;

					 for(int i = 0; i < depth; i++) depthString += "&nbsp;";
					 rs = BpelDatabase.query("SELECT COUNT(id) FROM current_parents WHERE id=" + id + " AND type=\'" + type + 
										           "\' AND name=\'" + name + "\'");
					 try
					 {
								rs.next();
								if( rs.getInt("COUNT(id)") > 0 )
								{
										  ret += "<item><name>" + name + "</name><value>parent</value></item>\n";
										  foundMatch = true;
								} else {
										  rs = BpelDatabase.query("SELECT COUNT(id) FROM bpel_process WHERE currentType=\'" + type + 
										                          "\' AND currentName=\'" + name + "\' AND id=" + id);
										  rs.next();
										  if( rs.getInt("COUNT(id)") > 0 )
										  {
													 ret += "<item><name>" + name + "</name><value>child</value></item>\n";
										  }
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }

					 for(int i = 0; i < children.size(); i++)
					 {	
								ret += children.elementAt(i).displayXml(id);
					 }
					 return ret;
		  }

		  /**
			* Return an HTML formatted display of the process running
			*
			* @param id The id of the bpel process to display
			* @return String containing raw HTML for display
			*/
			
			public String display(int id)
		  {
					 String ret = "";
					 String depthString = "";
					 BpelResultSet rs;
					 boolean foundMatch = false;

					 for(int i = 0; i < depth; i++) depthString += "&nbsp;";
					 rs = BpelDatabase.query("SELECT COUNT(id) FROM current_parents WHERE id=" + id + " AND type=\'" + type + 
										           "\' AND name=\'" + name + "\'");
					 try
					 {
								rs.next();
								if( rs.getInt("COUNT(id)") > 0 )
								{
										  ret += depthString + "<FONT COLOR=\"#ff6600\">" + name + "(" + type + ")</FONT><BR>";
										  foundMatch = true;
								} else {
										  rs = BpelDatabase.query("SELECT COUNT(id) FROM bpel_process WHERE currentType=\'" + type + 
										                          "\' AND currentName=\'" + name + "\' AND id=" + id);
										  rs.next();
										  if( rs.getInt("COUNT(id)") == 0 )
										  {
													 ret += depthString + name + "(" + type + ")<BR>";
										  } else {
													 ret += depthString + "<FONT COLOR=\"#00ff22\">" + name + "(" + type + ")</FONT><BR>";
										  }
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }

					 for(int i = 0; i < children.size(); i++)
					 {	
								ret += children.elementAt(i).display(id);
					 }
					 return ret;
		  }
		  
		public static int yloc = 0;
		  public String displayRaph(int id)
		  {
					 String ret = "";
					 String retu = "];ca_colors = [";
					 String retur = "];ca_arrows = [";
					 BpelResultSet rs;
					 int depthInt = 1; 
					 

					 for(int i = 0; i < depth; i++) depthInt++;
					
					rs = BpelDatabase.query("SELECT COUNT(id) FROM current_parents WHERE id=" + id + " AND type=\'" + type + 
										           "\' AND name=\'" + name + "\'");
					if(!name.equals(""))
          {
					 try
					 {
								rs.next();
								if( rs.getInt("COUNT(id)") > 0 )
								{
										  retu += "[\"#f00\"],";
								} else {
										  rs = BpelDatabase.query("SELECT COUNT(id) FROM bpel_process WHERE currentType=\'" + type + 
										                          "\' AND currentName=\'" + name + "\' AND id=" + id);
										  rs.next();
										  if( rs.getInt("COUNT(id)") == 0 )
										  {
													 retu += "[\"#000\"],";
										  } else {
													 retu += "[\"#0f0\"],";
										  }
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }
          
					  ret += "['rect',"+(depthInt * 40)+","+((yloc+1)*30)+",160,20,0,'"+name+"'],";
					  retur += "["+yloc+","+(yloc + 1)+",\"#000\"],";
					  yloc++;
          }			
           
					 for(int i = 0; i < children.size(); i++)
					 {	

                String t = children.elementAt(i).displayRaph(id); //recursion nonsense (gets the children boxes/arrows)
                try
								{
								ret += t.substring(0, t.lastIndexOf("];ca_colors = [")); //place the new ca_boxes before the new ca_arrows
								retu += t.substring(t.lastIndexOf("];ca_colors = [")+ 15, t.lastIndexOf("];ca_arrows = [")); 
								retur += t.substring(t.lastIndexOf("];ca_arrows = [")+ 15, t.length()); 
								}
								catch ( Exception e ) {
								//try catch is here in case there is an empty returned string
								//this sometimes happens and would otherwise screw up the ca_arrows
					 }
					 }
					 return ret+retu+retur; //return a compiled string of ca_boxes, ca_arrows, and ca_colors
		  }

		  /**
			* Add a child operation
			*
			* @param child The operation to add
			*/
		  public void addChild( Operation child )
		  {
					 children.addElement( child );
		  }

		  /**
			* Constructor requiring the name
			*
			* @param oname The name of the operation
			* @param otype The type of the operation
			* @param odepth The depth we are currently at, IE indentation level
			*/
		  public Operation( String oname, String otype, int odepth )
		  {
					 super(oname, otype, odepth);
		  }
}
