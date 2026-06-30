import java.util.Scanner;

/**
 * Main Application - Email Client System
 * Integrates SMTP/IMAP client with TCP notification server
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("\n==== EMAIL CLIENT WITH NOTIFICATION SYSTEM (JAVA) ====\n");

        Scanner scanner = new Scanner(System.in);

        // Initialize components
        System.out.println("==== SETUP CONFIGURATION ====");

        System.out.print("Enter your email:  ");
        String senderEmail = scanner.nextLine();

        System.out.print("Enter your email password: ");
        String senderPassword = scanner.nextLine();

        System.out.print("Enter SMTP server (e.g., smtp.gmail. com): ");
        String smtpServer = scanner.nextLine();

        System.out.print("Enter IMAP server (e. g., imap.gmail.com): ");
        String imapServer = scanner.nextLine();

        System.out.print("Enter SMTP port (e.g., 587): ");
        int smtpPort = Integer.parseInt(scanner.nextLine());

        System.out. print("Enter IMAP port (e.g., 993): ");
        int imapPort = Integer.parseInt(scanner.nextLine());

        // Create email client
        EmailClient emailClient = new EmailClient(
                senderEmail, senderPassword,
                smtpServer, imapServer,
                smtpPort, imapPort
        );

        // Create and start notification server
        NotificationServer notificationServer = new NotificationServer(9999);
        notificationServer. start();

        // Create notification client
        NotificationClient notificationClient = new NotificationClient("localhost", 9999);

        boolean running = true;
        while (running) {
            System. out.println("\n==== MAIN MENU ====");
            System.out.println("1. Send Email (SMTP)");
            System. out.println("2. Receive Latest Email (IMAP)");
            System.out.println("3. Show Performance Metrics");
            System.out.println("4. Export Metrics to File");
            System.out.println("5. Show Notifications Received");
            System.out.println("6. Exit");
            System.out.println("===================");

            System.out.print("\nChoose option (1-6): ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    sendEmailMenu(scanner, emailClient, notificationClient);
                    break;
                case "2":
                    receiveEmailMenu(emailClient, notificationClient);
                    break;
                case "3":
                    emailClient.getMetrics().printReport();
                    break;
                case "4":
                    exportMetricsMenu(scanner, emailClient);
                    break;
                case "5":
                    showNotifications(notificationServer);
                    break;
                case "6":
                    running = false;
                    System.out.println("\nExiting application...");
                    break;
                default:
                    System. out.println("Invalid option. Please try again.");
            }
        }

        notificationServer.stop();
        scanner.close();
        System.exit(0);
    }

    private static void sendEmailMenu(Scanner scanner, EmailClient emailClient,
                                      NotificationClient notificationClient) {
        System.out.println("\n==== SEND EMAIL ====");

        System.out.print("Recipient email: ");
        String recipientEmail = scanner.nextLine();

        System.out.print("Subject: ");
        String subject = scanner.nextLine();

        System.out.print("Body:  ");
        String body = scanner. nextLine();

        boolean success = emailClient.sendEmail(recipientEmail, subject, body);

        if (success) {
            notificationClient.sendNotification("Email Sent Successfully");
        } else {
            notificationClient.sendNotification("Email Sending Failed");
        }
    }

    private static void receiveEmailMenu(EmailClient emailClient,
                                         NotificationClient notificationClient) {
        System.out.println("\n==== RECEIVE EMAIL ====");

        EmailClient.Email email = emailClient.receiveLatestEmail();

        if (email != null) {
            System.out.println("\n[EMAIL DETAILS]");
            System.out. println("Subject: " + email.getSubject());
            System.out.println("Body: " + email.getBody());
            notificationClient.sendNotification("Email Received Successfully");
        } else {
            System.out.println("Failed to retrieve email");
        }
    }

    private static void exportMetricsMenu(Scanner scanner, EmailClient emailClient) {
        System.out.println("\n==== EXPORT METRICS ====");
        System.out.print("Enter filename (e.g., metrics.txt): ");
        String filename = scanner.nextLine();

        emailClient.getMetrics().exportToFile(filename);
    }

    private static void showNotifications(NotificationServer server) {
        System.out. println("\n==== NOTIFICATIONS RECEIVED ====");
        var notifications = server.getNotifications();

        if (notifications. isEmpty()) {
            System.out.println("No notifications received yet");
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, notifications.get(i));
            }
        }
    }
}