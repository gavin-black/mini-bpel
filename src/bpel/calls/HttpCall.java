package bpel.calls;
import java.net.*;
import java.io.*;
import java.util.*;
import bpel.Log;

/**
 * Static function for making calling URIs, can handle proxies if so configured
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class HttpCall 
{
  public static final boolean useProxy = false;
  public static final String proxy = "gatekeeper.mitre.org";
  public static final String proxyPort = "80";

  /**
	* Call a location on the web, will get a response only
	* 
	* @param uri The location to call
	* @return A string containing the received response
	*/
  public static String call( String uri) 
  {
			 return call( uri, null);
  }

  /**
	* Call a location on the web, as well as POST output
	* 
	* @param uri The location to call
	* @param output The text to POST
	* @return A string containing the received response
	*/
  public static String call( String uri, String output) 
  {
			 String logFunction = "HttpCall.call";
			 HttpURLConnection connection = null;
			 URLConnection urlCon = null; 
			 OutputStream out = null; 
			 InputStream in = null;

			 Log.logEntry( -1, logFunction);
			 String ret = "";
			 String line = "";

			 // TODO both of these are hacks to get around improper reading of special chars in the BPEL file
			 //      it should be trivial to fix, but I haven't yet
			 uri = uri.replace(' ', '&');
			 uri = uri.replace('^', '\"'); 

			 if( uri.length() > 200 ) System.err.println("HTTP uri: " + uri.substring(0, 200) + "...");
			 else System.err.println("HTTP uri: " + uri);
			 try 
			 {
						// Connect to the server
						URL u = new URL(uri);
						if ( useProxy )
						{
								  Properties systemProperties = System.getProperties();
								  systemProperties.setProperty("http.proxyHost",proxy);
								  systemProperties.setProperty("http.proxyPort",proxyPort);
						}
						System.setProperty( "sun.net.client.defaultReadTimeout", "10000" ); 
						Log.logEvent(-1, "URI: " + uri);
						urlCon = u.openConnection();
					   connection = (HttpURLConnection) urlCon;
						connection.setDoOutput(true);
						if( output != null )
						{
								  connection.setDoInput(true);
								  connection.setRequestMethod("POST");
								  out = connection.getOutputStream();
								  OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");
								  wout.write(output);
								  wout.flush();
						}else{
								  connection.setRequestMethod("GET");
						}

					  	in = connection.getInputStream();
						BufferedReader rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						while ((line = rd.readLine()) != null)
						{
								  ret += line + "\n";
						}
			 } catch (Exception e) {
						System.err.println(e);
					   ret = "ERROR";	
			 } finally {
						try
						{
						   if ( out != null) out.close();
					  	   if ( in != null) in.close();
					  	   if ( connection != null) connection.disconnect();
						} catch (Exception exception) {
						   System.err.println(exception);
						}
			 }
			 Log.logExit( -1, logFunction, ret );
			 return ret;
  } 

}
