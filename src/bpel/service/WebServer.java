package bpel.service;
import java.io.*;
import java.net.*;

/**
 * Trivial web server that spawns off a new Service to handle each connection
 *
 * @author Gavin Black
 * @author <a href="mailto:gblack@mitre.org">gblack@mitre.org</a>
 */
public class WebServer 
{  
   public static final int DEFAULT_PORT = 9090;

  /**
	* Set up a new webserver
	*
	* @param port The port number to listen on
	*/
   public static void newServer(int port) throws IOException {
      ServerSocket s = new ServerSocket(port);
      InetAddress  addrs= InetAddress.getLocalHost();         
      try {
         while(true) {
            Socket socket = s.accept();
            try {
               (new Service(socket)).start(); 
            } catch(Exception e) {
               socket.close();
            }
         }
      } finally {
        s.close();
      }
   } 

}
