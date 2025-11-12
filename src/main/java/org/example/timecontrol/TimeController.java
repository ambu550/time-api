package org.example.timecontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

@RestController
public class TimeController {

    private Process appProcess;
    private static final Logger logger = LoggerFactory.getLogger("TimeController");
    private final File appJar = new File("/app/myapp/myapp.jar");

    private static final int HEALTH_TIMEOUT_SECONDS = 30;

    @PostMapping("/set-time")
    public String setTime(@RequestBody TimeRequest req) throws Exception {

        String time = req.getTime();
        if (time == null || time.isBlank()) {
            return "Error: Missing 'time' in request body";
        }

        if (!appJar.exists()) {
            return "Error: myapp.jar not found in /app/myapp";
        }

        // Stop previous process if alive
        if (appProcess != null && appProcess.isAlive()) {
            appProcess.destroy();
        }

        // Start app with libfaketime
        ProcessBuilder pb = new ProcessBuilder(
                "bash", "-c",
                "./restart_app.sh \"" + time +"\""
        );
        pb.inheritIO();
        appProcess = pb.start();

        int exitCode = appProcess.waitFor();
        logger.info("Restart script end with exit code: {}", exitCode);

        // Handle optional health check
        String healthUrl = req.getHealthUrl();
        if (healthUrl == null || healthUrl.isBlank()) {
            return "FAKETIME set to " + time + " app restarted (no health check URL provided)";
        }

        boolean healthy = waitForHealth(healthUrl, HEALTH_TIMEOUT_SECONDS);
        if (healthy) {
            return "FAKETIME set to " + time + ", app restarted & healthy at " + healthUrl;
        } else {
            return "ERROR! FAKETIME set to " + time + ", app restarted but not healthy within timeout.";
        }
    }

    @PostMapping("/reset-time")
    public String resetTime(@RequestBody(required = false) HealthRequest req) throws Exception {

        if (!appJar.exists()) {
            return "Error: myapp.jar not found in /app/myapp";
        }

        // Stop previous process if alive
        if (appProcess != null && appProcess.isAlive()) {
            appProcess.destroy();
        }

        // Start app with libfaketime
        ProcessBuilder pb = new ProcessBuilder(
                "bash", "-c",
                "./restart_app.sh"
        );
        pb.inheritIO();
        appProcess = pb.start();

        int exitCode = appProcess.waitFor();
        logger.info("Restart script end with exit code: {}", exitCode);

        if (req == null) {
            return "Time set to local and app restarted (no health check URL provided)";
        }

        String healthUrl = req.getHealthUrl();

        boolean healthy = waitForHealth(healthUrl, HEALTH_TIMEOUT_SECONDS);
        if (healthy) {
            return "Time set to local, app restarted & healthy at " + healthUrl;
        } else {
            return "Time set to local, app restarted but not healthy within timeout.";
        }

    }

    @PostMapping("/check-health")
    public String checkHealth(@RequestBody HealthRequest req) throws InterruptedException {
        String healthUrl = req.getHealthUrl();

        boolean healthy = waitForHealth(healthUrl, HEALTH_TIMEOUT_SECONDS);
        if (healthy) {
            return MessageFormat.format("""
                Service healthy on {0}""",
                    healthUrl);
        } else {
            return "Service unhealthy";
        }

    }


    // DTO for JSON request
    private static class TimeRequest {
        private String time;
        private String healthUrl; // optional

        public String getTime() { return time; }
        public void setTime(String faketime) { this.time = faketime; }

        public String getHealthUrl() { return healthUrl; }
        public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }
    }

    private static class HealthRequest {
        private String healthUrl; // optional

        public String getHealthUrl() { return healthUrl; }
        public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }
    }

    private boolean waitForHealth(String healthUrl, int timeoutSeconds) throws InterruptedException {
        Instant start = Instant.now();
        Thread.sleep(2000);
        logger.info("Start health check.....");
        while (Duration.between(start, Instant.now()).getSeconds() < timeoutSeconds) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(healthUrl).openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                if (code == 200) {
                    logger.info("Service healthy!");
                    return true;
                }
            } catch (IOException ignored) {}
            try {
                logger.info("Recheck healthy...");
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
        }
        logger.info("Not healthy");
        return false;
    }

}
