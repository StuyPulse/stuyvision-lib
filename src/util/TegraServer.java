package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class TegraServer {
    
    public static void main(String[] args) {
        TegraServer ts = new TegraServer();
        ts.connectToAClient();
        Scanner s = new Scanner(System.in);
        while (s.hasNext()) {
            ts.sendData(parseMessage(s.nextLine()));
        }
        s.close();
    }
    private ServerSocket serverSocket;
    private Socket client;
    private BufferedReader clientReader;
    private BufferedWriter clientWriter;

    private static final int tegraPort = 7123;//7054;
    
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
            e.printStackTrace();
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
            System.out.println("Sent String");
        } catch (SocketException e) {
            // This occurs when the client closes, so we need
            // to clear current connection objects and reconnect
            client = null;
            clientWriter = null;
            clientReader = null;
            // And try sending it again, reconnecting to the client
            sendString(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String stringifyData(double[] vector) {
        // Exclude "\n" from return value because running `clientWriter.newLine()`
        // after `.write()`ing is more reliable than including "\n" in the
        // `.write()` String. `.newLine()` sends whatever newline sequence
        // is necessary for the system, not necessarily \n.
        return vector[0] + "," + vector[1] + "," + vector[2];
    }

    public boolean connectToAClient() {
        try {
            client = null;
            while (client == null) {
                try {
                    client = serverSocket.accept();
                    System.out.println(".accept()ed connection with client");
                } catch (SocketTimeoutException e) {
                    // Ignore and try again to accept()
                    System.out.println("timeout except");
                }
            }
            clientWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream()));
            // Probably won't ever be necessary:
            //clientReader =
            //        new BufferedReader(
            //                new InputStreamReader(client.getInputStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static double[] parseMessage(String data) {
        String[] dbls = data.split(",");
        if (dbls.length < 3) {
            return null;
        }
        double[] result = new double[3];
        for (int i = 0; i < dbls.length; i++) {
            // trim() takes off trailing \n
            result[i] = Double.parseDouble(dbls[i].trim());
        }
        // Don't do this here, parseMessage is only here for
        // testing as it is.
        //if (result[0] == Double.POSITIVE_INFINITY
        //        && result[1] == Double.POSITIVE_INFINITY
        //        && result[2] == Double.POSITIVE_INFINITY) {
        //    return null;
        //}
        return result;
    }
}
