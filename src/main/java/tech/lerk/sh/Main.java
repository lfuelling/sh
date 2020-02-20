package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Main class.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class Main {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static DatabaseManager databaseManager;
    private static ConfigManager configManager;

    public static void main(String[] args) {

        log.info("Loading configuration...");
        configManager = new ConfigManager();

        log.info("Preparing db...");
        try {
            databaseManager = new DatabaseManager(configManager.getDbConnection(),
                    configManager.getDbUser(), configManager.getDbPassword());
        } catch (SQLException e) {
            log.error("Unable to connect to database!", e);
            System.exit(1);
        }

        log.info("Generating routes...");
        Routes routes = new Routes();
        routes.add("GET", "/", "html/landing.html");
        routes.add("GET", "/style.css", "html/style.css");
        routes.add("GET", "/lib.js", "html/lib.js");
        routes.add("GET", "/favicon.ico", "images/favicon.ico");
        routes.add("GET", "/background.jpg", "images/background.jpg");
        routes.add("POST", "/", new ResolverResponse(configManager, databaseManager));

        log.info("Starting Server...");

        ServerThread server = new ServerThread(routes, configManager, databaseManager);
        new Thread(server, "ServerThread").start();
    }
}
