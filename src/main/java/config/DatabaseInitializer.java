package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {
    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = "finance_tracker";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        try {
            ensureDatabaseExists();
            initializeSchema();
            System.out.println("Database initialization completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void ensureDatabaseExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(BASE_URL + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Check if database exists
            var rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'");
            if (!rs.next()) {
                System.out.println("Creating database " + DB_NAME + "...");
                stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            } else {
                System.out.println("Database " + DB_NAME + " already exists.");
            }
        }
    }

    private static void initializeSchema() throws SQLException, IOException {
        String sqlPath = "db.sql";
        String sql;
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlPath))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }

        try (Connection conn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Executing " + sqlPath + "...");
            // Split by semicolon, but handle the fact that some semicolons might be inside comments or strings
            // For a simple script like this, we can try executing the whole block if the driver supports it,
            // or split by some heuristic. PostgreSQL driver supports multiple statements in one executeUpdate if configured,
            // but usually it's better to split or use a dedicated tool.
            // Let's try executing the whole thing first.
            stmt.executeUpdate(sql);
            System.out.println("Schema initialized.");
        }
    }
}
