package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Class that represents the server.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
final class Server {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private ServerSocket socket;
    private Socket client;
    private Routes routes;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;

    Server(Routes routes, ConfigManager configManager, DatabaseManager databaseManager) throws IOException {
        socket = new ServerSocket(configManager.getPort());
        this.routes = routes;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
    }

    Request accept() throws IOException {
        client = socket.accept();
        InputStream is = client.getInputStream();
        StringBuilder raw = new StringBuilder();
        int counter = 0;
        int c;
        do {
            if (counter >= configManager.getMaxSize()) {
                break; //TODO: maybe don't just cut stuff but it's enough for this purpose.
            }
            counter++;
            c = is.read();
            raw.append((char) c);
        } while (is.available() > 0);
        return new Request(raw.toString());
    }

    void close() throws IOException {
        socket.close();
    }

    private Response getResponse(Request req) {
        if (req.getMethod() != null) {
            if (req.getMethod().equals("GET") || req.getMethod().equals("POST")) {
                IResponse mappedResponse = routes.get(req.getMethod() + "_" + req.getUrl());
                if (mappedResponse == null) {
                    String key = req.getUrl().replaceFirst("/", "");
                    try {
                        String urlForKey = databaseManager.getUrlForKey(key);
                        if (urlForKey != null && !urlForKey.isEmpty()) {
                            String message = "You are being redirected...";
                            return new Response(message.getBytes(),
                                    Response.TEMPORARY_REDIRECT, Response.TEXT_PLAIN, false, urlForKey);
                        }
                    } catch (DatabaseManager.NoResultException e) {
                        log.info("No value found for key: '" + key + "'!");
                    } catch (SQLException e) {
                        log.error("Unable to resolve key: '" + key + "'!", e);
                    }
                    return Response.getGenericErrorResponse(req);
                }
                return mappedResponse.getResponse(req);
            } else {
                String message = "Method '" + req.getMethod() + "' is not allowed!";
                return new Response(message.getBytes(),
                        Response.METHOD_NOT_ALLOWED, Response.TEXT_PLAIN, true);
            }
        } else {
            return new Response("Method is null!\n\n" + req.toString(),
                    Response.INTERNAL_SERVER_ERROR, Response.TEXT_PLAIN);
        }
    }

    void sendResponse(Request req) throws IOException {
        Response resp = getResponse(req);
        try (OutputStream out = client.getOutputStream()) {
            out.write(resp.getResponseBytes());
        }
    }

}