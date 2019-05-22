package tech.lerk.sh;

/**
 * Interface to get responses.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public interface IResponse {
    Response getResponse(Request req);
}