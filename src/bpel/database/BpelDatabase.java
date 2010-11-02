package bpel.database;
import java.sql.*;
import bpel.Log;
		  
/**
 * Static functions for directly running SQL queries
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class BpelDatabase 
{
		  public static final String url = "jdbc:mysql://localhost:3306/bpel_engine";
		  public static final String user = "foo";
		  public static final String pass = "foo";
		  public static Connection qCon = null;
		  /**
			* Run an SQL query
			*
			* @param statement query to run
			* @return The set of results from the query, null if failure
			*/
		  public static BpelResultSet query(String statement)
		  {
					 String logFunction = "BpelDatabase.query";
					 Log.logEntry( -1, logFunction);
					 ResultSet rs = null;
					 Statement stmt = null;
					 BpelResultSet ret = null;
					 try 
					 {
								Class.forName("com.mysql.jdbc.Driver");
								if( qCon == null) qCon = DriverManager.getConnection(url,user, pass);
								stmt = qCon.createStatement();
								rs = stmt.executeQuery(statement);
							   ret = new BpelResultSet(rs);	
					 } catch( Exception e ) {
								e.printStackTrace();
					 } finally {
								try
								{
								   if( stmt != null ) stmt.close();
								} catch( Exception e ) {
								   e.printStackTrace();
								}
					 }
					 Log.logExit( -1, logFunction, "BpelResultSet" );
					 return ret;
		  }

		  /**
			* Run an SQL update or insert
			*
			* @param statement update or insert string
			* @return true if successfull, false on error
			*/
		  public static boolean update(String statement)
		  {
					 String logFunction = "BpelDatabase.update";
					 Log.logEntry( -1, logFunction);
					 boolean ret = true;
					 Statement stmt = null;
					 try 
					 {
								Class.forName("com.mysql.jdbc.Driver");
								if( qCon == null) qCon = DriverManager.getConnection(url,user, pass);
								stmt = qCon.createStatement();
								stmt.executeUpdate(statement);
					 } catch( Exception e ) {
								ret = false;
								e.printStackTrace();
					 } finally {
								try
								{
								   if( stmt != null ) stmt.close();
								} catch( Exception e ) {
								   e.printStackTrace();
								}
					 }
					 Log.logExit( -1, logFunction, ret );
					 return ret;
		  }
}
