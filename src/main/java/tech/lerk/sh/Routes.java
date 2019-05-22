package tech.lerk.sh;

import java.util.HashMap;

public class Routes {

    private final HashMap<String, IResponse> routes;

    public Routes() {
        routes = new HashMap<>();
    }

    IResponse get(String key) {
        return routes.get(key);
    }

    public void add(String method, String url, IResponse resp) {
        routes.put(method + "_" + url, resp);
    }

    public void add(String method, String url, String filepath) {
        routes.put(method + "_" + url, req -> Response.fromFile(req, filepath));
    }

}