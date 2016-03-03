package util;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class TegraServer {

    private ServerSocket socket;
    private Socket portInput;

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
            //socket = new ServerSocket(socketPort);
            //socket.setSoTimeout(socketTimeout);
            socket = new Socket(tegraHost, tegraPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            portInput = socket.accept();
            BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(portInput.getInputStream()));

            while (!Thread.currentThread().isInterrupted()) {
                latestData.set(parseMessage(in.readLine()));
                System.out.println(Arrays.toString(latestData.get()));
            }
            socket.close();
        } catch (IOException e) {
            if (e instanceof InterruptedIOException) {
                System.out.println("Connection timed out");
            }
            e.printStackTrace();
        }
    }

    /**
     * Parses three doubles separated by commas into an array of 3
     * <code>double</code>s
     * @param data Three <code>double</code> literals separated by commas,
     * with optional whitespace around each <code>double</code>
     * @return An array of three <code>double</code>s, read from
     * <code>data</code>
     */
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
        if (result[0] == Double.POSITIVE_INFINITY
                && result[1] == Double.POSITIVE_INFINITY
                && result[2] == Double.POSITIVE_INFINITY) {
            return null;
        }
        return result;
    }
    
    public double[] getMostRecent() {
        return latestData.get();
    }
}
