package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Response that tries to find a URL for a given key/path.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class ResolverResponse implements IResponse {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ResolverResponse.class);

    private static final String DUPLICATE_URL_ERROR_MSG = "ERROR: duplicate key value violates unique constraint \"mappings_value_uindex\"";

    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;

    ResolverResponse(ConfigManager configManager, DatabaseManager databaseManager) {
        this.configManager = configManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public Response getResponse(Request req) {
        if (req.getAttribute("password").equals(configManager.getAppPassword())) {
            String key = req.getAttribute("key");
            String value = req.getAttribute("value");

            if (key == null || key.isEmpty() || key.equals("null")) {
                key = new RandomString(8).nextString();
            }

            if (value.isEmpty()) {
                return new Response("URL may not be empty!", Response.BAD_REQUEST);
            }

            try {
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                log.warn("Unable to decode url: '" + value + "'!", e);
                return new Response("Unable to parse url '" + value + "':\n\t" + e.getMessage(),
                        Response.BAD_REQUEST);
            }

            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return new Response("URL '" + value + "' is invalid!\n" + e.getMessage(),
                        Response.BAD_REQUEST);
            }

            try {
                databaseManager.saveUrl(key, value);
            } catch (SQLException e) {
                log.error("Unable to save url: '" + value + "' with key: '" + key + "'!", e);
                if (e.getMessage().contains(DUPLICATE_URL_ERROR_MSG)) {
                    try {
                        return new Response("This URL is already saved as: '" + databaseManager.getKeyForUrl(value) + "'");
                    } catch (SQLException ex) {
                        log.error("Unable to look up key for url: '" + value + "' but it seems to be existing!", ex);
                        return new Response("Unable to save url:\n\t" + e.getMessage() +
                                "\nError looking up already existing url!\n\t" + ex.getMessage(),
                                Response.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    return new Response("Unable to save url:\n\t" + e.getMessage(),
                            Response.INTERNAL_SERVER_ERROR);
                }
            }

            return new Response("OK!\nURL '" + value + "' was saved as '" + key + "'!");
        } else {
            return new Response("You need to enter the correct password for this to work!",
                    Response.UNAUTHORIZED);
        }
    }
}
