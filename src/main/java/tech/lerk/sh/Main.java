package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

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
        routes.add("GET", "/favicon.ico", req -> new Response("Not Found!", Response.NOT_FOUND, Response.TEXT_PLAIN));
        routes.add("POST", "/", new ResolverResponse(configManager, databaseManager));

        log.info("Starting Server...");

        ServerThread server = new ServerThread(routes, configManager, databaseManager);
        new Thread(server, "ServerThread").start();

        Scanner sc = new Scanner(System.in);
        System.out.println("\nType 'stop' to stop!\n");
        while (true) {
            try {
                String line = sc.nextLine();
                if (line.equals("stop")) {
                    server.stop();
                    System.exit(0);
                } else {
                    log.warn("Invalid input '" + line + "'! Type 'stop' to stop!");
                }
            } catch (NoSuchElementException e) {
                log.warn("It seems like this application is running in a non-interactive shell. Kill this process to stop.");
                //noinspection InfiniteLoopStatement,StatementWithEmptyBody
                while (true) {
                }
            }
        }
    }
}
