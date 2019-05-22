package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;

/**
 * Class that represents a response.
 * <p>
 * Response code constants are according to
 * https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
final class Response {

    static final String OK = "200 OK";
    static final String TEMPORARY_REDIRECT = "307 Temporary Redirect";
    static final String BAD_REQUEST = "400 Bad Request ";
    static final String UNAUTHORIZED = "401 Unauthorized";
    static final String NOT_FOUND = "404 Not Found";
    static final String METHOD_NOT_ALLOWED = "405 Method Not Allowed";
    static final String INTERNAL_SERVER_ERROR = "500 Internal Server Error";

    static final String TEXT_HTML = "text/html";
    static final String TEXT_PLAIN = "text/plain";

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private final String body;

    Response(String body) {
        this(body, Response.OK);
    }

    Response(String body, String code) {
        this(body, code, TEXT_HTML, false, null);
    }

    Response(String body, String code, String contentType) {
        this(body, code, contentType, false);
    }

    Response(String body, String code, String contentType, boolean addAllow) {
        this(body, code, contentType, addAllow, null);
    }

    Response(String body, String code, String contentType, boolean addAllow, String location) {
        Date date = new Date();
        String start = "HTTP/1.1 " + code + "\r\n";
        String header = "Date: " + date.toString() + "\r\n";
        header += "Content-Type: " + contentType + "\r\n";
        header += "Content-length: " + body.length() + "\r\n";

        if (addAllow) {
            header += "Allow: GET, POST\r\n";
        }

        if (location != null) {
            header += "Location: " + location + "\r\n";
        }

        header += "\r\n";
        this.body = start + header + body;
    }

    public String toString() {
        return body;
    }

    private static String replaceRequestAttribute(String res, Request req) {
        Iterator itr = req.getAttributeIterator();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            String val = req.getAttribute(key);
            res = res.replace("${" + key + "}", val);
        }
        return res;
    }

    static Response getGenericErrorResponse(Request req) {
        return new Response("Nothing found for url '" + req.getUrl() + "' with method '" + req.getMethod() + "'!",
                Response.NOT_FOUND, TEXT_PLAIN);
    }

    static Response fromFile(Request req, String path) {
        StringBuilder res = new StringBuilder();
        try {
            ClassLoader classLoader = Response.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(path);
            if (resourceAsStream != null) {
                InputStreamReader reader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
                int c;
                for (c = reader.read(); c != -1; c = reader.read()) {
                    res.append((char) c);
                }
            } else {
                throw new FileNotFoundException("Resource is null: '" + path + "'!");
            }
        } catch (FileNotFoundException e) {
            log.error("Unable to find file: '" + path + "'!", e);
            return Response.getGenericErrorResponse(req);
        } catch (IOException e) {
            log.error("Unable to read file: '" + path + "'!", e);
            return Response.getGenericErrorResponse(req);
        }
        res = new StringBuilder(replaceRequestAttribute(res.toString(), req));
        if (path.endsWith(".css")) {
            return new Response(res.toString(), OK, "text/css");
        } else {
            return new Response(res.toString());
        }
    }
}