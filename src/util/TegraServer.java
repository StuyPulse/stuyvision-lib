package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TegraServer {

    private Socket server;
    private PrintWriter serverOut;

    // Tegra 1: 10.42.0.12
    // Tegra 2: 10.42.0.55
    // Tegra 3: 10.42.0.98
    private static final String tegraHost = "10.42.0.55";

    private static final int tegraPort = 7054;

    public TegraServer() {
        setupSocket();
    }

    /**
     * Creates a server socket on port 7054
     */
    private void setupSocket() {
        try {
            server = new Socket(tegraHost, tegraPort);
            serverOut = new PrintWriter(server.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the socket has been created
     * @return <code>true</code> if the socket has been created, <code>false</code> otherwise
     */
    public boolean socketExists() {
        return serverOut != null;
    }

    /**
     * Send an array of doubles using the socket
     * @param data Array of doubles to send
     */
    public void sendDoubles(double[] data) {
        if (data.length != 3) {
            return;
        }
        String msg = data[0] + "," + data[1] + "," + data[2];
        if (serverOut != null) {
            serverOut.println(msg);
        } else {
            setupSocket();
        }
    }
}