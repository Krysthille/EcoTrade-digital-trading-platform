package com.ecotrade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Community {

    public static JPanel getPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // ===== POST FORM =====
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Post"));
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(500, 500));

        JTextField productField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        JButton chooseImgBtn = new JButton("Choose Images");
        JLabel selectedFileLabel = new JLabel("No files chosen");
        JPanel imagePreviewPanel = new JPanel();
        imagePreviewPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        imagePreviewPanel.setBackground(Color.WHITE);
        JButton submitBtn = new JButton("Upload Post");

        final java.util.List<File> selectedFiles = new ArrayList<>();

        chooseImgBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                selectedFiles.clear();
                imagePreviewPanel.removeAll();
                StringBuilder fileNames = new StringBuilder();

                for (File f : files) {
                    selectedFiles.add(f);
                    fileNames.append(f.getName()).append(", ");

                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(100, 70, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(img));
                    imagePreviewPanel.add(imgLabel);
                }

                selectedFileLabel.setText(fileNames.length() > 0 ? fileNames.toString() : "No files chosen");
                imagePreviewPanel.revalidate();
                imagePreviewPanel.repaint();
            }
        });

        formPanel.add(createLabeledComponent("Product Name:", productField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createLabeledComponent("Description:", descScroll));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createLabeledComponent("Images:", chooseImgBtn));
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(selectedFileLabel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(imagePreviewPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(submitBtn);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        int currentUserId = Land.currentUserId;

        // int currentUserId = 0;
        // ===== SUBMIT POST =====
        submitBtn.addActionListener((ActionEvent e) -> {
            String product = productField.getText().trim();
            String description = descArea.getText().trim();

            if (product.isEmpty() || description.isEmpty() || selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "All fields are required and at least one image must be selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                File destDir = new File("src/img/posts");
                if (!destDir.exists()) destDir.mkdirs();

                StringBuilder imageFileNames = new StringBuilder();
                for (File file : selectedFiles) {
                    String imageFileName = file.getName();
                    File destFile = new File(destDir, imageFileName);
                    if (!file.getAbsolutePath().equals(destFile.getAbsolutePath())) {
                        Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    imageFileNames.append(imageFileName).append(",");
                }

                if (imageFileNames.length() > 0) {
                    imageFileNames.setLength(imageFileNames.length() - 1); // Remove trailing comma
                }

                try (Connection conn = DriverManager.getConnection
                ("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
                    String insertQuery = "INSERT INTO community (user_id, product_name, description, image_path) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(insertQuery);
                    stmt.setInt(1, currentUserId);
                    stmt.setString(2, product);
                    stmt.setString(3, description);
                    stmt.setString(4, imageFileNames.toString());
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(mainPanel, "Post added successfully!");

                    JPanel refreshed = getPanel();
                    mainPanel.removeAll();
                    for (Component c : refreshed.getComponents()) {
                        mainPanel.add(c);
                    }
                    mainPanel.revalidate();
                    mainPanel.repaint();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mainPanel, "Failed to save post or images.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ===== LOAD POSTS =====
        try (Connection conn = DriverManager.getConnection
        ("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String postQuery = "SELECT community.*, login.username FROM community " +
                    "JOIN login ON community.user_id = login.id " +
                    "ORDER BY community.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(postQuery);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int postId = rs.getInt("id");
                int userIdOfPostOwner = rs.getInt("user_id"); // Retrieve the user ID of the post owner
            
                JPanel postPanel = new JPanel();
                postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
                postPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                postPanel.setBackground(new Color(248, 248, 248));
                postPanel.setMaximumSize(new Dimension(500, 700));
            
                String username = rs.getString("username");
                Timestamp createdAt = rs.getTimestamp("created_at");
                String formattedDateTime = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(createdAt);
            
                JLabel userLabel = new JLabel("<html><b>" + username + "</b> | Posted on " + formattedDateTime + "</html>");
                JLabel nameLabel = new JLabel("Product: " + rs.getString("product_name"));
                JLabel descLabel = new JLabel("<html><p style='width:400px;'>" + rs.getString("description") + "</p></html>");
            
                JPanel imageContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
                imageContainer.setBackground(new Color(248, 248, 248));
                String[] imagePaths = rs.getString("image_path").split(",");
            
                for (String image : imagePaths) {
                    File imgFile = new File("src/img/posts/" + image.trim());
                    if (imgFile.exists()) {
                        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                        JLabel imgLabel = new JLabel(new ImageIcon(img));
                        imageContainer.add(imgLabel);
                    }
                }
            
                postPanel.add(userLabel);
                postPanel.add(nameLabel);
                postPanel.add(descLabel);
                postPanel.add(Box.createVerticalStrut(5));
                postPanel.add(imageContainer);
                postPanel.add(Box.createVerticalStrut(10));
            
                // MESSAGES BUTTON
                JButton messagesBtn = new JButton("Message Trader");
                messagesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                messagesBtn.setMaximumSize(new Dimension(150, 25));
                messagesBtn.addActionListener(evt -> {
                    JPanel messagePanel = Messages.getPanel(Land.currentUserId, userIdOfPostOwner); // Corrected syntax
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
                    topFrame.setContentPane(messagePanel);
                    topFrame.revalidate();
                    topFrame.repaint();
                });
                postPanel.add(messagesBtn);
                postPanel.add(Box.createVerticalStrut(10));
            
                // MESSAGES BUTTON
                // JButton messagesBtn = new JButton("Message Trader");
                // messagesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                // messagesBtn.setMaximumSize(new Dimension(150, 25));
                // messagesBtn.addActionListener(evt -> {
                //     JPanel messagePanel = Messages.getPanel(currentUserId, postId);
                //     JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
                //     topFrame.setContentPane(messagePanel);
                //     topFrame.revalidate();
                //     topFrame.repaint();
                // });
                // postPanel.add(messagesBtn);
                // postPanel.add(Box.createVerticalStrut(10));


                


                // COMMENTS SECTION
                postPanel.add(new JLabel("Comments:"));
                JPanel commentSection = new JPanel();
                commentSection.setLayout(new BoxLayout(commentSection, BoxLayout.Y_AXIS));
                commentSection.setBackground(new Color(240, 240, 240));
                loadCommentsRecursively(commentSection, conn, postId, 0, currentUserId, 0);

                JPanel commentInputPanel = new JPanel();
                commentInputPanel.setLayout(new BoxLayout(commentInputPanel, BoxLayout.X_AXIS));
                commentInputPanel.setBackground(new Color(240, 240, 240));
                JTextField newCommentField = new JTextField();
                JButton postCommentBtn = new JButton("Add Comment");
                commentInputPanel.add(newCommentField);
                commentInputPanel.add(Box.createHorizontalStrut(5));
                commentInputPanel.add(postCommentBtn);

                postCommentBtn.addActionListener(ev -> {
                    String newComment = newCommentField.getText().trim();
                    if (!newComment.isEmpty()) {
                        try (Connection commentConn = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
                            String insertComment = "INSERT INTO comments (post_id, user_id, comment, parent_comment_id) VALUES (?, ?, ?, NULL)";
                            PreparedStatement commentStmt = commentConn.prepareStatement(insertComment);
                            commentStmt.setInt(1, postId);
                            commentStmt.setInt(2, currentUserId);
                            commentStmt.setString(3, newComment);
                            commentStmt.executeUpdate();

                            newCommentField.setText("");
                            commentSection.removeAll();
                            loadCommentsRecursively(commentSection, commentConn, postId, 0, currentUserId, 0);
                            commentSection.revalidate();
                            commentSection.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                postPanel.add(commentSection);
                postPanel.add(Box.createVerticalStrut(10));
                postPanel.add(commentInputPanel);
                mainPanel.add(postPanel);
                mainPanel.add(Box.createVerticalStrut(20));
            }

        } catch (Exception e) {
            e.printStackTrace();
            mainPanel.add(new JLabel("Failed to load posts."));
        }

        return mainPanel;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
   private static void loadCommentsRecursively(JPanel parentPanel, Connection conn, int postId, int parentCommentId, int currentUserId, int indentLevel) {
    try {
        String query = "SELECT c.*, l.username FROM comments c " +
                       "JOIN login l ON c.user_id = l.id " +
                       "WHERE c.post_id = ? AND " +
                       (parentCommentId == 0 ? "c.parent_comment_id IS NULL" : "c.parent_comment_id = ?") +
                       " ORDER BY c.created_at ASC";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, postId);
        if (parentCommentId != 0) {
            stmt.setInt(2, parentCommentId);
        }

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int commentId = rs.getInt("id");
            String username = rs.getString("username");
            String comment = rs.getString("comment");
            Timestamp createdAt = rs.getTimestamp("created_at");

            String formattedDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm a").format(createdAt);

            // === Comment Panel ===
            JPanel commentPanel = new JPanel();
            commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
            commentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            commentPanel.setBackground(new Color(245, 245, 245));
            commentPanel.setBorder(BorderFactory.createEmptyBorder(5, indentLevel * 20, 5, 5));

            JLabel userLabel = new JLabel("<html><b>" + username + "</b> <i>(" + formattedDate + ")</i></html>");
            JLabel commentLabel = new JLabel("<html><p style='width:400px;'>" + comment + "</p></html>");

            commentPanel.add(userLabel);
            commentPanel.add(commentLabel);

            // === Reply Panel (Always Visible) ===
            JPanel replyPanel = new JPanel();
            replyPanel.setLayout(new BoxLayout(replyPanel, BoxLayout.X_AXIS));
            replyPanel.setBackground(new Color(245, 245, 245));
            replyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            replyPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JTextField replyField = new JTextField();
            JButton replyBtn = new JButton("Reply");

            replyPanel.add(replyField);
            replyPanel.add(Box.createHorizontalStrut(5));
            replyPanel.add(replyBtn);

            // Reply Action
            replyBtn.addActionListener(e -> {
                String replyText = replyField.getText().trim();
                if (Land.currentUserId <= 0) {
                    JOptionPane.showMessageDialog(null, "You must be logged in to reply.");
                    return;
                }
            
                if (!replyText.isEmpty()) {
                    try (
                        Connection newConn = DriverManager.getConnection
                        ("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@");
                        PreparedStatement replyStmt = newConn.prepareStatement
                        ("INSERT INTO comments (post_id, user_id, comment, parent_comment_id) VALUES (?, ?, ?, ?)")
                    ) {
                        replyStmt.setInt(1, postId);
                        replyStmt.setInt(2, Land.currentUserId);
                        replyStmt.setString(3, replyText);
                        replyStmt.setInt(4, commentId);
                        replyStmt.executeUpdate();
            
                        replyField.setText("");
                        parentPanel.removeAll();
                        loadCommentsRecursively(parentPanel, newConn, postId, 0, Land.currentUserId, 0);
                        parentPanel.revalidate();
                        parentPanel.repaint();
            
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to post reply. Please try again.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Reply cannot be empty.");
                }
            });
            

            commentPanel.add(replyPanel);
            parentPanel.add(commentPanel);

            // === Load Nested Replies ===
            loadCommentsRecursively(parentPanel, conn, postId, commentId, currentUserId, indentLevel + 1);
        }

        } catch (Exception e) {
        e.printStackTrace();
        parentPanel.add(new JLabel("Failed to load comments."));
    }
}


    private static JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        panel.add(label, BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    public Container getMainPanel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMainPanel'");
    }
}
