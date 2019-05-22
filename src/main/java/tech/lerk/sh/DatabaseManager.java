package tech.lerk.sh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final Connection connection;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        log.info("Connecting to database...");
        connection = DriverManager.getConnection(url, user, password);
    }

    public String getUrlForKey(String key) throws SQLException {
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

    public void saveUrl(String key, String url) throws SQLException {
        String sql = "INSERT INTO sh.mappings (KEY, VALUE) VALUES (?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, url);
        preparedStatement.executeUpdate();
    }

    public String getKeyForUrl(String url) throws SQLException {
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

    public static class NoResultException extends SQLException {
        public NoResultException(String message) {
            super(message);
        }
    }
}
