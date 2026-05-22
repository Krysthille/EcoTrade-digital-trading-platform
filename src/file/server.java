package file;

import java.io.*;
import java.net.*;
import java.sql.*;

public class server {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ecotrade";
    private static final String DB_USER = "root";  
    private static final String DB_PASSWORD = "";  

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
                    String jsonData = "";
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {} // Skip headers
                    while (reader.ready()) jsonData += (char) reader.read();

                    String username = jsonData.split("\"username\":\"")[1].split("\"")[0];
                    String password = jsonData.split("\"pass\":\"")[1].split("\"")[0];

                    boolean isAuthenticated = authenticateUser(username, password);

                    if (isAuthenticated) {
                        writer.write("HTTP/1.1 302 Found\r\n");
                        writer.write("Location: http://localhost:9090/Home"); // Correct URL
                        // writer.write("Content-Length: 0\r\n");
                        writer.write("\r\n");
                    } else {
                        writer.write("HTTP/1.1 401 Unauthorized\r\n");
                        // writer.write("Content-Type: text/plain\r\n");
                        // writer.write("Content-Length: 23\r\n");
                        // writer.write("\r\n");
                        writer.write("Invalid username or password.");
                    }
                    writer.flush();
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM login WHERE username = ? AND pass = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Authentication successful if a row is found

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
