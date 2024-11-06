import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.SystemInfoController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.HashMap;

public class Application {
    private static HttpServer server;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        try {
            startHttpServer();
            synchronized (SystemInfoController.class) {
                SystemInfoController.class.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8199), 0);

        server.createContext("/authenticate", new AuthenticationHandler());
        server.createContext("/system-info", new SystemInfoHandler());
        server.createContext("/shutdown", new ShutdownHandler());

        server.start();
    }

    static class AuthenticationHandler implements HttpHandler {
        private static final String VALID_USERNAME = "testuser";
        private static final String VALID_PASSWORD = "testpwd";
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap credentials;

                try (InputStream requestBody = exchange.getRequestBody()) {
                    credentials = objectMapper.readValue(requestBody, HashMap.class);
                }

                String username = (String) credentials.get("username");
                String password = (String) credentials.get("password");

                String response;
                int statusCode;
                if (VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password)) {
                    response = "Authentication Successful";
                    statusCode = 200;
                } else {
                    response = "Invalid credentials";
                    statusCode = 401;
                }

                exchange.sendResponseHeaders(statusCode, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class SystemInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                SystemInfoController controller = new SystemInfoController();
                try {
                    String localSystemInfo = controller.getSystemInfo();
                    String nodeSystemInfo = controller.getNodeSystemInfo();
                    String response = localSystemInfo + "\n" + nodeSystemInfo;
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class ShutdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "Shutting down the server...";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                SystemInfoController controller = new SystemInfoController();
                try {
                    controller.shutdownNodeServer();
                    System.out.println("Node.js server shut down successfully.");
                } catch (URISyntaxException | InterruptedException e) {
                    System.err.println("Node.js server is already shut down or unreachable.");
                } catch (IOException e) {
                    System.err.println("Failed to shut down Node.js server: " + e.getMessage());
                }

                System.exit(0);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
