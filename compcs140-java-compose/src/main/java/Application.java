import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.SystemInfoController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

public class Application {
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
        HttpServer server = HttpServer.create(new InetSocketAddress(8199), 0);
        server.createContext("/", new HttpHandler() {
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
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });
        server.start();
        System.out.println("Server started on port 8080...");
    }
}
