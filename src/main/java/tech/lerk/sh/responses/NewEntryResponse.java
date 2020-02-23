package tech.lerk.sh.responses;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.sh.Main;
import tech.lerk.sh.RandomString;
import tech.lerk.sh.managers.ConfigManager;
import tech.lerk.sh.managers.DatabaseManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Response that saves a new entry.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class NewEntryResponse implements IResponse {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NewEntryResponse.class);

    private static final String DUPLICATE_URL_ERROR_MSG = "ERROR: duplicate key value violates unique constraint \"mappings_value_uindex\"";

    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final Handlebars handlebars = new Handlebars();
    private Template errorTemplate;

    public NewEntryResponse(ConfigManager configManager, DatabaseManager databaseManager) {
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        try {
            errorTemplate = handlebars.compile("html/error");
        } catch (IOException e) {
            log.error("Error compiling error template!", e);
        }
    }

    @Override
    public Response getResponse(Request req) {
        if (req.getAttribute("password").equals(configManager.getAppPassword())) {
            String key = req.getAttribute("key");
            String value = req.getAttribute("value");

            if (key == null || key.isEmpty() || key.equals("null")) {
                key = new RandomString(8, Main.getRandom()).nextString();
            }

            if (value.isEmpty()) {
                return new Response(applyErrorTemplate("URL may not be empty!"),
                        Status.BAD_REQUEST, ContentType.TEXT_HTML);
            }

            try {
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                log.warn("Unable to decode url: '" + value + "'!", e);
                return new Response(applyErrorTemplate("Unable to parse url '" + value + "':\n\t" + e.getMessage()),
                        Status.BAD_REQUEST, ContentType.TEXT_HTML);
            }

            try {
                new URL(value);
            } catch (MalformedURLException e) {
                return new Response(applyErrorTemplate("Invalid URL!"),
                        Status.BAD_REQUEST, ContentType.TEXT_HTML);
            }

            try {
                databaseManager.saveUrl(key, value);
            } catch (SQLException e) {
                log.error("Unable to save url: '" + value + "' with key: '" + key + "'!", e);
                if (e.getMessage().contains(DUPLICATE_URL_ERROR_MSG)) {
                    try {
                        Template template = handlebars.compile("html/already-existing");
                        return new Response(template.apply(databaseManager.getKeyForUrl(value)), ContentType.TEXT_HTML);
                    } catch (SQLException ex) {
                        log.error("Unable to look up key for url: '" + value + "' but it seems to be existing!", ex);
                        return new Response(applyErrorTemplate("Unable to save url:\n\t" + e.getMessage() +
                                "\n\nError looking up already existing url!\n\t" + ex.getMessage()),
                                Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_HTML);
                    } catch (IOException ex) {
                        log.error("Unable to render response!", e);
                        return new Response(applyErrorTemplate("Unable to save url:\n\t" + e.getMessage() +
                                "\n\nError looking up already existing url!\n\t" + ex.getMessage()),
                                Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_HTML);
                    }
                } else {
                    return new Response(applyErrorTemplate("Unable to save url:\n\t" + e.getMessage()),
                            Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_HTML);
                }
            }

            try {
                return new Response(handlebars.compile("html/saved").apply(key),
                        Status.OK, ContentType.TEXT_HTML);
            } catch (IOException e) {
                log.error("Unable to render response!", e);
                return new Response("Link was saved as: '/" + key + "'!");
            }
        } else {
            return new Response(applyErrorTemplate("You entered an incorrect password!"),
                    Status.UNAUTHORIZED, ContentType.TEXT_HTML);
        }
    }

    private String applyErrorTemplate(String message) {
        if (errorTemplate != null) {
            try {
                return errorTemplate.apply(message);
            } catch (IOException e) {
                log.error("Error applying template!", e);
            }
        }
        return message;
    }
}
