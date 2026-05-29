package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DATABASE = "finance_tracker";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "password";

    private static final String URL = resolveUrl();
    private static final String USER = readConfig("DB_USER", DEFAULT_USER);
    private static final String PASSWORD = readConfig("DB_PASSWORD", DEFAULT_PASSWORD);

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                throw new SQLException(
                        "Could not connect to database at " + URL + " as user '" + USER + "'. "
                                + "Check PostgreSQL is running and set DB_URL or DB_NAME/DB_HOST/DB_PORT/DB_USER/DB_PASSWORD.",
                        e);
            }

            try {
                initializeSchema(connection);
            } catch (SQLException e) {
                closeQuietly(connection);
                connection = null;
                throw new SQLException("Could not initialize required database schema updates.", e);
            }

            System.out.println("Database connected: " + URL + " as " + USER);
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            System.out.println("Database connection closed.");
        }
    }

    private static void closeQuietly(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // Preserve the original schema initialization error.
        }
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    ALTER TABLE IF EXISTS tags
                    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    """);
        }
    }

    private static String resolveUrl() {
        String explicitUrl = readConfig("DB_URL", null);
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            return explicitUrl.trim();
        }

        String host = readConfig("DB_HOST", DEFAULT_HOST);
        String port = readConfig("DB_PORT", DEFAULT_PORT);
        String database = readConfig("DB_NAME", DEFAULT_DATABASE);
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    private static String readConfig(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
