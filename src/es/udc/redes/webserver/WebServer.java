package es.udc.redes.webserver;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class WebServer {

    public static void main(String[] args) {

        ServerSocket server;
        Socket socket;
        Properties config = new Properties();
        int port = 1111;


        try {

            config.load(new FileReader("p1-files/server.properties"));
            port = Integer.parseInt(config.getProperty("PORT"));
        } catch (IOException ex) {

        }

        try {
            server = new ServerSocket(port);
            do {
                socket = server.accept();
                new ServerThread(socket,config).start();
            } while (true);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
