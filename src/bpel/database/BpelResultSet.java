package bpel.database;
import java.sql.*;
import bpel.Log;
import java.util.HashMap;
import java.util.Vector;
		  
/**
 * Same as a ResultSet by does not require an open connection
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class BpelResultSet 
{

		  private Vector<HashMap> results;
        private int currentCount;

		  /**
			* Constructor
			*
			* @param rs SQL Result set to transform
			*/
		  public BpelResultSet( ResultSet rs )
		  {
					 String logFunction = "BpelResultSet";
					 Log.logEntry( -1, logFunction);
					 currentCount = -1;
					 results = new Vector<HashMap>();
					 try 
					 {
					    while( rs.next() )
					    {
							 HashMap<String, String> curr = new HashMap<String, String>();
                      ResultSetMetaData meta = rs.getMetaData();
                      int cols = meta.getColumnCount();
					       for (int i=1; i<cols+1; i++) 
						    {
							    curr.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
						    }
							 results.addElement(curr);
						 }
                } catch (SQLException e) {
					    e.printStackTrace();
					 }
					 Log.logExit( -1, logFunction );
		  }

		  /**
			* Increment to the next result
			*
			* @return true if there are elements left, false otherwise
			*/
		  public boolean next()
		  {
					 String logFunction = "BpelResultSet.next";
					 boolean ret = true;
					 Log.logEntry( -1, logFunction);
					 
					 if( currentCount + 1 >= results.size() )
					 {
	                ret = false;
					 } else {
					    currentCount ++;
					 }

					 Log.logExit( -1, logFunction, ret );
					 return ret;
		  }

		  /**
			* Get a string from the results
			*
			* @param name the value to retrieve
			* @return string containing the value
			*/
		  public String getString( String name )
		  {
					 String logFunction = "BpelResultSet.getString";
					 Log.logEntry( -1, logFunction);
					 @SuppressWarnings("unchecked")
					 HashMap<String, String> curr = (HashMap<String, String>)results.elementAt(currentCount);
					 Log.logExit( -1, logFunction, name );
					 return (String)curr.get(name);
		  }

		  /**
			* Get an integer from the results
			*
			* @param name the value to retrieve
			* @return string containing the value
			*/
		  public int getInt( String name )
		  {
					 String logFunction = "BpelResultSet.getInt";
					 Log.logEntry( -1, logFunction);
					 
					 int ret = Integer.valueOf(getString(name));
					 
					 Log.logExit( -1, logFunction, ret );
					 return ret;
		  }

		  /**
			* Unit test
			*
			* @param args commandline arguments, none needed
			*/
        public static void main( String[] args )
        {
					 BpelResultSet br = BpelDatabase.query("SELECT id, state FROM bpel_process;");
					 while( br.next() )
                {
                    System.out.println("ID: " + br.getString("id"));
                    System.out.println("   STATE: " + br.getString("state"));
					 }
        }

}
