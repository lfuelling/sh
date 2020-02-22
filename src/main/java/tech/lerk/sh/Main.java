package tech.lerk.sh;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

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
        routes.add(Method.GET, "/lib.js", "js/lib.js");
        routes.add(Method.GET, "/favicon.ico", "images/favicon.ico");
        routes.add(Method.GET, "/background.jpg", "images/background.jpg");
        routes.add(Method.POST, "/", new ResolverResponse(configManager, databaseManager));
        routes.addCatchAll(req -> {
            String key = req.getUrl().replaceFirst("/", "");
            try {
                String urlForKey = databaseManager.getUrlForKey(key);
                if (urlForKey != null && !urlForKey.isEmpty()) {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Location", urlForKey);
                    return new Response("You are being redirected...".getBytes(),
                            Status.TEMPORARY_REDIRECT, ContentType.TEXT_PLAIN, headers);
                }
            } catch (DatabaseManager.NoResultException e) {
                log.info("No value found for key: '" + key + "'!", e);
            } catch (SQLException e) {
                log.error("Unable to resolve key: '" + key + "'!", e);
            }
            try {
                Handlebars handlebars = new Handlebars();
                Template template = handlebars.compile("html/error");
                return new Response(template.apply("Not Found!"),
                        Status.NOT_FOUND, ContentType.TEXT_HTML);
            } catch (IOException e) {
                log.error("Unable to render response!", e);
                return new Response("Not Found!",
                        Status.NOT_FOUND, ContentType.TEXT_HTML);
            }
        });

        log.info("Starting Server...");

        Server.start(routes, configManager.getPort(), configManager.getMaxSize());
    }
}
