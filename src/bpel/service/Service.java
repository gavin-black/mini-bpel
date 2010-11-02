package bpel.service;
import java.io.*;
import java.net.*;
import bpel.BpelEngine;

/**
 * Very simple service to read in an http call, kick off the BPEL Engine, and send a response 
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class Service extends Thread 
{
  private Socket socket;
  private BufferedReader request;
  private PrintWriter response;
  private static String text = "";

  /**
	* Constructor, the response and request ar set up to be printable buffered readers and writers on the socket stream
	*
	* @param s socket to set up the streams on
	*/
  public Service(Socket s) throws IOException {
    socket = s;
    request = 
      new BufferedReader(
        new InputStreamReader(
          socket.getInputStream()));

    response = 
      new PrintWriter(
        new BufferedWriter(
          new OutputStreamWriter(
            socket.getOutputStream())), true);
  }
 
 /**
  * Start a request handling thread, should not be called directly
  */
  public void run() {
	int port = 9090;
	BpelEngine bpelEngine = new BpelEngine();
	try 
	{
	     String str = request.readLine();
	     if(str.indexOf("GET") >= 0 && str.indexOf("?") >=0)
		  { 
					 System.err.println("Raw data: " + str);
					 bpelEngine.begin(str.substring(str.indexOf("?") + 1, str.lastIndexOf(" HTTP")));
					 response.println("HTTP/1.1\n");
				    response.println(bpelEngine.getResponse() );
		  }
		  else response.println("ERROR: Could not parse request!");
	} catch (Exception e) {
			  e.printStackTrace();
	} finally {
	    try 
		 {
					socket.close();
	    } catch(IOException e) {
					e.printStackTrace();
		 }
	}
    }
}

