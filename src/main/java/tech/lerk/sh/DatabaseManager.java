package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Utility class that manages database operations.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class DatabaseManager {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final Connection connection;

    DatabaseManager(String url, String user, String password) throws SQLException {
        log.info("Connecting to database...");
        connection = DriverManager.getConnection(url, user, password);
    }

    String getUrlForKey(String key) throws SQLException {
        String sql = "SELECT VALUE FROM sh.mappings WHERE key = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("VALUE");
        } else {
            throw new NoResultException("No values found for key: '" + key + "'");
        }
    }

    void saveUrl(String key, String url) throws SQLException {
        String sql = "INSERT INTO sh.mappings (KEY, VALUE) VALUES (?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, url);
        preparedStatement.executeUpdate();
    }

    String getKeyForUrl(String url) throws SQLException {
        String sql = "SELECT KEY FROM sh.mappings WHERE value = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, url);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("KEY");
        } else {
            throw new NoResultException("No key found for url: '" + url + "'");
        }
    }

    static class NoResultException extends SQLException {
        NoResultException(String message) {
            super(message);
        }
    }
}
