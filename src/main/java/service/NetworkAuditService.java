package service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkAuditService {
    private static final DateTimeFormatter LOG_TIMESTAMP = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int CONNECT_TIMEOUT_MS = 2_000;

    private final ExecutorService executorService;
    private final Path auditLogPath;

    public NetworkAuditService() {
        this(defaultAuditLogPath());
    }

    NetworkAuditService(Path auditLogPath) {
        this.auditLogPath = auditLogPath;
        this.executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "network-audit-writer");
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<AuditResult> auditSocketAsync(String host, int port, String profileName) {
        return CompletableFuture.supplyAsync(() -> auditSocket(host, port, profileName), executorService);
    }

    public Path getAuditLogPath() {
        return auditLogPath;
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    private AuditResult auditSocket(String host, int port, String profileName) {
        LocalDateTime checkedAt = LocalDateTime.now();
        boolean reachable = false;
        String message;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            reachable = true;
            message = "Socket connection succeeded";
        } catch (IOException ex) {
            message = "Socket connection failed: " + ex.getMessage();
        }

        AuditResult result = new AuditResult(checkedAt, host, port, profileName, reachable, message, auditLogPath);
        appendAuditLine(result);
        return result;
    }

    private void appendAuditLine(AuditResult result) {
        try {
            Path parent = auditLogPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    auditLogPath,
                    result.toLogLine() + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not write network audit log to " + auditLogPath, ex);
        }
    }

    private static Path defaultAuditLogPath() {
        return Path.of(System.getProperty("user.home"), ".fintrack", "network-audit.log");
    }

    public record AuditResult(LocalDateTime checkedAt,
                              String host,
                              int port,
                              String profileName,
                              boolean reachable,
                              String message,
                              Path logPath) {
        public String toLogLine() {
            return String.format(
                    "%s | profile=%s | endpoint=%s:%d | reachable=%s | %s",
                    checkedAt.format(LOG_TIMESTAMP),
                    sanitize(profileName),
                    host,
                    port,
                    reachable,
                    sanitize(message));
        }

        public String summary() {
            String status = reachable ? "reachable" : "unreachable";
            return String.format("%s:%d is %s. Audit saved to %s", host, port, status, logPath);
        }

        private static String sanitize(String value) {
            if (value == null || value.isBlank()) {
                return "unknown";
            }
            return value.replace('\n', ' ').replace('\r', ' ').trim();
        }
    }
}
