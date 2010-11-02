package bpel.parse;
import bpel.database.*;

/**
 * Parse RTSTC input, RTSTC is a way to avoid making calls across the network by having machines
 * running both the service they are calling and this BPEL execution engine. 
 * For example normally in orchestration The engine would call Service A, get a response back,
 * and then call service B.  In RTSTC, The engine would call A with it's BPEL and current state and 
 * immediately terminate.  Service A would have a BPEL engine on the same machine that would handle the 
 * incoming request, strip the BPEL specific parts off, and then call Service A on that machine.  It could
 * repeat the same process for service B or continue in a normal manner. 
 * TODO: Change to POST
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class RtstcParser
{
		  /**
			* Parse out the current leaf node that should be running when resuming execution
			*
			* @param id The Id of the BPEL process
			* @param input The string containing the current node in the format type::name
			* @return true on success, false otherwise
			*/
		  public static boolean parseCurrent( int id, String input )
		  {
					 boolean ret = true;

					 String[] parts = input.split("::");
					 if( parts.length != 2 ) 
								ret = false;
					 else
								BpelDatabase.update("UPDATE bpel_process SET state = \'resume\', currentType = \'" + 
													 parts[0] + "\', currentName = \'" + parts[1] + "\' WHERE id = " + id);

					 return ret;
		  }

		  /** 
			* Parse out the parents of the currently running node, needed to properly resume execution
			* @param id The id of the bpel process
			* @param input The string containing the parent list in the format type::name;type::name;...
			* @return true on success, false otherwise
			*/
		  public static boolean parseParents( int id, String input )
		  {
					 boolean ret = true;
					 String[] lines = input.split(";");
					 System.out.println("PARENT STRING " + input);
					 for( int i = 0; i < lines.length; i ++ )
					 {
								String[] parts = lines[i].split("::");
					         System.out.println("line " + i + ")"  + lines[i]);
								if( parts.length != 2 ) 
										  ret = false;
								else
					             BpelDatabase.update("INSERT INTO current_parents(id, type, name) values (" + id + 
													 ", \'" +  parts[0] + "\', \'" + parts[1] + "\');"); 
							   ret = true;
					 }

					 return ret;
		  }

		  /** 
			* Parse out the variables and their state for resuming execution
			* @param id The id of the bpel process
			* @param input The string containing the variable list in the format name::value;name::value;...
			* @return true on success, false otherwise
			*/
		  public static boolean parseVariables( int id, String input )
		  {
					 boolean ret = true;

					 String[] lines = input.split(";");
					 for( int i = 0; i < lines.length; i ++ )
					 {
								String[] parts = lines[i].split("::");
								System.out.println("LINEVAR" + i + ")" + lines[i]); 
								if( parts.length < 2  || parts.length > 3) 
										  ret = false;
								else if ( parts.length == 3 )
								{
					             BpelDatabase.update("INSERT INTO variables(id, type, name, value) values (" + id + 
													 ", \'" +  parts[0] + "\', \'" + parts[1] + "\' , \'" + parts[2] + "\');");
									 System.err.println("RTSTC Insert:" + "type=" + parts[0] + ", name=" + parts[1] + ", value=" + parts[2]); 
								} else {
					             BpelDatabase.update("INSERT INTO variables(id, type, name, value) values (" + id + 
													 ", \'" +  parts[0] + "\', \'" + parts[1] + "\' , \'\');");
									 System.err.println("RTSTC Insert:" + "type=" + parts[0] + ", name=" + parts[1]); 
								}

					 }

					 return ret;
		  }

		  /** 
			* Parse all arguments needed for resuming execution
			* @param id The id of the bpel process
			* @param input The string containing everything
			* @return true on success, false otherwise
			*/
		  public static boolean parseArgs( int id, String args )
		  {
					 boolean ret = true;

					 // Break up the input parameters
					 String[] lines = args.split("&");
					 for( int i = 0; i < lines.length; i ++ )
					 {
								if( lines[i].startsWith("parents=") )
										  parseParents( id, lines[i].substring("parents=".length()));
								else if( lines[i].startsWith("current=") )
										  parseCurrent( id, lines[i].substring("current=".length()));
								else if( lines[i].startsWith("variables=") )
										  parseVariables( id, lines[i].substring("variables=".length()));
								else 
										  ret = false;
					 }

					 return ret;
		  }
}
