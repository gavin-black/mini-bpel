package bpel.operations;
import bpel.Log;
import bpel.database.*;
import bpel.Log;

/**
 * All nodes that are parents of other nodes inherit from this class
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public abstract class InternalNode extends Operation
{
		  private String comp = "";

		  /**
			* Execute an internal node,  Adds the node as parent when running and remove it when done.
			*
			* @param oname The name of the node
			* @param odepth The indentation level
			*/
		  public boolean execute( int id )
		  {
					 String logFunction = "InternalNode.execute";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 try{
					 BpelResultSet rs = BpelDatabase.query("SELECT state FROM bpel_process WHERE id=" + id);
					 rs.next();
					 if( "run".equals(rs.getString("state")) )
					 {
					         Log.logEvent( id, "Internal Node Run: " + name + ", " + type );
								Log.addPublic(id, name, type, "Start");
								BpelDatabase.update("INSERT INTO current_parents(id, type, name) values (" + id + ",\'" + type +
													 "\', \'" + name + "\');");
								action(id);
								ret = true;

								// If we are pausing the process leave the current parents intact, and don't add to the log
								rs = BpelDatabase.query("SELECT state FROM bpel_process WHERE id=" + id);
								rs.next();
								if( "run".equals(rs.getString("state")) )
								{
										  Log.addPublic(id, name, type, "Success");
										  BpelDatabase.update("DELETE FROM current_parents WHERE id=" + id + " AND type=\'" + type + 
													 "\' AND name=\'" + name + "\'");
								}
					 } else if( "resume".equals(rs.getString("state")) ) {
					         Log.logEvent( id, "Internal Node Resume: " + name + ", " + type );
								rs = BpelDatabase.query(
													 "SELECT COUNT(id) FROM current_parents WHERE id=" + id + 
													 " AND name=\'" + name +"\'" );
								rs.next();

								// If the parent is in the database
							   if( rs.getInt("COUNT(id)") > 0 )
								{
										  action(id);
										  ret = true;
								        BpelDatabase.update("DELETE FROM current_parents WHERE id=" + id + " AND type=\'" + type + 
													 "\' AND name=\'" + name + "\'");
								}
					 } else {
					         Log.logEvent( id, "Internal Node Other: " + name + ", " + type );
					 }
					 } catch ( Exception e ) {
								System.err.println(e);
					 }
					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }

		  /**
			* Abstract function for running nodes
			*
			* @param id The current process
			*/
		  public abstract void action( int id );

		  /**
			* Constructor for an internal node, called by classes that extend it.
			*
			* @param oname The name of the node
			* @param otype The type of node this is
			* @param odepth The indentation level
			*/
		  public InternalNode( String oname, String otype, int odepth )
		  {
					 super(oname, otype, odepth);
		  }

}
