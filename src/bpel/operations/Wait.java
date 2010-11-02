package bpel.operations;
import java.util.Date;
import java.text.SimpleDateFormat;
import bpel.Log;

/**
 * Wait for a period of time (IE ten seconds) or till a specific time ( IE 3pm on Tuesday )
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Wait extends LeafNode
{
		  private static final int untilSleep = 1000;

		  boolean until = false;
		  Date untilDate;
		  int years = 0;
		  int months = 0;
		  int days = 0;
		  int hours = 0;
		  int minutes = 0;
		  int seconds = 0;
		  int milliseconds = 0;

		  public void action(int id)
		  {
					String logFunction = "Wait.action";
					Log.logEntry( id, logFunction);
					long sleepTime = milliseconds + 
					     	           1000 * seconds + 
								  		  60000 * minutes + 
										  360000 * hours;
					try
					{
							  if(!until)
							  {
										 Thread.sleep(sleepTime);
							  } else {
										 Date curDate = new Date();
										 while( curDate.before(untilDate) ) 
										 {
													curDate = new Date();
													Thread.sleep(untilSleep);
										 }
							  }
					} catch (Exception e)
					{
							  System.err.println(e);
					}
					Log.logExit( id, logFunction);
		  }

		  public Wait( String oname, int odepth, String timeStr)
		  {
					 super(oname, "Wait", odepth);
					 String logFunction = "Wait_Constructor";
					 Log.logEntry( -1, logFunction);
					 if( timeStr != null ) 
					 {
								if( timeStr.startsWith("P") ) // For
								{ 
										  String sString = timeStr.substring( timeStr.indexOf("P") + 1, timeStr.indexOf("Y") );
										  years = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.indexOf("Y") + 1, timeStr.indexOf("M") );
										  months = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.indexOf("M") + 1, timeStr.indexOf("D") );
										  days = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.indexOf("T") + 1, timeStr.indexOf("H") );
										  hours = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.indexOf("H") + 1, timeStr.lastIndexOf("M") );
										  minutes = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.lastIndexOf("M") + 1, timeStr.indexOf(".") );
										  seconds = Integer.parseInt(sString);
										  sString = timeStr.substring( timeStr.indexOf(".") + 1, timeStr.indexOf("S") );
										  milliseconds = Integer.parseInt(sString);
								} else { // Until
										  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
										  try
										  {
													 until = true;
													 untilDate = formatter.parse(timeStr);
										  } catch (Exception e) {
													 System.err.println(e);
										  }
								}
					 } else {
								System.err.println("Wait statement must have a time!");
								System.exit(-1);
					 }
					 Log.logExit( -1, logFunction);
		  }
}
