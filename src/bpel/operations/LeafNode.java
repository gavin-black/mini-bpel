package bpel.operations;
import bpel.database.*;
import bpel.Log;

/**
 * All nodes that have no child nodes inherit from this class
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public abstract class LeafNode extends Operation
{
		  private String comp = "";

		  public boolean execute( int id )
		  {
					 String logFunction = "LeafNode.execute";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 try
					 {
								BpelResultSet rs = BpelDatabase.query(
													 "SELECT currentType, currentName, state FROM bpel_process WHERE id=" + id);
								rs.next();
								if( "run".equals(rs.getString("state")) )
								{
										  BpelDatabase.update("UPDATE bpel_process SET currentType=\'" + type + 
																"\', currentName=\'" + name + "\' WHERE id = " + id);
								        Log.addPublic(id, name, type, "Start");
										  action(id);
								        Log.addPublic(id, name, type, "Success");
										  ret = true;
								} 
								else if( "resume".equals(rs.getString("state")) && 
													 type.equals( rs.getString("currentType")) && 
													 name.equals( rs.getString("currentName")) )
								{
										  BpelDatabase.update("UPDATE bpel_process SET state = \'run\' WHERE id = " + id);
										  ret = true;
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }
					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }


		  public abstract void action( int id );

		  public LeafNode( String oname, String otype, int odepth )
		  {
					 super(oname, otype, odepth);
		  }

}
