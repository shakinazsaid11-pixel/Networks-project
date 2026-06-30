
import java.io.*;
import java.net.*;
import java.util.*;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;


    /**
     * TCP Notification Server
     * Listens on port 9999 and receives notification messages from email client
     * Displays:  "Email Sent Successfully", "Email Received Successfully", "Email Sending Failed"
     */
    public class NotificationServer {

        private int port;
        private ServerSocket serverSocket;
        private boolean running;
        private List<String> notifications;

        public NotificationServer(int port) {
            this.port = port;
            this.notifications = Collections.synchronizedList(new ArrayList<>());
            this.running = false;
        }

        /**
         * Start the TCP notification server
         */
        public void start() {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                System.out.println("✓ Notification Server started on port " + port);

                // Accept connections in a separate thread
                Thread acceptThread = new Thread(() -> {
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            new Thread(new ClientHandler(clientSocket)).start();
                        } catch (IOException e) {
                            if (running) {
                                System.err.println("Error accepting client: " + e.getMessage());
                            }
                        }
                    }
                });
                acceptThread.setDaemon(true);
                acceptThread.start();

            } catch (IOException e) {
                System.err.println("✗ Error starting notification server: " + e.getMessage());
                running = false;
            }
        }

        /**
         * Stop the notification server
         */
        public void stop() {
            running = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                System.out.println("✓ Notification Server stopped");
            } catch (IOException e) {
                System.err.println("Error closing server: " + e.getMessage());
            }
        }

        public List<String> getNotifications() {
            return notifications;
        }

        /**
         * Handler for each client connection
         */
        private class ClientHandler implements Runnable {
            private Socket socket;

            public ClientHandler(Socket socket) {
                this.socket = socket;
            }

            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );

                    // Read notification message
                    String message = in.readLine();
                    if (message != null) {
                        notifications.add(message);
                        System.out.println("[SERVER] Notification received: " + message);
                    }

                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        }
    }

