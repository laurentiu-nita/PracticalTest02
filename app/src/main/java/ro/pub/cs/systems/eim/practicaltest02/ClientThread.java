package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private final String address;
    private final int port;
    private final String operation;
    private final TextView operationResultTextView;

    private Socket socket;

    public ClientThread(String address, int port, String operation, TextView operationResultTextView) {
        this.address = address;
        this.port = port;
        this.operation = operation;
        this.operationResultTextView = operationResultTextView;
    }

    @Override
    public void run() {
        try {
            // tries to establish a socket connection to the server
            socket = new Socket(address, port);

            // gets the reader and writer for the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            // sends the operation to the server
            printWriter.println(operation);
            printWriter.flush();

            String operationInformation;

            // reads the operation information from the server
            while ((operationInformation = bufferedReader.readLine()) != null) {
                final String finalizedOperationInformation = operationInformation;

                // updates the UI with the operation information. This is done using postt() method to ensure it is executed on UI thread
                operationResultTextView.post(() -> operationResultTextView.setText(finalizedOperationInformation));
            }
        } // if an exception occurs, it is logged
        catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    // closes the socket regardless of errors or not
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
