# Networks-project
Simple Email Client System Integrates SMTP/IMAP client with TCP notification server

# 📧 Email Client with TCP Notification System (Java)

A Java console application that combines an **SMTP/IMAP email client** with a **custom TCP notification server**, while tracking detailed **network performance metrics** (latency, packets, throughput) for each operation — designed for protocol analysis (e.g. with Wireshark).

---

## 📌 Overview

The app lets a user send and receive email over real SMTP/IMAP servers (e.g. Gmail), while a locally-running TCP server listens for and logs "notification" events triggered by those email operations (success/failure). All network activity is timed and reported through a performance metrics module.

---

## 🧩 Components

| File | Responsibility |
|------|----------------|
| `Main.java` | Console menu — orchestrates the email client, notification server/client, and metrics |
| `EmailClient.java` | Sends mail via **SMTP** and fetches the latest message via **IMAP**, using JavaMail |
| `NotificationServer.java` | TCP server (port `9999`) that accepts connections and logs incoming notification strings |
| `NotificationClient.java` | TCP client that connects to the notification server and sends a status message |
| `PerformanceMetrics.java` | Tracks latency/packets/bytes/throughput for SMTP and IMAP operations, prints a report, and exports to file |

---

## ⚙️ How It Works

1. On startup, the user enters their email, password, SMTP/IMAP host, and ports
2. A `NotificationServer` starts listening on port `9999`
3. From the main menu, the user can:
   1. **Send Email (SMTP)** — composes and sends a message, then fires a TCP notification
   2. **Receive Latest Email (IMAP)** — fetches the most recent inbox message, then fires a TCP notification
   3. **Show Performance Metrics** — prints latency/throughput report for all operations so far
   4. **Export Metrics to File** — writes the same data to a text file
   5. **Show Notifications Received** — lists all notification messages the server has logged
   6. **Exit**

---

## 🛠️ Tech Stack

`Java` · `javax.mail` (JavaMail API) · `java.net` (Sockets/ServerSocket) · `java.util.concurrent`-style threading via `Thread`

**Dependencies** (in `lib/`):
- `jakarta.mail-2.0.2.jar`
- `jakarta.activation-api-2.1.0.jar`

---

## 🚀 How to Run

1. Open the project in IntelliJ IDEA (project includes `.idea` config files) or any Java IDE
2. Make sure the JARs in `lib/` are on the classpath
3. Run `Main.java`
4. When prompted for email credentials, use an **app password** (not your real account password) if using Gmail — see [Google App Passwords](https://support.google.com/accounts/answer/185833)
5. Typical Gmail settings:
   - SMTP: `smtp.gmail.com`, port `587`
   - IMAP: `imap.gmail.com`, port `993`

---

## 📊 Performance Metrics & Wireshark Notes

The app prints a report with per-operation latency, packet/byte counts (estimated), and throughput, plus filter hints for protocol analysis:
```
SMTP Traffic: tcp.port == 465 or tcp.port == 587
IMAP Traffic: tcp.port == 993
Notification Traffic: tcp.port == 9999
```

⚠️ Byte counts for SMTP/IMAP are **hardcoded estimates** (1024 / 2048 bytes per operation), not measured from actual packets — useful for a rough demo, but not accurate for real analysis. If you want real packet sizes, capture with Wireshark using the filters above instead of relying on the in-app numbers.

---

## Known Issue

## 🐛 Bug  — IMAP Port Mismatch on SSL Socket Factory

In `EmailClient.java`, inside `receiveLatestEmail()`:

```java
props.put("mail.imap.socketFactory.port", imapPort);
props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
```

This sets up an **SSL socket factory but never sets `mail.imap.ssl.enable` to `"true"`**, and relies only on `starttls`. With many providers (notably Gmail), IMAP **port 993** expects a direct SSL/TLS connection from the start, not STARTTLS. Mixing `starttls.enable` with a `socketFactory.class` of `SSLSocketFactory` on port 993 frequently causes:

```
javax.mail.MessagingException: Connection reset / EOFException / Could not connect to IMAP
```

**Fix:** for port 993, use direct SSL instead of STARTTLS:

```java
props.put("mail.store.protocol", "imaps");
props.put("mail.imaps.host", imapServer);
props.put("mail.imaps.port", imapPort);
props.put("mail.imaps.ssl.enable", "true");
...
Store store = session.getStore("imaps");
store.connect(imapServer, imapPort, senderEmail, senderPassword);
```

The same SSL-vs-STARTTLS mismatch pattern also affects `sendEmail()` for SMTP — if `smtpPort` is `465` (implicit SSL) rather than `587` (STARTTLS), `mail.smtp.starttls.enable` should be `false` and `mail.smtp.ssl.enable` should be `true`. As written, the code assumes STARTTLS (port 587) and will likely fail if a user enters port 465.

---

## 📋 Other Notes

- The `out/` directory contains compiled `.class` files and IDE artifacts — safe to delete/regenerate, not source of truth
- Credentials (email/password) are read via console input only and never persisted to disk — fine for a demo, but don't hardcode real credentials anywhere if you extend this
- `NotificationServer` notifications are stored only in memory (`List<String>`) — they're lost when the app exits
