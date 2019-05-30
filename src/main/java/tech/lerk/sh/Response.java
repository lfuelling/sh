package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Class that represents a response.
 * <p>
 * Response code constants are according to
 * https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html.
 * <p>Please note: Using a byte array as body allows for a maximum
 * response size of ~2.147GB or 2,147MB as it's limited to {@link Integer#MAX_VALUE}.</p>
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
    static final String TEXT_CSS = "text/css";
    static final String TEXT_PLAIN = "text/plain";
    static final String IMAGE_XICON = "image/x-icon";
    static final String IMAGE_JPEG = "image/jpeg";

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Response.class);

    private final byte[] body;
    private String header;

    Response(byte[] body) {
        this(body, Response.OK);
    }

    Response(byte[] body, String code) {
        this(body, code, TEXT_PLAIN, false, null);
    }

    Response(byte[] body, String code, String contentType) {
        this(body, code, contentType, false);
    }

    Response(byte[] body, String code, String contentType, boolean addAllow) {
        this(body, code, contentType, addAllow, null);
    }

    Response(byte[] body, String code, String contentType, boolean addAllow, String location) {
        this.body = body;
        buildHeader(code, contentType, addAllow, location);
    }

    Response(String body)
    {
        this(body, OK);
    }

    Response(String body, String code)
    {
        this(body, code, TEXT_PLAIN);
    }

    public Response(String body, String code, String contentType)
    {
        this(body.getBytes(), code, contentType);
    }

    private void buildHeader(String code, String contentType, boolean addAllow, String location)
    {
        Date date = new Date();
        String start = "HTTP/1.1 " + code + "\r\n";
        header = "Date: " + date.toString() + "\r\n";
        header += "Content-Type: " + contentType + "\r\n";
        header += "Content-length: " + body.length + "\r\n";

        if (addAllow) {
            header += "Allow: GET, POST\r\n";
        }

        if (location != null) {
            header += "Location: " + location + "\r\n";
        }

        header += "\r\n";
        header = start + header;
    }

    static Response getGenericErrorResponse(Request req) {
        return new Response("Nothing found for url '" + req.getUrl() + "' with method '" + req.getMethod() + "'!",
          Response.NOT_FOUND, TEXT_PLAIN);
    }

    static Response fromFile(Request req, String path) {
      try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        try {
            ClassLoader classLoader = Response.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(path);
            if (resourceAsStream != null) {
                int b;
                while ((b = resourceAsStream.read()) != -1){
                    os.write(b);
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

        if (path.endsWith(".css")) {
            return new Response(os.toByteArray(), OK, TEXT_CSS);
        } else if (path.endsWith(".htm") || path.endsWith(".html")) {
            return new Response(os.toByteArray(), OK, TEXT_HTML);
        } else if(path.endsWith(".ico")) {
            return new Response(os.toByteArray(), OK, IMAGE_XICON);
        } else if(path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return new Response(os.toByteArray(), OK, IMAGE_JPEG);
        } else {
            return new Response(os.toByteArray());
        }
      } catch (IOException e) {
        log.error("Unable to write response to: '" + req.getUrl() + "'!", e);
        return Response.getGenericErrorResponse(req);
      }
    }

    public byte[] getResponseBytes()
    {
        byte[] headerBytes = header.getBytes();
        byte[] res = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, res, 0, headerBytes.length);
        System.arraycopy(body, 0, res, headerBytes.length, body.length);
        return res;
    }
}