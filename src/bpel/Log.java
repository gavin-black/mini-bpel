package bpel;
import bpel.database.*;
import java.sql.*;
import java.util.Date;

/**
 * Static functions for adding logging
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 *
 */
public class Log
{
		  // Verbosity of the logging
		  // 0: No logs
		  // 1: Events only
		  // 2: Tracing without return values and events
		  // 3: Full tracing and events
		  public static final int LOG_VERBOSITY = 3;

		  /**
			* Add an exposed log entry.  This is accessible via the web interface
			*
			* @param id The id of the bpel process the log is for
			* @param name The name of the operation to insert
			* @param type The type of the operation to insert
			* @return true if added to the log, false otherwise
			*/
		  public static boolean addPublic( int id, String name, String type, String description )
		  {
					 boolean ret = true;
					 String timestamp = (new Date()).toString();

					 if ( BpelDatabase.update("INSERT INTO log_entries(id, type, name, description) values (" + id + 
													 ", \'" + type + "\', \'" + name + "\', \'" +  description + "\');") ) ret = true;

					 return ret;
		  }

		  /**
			* Retrieve exposed log entries
			*
			* @param id The id of the bpel process to retrieve the log for
			* @return String containing the entries 
			*/
		  public static String retrievePublic( int id )
		  {
					 return retrievePublic( id, null);
		  }

		  /**
			* Retrieve exposed log entries
			*
			* @param id The id of the bpel process to retrieve the log for
			* @return String containing the entries 
			*/
		  public static String retrievePublic( int id, String time )
		  {
					 String ret = "";

					 BpelResultSet rs = null;

					 if (time == null)  
								rs = BpelDatabase.query(
									     "SELECT num,timestamp, type, name, description FROM log_entries WHERE id = \'" + id + "\'");
					 else
					 {
								if( time.indexOf("%20") > 0 )
										  time = time.substring(0, time.indexOf("%") - 1) + " " + 
													time.substring(time.indexOf("%20") + 3);
								time.replace(':', ' ');
					         System.err.println("LOGGG: " + time);
								rs = BpelDatabase.query(
									     "SELECT num,timestamp, type, name, description FROM log_entries WHERE timestamp >= \'"+ time +"\'");
					 }
					 try
					 {
								while( rs.next() )
								{
										  ret += "- num  : " +  rs.getString("num") + "\n";
										  ret += "  time : " +  rs.getString("timestamp") + "\n";
										  ret += "  ip   : " +  "129.83.174.18:9090\n";  // TODO real data
										  ret += "  diag : " +  "newProcess" + "\n"; // TODO real data
										  ret += "  thrd : " +  id + "\n";
										  ret += "  name : " +  rs.getString("name") + "\n";
										  ret += "  type : " +  rs.getString("type") + "\n";
										  ret += "  desc : " +  rs.getString("description") + "\n";
								}
					 } catch ( Exception e ) {
								System.err.println(e);
					 }
					 return ret;
		  }

		  /**
			* Add a function entry to the debugging log 
			*/
		  public static void logEntry( int id, String functionName)
		  {
					 if ( LOG_VERBOSITY > 1 ) logEvent(id, "ENTRY: " + functionName);
		  }

		  /**
			* Add a function exit to the debugging log
			*/
		  public static void logExit( int id, String functionName, String retValue, String type)
		  {
					 if( LOG_VERBOSITY > 1 )
					 {
								if( LOG_VERBOSITY > 2 ) logEvent(id, "EXIT(" + retValue + "): " + functionName);
								else logEvent(id, "EXIT(" + type + "): " + functionName);
					 }
		  }

		  /**
			* Add a function exit to the debugging log
			*/
		  public static void logExit( int id, String functionName, String retValue )
		  {
					 logExit( id, functionName, retValue, "String: " + retValue );
		  }

		  /**
			* Add a function exit to the debugging log
			*/
		  public static void logExit( int id, String functionName, boolean retValue )
		  {
					 logExit( id, functionName, "" + retValue, "boolean: " + retValue );
		  }

		  /**
			* Add a function exit to the debugging log 
			*/
		  public static void logExit( int id, String functionName, int retValue )
		  {
					 logExit( id, functionName, "" + retValue, "int: " + retValue  );
		  }

		  /**
			* Add a function exit to the debugging log 
			*/
		  public static void logExit( int id, String functionName )
		  {
					 logExit( id, functionName, "void", "void" );
		  }

		  /**
			* Add a debugging log event to the file
			*/
		  public static void logEvent( int id, String text)
		  {
					 if( LOG_VERBOSITY == 0 ) return; 
					 String l = "<" + id + "> " + text;
					 System.out.println(l);
		  }
}
