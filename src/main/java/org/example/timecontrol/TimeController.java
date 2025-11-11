package org.example.timecontrol;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

@RestController
public class TimeController {

    private Process appProcess;
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
    public String resetTime(@RequestBody(required = false) ResetRequest req) throws Exception {

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

        if (req == null) {
            return "Time set to local and app restarted (no health check URL provided)";
        }

        String healthUrl = req.getHealthUrl();

        boolean healthy = waitForHealth(healthUrl, HEALTH_TIMEOUT_SECONDS);
        if (healthy) {
            return "Time set to local, app restarted & healthy at " + healthUrl;
        } else {
            return "ime set to local, app restarted but not healthy within timeout.";
        }

    }



    @GetMapping("/status")
    public String status() {
        if (appProcess != null && appProcess.isAlive()) {
            return "App running";
        } else {
            return "App stopped";
        }
    }


    // DTO for JSON request
    public static class TimeRequest {
        private String time;
        private String healthUrl; // optional

        public String getTime() { return time; }
        public void setTime(String faketime) { this.time = faketime; }

        public String getHealthUrl() { return healthUrl; }
        public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }
    }

    public static class ResetRequest {
        private String healthUrl; // optional

        public String getHealthUrl() { return healthUrl; }
        public void setHealthUrl(String healthUrl) { this.healthUrl = healthUrl; }
    }

    private boolean waitForHealth(String healthUrl, int timeoutSeconds) throws InterruptedException {
        Instant start = Instant.now();
        Thread.sleep(2000);
        while (Duration.between(start, Instant.now()).getSeconds() < timeoutSeconds) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(healthUrl).openConnection();
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                connection.setRequestMethod("GET");
                int code = connection.getResponseCode();
                if (code == 200) {
                    return true;
                }
            } catch (IOException ignored) {}
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        return false;
    }

}
