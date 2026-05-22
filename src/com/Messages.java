
package com.ecotrade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Messages {
    private static List<String> contacts = new ArrayList<>();
    private static int senderId;

    public static JPanel getPanel(int sId, int receiverId) {
        senderId = sId;
        contacts = loadContactsFromDatabase();
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = createContactsPanel();
        JPanel rightPanel = createMessagesPanel("Select a contact");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private static JPanel createContactsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 240, 240));

        JLabel label = new JLabel("Contacts");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(label);

        for (String contact : contacts) {
            JButton btn = new JButton(contact);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.addActionListener(e -> {
                int receiverId = getUserIdByName(contact);
                if (receiverId == -1) {
                    JOptionPane.showMessageDialog(null, "Invalid user selected.");
                    return;
                }

                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
                topFrame.getContentPane().removeAll();

                JPanel newPanel = createConversationView(contact, senderId, receiverId);
                topFrame.getContentPane().add(newPanel);
                topFrame.revalidate();
                topFrame.repaint();
            });
            panel.add(btn);
        }

        return panel;
    }

    private static JPanel createMessagesPanel(String headerText) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel header = new JLabel(headerText);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(header, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendBtn = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel createConversationView(String contact, int senderId, int receiverId) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel header = new JLabel("Chat with " + contact);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(header, BorderLayout.NORTH);

        JTextArea messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadMessages(senderId, receiverId, messageArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendBtn = new JButton("Send");

        sendBtn.addActionListener(e -> {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                boolean success = saveMessage(senderId, receiverId, msg);
                if (success) {
                    inputField.setText("");
                    messageArea.append("You: " + msg + "\n");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to send message.");
                }
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    private static boolean saveMessage(int sender, int receiver, String msg) {
        if (sender <= 0 || receiver <= 0) {
            System.err.println("Invalid sender or receiver ID.");
            return false;
        }

        String dbUrl = "jdbc:mysql://localhost:3306/ecotrade";
        String dbUser = "root";
        String dbPassword = "ViewAl3x@M3nd0z@";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sender);
            stmt.setInt(2, receiver);
            stmt.setString(3, msg);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void loadMessages(int sender, int receiver, JTextArea messageArea) {
        String dbUrl = "jdbc:mysql://localhost:3306/ecotrade";
        String dbUser = "root";
        String dbPassword = "ViewAl3x@M3nd0z@";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "SELECT sender_id, message FROM messages WHERE " +
                         "(sender_id = ? AND receiver_id = ?) OR " +
                         "(sender_id = ? AND receiver_id = ?) ORDER BY timestamp";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sender);
            stmt.setInt(2, receiver);
            stmt.setInt(3, receiver);
            stmt.setInt(4, sender);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int sId = rs.getInt("sender_id");
                String msg = rs.getString("message");

                String senderLabel = (sId == sender) ? "You" : getUsernameById(sId);
                messageArea.append(senderLabel + ": " + msg + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadContactsFromDatabase() {
        List<String> contactList = new ArrayList<>();
        String dbUrl = "jdbc:mysql://localhost:3306/ecotrade";
        String dbUser = "root";
        String dbPassword = "ViewAl3x@M3nd0z@";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "SELECT username FROM login WHERE id != ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contactList.add(rs.getString("username"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactList;
    }

    private static int getUserIdByName(String name) {
        String dbUrl = "jdbc:mysql://localhost:3306/ecotrade";
        String dbUser = "root";
        String dbPassword = "ViewAl3x@M3nd0z@";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "SELECT id FROM login WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static String getUsernameById(int id) {
        String dbUrl = "jdbc:mysql://localhost:3306/ecotrade";
        String dbUser = "root";
        String dbPassword = "ViewAl3x@M3nd0z@";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "SELECT username FROM login WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Unknown";
    }
}