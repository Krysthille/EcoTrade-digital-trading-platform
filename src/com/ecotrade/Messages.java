package com.ecotrade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Messages {
    private static final Statement DBUtils = null;
    private String traderUsername;
    private static int currentUserId;

    private static void showNotification(String text) {
        if (!SystemTray.isSupported()) return;
        SystemTray tray = SystemTray.getSystemTray();
        Image icon = Toolkit.getDefaultToolkit().getImage("src/img/icon.png");
        TrayIcon trayIcon = new TrayIcon(icon, "EcoTrade");
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            return;
        }
        trayIcon.displayMessage("EcoTrade", text, TrayIcon.MessageType.INFO);
        // Remove icon after 5 seconds
        new Timer(5000, e -> tray.remove(trayIcon)).start();
    }
    
    public Messages(int userId, String traderUsername) {
        this.currentUserId = userId;
        this.traderUsername = traderUsername;
    }

    public JPanel getPanel() {
        int selectedUserId = getMostRecentConversation(currentUserId);
        if (selectedUserId == -1) {
            ArrayList<User> contacts = getContacts(currentUserId);
            if (!contacts.isEmpty()) {
                selectedUserId = contacts.get(0).userId;
            }
        }
        return getPanel(currentUserId, selectedUserId, false, null);
    }

    public static JPanel getPanel(int userId, int selectedUserId) {
        return getPanel(userId, selectedUserId, false, null);
    }

    public static JPanel getPanel(int userId, int selectedUserId, boolean showBackButton, Runnable onBack) {
        currentUserId = userId;

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Header with title and optional back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("EcoTrade - Messages");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        if (showBackButton && onBack != null) {
            JButton backButton = new JButton(" \uD83D\uDD19 Back");
            backButton.setMargin(new Insets(2, 6, 2, 6));
            backButton.setFont(new Font("Arial", Font.PLAIN, 12));
            backButton.setFocusPainted(false);
            backButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            backButton.addActionListener(e -> onBack.run());
        
            JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightHeader.setOpaque(false);
            rightHeader.add(backButton);
            headerPanel.add(rightHeader, BorderLayout.EAST);
        }
        

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Contacts list
        JPanel contactListPanel = new JPanel();
        contactListPanel.setLayout(new BoxLayout(contactListPanel, BoxLayout.Y_AXIS));
        JScrollPane contactScrollPane = new JScrollPane(contactListPanel);
        contactScrollPane.setPreferredSize(new Dimension(200, 0));

        ArrayList<User> contacts = getContacts(userId);
        for (User user : contacts) {
            JButton contactButton = new JButton(user.username);
            contactButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            contactButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            contactButton.setFocusPainted(false);

            int finalSelectedUserId = user.userId;
            contactButton.addActionListener(e -> {
                JPanel chatPanel = createChatPanel(userId, finalSelectedUserId, user.username);
                mainPanel.remove(2);
                mainPanel.add(chatPanel, BorderLayout.CENTER);
                mainPanel.revalidate();
                mainPanel.repaint();
            });

            contactListPanel.add(contactButton);
        }

        mainPanel.add(contactScrollPane, BorderLayout.WEST);

        if (selectedUserId != -1) {
            String selectedUsername = getUsernameById(selectedUserId);
            JPanel chatPanel = createChatPanel(userId, selectedUserId, selectedUsername);
            mainPanel.add(chatPanel, BorderLayout.CENTER);
        } else {
            mainPanel.add(new JPanel(), BorderLayout.CENTER);
        }

        return mainPanel;
    }

    private static JPanel createChatPanel(int senderId, int receiverId, String otherUsername) {
        JPanel chatPanel = new JPanel(new BorderLayout());

        JLabel header = new JLabel("Chat with " + otherUsername);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatPanel.add(header, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        loadMessages(senderId, receiverId, messageArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                saveMessage(senderId, receiverId, message);
                messageArea.append("[" + new Timestamp(System.currentTimeMillis()) + "] You: " + message + "\n");
                inputField.setText("");
                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        return chatPanel;
    }

    private static void loadMessages(int senderId, int receiverId, JTextArea messageArea) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String sql = """
                SELECT sender_id, message, created_at FROM messages
                WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)
                ORDER BY created_at
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int sender = rs.getInt("sender_id");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("created_at");

                String senderName = sender == senderId ? "You" : getUsernameById(sender);
                messageArea.append("[" + timestamp + "] " + senderName + ": " + message + "\n");
            }

            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private static void saveMessage(int senderId, int receiverId, String message) {
    //     try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
    //         String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
    //         PreparedStatement stmt = conn.prepareStatement(sql);
    //         stmt.setInt(1, senderId);
    //         stmt.setInt(2, receiverId);
    //         stmt.setString(3, message);
    //         stmt.executeUpdate();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    // bag.o ang naa sa ubos
    // private static void saveMessage(int senderId, int receiverId, String message) {
    //     try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
    //         String sql = "INSERT INTO messages (sender_id, recipient_id, message) VALUES (?, ?, ?)";
    //         PreparedStatement stmt = conn.prepareStatement(sql);
    //         stmt.setInt(1, senderId);
    //         stmt.setInt(2, receiverId); // Note: receiverId now maps to recipient_id
    //         stmt.setString(3, message);
    //         stmt.executeUpdate();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }


    private static void saveMessage(int senderId, int receiverId, String message) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/ecotrade", 
                "root", 
                "ViewAl3x@M3nd0z@")) 
        {
            String sql = "INSERT INTO messages (sender_id, receiver_id, recipient_id, message) VALUES (?, ?, ?, ?)";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setInt(1, senderId);      // sender_id
stmt.setInt(2, receiverId);    // receiver_id
stmt.setInt(3, receiverId);    // recipient_id (likely same as receiverId)
stmt.setString(4, message);    // message
stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private static String getUsernameById(int userId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String sql = "SELECT username FROM login WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static ArrayList<User> getContacts(int userId) {
        ArrayList<User> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String sql = """
                SELECT DISTINCT l.id, l.username
                FROM login l
                WHERE l.id != ? AND (
                    l.id IN (SELECT receiver_id FROM messages WHERE sender_id = ?) OR
                    l.id IN (SELECT sender_id FROM messages WHERE receiver_id = ?)
                )
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                list.add(new User(id, username));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private static int getMostRecentConversation(int userId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String sql = """
                SELECT 
                    CASE 
                        WHEN sender_id = ? THEN receiver_id 
                        ELSE sender_id 
                    END AS user_id,
                    MAX(created_at) AS last_time
                FROM messages
                WHERE sender_id = ? OR receiver_id = ?
                GROUP BY user_id
                ORDER BY last_time DESC
                LIMIT 1
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static class User {
        int userId;
        String username;
        public User(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    public static JPanel getChatPanelWithUser(int senderId, int receiverId) {
        return getPanel(senderId, receiverId);
    }

    public void sendMessage(String initialMessage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }
}
