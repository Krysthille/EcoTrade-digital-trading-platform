package file;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Registration {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecotrade";
    private static final String DB_USER = "root";  // Change if needed
    private static final String DB_PASSWORD = "1234";  // Change if needed

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("🚀 Java Backend Running on Port 9090...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔹 Connection Received...");

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String request = reader.readLine();
                if (request != null && request.startsWith("POST")) {
                    String jsonData = reader.readLine();
                    String fullname = jsonData.split("\"fullname\":\"")[1].split("\"")[0];
                    String email = jsonData.split("\"email\":\"")[1].split("\"")[0];
                    String username = jsonData.split("\"username\":\"")[1].split("\"")[0];
                    String password = jsonData.split("\"password\":\"")[1].split("\"")[0];

                    boolean isRegistered = registerUser(fullname, email, username, password);
                    
                    if (isRegistered) {
                        writer.write("HTTP/1.1 302 Found\r\n");
                        writer.write("Location: Login.html\r\n");
                        writer.write("\r\n");
                    } else {
                        writer.write("HTTP/1.1 400 Bad Request\r\n");
                        writer.write("Content-Type: text/plain\r\n");
                        writer.write("Registration failed, please try again.\r\n");
                    }
                    writer.flush();
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean registerUser(String fullname, String email, String username, String password) {
        String sql = "INSERT INTO ecoregister (fullname, email, username, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullname);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);  // ⚠️ Consider hashing passwords in real applications

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
