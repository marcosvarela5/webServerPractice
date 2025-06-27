package es.udc.redes.webserver;

import javax.swing.text.html.HTML;
import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Map;


public class ServerThread extends Thread {

    private final Socket socket;
    private String web_root = "p1-files/";
    private final Properties config;
    private Date modifiedSince;
    private String request;
    private String log_access_file = "/log/access.log";
    private String log_error_file = "/log/error.log";
    private final ArrayList<String> headLines = new ArrayList<>();
    private int code;
    private long resourceSize;
    private boolean allow = false;

    public ServerThread(Socket s, Properties config) {
        this.socket = s;
        this.config = config;
        web_root = config.getProperty("BASE_DIRECTORY");
        log_access_file = config.getProperty("LOG_ACCESS_FILE");
        log_error_file = config.getProperty("LOG_ERROR_FILE");
        allow = Boolean.parseBoolean(config.getProperty("ALLOW"));
    }

    public void run() {
        String line;

        try {
            //Set input channel
            BufferedReader sInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Set output channel
            DataOutputStream sOutput = new DataOutputStream(socket.getOutputStream());
            socket.setSoTimeout(300000);

            // This code processes HTTP requests and generates
            // HTTP responses
            request = sInput.readLine();
            do {
                line = sInput.readLine();
                if (!line.equals("")) {
                    headLines.add(line);
                    if (line.startsWith("If-Modified-Since:")) {
                        modifiedSince = obtainModifiedSince(line);
                    }
                } else {
                    break;
                }
            } while (true);
            for (String s : headLines) {
            }

            String[] requestPart = request.split(" ");
            String method = requestPart[0];
            String requestedURL = requestPart[1];
            String clientVersion = requestPart[2];

            if (requestPart.length != 3 || !(method.equals("GET") || method.equals("HEAD"))) {
                code = 400;
                sOutput.write(("HTTP/1.1 400 Bad Request\r\n").getBytes());
                sOutput.write(generateHeader("error/error400.html").getBytes());
                sOutput.write("\r\n".getBytes());
                sOutput.write(obtainBytes("error/error400.html"));
                sOutput.flush();
            } else if (requestedURL.contains(".do")) {
                int barIndex = requestedURL.lastIndexOf('/');
                int pointIndex = requestedURL.lastIndexOf('.');
                String servletName = "es.udc.redes.webserver." + requestedURL.substring(barIndex + 1, pointIndex);

                int askIndex = requestedURL.lastIndexOf('?');
                String consult = requestedURL.substring(askIndex + 1);
                System.out.println("Consultation submitted: " + consult);
                String[] mapInput = consult.split("&");
                int index;
                Map<String, String> param = new Hashtable<>();
                for (String s : mapInput) {
                    index = s.indexOf('=');
                    param.put(s.substring(0, index), s.substring(index + 1));
                    System.out.println(s);
                }
                System.out.println(" ******** Map **********");
                for (Map.Entry e : param.entrySet()) {
                    System.out.println(e);
                }
                System.out.println("*********************************");

                String generatedDynamicResource = null;
                try {
                    generatedDynamicResource = ServerUtils.processDynRequest(servletName, param);
                } catch (Exception exception) {
                    System.out.println(exception.getCause());
                }
                System.out.println("Dynamic resource: " + generatedDynamicResource);
                resourceSize = generatedDynamicResource.length();

                StringBuilder sb = new StringBuilder();
                sb.append("HTTP/1.1 200 OK").append("\r\n");
                code = 200;
                sb.append("Date: ").append(generateDateNow()).append("\r\n");
                sb.append("Server: WebServer_390\r\n");
                sb.append("Content-Length: ").append(resourceSize).append("\r\n");
                sb.append("Content-Type: ").append("text/html").append("\r\n");
                sOutput.write(sb.toString().getBytes());
                sOutput.write("\r\n".getBytes());
                sOutput.write(generatedDynamicResource.getBytes());
                sOutput.flush();
                addLog();

            }
            else if ((web_root + requestedURL).endsWith("/") && !(new File((web_root + requestedURL) + config.getProperty("DEFAULT_FILE")).exists()) && allow) {
                File directory = new File((web_root + requestedURL));
                String directoryList = null;
                try {
                    directoryList = HTMLroute(directory);
                } catch (Exception exception) {

                }
                resourceSize = directoryList.length();

                StringBuilder sb = new StringBuilder();
                sb.append("HTTP/1.1 200 OK").append("\r\n");
                code = 200;

                sb.append("Date: ").append(generateDateNow()).append("\r\n");
                sb.append("Server: WebServer_390\r\n");
                sb.append("Content-Length: ").append(resourceSize).append("\r\n");
                sb.append("Content-Type: ").append("text/html").append("\r\n");
                sOutput.write(sb.toString().getBytes());
                sOutput.write("\r\n".getBytes());

                sOutput.write(directoryList.getBytes());
                sOutput.flush();
                addLog();
            }else {
                // Si no queremos redirigir a ningun archivo concreto, dirige por defecto a index.html como se especifica
                // en el archivo de configuracion
                if ((requestedURL).endsWith("/")) {
                    requestedURL = requestedURL + config.getProperty("DEFAULT_FILE");
                }
                sOutput.write(generateStateHeader((requestedURL)).getBytes());
                sOutput.write("\r\n".getBytes());
                if (method.equals("GET")) {
                    sOutput.write(obtainBytes(requestedURL));
                }
                sOutput.flush();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateDateNow() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String strDateNow = sdf.format(now);

        return strDateNow;
    }

    private String generateDateLastModified(long lastModified) {

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String strLastModified = sdf.format(new Date(lastModified));
        return strLastModified;
    }
    private String generateStateHeader(String requestedResource) {

        StringBuilder sb = new StringBuilder();
        String fileName;
        File f = new File(web_root + requestedResource);

        if (f.exists()) {
            if (f.canRead()) {
                if (modifiedSince != null && (f.lastModified() / 1000 == modifiedSince.getTime() / 1000)) {
                    code = 304;
                    sb.append("HTTP/1.1 304 Not Modified").append("\r\n");
                    sb.append("Date:").append(generateDateNow()).append("\r\n");
                    sb.append("Server: WebServer_390\r\n");
                    return sb.toString();
                } else {
                    code = 200;
                    sb.append("HTTP/1.1 200 OK").append("\r\n");
                    fileName = requestedResource;
                }
            } else {
                code = 403;
                sb.append("HTTP/1.1 403 Forbidden").append("\r\n");
                fileName = web_root + "error/error403.html";
                f = new File(fileName);
            }
        } else {
            code = 404;
            sb.append("HTTP/1.1 404 Not Found").append("\r\n");
            fileName = web_root + "error/error404.html";
            f = new File(fileName);
        }

        return getString(fileName, sb, f, requestedResource.endsWith(".jpeg"));
    }

    private String getString(String requestedRsc, StringBuilder sb, File f, boolean b) {
        sb.append("Date: ").append(generateDateNow()).append("\r\n");
        sb.append("Server: WebServer_390\r\n");
        sb.append("Last-Modified: ").append(generateDateLastModified(f.lastModified())).append("\r\n");
        resourceSize = f.length();
        sb.append("Content-Length: ").append(resourceSize).append("\r\n");

        if (requestedRsc.endsWith(".html")) {
            sb.append("Content-Type: ").append("text/html").append("\r\n");
        } else if (requestedRsc.endsWith(".txt")) {
            sb.append("Content-Type: ").append("text/plain").append("\r\n");
        } else if (requestedRsc.endsWith(".png")) {
            sb.append("Content-Type: ").append("image/png").append("\r\n");
        } else if (requestedRsc.endsWith(".gif")) {
            sb.append("Content-Type: ").append("image/gif").append("\r\n");
        } else if (requestedRsc.endsWith(".jpg") || b) {
            sb.append("Content-Type: ").append("image/jpeg").append("\r\n");
        } else {
            sb.append("Content-Type: ").append("application/octet-stream").append("\r\n");
        }
        addLog();
        return sb.toString();
    }

    private String generateHeader(String requestedRsc) {

        StringBuilder sb = new StringBuilder();
        File f = new File(web_root + requestedRsc);

        return getString(requestedRsc, sb, f, requestedRsc.endsWith(".jpeg"));
    }

    private byte[] obtainBytes(String requestedResource) {
        int b;
        ByteArrayOutputStream byteOutput = null;
        String fileName;

        File file = new File(web_root + requestedResource);

        if (file.exists()) {
            if (file.canRead()) {

                fileName = web_root + requestedResource;
            } else {

                fileName = web_root + "error/error403.html";
            }
        } else {
            fileName = web_root + "error/error404.html";
        }
        try {
            byteOutput = new ByteArrayOutputStream(); //Buffer
            BufferedInputStream fileReader = null;
            try {
                fileReader = new BufferedInputStream(new FileInputStream(fileName));
            } catch (IOException iOException) {
            }
            while ((b = fileReader.read()) != -1) {
                byteOutput.write(b);
            }
            fileReader.close();
        } catch (IOException iOException) {

        }
        return byteOutput.toByteArray();
    }

    private Date obtainModifiedSince(String line) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzzz");
        try {
            return sdf.parse(line.substring(line.indexOf(':') + 2));
        } catch (ParseException ex) {
            return null;
        }
    }

    private void addLog() {
        FileWriter fw = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
        try {
            if (code >= 200 && code <= 399) {
                fw = new FileWriter(web_root + log_access_file, true);
                fw.append(request + "\n");
                fw.append(socket.getInetAddress().getHostAddress() + "\n");
                fw.append(sdf.format(new Date()) + "\n");
                fw.append(code + "\n");
                fw.append(resourceSize + "\n");

            } else if (code >= 400 && code <= 599) {
                fw = new FileWriter(web_root + log_error_file, true);
                fw.append(request + "\n");
                fw.append(socket.getInetAddress().getHostAddress() + "\n");
                fw.append(sdf.format(new Date()) + "\n");
                fw.append(code + "\n");
            }
            fw.append("------------------------------------------------------\n");
            fw.flush();
            fw.close();

        } catch (IOException ex) {
            System.out.println("ERROR: Error writing in access log");
        }
    }

    private String HTMLroute(File route) {
        StringBuilder sb = new StringBuilder();
        String[] input = route.list();
        sb.append("<html><head><title>Path List</title></head><body>");
        for (String e : input) {
            sb.append("<h4><a href=\"" + e + "\">" + e + "</a></h4>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }
}
