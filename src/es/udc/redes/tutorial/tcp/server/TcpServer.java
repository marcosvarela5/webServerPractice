package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;


//** Multithread TCP echo server. *//*


public class TcpServer {

  public static void main(String argv[]) {
    if (argv.length != 1) {
      System.err.println("Format: es.udc.redes.tutorial.tcp.server.TcpServer <port>");
      System.exit(-1);
    }
    ServerSocket socket = null;
    try {
      // Create a server socket
      // Set a timeout of 300 secs
      int port = Integer.parseInt(argv[0]);
      socket = new ServerSocket(port);
      socket.setSoTimeout(300000);

      while (true) {
        // Wait for connections
        Socket accept = socket.accept();
        // Create a ServerThread object, with the new connection as parameter
        ServerThread server = new ServerThread(accept);
        // Initiate thread using the start() method
        server.start();
      }
    } catch (SocketTimeoutException e) {
      System.err.println("Nothing received in 300 secs");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
     } finally{
	    try{
	      socket.close();
        }
	    catch (IOException exc){
	      exc.printStackTrace();
        }
    }
  }
}
