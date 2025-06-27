package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;


//** Thread that processes an echo server connection. *//*

public class ServerThread extends Thread {

  private Socket socket;

  public ServerThread(Socket s) {
    // Store the socket s
    socket = s;
  }

  public void run() {
    try {
      // Set the input channel
      BufferedReader socketInput = new BufferedReader(new InputStreamReader(
              socket.getInputStream()));
      // Set the output channel
      PrintWriter socketOutput = new PrintWriter(socket.getOutputStream(), true);;
      // Receive the message from the client
      String readLine = socketInput.readLine();
      System.out.println("SERVER: Received " + readLine + " from " + socket.getLocalAddress() + " port: " + socket.getPort());
      // Sent the echo message to the client
      socketOutput.println(readLine);
      System.out.println("SERVER: Sending " + readLine + " to " + socket.getLocalAddress() + " port: " + socket.getPort());
      // Close the streams
      socketOutput.close();
      socketInput.close();
    } catch (SocketTimeoutException e) {
      System.err.println("Nothing received in 300 secs");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      } finally {
	try{
	  socket.close();
    } catch(IOException exc){
	  exc.printStackTrace();
      }
    }
  }
}
