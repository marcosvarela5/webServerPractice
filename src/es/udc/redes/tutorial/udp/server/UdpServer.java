package es.udc.redes.tutorial.udp.server;

import javax.xml.crypto.Data;
import java.net.*;

/**
 * Implements a UDP echo sqerver.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }

        DatagramSocket socket = null;
        try {
            // Create a server socket
            int port = Integer.parseInt(argv[0]);
            socket = new DatagramSocket(port);
            socket.setSoTimeout(300000);
            // Set max. timeout to 300 secs
            while (true) {
                // Prepare datagram for reception
                byte array[] = new byte[1024];
                DatagramPacket sData = new DatagramPacket(array, array.length);
                
                // Receive the message
                socket.receive(sData);
                System.out.println("SERVER: Received "
                        + new String(sData.getData()) + " from "
                        + sData.getAddress().toString() + ":"
                        + sData.getPort());
                
                // Prepare datagram to send response
                int resPort = sData.getPort();
                InetAddress clientAdress = sData.getAddress();
                DatagramPacket sendDatagram = new DatagramPacket(array, array.length, clientAdress, resPort);

                // Send response
                socket.send(sendDatagram);
                System.out.println("CLIENT: Sending "
                        + new String(sData.getData()) + " to "
                        + sData.getAddress().toString() + ":"
                        + sData.getPort());
            }
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
// Close the socket
            socket.close();
        }
    }
}
