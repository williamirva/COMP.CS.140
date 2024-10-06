package controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
public class SystemInfoController {

    public String getSystemInfo() throws IOException, InterruptedException {
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        String processes = executeCommand("ps -aux");
        String diskSpace = executeCommand("df -h");
        String uptime = executeCommand("uptime -p");

        return String.format("IP Address: %s\nProcesses:\n%s\nDisk Space:\n%s\nUptime: %s\n", ipAddress, processes, diskSpace, uptime);
    }

    public String getNodeSystemInfo() throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://node-app:8080/system-info"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map systemInfo = objectMapper.readValue(jsonResponse, Map.class);

            return "System Information:\n" +
                    "IP Address: " + systemInfo.get("ipAddress") + "\n" +
                    "Processes: \n" + systemInfo.get("processes") + "\n" +
                    "Disk Space: \n" + systemInfo.get("diskSpace") + "\n" +
                    "Uptime: " + systemInfo.get("uptime") + "\n";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing system info";
        }
    }

    private String executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("bash", "-c", command);

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }

        return output.toString();
    }
}

