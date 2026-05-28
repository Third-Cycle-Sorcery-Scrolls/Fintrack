package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/" + (System.getProperty("DB_NAME") != null ? System.getProperty("DB_NAME"): System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "finance_tracker");
    private static final String USER = System.getProperty("DB_USER") != null ? System.getProperty("DB_USER") : System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
    private static final String PASSWORD = System.getProperty("DB_PASSWORD") != null ? System.getProperty("DB_PASSWORD") : System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "password";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected.");
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
}