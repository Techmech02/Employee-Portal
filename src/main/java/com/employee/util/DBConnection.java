package com.employee.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/employee_portal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String DB_URL = valueOrDefault("EMPLOYEE_DB_URL", DEFAULT_URL);
    private static final String DB_USER = valueOrDefault("EMPLOYEE_DB_USER", "root");
    private static final String DB_PASSWORD = valueOrDefault("EMPLOYEE_DB_PASSWORD", "12345678");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC driver not found: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static String valueOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key);
        }
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
