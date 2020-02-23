package tech.lerk.sh;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.sh.managers.ConfigManager;
import tech.lerk.sh.managers.DatabaseManager;
import tech.lerk.sh.responses.ColorCssResponse;
import tech.lerk.sh.responses.NewEntryResponse;
import tech.lerk.sh.responses.ResolverResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

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

    private static final SecureRandom random = new SecureRandom();

    public static SecureRandom getRandom() {
        return random;
    }

    public static void main(String[] args) {

        log.info("Loading configuration...");
        ConfigManager configManager = new ConfigManager();

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
        routes.add(Method.GET, "/", "html/landing.html");
        routes.add(Method.GET, "/style.css", "css/style.css");
        routes.add(Method.GET, "/favicon.ico", "images/favicon.ico");
        routes.add(Method.GET, "/background.jpg", "images/background.jpg");
        routes.add(Method.POST, "/", new NewEntryResponse(configManager, databaseManager));
        routes.add(Method.GET, "/colors.css", new ColorCssResponse());
        routes.addCatchAll(new ResolverResponse(databaseManager));

        log.info("Starting Server...");

        Server.start(routes, configManager.getPort(), configManager.getMaxSize());
    }
}
