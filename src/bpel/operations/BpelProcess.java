package bpel.operations;
import bpel.database.*;
import bpel.Log;

/**
 * Top level BPEL Process operation
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class BpelProcess extends Operation
{

		  /**
			* Execute a process and update the state to be process.end when done
			* Note: Every other node uses LeafNode and InternalNode execute statements
			*
			* @param id The id of the bpel process to execute
			* @return true
			*/
		  public boolean execute( int id )
		  {
					 String logFunction = "BpelProcess.execute";
					 Log.logEntry( id, logFunction);
					 Log.addPublic(id, name, "Process", "Start");
					 for(int i = 0; i < children.size(); i++)
					 {
							  	children.elementAt(i).execute(id);
					 }
					 Log.addPublic(id, name, "Process", "Success");

					 try {
								BpelResultSet rs = BpelDatabase.query(
													 "SELECT currentType, currentName, state FROM bpel_process WHERE id=" + id);
								rs.next();
								if( "run".equals(rs.getString("state")) )
								{
										  BpelDatabase.update(
												"UPDATE bpel_process SET currentType=\'process\', currentName=\'end\' WHERE id = " + id);
										  BpelDatabase.update("UPDATE bpel_process SET state = \'complete\' WHERE id = " + id);
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }
					 Log.logExit( id, logFunction, true);
					 return true;
		  }

		  /**
			* Return an HTML formatted display of the process running
			*
			* @param id The id of the bpel process to display
			* @return String containing raw HTML for display
			*/
		  public BpelProcess ( String oname, int odepth )
		  {
					 super(oname, "Process", odepth);
		  }
}
