package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class CommunicationThread extends Thread {
    private final ServerThread serverThread;
    private final Socket socket;

    static final String MAX_INT_STR = Integer.toString(Integer.MAX_VALUE);

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    // run() method: The run method is the entry point for the thread when it starts executing.
    // It's responsible for reading data from the client, interacting with the server,
    // and sending a response back to the client.
    @Override
    public void run() {
        // It first checks whether the socket is null, and if so, it logs an error and returns.
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Create BufferedReader and PrintWriter instances for reading from and writing to the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (operation)");

            // Read the operation values sent by the client
            String operation = bufferedReader.readLine();
//            String informationType = bufferedReader.readLine();
            if (operation == null || operation.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (operation)!");
                return;
            }

            String resultString;

            // Split operation into operator, operand1, operand2
            String[] operationParts = operation.split(",");
            String operator = operationParts[0];
            String operand1 = operationParts[1];
            String operand2 = operationParts[2];

            int operand1Int;
            int operand2Int;

            // Cast operands to int
            try {
                operand1Int = Integer.parseInt(operand1);
            } catch (NumberFormatException e) {
                resultString = "overflow";
                printWriter.println(resultString);
                printWriter.flush();
                return;
            }

            try {
                operand2Int = Integer.parseInt(operand2);
            } catch (NumberFormatException e) {
                resultString = "overflow";
                printWriter.println(resultString);
                printWriter.flush();
                return;
            }

            // If operator is add, then add the two operands
            int result = 0;
            if (operator.equals("add")) {
                try {
                    result = operand1Int + operand2Int;
                } catch (NumberFormatException e) {
                    resultString = "overflow";
                    printWriter.println(resultString);
                    printWriter.flush();
                    return;
                }
            }

            // If operator is mul, then multiply the two operands, but add a sleep of 2 seconds
            if (operator.equals("mul")) {
                Thread.sleep(2000);
                try {
                    result = operand1Int * operand2Int;
                } catch (NumberFormatException e) {
                    resultString = "overflow";
                    printWriter.println(resultString);
                    printWriter.flush();
                    return;
                }
            }

            // Send the result back to the client
            printWriter.println(String.valueOf(result));
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (InterruptedException e) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + e.getMessage());
            if (Constants.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    public static boolean checkInt(String s) {
        return s.length() > MAX_INT_STR.length() || s.compareTo(MAX_INT_STR) > 0;
    }
}
