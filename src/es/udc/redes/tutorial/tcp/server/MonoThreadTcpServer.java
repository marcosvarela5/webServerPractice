package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        Socket socket = null;
        try {
            // Create a server socket
            ServerSocket server = null;
            // Set a timeout of 300 secs
            int port = Integer.parseInt(argv[0]);
            server = new ServerSocket(port);
            server.setSoTimeout(300000);
            
            while (true) {
                // Wait for connections
                socket = server.accept();

                // Set the input channel
                BufferedReader socketInput = new BufferedReader
                        (new InputStreamReader(socket.getInputStream()));
                
                // Set the output channel
                PrintWriter socketOutput = new PrintWriter
                        (socket.getOutputStream(), true);
                
                // Receive the client message
                String readLine = socketInput.readLine();
                System.out.println("SERVER: Received " + readLine + " from " + socket.getLocalAddress() + " port: " + socket.getPort());
                
                // Send response to the client
                socketOutput.println(readLine);
                System.out.println("SERVER: Sending " + readLine + " to " + socket.getLocalAddress() + " port: " + socket.getPort());

                // Close the streams
                socketOutput.close();
                socketInput.close();
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
        //    System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
	//Close the socket
            try{
                socket.close();
            } catch (IOException exc){
                exc.printStackTrace();
            }
        }
    }
}
