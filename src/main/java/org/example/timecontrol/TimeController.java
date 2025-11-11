package org.example.timecontrol;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
public class TimeController {

    private Process appProcess;
    private final File appJar = new File("/app/myapp/myapp.jar");

    @PostMapping("/set-time")
    public String setTime(@RequestBody TimeRequest req) throws Exception {

        if (req.getTime() == null || req.getTime().isBlank()) {
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
                "./restart_app.sh \"" + req.getTime() +"\""
        );
        pb.inheritIO();
        appProcess = pb.start();

        return "FAKETIME set to " + req.getTime() + " and app restarted";
    }

    @PostMapping("/reset-time")
    public String resetTime() throws Exception {

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

        return "Time set to local and app restarted";
    }



    @GetMapping("/status")
    public String status() {
        if (appProcess != null && appProcess.isAlive()) {
            return "App running";
        } else {
            return "App stopped";
        }
    }

    static class TimeRequest {
        private String time;
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }
}
