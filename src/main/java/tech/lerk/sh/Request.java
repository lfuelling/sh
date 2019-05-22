package tech.lerk.sh;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class that represents a request.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
final class Request {

    private String req;
    private String method = null;
    private String url = null;
    private String httpVersion = null;
    private HashMap<String, String> attrs;

    Request(String req) {
        this.req = req;
        attrs = new HashMap<>();
        parse();
    }

    private void parse() {
        String[] temp = req.split("\r\n");
        String firstLine = temp[0];
        String[] firstLineSplit = firstLine.split(" ");
        if (firstLineSplit.length == 3) {
            method = firstLineSplit[0];
            httpVersion = firstLineSplit[2];
            if (method.equals("POST")) {
                url = firstLineSplit[1];
                setAttributes(temp[temp.length - 1]);
            } else if (method.equals("GET")) {
                String[] arr = firstLineSplit[1].split("[?]");
                if (arr.length == 2) {
                    url = arr[0];
                    setAttributes(arr[1]);
                } else {
                    url = firstLineSplit[1];
                }
            } else {
                url = firstLineSplit[1];
            }
        }
    }

    Iterator getAttributeIterator() {
        return attrs.keySet().iterator();
    }

    private void setAttributes(String rawAttributes) {
        String[] attribs = rawAttributes.split("&");

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < attribs.length; i++) {
            String[] attr = attribs[i].split("=");
            if (attr.length == 2) {
                setAttribute(attr[0], attr[1].replace("+", " "));
            }
        }
    }

    String getAttribute(String key) {
        String ret = attrs.get(key);
        if (ret == null) {
            return "null";
        }
        return ret;
    }

    private void setAttribute(String key, String value) {
        attrs.put(key, value);
    }

    String getMethod() {
        return method;
    }

    String getHttpVersion() {
        return httpVersion;
    }

    String getUrl() {
        return url;
    }

    public String toString() {
        return req;
    }
}