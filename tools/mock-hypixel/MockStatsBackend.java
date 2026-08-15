import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A loopback-only mock implementation of the Stats backend contract.
 *
 * <p>This development tool returns fixed fixture data and never contacts Hypixel.
 * It is not a production backend and must not be represented as one during an
 * application review.</p>
 */
public final class MockStatsBackend {
    private static final int DEFAULT_PORT = 8080;
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final int MAX_HEADER_LINES = 100;
    private static final ExecutorService WORKERS = Executors.newFixedThreadPool(4);

    private static final String STATS_RESPONSE = "{"
            + "\"success\":true,"
            + "\"player\":{"
            + "\"displayname\":\"MockPlayer\","
            + "\"newPackageRank\":\"MVP_PLUS\","
            + "\"rankPlusColor\":\"GOLD\","
            + "\"stats\":{"
            + "\"Bedwars\":{"
            + "\"final_kills_bedwars\":1234,"
            + "\"final_deaths_bedwars\":123,"
            + "\"wins_bedwars\":456,"
            + "\"losses_bedwars\":78,"
            + "\"beds_broken_bedwars\":901,"
            + "\"beds_lost_bedwars\":87,"
            + "\"winstreak\":12},"
            + "\"Duels\":{"
            + "\"wins\":3456,"
            + "\"losses\":234,"
            + "\"kills\":4567,"
            + "\"deaths\":345,"
            + "\"current_winstreak\":8,"
            + "\"best_overall_winstreak\":42}},"
            + "\"achievements\":{\"bedwars_level\":123},"
            + "\"networkExp\":1500000},"
            + "\"guild\":{\"tag\":\"MOCK\",\"tagColor\":\"AQUA\"}}";

    private MockStatsBackend() {
    }

    public static void main(String[] args) throws IOException {
        final int port = port(args);
        final ServerSocket server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close(server);
                WORKERS.shutdownNow();
            }
        }, "mock-stats-backend-shutdown"));

        System.out.println("Mock Stats backend listening on http://127.0.0.1:" + port);
        System.out.println("Set the mod's Stats backend URL to http://127.0.0.1:" + port);
        while (!server.isClosed()) {
            try {
                final Socket socket = server.accept();
                WORKERS.execute(new Runnable() {
                    @Override
                    public void run() {
                        serve(socket);
                    }
                });
            } catch (IOException exception) {
                if (!server.isClosed()) {
                    System.err.println("Unable to accept a mock-backend connection: " + exception.getMessage());
                }
            }
        }
    }

    private static int port(String[] args) {
        if (args.length == 0) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
            return port;
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Port must be an integer between 1 and 65535.");
        }
    }

    private static void serve(Socket socket) {
        try (Socket client = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
            client.setSoTimeout(SOCKET_TIMEOUT_MS);
            String requestLine = reader.readLine();
            if (requestLine == null) {
                return;
            }
            readHeaders(reader);

            String[] request = requestLine.split(" ", 3);
            if (request.length < 2) {
                writeJson(client.getOutputStream(), 400, error("Malformed HTTP request."));
                return;
            }
            if (!"GET".equals(request[0])) {
                writeJson(client.getOutputStream(), 405, error("Only GET is supported."));
                return;
            }

            String target = request[1];
            int queryStart = target.indexOf('?');
            String path = queryStart < 0 ? target : target.substring(0, queryStart);
            if ("/health".equals(path)) {
                writeJson(client.getOutputStream(), 200, "{\"status\":\"ok\"}");
                return;
            }
            if (!"/v1/stats".equals(path)) {
                writeJson(client.getOutputStream(), 404, error("Unknown endpoint."));
                return;
            }
            if (queryStart < 0 || !hasUuid(target.substring(queryStart + 1))) {
                writeJson(client.getOutputStream(), 400, error("A 32-character UUID query parameter is required."));
                return;
            }

            writeJson(client.getOutputStream(), 200, STATS_RESPONSE);
        } catch (IOException exception) {
            System.err.println("Mock-backend request failed: " + exception.getMessage());
        }
    }

    private static void readHeaders(BufferedReader reader) throws IOException {
        for (int line = 0; line < MAX_HEADER_LINES; line++) {
            String header = reader.readLine();
            if (header == null || header.isEmpty()) {
                return;
            }
        }
        throw new IOException("Too many HTTP headers.");
    }

    private static boolean hasUuid(String query) {
        String[] parameters = query.split("&");
        for (String parameter : parameters) {
            int separator = parameter.indexOf('=');
            if (separator > 0 && "uuid".equals(parameter.substring(0, separator))) {
                String value = parameter.substring(separator + 1);
                return value.matches("[0-9a-fA-F]{32}");
            }
        }
        return false;
    }

    private static void writeJson(OutputStream output, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 " + status + " " + statusText(status) + "\r\n"
                + "Content-Type: application/json; charset=utf-8\r\n"
                + "Content-Length: " + bytes.length + "\r\n"
                + "Connection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        output.write(bytes);
        output.flush();
    }

    private static String error(String message) {
        return "{\"success\":false,\"cause\":\"" + message + "\"}";
    }

    private static String statusText(int status) {
        if (status == 200) return "OK";
        if (status == 400) return "Bad Request";
        if (status == 404) return "Not Found";
        if (status == 405) return "Method Not Allowed";
        return "Internal Server Error";
    }

    private static void close(ServerSocket server) {
        try {
            server.close();
        } catch (IOException ignored) {
            // The process is stopping, so there is nothing useful to recover here.
        }
    }
}
