package com.newgen.loan.db;

import java.io.InputStream;
import java.util.Properties;

/**
 * Module 2 - Database Configuration
 * Loads database connection properties from db.properties or uses defaults.
 */
public class DBConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Notice: db.properties not found or unreadable, using default settings.");
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url", "jdbc:mysql://localhost:3306/loan_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getUser() {
        return properties.getProperty("db.user", "root");
    }

    public static String getPassword() {
        return properties.getProperty("db.password", "system");
    }

    public static String getDriver() {
        return properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }
}
