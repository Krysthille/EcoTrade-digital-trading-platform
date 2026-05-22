package com.ecotrade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Community {

    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Color PRIMARY_COLOR = new Color(76, 175, 80); // Green
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    public static JPanel getPanel() {
        ImageIcon bgIcon = new ImageIcon("src/img/bg.jpg"); // Use your actual path
        Image bgImage = bgIcon.getImage();
        JPanel mainPanel = new BackgroundPanel(bgImage);

        // ===== POST FORM =====
        // ===== NEW: POST BUTTON =====
        JButton openFormButton = new JButton("➕ Add New Post");
        openFormButton.setBackground(PRIMARY_COLOR);
        openFormButton.setForeground(Color.WHITE);
        openFormButton.setFont(LABEL_FONT);
        openFormButton.setFocusPainted(false);
        openFormButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(openFormButton);
        mainPanel.add(Box.createVerticalStrut(20));

        openFormButton.addActionListener(e -> {
            showPostDialog(mainPanel);
        });

        int currentUserId = Land.currentUserId;

        // ===== LOAD POSTS =====
        String postQuery = "SELECT community.*, login.username FROM community " +
                "JOIN login ON community.user_id = login.id " +
                "ORDER BY community.created_at DESC";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@");
                PreparedStatement stmt = conn.prepareStatement(postQuery);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int postId = rs.getInt("id");

                JPanel postPanel = new JPanel();
                postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
                postPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                postPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
                postPanel.setBackground(new Color(250, 250, 250));

                postPanel.setMaximumSize(new Dimension(500, 700));

                String username = rs.getString("username");
                Timestamp createdAt = rs.getTimestamp("created_at");
                String formattedDateTime = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a").format(createdAt);

                JLabel userLabel = new JLabel(
                        "<html><b>" + username + "</b> | Posted on " + formattedDateTime + "</html>");
                JLabel nameLabel = new JLabel("Product: " + rs.getString("product_name"));
                JLabel descLabel = new JLabel(
                        "<html><p style='width:400px;'>" + rs.getString("description") + "</p></html>");

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

                // ========================= MESSAGE BUTTON
                int postOwnerUserId = rs.getInt("user_id");
                JButton messagesBtn = new JButton("Message Trader");
                messagesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                messagesBtn.setMaximumSize(new Dimension(150, 25));

                messagesBtn.addActionListener(evt -> {
                    // Get the message panel (need to pass in the current and post owner user IDs)
                    JPanel messagePanel = Messages.getPanel(Land.currentUserId, postOwnerUserId);

                    // Assuming the mainPanel is the panel for all the content in the JFrame
                    // Replace or refresh the main content with the message panel
                    mainPanel.removeAll(); // Clear the current content
                    mainPanel.add(messagePanel); // Add the message panel
                    mainPanel.revalidate(); // Revalidate the layout
                    mainPanel.repaint(); // Repaint the panel to reflect the changes
                });

                postPanel.add(messagesBtn);
                postPanel.add(Box.createVerticalStrut(10));

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
                        try (Connection commentConn = DriverManager
                                .getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
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

    static class BackgroundPanel extends JPanel {
        private final Image backgroundImage;

        public BackgroundPanel(Image image) {
            this.backgroundImage = image;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void loadCommentsRecursively(JPanel parentPanel, Connection conn, int postId, int parentCommentId,
            int currentUserId, int indentLevel) {
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
                commentPanel.setBackground(new Color(255, 255, 255));
                commentPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(5, indentLevel * 20 + 10, 5, 10)));

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
                                Connection newConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade",
                                        "root", "ViewAl3x@M3nd0z@");
                                PreparedStatement replyStmt = newConn.prepareStatement(
                                        "INSERT INTO comments (post_id, user_id, comment, parent_comment_id) VALUES (?, ?, ?, ?)")) {
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
        panel.setBackground(BACKGROUND_COLOR);
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        component.setFont(FIELD_FONT);
        panel.add(label, BorderLayout.NORTH); // Better label positioning
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private static void showPostDialog(JPanel parent) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parent), "Add New Post", true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(parent);
    
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(BACKGROUND_COLOR);
    
        JTextField productField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
    
        // === CATEGORY RADIO BUTTONS ===
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JRadioButton catProducts = new JRadioButton("Products");
        JRadioButton catTools = new JRadioButton("Tools & Appliances");
        JRadioButton catServices = new JRadioButton("Services");
        ButtonGroup categoryGroup = new ButtonGroup();
        categoryGroup.add(catProducts);
        categoryGroup.add(catTools);
        categoryGroup.add(catServices);
        catProducts.setSelected(true); // Default selection
    
        categoryPanel.add(catProducts);
        categoryPanel.add(catTools);
        categoryPanel.add(catServices);
    
        JButton chooseImgBtn = new JButton("Choose Images");
        JLabel selectedFileLabel = new JLabel("No files chosen");
        JPanel imagePreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imagePreviewPanel.setOpaque(false);
    
        // Create a panel for the submit button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center alignment
        JButton submitBtn = new JButton("Upload Post");
        submitBtn.setBackground(PRIMARY_COLOR);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(FIELD_FONT);
        buttonPanel.add(submitBtn); // Add the button to the button panel
    
        final java.util.List<File> selectedFiles = new ArrayList<>();
    
        chooseImgBtn.addActionListener(evt -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedFiles.clear();
                imagePreviewPanel.removeAll();
                StringBuilder fileNames = new StringBuilder();
                for (File f : fileChooser.getSelectedFiles()) {
                    selectedFiles.add(f);
                    fileNames.append(f.getName()).append(", ");
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(80, 60, Image.SCALE_SMOOTH);
                    imagePreviewPanel.add(new JLabel(new ImageIcon(img)));
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
        formPanel.add(createLabeledComponent("Category:", categoryPanel)); // Add category panel
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createLabeledComponent("Images:", chooseImgBtn));
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(selectedFileLabel);
        formPanel.add(imagePreviewPanel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(buttonPanel); // Add the button panel
    
        submitBtn.addActionListener(e -> {
            String product = productField.getText().trim();
            String description = descArea.getText().trim();
            String category = catProducts.isSelected() ? "Products"
                            : catTools.isSelected() ? "Tools & Appliances"
                            : "Services"; // Get selected category
            int currentUserId = Land.currentUserId;
        
            if (product.isEmpty() || description.isEmpty() || selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            try {
                File destDir = new File("src/img/posts");
                if (!destDir.exists())
                    destDir.mkdirs();
                StringBuilder imagePaths = new StringBuilder();
        
                for (File f : selectedFiles) {
                    File dest = new File(destDir, f.getName());
                    Files.copy(f.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    imagePaths.append(f.getName()).append(",");
                }
        
                if (imagePaths.length() > 0)
                    imagePaths.setLength(imagePaths.length() - 1);
        
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                        "ViewAl3x@M3nd0z@")) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO community (user_id, product_name, description, image_path, category) VALUES (?, ?, ?, ?, ?)");
                    stmt.setInt(1, currentUserId);
                    stmt.setString(2, product);
                    stmt.setString(3, description);
                    stmt.setString(4, imagePaths.toString());
                    stmt.setString(5, category); // Include the category in the insert
                    stmt.executeUpdate();
                }
        
                JOptionPane.showMessageDialog(dialog, "Post uploaded!");
                dialog.dispose();
        
                // Refresh the main panel
                JPanel refreshed = getPanel();
                parent.removeAll();
                for (Component c : refreshed.getComponents()) {
                    parent.add(c);
                }
                parent.revalidate();
                parent.repaint();
        
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error uploading post:\n" + ex.getMessage());
            }
        });

        dialog.setContentPane(formPanel);
        dialog.setVisible(true);
    }


}
