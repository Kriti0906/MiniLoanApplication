package com.newgen.loan.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Module 2 - Database Connection Manager
 * Provides thread-safe access to JDBC connections using Singleton pattern / factory methods.
 */
public class DBConnection {

    static {
        try {
            Class.forName(DBConfig.getDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Ensure mysql-connector-j jar is on the classpath.");
        }
    }

    private DBConnection() {
        // Private constructor to prevent direct instantiation
    }

    /**
     * Gets a new database connection.
     * Callers are responsible for closing connections (prefer try-with-resources).
     *
     * @return Connection instance
     * @throws SQLException if a connection failure occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DBConfig.getUrl(),
                DBConfig.getUser(),
                DBConfig.getPassword()
        );
    }

    /**
     * Helper method to test database connectivity.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
