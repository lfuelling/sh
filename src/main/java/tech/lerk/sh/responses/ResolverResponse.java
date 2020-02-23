package tech.lerk.sh.responses;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.sh.managers.DatabaseManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class ResolverResponse implements IResponse {
    private static final Logger log = LoggerFactory.getLogger(ResolverResponse.class);

    private final DatabaseManager databaseManager;

    public ResolverResponse(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Response getResponse(Request req) {
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
    }
}
