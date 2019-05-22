package tech.lerk.sh;

import java.util.HashMap;

/**
 * Class that holds all the routes.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class Routes {

    /**
     * The routes.
     */
    private final HashMap<String, IResponse> routes;

    /**
     * Constructor.
     */
    Routes() {
        routes = new HashMap<>();
    }

    IResponse get(String key) {
        return routes.get(key);
    }

    void add(String method, String url, IResponse resp) {
        routes.put(method + "_" + url, resp);
    }

    void add(String method, String url, String filepath) {
        routes.put(method + "_" + url, req -> Response.fromFile(req, filepath));
    }

}