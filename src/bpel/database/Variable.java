package bpel.database;
import bpel.Log;

/**
 * Static functions for manipulating variables in the database
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class Variable
{
		  
		  /**
			* See if a variable already exists in the database
			*
			* @param id The id of the bpel process to check
			* @param name The name of the variable to look for
			* @return true if it exists, false otherwise
			*/
		  public static boolean exists( int id, String name )
		  {
					 String logFunction = "Variable.exists";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 BpelResultSet rs = BpelDatabase.query("SELECT COUNT(name) FROM variables WHERE name = \'" + name + "\' AND id = \'" + id + "\'" );

					 try 
					 {
								rs.next();
								ret = ( rs.getInt("COUNT(name)") > 0 );
					 } catch ( Exception e ) {
								System.err.println(e);
					 }

					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }

		  /**
			* Add a variable to the database
			*
			* @param id The id of the bpel process the variable is for
			* @param name The name of the variable to insert
			* @param type The variable type to insert
			* @return true if the variable was added, false otherwise
			*/
		  public static boolean add( int id, String name, String type )
		  {
					 String logFunction = "Variable.add";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 String strippedType = type;
					 String changedName = name; //.replace(".", "_");

					 if( type.indexOf(":") >=0  ) 
								strippedType = type.substring( type.indexOf(":") + 1);
					 
					 if ( BpelDatabase.update("INSERT INTO variables(id, name, type, value) values (" + id + 
													 ", \'" +  changedName + "\', \'" + strippedType + "\', \'\');") ) ret = true;

					 Log.logExit( id, logFunction, ret);
					 return ret;

		  }

		  /**
			* Retrieve the type of a variable from the database
			*
			* @param id The id of the bpel process
			* @param name The name of the variable to get
			* @return the value of the variable, null on error
			*/
		  public static String type( int id, String name )
		  {
					 String logFunction = "Variable.type";
					 Log.logEntry( id, logFunction);
					 String ret = null;
					 String changedName = name; //.replace(".", "_");
					 BpelResultSet rs = BpelDatabase.query("SELECT type FROM variables WHERE name = \'" + changedName + 
										  "\' AND id = \'" + id + "\'");
					 if( rs != null )
					 {
								try 
								{
										  rs.next();
										  ret = rs.getString("type");
								} catch ( Exception e ) {
										  System.err.println(e);
								}
					 }

					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }
		  /**
			* Retrieve the value of a variable from the database
			*
			* @param id The id of the bpel process
			* @param name The name of the variable to get
			* @return the value of the variable, null on error
			*/
		  public static String retrieve( int id, String name )
		  {
					 String logFunction = "Variable.retrieve(" + name + ")";
					 Log.logEntry( id, logFunction);
					 String ret = null;
					 String changedName = name; // .replace(".", "_");
					 BpelResultSet rs = BpelDatabase.query("SELECT value FROM variables WHERE name = \'" + changedName + 
										  "\' AND id = \'" + id + "\'");
					 if( rs != null )
					 {
								try 
								{
										  rs.next();
										  ret = rs.getString("value");
								} catch ( Exception e ) {
										  System.err.println(e);
								}
					 }

					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }

		  /**
			* Change the value of a variable
			*
			* @param id The id of the bpel process of the variable
			* @param name The name of the variable to change
			* @param value The new value to use
			* @return true if the variable was changed, false otherwise
			*/
		  public static boolean update( int id, String name, String value )
		  {
					 String logFunction = "Variable.update";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 String changedName = name; //.replace(".", "_");
					 String changedValue = value.replace("'", "\\'");

					 if ( BpelDatabase.update("UPDATE variables SET value=\'" + changedValue + "\' WHERE id=\'" + id + 
													 "\' AND name=\'" + changedName + "\';") ) ret = true;
					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }

		  /**
			* Clear a process from the database to reuse that number
			*
			* @param id The id of the bpel process to clear
			* @return true if it was successfully cleared
			*/
		  public static boolean clearProcess( int id )
		  {
					 String logFunction = "Variable.clearProcess";
					 Log.logEntry( id, logFunction);
					 boolean ret = false;
					 if (  BpelDatabase.update("DELETE FROM variables WHERE id=\'" + id + "\';") &&
						    BpelDatabase.update("DELETE FROM bpel_process WHERE id=\'" + id + "\';") &&
						    BpelDatabase.update("DELETE FROM current_parents WHERE id=\'" + id + "\';") )
								ret = true;
					 Log.logExit( id, logFunction, ret);
					 return ret;
		  }
}
