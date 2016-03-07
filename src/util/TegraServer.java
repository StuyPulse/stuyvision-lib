package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class TegraServer {

    private ServerSocket serverSocket;
    private Socket client;
    private BufferedWriter clientWriter;

    private static final int tegraPort = 7123;

    private static final int soTimeout = 500; // in ms

    public TegraServer() {
        setupServer();
    }

    /**
     * Initializes the server socket on port 7054
     */
    private void setupServer() {
        try {
            System.out.println("About to set up server");
            serverSocket = new ServerSocket(tegraPort);
            serverSocket.setSoTimeout(soTimeout);
            System.out.println("Set up server");
        } catch (IOException e) {
            System.out.println("An IOException has occured when"
                + " creating `serverSocket`. Message: " + e.getMessage());
        }
    }

    public void sendData(double[] vector) {
        sendString(stringifyData(vector));
    }

    public void sendString(String data) {
        if (clientWriter == null) {
            connectToAClient();
        }
        try {
            clientWriter.write(data);
            clientWriter.newLine();
            clientWriter.flush();
            System.out.println("Sent data");
        } catch (SocketException e) {
            // This occurs when the client closes, so we need
            // to clear current connection objects and reconnect
            client = null;
            clientWriter = null;
            // And try sending it again, reconnecting to the client
            sendString(data);
        } catch (IOException e) {
            System.out.println("An IOException has occured. Message: " + e.getMessage());
        }
    }

    public String stringifyData(double[] vector) {
        // Don't include "\n" in return value because running `clientWriter.newLine()`
        // after `.write()`ing is more reliable than including "\n" in the
        // `.write()` String. `.newLine()` sends whatever newline sequence
        // is necessary for the system, not necessarily \n.
        if (vector == null) {
            return "none";
        }
        return vector[0] + "," + vector[1] + "," + vector[2];
    }

    public boolean connectToAClient() {
        try {
            client = null;
            while (client == null) {
                try {
                    client = serverSocket.accept();
                    System.out.println("Connected with client");
                } catch (SocketTimeoutException e) {
                    // Ignore and try again to accept()
                    System.out.println("Connection timed out. Trying again.");
                }
            }
            clientWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream()));
            return true;
        } catch (IOException e) {
            System.out.println("An IOException has occured. Message: " + e.getMessage());
            return false;
        }
    }

    // Only for testing/demoing without a roborio:
    private static double[] parseMessage(String data) {
        if (data.equals("none")) {
            return null;
        }
        String[] dbls = data.split(",");
        if (dbls.length < 3) {
            return null;
        }
        double[] result = new double[3];
        for (int i = 0; i < dbls.length; i++) {
            // parseDouble auto-trims
            result[i] = Double.parseDouble(dbls[i]);
        }
        return result;
    }

    // Only run during testing
    public static void main(String[] args) {
        TegraServer ts = new TegraServer();
        ts.connectToAClient();
        Scanner s = new Scanner(System.in);
        while (s.hasNext()) {
            ts.sendData(parseMessage(s.nextLine()));
        }
        s.close();
    }
}
