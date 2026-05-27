package org.atm.igiresystem.lab2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    private static final String URL      = "jdbc:postgresql://localhost:5432/IgireSystemDB";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "God";

    private Connect() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
