package util;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.UnknownHostException;

public class ClientSocket {

    private static String host = "localhost"; // TODO
    private static int port = 7054;

    private Socket server;
    private PrintWriter serverOut;

    public ClientSocket() {
        setupSocket();
    }
    
    public boolean setupSocket() {
        try {
            server = new Socket(host, port);
            serverOut = new PrintWriter(server.getOutputStream(), true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean socketExists() {
        return serverOut != null;
    }

    public void sendDoubles(double[] data) {
        if (data.length != 3) {
            return;
        }
        String msg = data[0] + "," + data[1] + "," + data[2];
        serverOut.println(msg);
    }
}
