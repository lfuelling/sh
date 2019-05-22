package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Class that represents the thread the server is running on.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class ServerThread implements Runnable {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ServerThread.class);

    private boolean shouldStop = false;
    private final Routes routes;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;

    ServerThread(Routes routes, ConfigManager configManager, DatabaseManager databaseManager) {
        this.routes = routes;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {
        try {
            Server server = new Server(routes, configManager, databaseManager);
            log.info("Server started!");
            while (!shouldStop) {
                try {
                    Request req = server.accept();
                    server.sendResponse(req);
                } catch (IOException e) {
                    log.warn("Unable to handle request!", e);
                }
            }
            server.close();
        } catch (IOException e) {
            log.error("Unable to run server!", e);
        }
    }

    void stop() {
        shouldStop = true;
    }
}