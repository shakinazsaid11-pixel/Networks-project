import java.io.*;
import java.net.*;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;


    /**
     * Notification Client
     * Sends notification messages to the TCP server after email operations
     */
    public class NotificationClient {

        private String serverAddress;
        private int serverPort;

        public NotificationClient(String serverAddress, int serverPort) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
        }

        /**
         * Send a notification to the server
         * @param message Notification message to send
         * @return true if successful, false otherwise
         */
        public boolean sendNotification(String message) {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()),
                        true
                );

                out.println(message);
                out.flush();
                socket.close();

                return true;
            } catch (IOException e) {
                System.err.println("✗ Error sending notification: " + e.getMessage());
                return false;
            }
        }
    }

