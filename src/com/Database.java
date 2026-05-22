// Database.java

package com.ecotrade;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/ecotrade";
    private static final String USER = "root";
    private static final String PASSWORD = "ViewAl3x@M3nd0z@"; // Put your MySQL password here

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
