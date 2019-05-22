package sh.lrk.sh.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;

public final class Response {

    // https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html
    public static final String OK = "200 OK";
    public static final String TEMPORARY_REDIRECT = "307 Temporary Redirect";
    public static final String BAD_REQUEST = "400 Bad Request ";
    public static final String UNAUTHORIZED = "401 Unauthorized";
    public static final String NOT_FOUND = "404 Not Found";
    public static final String METHOD_NOT_ALLOWED = "405 Method Not Allowed";
    public static final String INTERNAL_SERVER_ERROR = "500 Internal Server Error";

    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";

    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private final String body;

    public Response(String body) {
        this(body, Response.OK);
    }

    public Response(String body, String code) {
        this(body, code, TEXT_HTML, false, null);
    }

    public Response(String body, String code, String contentType) {
        this (body, code, contentType, false);
    }

    public Response(String body, String code, String contentType, boolean addAllow) {
        this(body, code, contentType, addAllow, null);
    }

    public Response(String body, String code, String contentType, boolean addAllow, String location) {
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

    public static Response getGenericErrorResponse(Request req) {
        return new Response("Nothing found for url '" + req.getUrl() + "' with method '" + req.getMethod() + "'!",
                Response.NOT_FOUND, TEXT_PLAIN);
    }

    public static Response fromFile(Request req, String path) {
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