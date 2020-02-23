package tech.lerk.sh.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to read the configuration file.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class ConfigManager {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    private int port = 8080; // default value
    private int maxSize = 2048000000; // default value (2MB)
    private String dbConnection = "";
    private String dbUser = "";
    private String dbPassword = "";
    private String appPassword = "howdoyouturnthison"; // default value

    public ConfigManager() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            Properties properties = new Properties();
            properties.load(in);

            port = Integer.parseInt(properties.getProperty("server.port"));
            maxSize = Integer.parseInt(properties.getProperty("server.maxsize"));
            dbConnection = properties.getProperty("db.connection");
            dbUser = properties.getProperty("db.user");
            dbPassword = properties.getProperty("db.password");
            appPassword = properties.getProperty("app.password");

        } catch (NumberFormatException e) {
            log.warn("Unable to parse config, using defaults!", e);
        } catch (FileNotFoundException e) {
            log.warn("Unable to find config, using defaults!", e);
        } catch (IOException e) {
            log.warn("Unable to read config, using defaults!", e);
        }
    }

    public int getPort() {
        return port;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getDbConnection() {
        return dbConnection;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getAppPassword() {
        return appPassword;
    }
}
