import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;



    /**
     * Email Client - Handles both SMTP sending and IMAP receiving
     * Includes error handling, performance metrics, and proper connection management
     */
    public class EmailClient {

        private String senderEmail;
        private String senderPassword;
        private String smtpServer;
        private String imapServer;
        private int smtpPort;
        private int imapPort;
        private PerformanceMetrics metrics;

        public EmailClient(String senderEmail, String senderPassword,
                           String smtpServer, String imapServer,
                           int smtpPort, int imapPort) {
            this.senderEmail = senderEmail;
            this.senderPassword = senderPassword;
            this.smtpServer = smtpServer;
            this.imapServer = imapServer;
            this.smtpPort = smtpPort;
            this.imapPort = imapPort;
            this.metrics = new PerformanceMetrics();
        }

        /**
         * Sends an email via SMTP
         * @param recipientEmail Recipient's email address
         * @param subject Email subject
         * @param body Email body text
         * @return true if successful, false otherwise
         */
        public boolean sendEmail(String recipientEmail, String subject, String body) {
            long startTime = System.currentTimeMillis();

            try {
                // Configure SMTP properties
                Properties props = new Properties();
                props.put("mail. smtp.host", smtpServer);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props. put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.socketFactory.port", smtpPort);
                props.put("mail.smtp. socketFactory.class", "javax. net.ssl.SSLSocketFactory");

                // Create session with authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                // Create and configure message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(body);

                // Send the email
                Transport.send(message);

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                metrics.recordSmtpSend(latency);
                System.out.println("✓ Email sent successfully to:  " + recipientEmail);
                System.out.println("  Latency: " + latency + "ms");

                return true;

            } catch (AuthenticationFailedException e) {
                System. err.println("✗ Authentication Failed: Invalid email or password");
                return false;
            } catch (MessagingException e) {
                System. err.println("✗ Error sending email: " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                System.err.println("✗ Unexpected error:  " + e.getMessage());
                return false;
            }
        }

        /**
         * Receives the latest email via IMAP
         * @return Email object containing subject and body, or null if failed
         */
        public Email receiveLatestEmail() {
            long startTime = System.currentTimeMillis();

            try {
                // Configure IMAP properties
                Properties props = new Properties();
                props. put("mail.imap.host", imapServer);
                props.put("mail.imap.port", imapPort);
                props.put("mail.imap.starttls.enable", "true");
                props.put("mail. imap.starttls.required", "true");
                props.put("mail.imap. socketFactory.port", imapPort);
                props.put("mail.imap.socketFactory.class", "javax.net.ssl. SSLSocketFactory");

                // Create session
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                // Connect to IMAP server
                Store store = session.getStore("imap");
                store.connect(imapServer, imapPort, senderEmail, senderPassword);

                // Open inbox
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);

                if (inbox.getMessageCount() == 0) {
                    System.out.println("✓ No emails in inbox");
                    inbox.close(false);
                    store.close();
                    return null;
                }

                // Get the latest message
                Message message = inbox. getMessage(inbox.getMessageCount());

                Email email = new Email(
                        message.getSubject(),
                        message.getContent().toString()
                );

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                metrics.recordImapFetch(latency);
                System.out.println("✓ Email received successfully");
                System.out.println("  Subject: " + email.getSubject());
                System.out.println("  Latency: " + latency + "ms");

                // Close connections properly
                inbox.close(false);
                store.close();

                return email;

            } catch (AuthenticationFailedException e) {
                System.err.println("✗ IMAP Authentication Failed");
                return null;
            } catch (MessagingException e) {
                System.err.println("✗ Error receiving email: " + e.getMessage());
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                System.err.println("✗ Unexpected error:  " + e.getMessage());
                return null;
            }
        }

        public PerformanceMetrics getMetrics() {
            return metrics;
        }

        /**
         * Simple Email data class
         */
        public static class Email {
            private String subject;
            private String body;

            public Email(String subject, String body) {
                this.subject = subject;
                this.body = body;
            }

            public String getSubject() { return subject; }
            public String getBody() { return body; }
        }
    }

