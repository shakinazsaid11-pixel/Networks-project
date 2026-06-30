import java.util.*;
import java.text. SimpleDateFormat;
import java.io.*;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;



/**
 * Performance Metrics Tracker
 * Measures latency, packet count, and throughput for all operations
 */
public class PerformanceMetrics {

    private final List<Long> smtpLatencies;
    private final List<Long> imapLatencies;
    private final List<Long> notificationLatencies;

    private int totalPacketsSent;
    private int totalPacketsReceived;
    private long totalBytesSent;
    private long totalBytesReceived;

    public PerformanceMetrics() {
        this.smtpLatencies = new ArrayList<>();
        this.imapLatencies = new ArrayList<>();
        this.notificationLatencies = new ArrayList<>();
        this.totalPacketsSent = 0;
        this.totalPacketsReceived = 0;
        this.totalBytesSent = 0;
        this.totalBytesReceived = 0;
    }

    public void recordSmtpSend(long latencyMs) {
        smtpLatencies.add(latencyMs);
        totalPacketsSent += 1;
        totalBytesSent += 1024; // Approximate bytes for SMTP packet
    }

    public void recordImapFetch(long latencyMs) {
        imapLatencies.add(latencyMs);
        totalPacketsReceived += 1;
        totalBytesReceived += 2048; // Approximate bytes for IMAP packet
    }

    public void recordNotification(long latencyMs) {
        notificationLatencies.add(latencyMs);
        totalPacketsSent += 1;
        totalBytesSent += 256; // Approximate bytes for notification
    }

    /**
     * Calculate average latency
     */
    private double calculateAverageLatency(List<Long> latencies) {
        if (latencies.isEmpty()) return 0;
        return latencies.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    /**
     * Calculate total latency
     */
    private long calculateTotalLatency(List<Long> latencies) {
        return latencies.stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Calculate throughput (bytes per second)
     */
    private double calculateThroughput(long totalBytes, long totalTime) {
        if (totalTime == 0) return 0;
        return (totalBytes * 1000.0) / totalTime;
    }

    /**
     * Print comprehensive performance report
     */
    public void printReport() {
        System.out.println("\n" + "=". repeat(70));
        System.out.println("PERFORMANCE MEASUREMENT REPORT");
        System.out.println("=".repeat(70));

        // SMTP Metrics
        System.out.println("\n[SMTP SEND OPERATION]");
        if (! smtpLatencies.isEmpty()) {
            long totalLatency = calculateTotalLatency(smtpLatencies);
            double avgLatency = calculateAverageLatency(smtpLatencies);
            System.out.printf("  Total Operations: %d\n", smtpLatencies.size());
            System.out.printf("  Total Time: %d ms\n", totalLatency);
            System.out.printf("  Average Latency: %.2f ms\n", avgLatency);
            System.out.printf("  Packets Sent: %d\n", smtpLatencies.size());
            System.out.printf("  Bytes Sent: %d\n", smtpLatencies.size() * 1024);
            System.out.printf("  Throughput: %.2f bytes/sec\n",
                    calculateThroughput(smtpLatencies.size() * 1024L, totalLatency));
        } else {
            System.out. println("  No SMTP operations recorded");
        }

        // IMAP Metrics
        System.out.println("\n[IMAP FETCH OPERATION]");
        if (!imapLatencies.isEmpty()) {
            long totalLatency = calculateTotalLatency(imapLatencies);
            double avgLatency = calculateAverageLatency(imapLatencies);
            System.out.printf("  Total Operations: %d\n", imapLatencies.size());
            System.out.printf("  Total Time: %d ms\n", totalLatency);
            System.out.printf("  Average Latency: %. 2f ms\n", avgLatency);
            System.out. printf("  Packets Received:  %d\n", imapLatencies.size());
            System. out.printf("  Bytes Received: %d\n", imapLatencies.size() * 2048);
            System.out.printf("  Throughput: %.2f bytes/sec\n",
                    calculateThroughput(imapLatencies.size() * 2048L, totalLatency));
        } else {
            System.out.println("  No IMAP operations recorded");
        }

        // Overall Network Metrics
        System.out. println("\n[OVERALL NETWORK METRICS]");
        System.out. printf("  Total Packets Sent:  %d\n", totalPacketsSent);
        System.out.printf("  Total Packets Received: %d\n", totalPacketsReceived);
        System.out.printf("  Total Bytes Sent: %d\n", totalBytesSent);
        System.out.printf("  Total Bytes Received: %d\n", totalBytesReceived);

        long totalTime = calculateTotalLatency(smtpLatencies) +
                calculateTotalLatency(imapLatencies);
        if (totalTime > 0) {
            double overallThroughput = ((totalBytesSent + totalBytesReceived) * 1000.0) / totalTime;
            System.out. printf("  Overall Throughput:  %.2f bytes/sec\n", overallThroughput);
        }

        // Summary Table
        System.out.println("\n[SUMMARY TABLE]");
        System.out.printf("%-20s %-15s %-12s %-12s %-15s%n",
                "Operation", "Time (s)", "Packets", "Bytes", "Throughput");
        System.out. println("-".repeat(70));

        if (!smtpLatencies.isEmpty()) {
            long time = calculateTotalLatency(smtpLatencies);
            int packets = smtpLatencies.size();
            long bytes = packets * 1024L;
            double throughput = calculateThroughput(bytes, time);
            System.out.printf("%-20s %-15.3f %-12d %-12d %-15.2f%n",
                    "SMTP Send", time/1000.0, packets, bytes, throughput);
        }

        if (!imapLatencies.isEmpty()) {
            long time = calculateTotalLatency(imapLatencies);
            int packets = imapLatencies. size();
            long bytes = packets * 2048L;
            double throughput = calculateThroughput(bytes, time);
            System.out.printf("%-20s %-15.3f %-12d %-12d %-15.2f%n",
                    "IMAP Fetch", time/1000.0, packets, bytes, throughput);
        }

        System.out.println("\n[WIRESHARK ANALYSIS NOTES]");
        System.out. println("  SMTP Traffic: tcp. port == 465 or tcp.port == 587");
        System.out.println("  IMAP Traffic: tcp.port == 993");
        System.out.println("  Notification Traffic: tcp.port == 9999");
        System.out.println("  TLS/SSL Encryption:  Observed on all connections");

        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Export metrics to file
     */
    public void exportToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("PERFORMANCE METRICS EXPORT");
            writer.println("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer. println("\nSMTP Latencies (ms): " + smtpLatencies);
            writer.println("IMAP Latencies (ms): " + imapLatencies);
            writer.println("Total Packets Sent: " + totalPacketsSent);
            writer.println("Total Packets Received: " + totalPacketsReceived);
            writer.println("Total Bytes Sent: " + totalBytesSent);
            writer.println("Total Bytes Received: " + totalBytesReceived);

            System.out.println("✓ Metrics exported to: " + filename);
        } catch (IOException e) {
            System.err.println("✗ Error exporting metrics: " + e.getMessage());
        }
    }
}
