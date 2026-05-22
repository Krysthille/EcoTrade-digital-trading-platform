package com.ecotrade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Shop {

    static class ImagePanel extends JPanel {
        private Image backgroundImage;

        public ImagePanel(String imagePath) {
            backgroundImage = new ImageIcon(imagePath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static JPanel getShopPanel() {
        ImagePanel mainPanel = new ImagePanel("src/img/bg.jpg"); // Background image
        mainPanel.setLayout(new BorderLayout());

        // Top Panel (Header + Search Bar)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false); // Make the top panel transparent

        JLabel header = new JLabel("EcoTrade - Shop", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        header.setForeground(new Color(0, 100, 0)); // Change text color to green

        // Add horizontal struts to center the header
        topPanel.add(Box.createHorizontalGlue()); // Add glue before the header
        topPanel.add(header);
        topPanel.add(Box.createHorizontalGlue()); // Add glue after the header

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JTextField searchField = new JTextField(20);
        searchField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // Add border to search field
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        topPanel.add(searchPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Grid Panel inside ScrollPane
        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        gridPanel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white background for the grid
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Load Posts
        ArrayList<Post> allPosts = fetchPostsFromDatabase();
        displayPosts(allPosts, gridPanel);

        // Live Search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterPosts(searchField.getText(), allPosts, gridPanel);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterPosts(searchField.getText(), allPosts, gridPanel);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterPosts(searchField.getText(), allPosts, gridPanel);
            }
        });

        JButton myShopButton = new JButton("My Shop");
        myShopButton.addActionListener(e -> {
            ArrayList<Post> myPosts = new ArrayList<>();
            for (Post post : allPosts) {
                if (post.userId == Land.currentUserId) {
                    myPosts.add(post);
                }
            }

            gridPanel.removeAll();
            displayPosts(myPosts, gridPanel);
            gridPanel.revalidate();
            gridPanel.repaint();
        });
        searchPanel.add(myShopButton); // Add it beside the search box

        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> {
            gridPanel.removeAll();
            displayPosts(allPosts, gridPanel);
            gridPanel.revalidate();
            gridPanel.repaint();
        });
        searchPanel.add(showAllButton); // 👈 Add beside My Shop

        JButton myOrdersButton = new JButton("My Orders");
        myOrdersButton.addActionListener(e -> {
            ArrayList<Post> myOrders = new ArrayList<>();
            for (Post post : allPosts) {
                // Assuming you have a way to determine if a post is an order
                if (post.userId != Land.currentUserId) { // Change this condition based on your logic
                    myOrders.add(post);
                }
            }

            gridPanel.removeAll();
            displayPosts(myOrders, gridPanel);
            gridPanel.revalidate();
            gridPanel.repaint();
        });
        searchPanel.add(myOrdersButton); // Add it beside the Show All button

        return mainPanel;
    }

    private static void filterPosts(String query, ArrayList<Post> allPosts, JPanel gridPanel) {
        String lower = query.toLowerCase().trim();
        ArrayList<Post> filtered = new ArrayList<>();
        for (Post post : allPosts) {
            if (post.title.toLowerCase().contains(lower) || post.username.toLowerCase().contains(lower)) {
                filtered.add(post);
            }
        }
        gridPanel.removeAll();
        displayPosts(filtered, gridPanel);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private static void displayPosts(ArrayList<Post> posts, JPanel gridPanel) {
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS)); // vertical layout

        // Group posts by category
        Map<String, ArrayList<Post>> groupedByCategory = new LinkedHashMap<>();
        for (Post post : posts) {
            groupedByCategory
                    .computeIfAbsent(post.category != null ? post.category : "Uncategorized", k -> new ArrayList<>())
                    .add(post);
        }

        // For each category, add a title and a grid of cards
        for (Map.Entry<String, ArrayList<Post>> entry : groupedByCategory.entrySet()) {
            String category = entry.getKey();
            ArrayList<Post> categoryPosts = entry.getValue();

            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setFont(new Font("Arial", Font.BOLD, 18));
            categoryLabel.setBorder(new EmptyBorder(10, 5, 5, 5));
            gridPanel.add(categoryLabel);

            JPanel categoryPanel = new JPanel(new GridLayout(0, 3, 15, 15));
            categoryPanel.setOpaque(false);
            for (Post post : categoryPosts) {
                categoryPanel.add(createPostCard(post));
            }

            gridPanel.add(categoryPanel);
        }
    }

    private static ArrayList<Post> fetchPostsFromDatabase() {
        ArrayList<Post> posts = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = """
                        SELECT c.id, c.user_id, c.product_name, c.description, c.image_path, c.created_at, c.open_for_trading, l.username, c.category
                        FROM community c
                        JOIN login l ON c.user_id = l.id
                        ORDER BY c.created_at DESC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post(
                        rs.getInt("id"), // Post ID
                        rs.getString("product_name"), // Product name
                        rs.getString("description"), // Description
                        rs.getString("image_path"), // Image path
                        rs.getString("username"), // Trader's username
                        rs.getTimestamp("created_at"), // Timestamp of creation
                        rs.getBoolean("open_for_trading"), // Trading status
                        rs.getString("category"), // Category
                        rs.getInt("user_id") // User ID of the post owner
                );
                posts.add(post); // Add the post to the list
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print any SQL exceptions
        }
        return posts; // Return the list of posts
    }

    static class Post {
        int id;
        String title;
        String description;
        String imagePath;
        String username;
        Timestamp createdAt;
        boolean openForTrading;
        String category;
        int userId; // Add userId field

        public Post(int id, String title, String description, String imagePath, String username, Timestamp createdAt,
                boolean openForTrading, String category, int userId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.imagePath = imagePath;
            this.username = username;
            this.createdAt = createdAt;
            this.openForTrading = openForTrading;
            this.category = category;
            this.userId = userId; // Initialize userId
        }
    }

    private static JPanel createPostCard(Post post) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(250, 350));
        card.setMaximumSize(new Dimension(250, 350));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Load image
        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon icon = getImageIcon(post.imagePath);
        if (icon != null) {
            imageLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(230, 130, Image.SCALE_SMOOTH)));
        } else {
            imageLabel.setText("No Image");
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
        }

        // Title label
        JLabel titleLabel = new JLabel(post.title != null ? post.title : "Untitled");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Trader label
        JLabel traderLabel = new JLabel("By: " + post.username);
        traderLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        traderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Category label
        JLabel categoryLabel = new JLabel("Category: " + (post.category != null ? post.category : "N/A"));
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to the card
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(traderLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(categoryLabel);
        card.add(Box.createVerticalStrut(10));

        // Check if the current user is the owner of the post
        int currentUserId = Land.currentUserId; // Assuming you have a way to get the current user ID
        if (post.userId == currentUserId) {
            JButton toggleButton = new JButton(post.openForTrading ? "Mark as Sold" : "Mark as Unsold");
            toggleButton.setForeground(post.openForTrading ? Color.BLUE : Color.GREEN.darker());

            toggleButton.addActionListener(e -> {
                if (post.openForTrading) {
                    // Mark as sold
                    int confirm = JOptionPane.showConfirmDialog(card,
                            "Are you sure you want to mark this item as sold?",
                            "Confirm Sold", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = markAsSold(post.id);
                        if (success) {
                            post.openForTrading = false; // Update the post model
                            toggleButton.setText("Mark as Unsold");
                            toggleButton.setForeground(Color.GREEN.darker());
                            JOptionPane.showMessageDialog(card, "Item marked as sold.");
                        }
                    }
                } else {
                    // Mark as unsold
                    int confirm = JOptionPane.showConfirmDialog(card,
                            "Are you sure you want to mark this item as unsold?",
                            "Confirm Unsold", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = markAsUnsold(post.id);
                        if (success) {
                            post.openForTrading = true; // Update the post model
                            toggleButton.setText("Mark as Sold");
                            toggleButton.setForeground(Color.BLUE);
                            JOptionPane.showMessageDialog(card, "Item marked as unsold and available for trading.");
                        }
                    }
                }
            });

            card.add(toggleButton); // Add the toggle button to the card
        }

        // Adding an event listener for clicking on the card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showProductDetails(post);
            }
        });

        return card;
    }

    private static boolean markAsSold(int postId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "UPDATE community SET open_for_trading = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, false); // Mark as sold (not open for trading)
                stmt.setInt(2, postId);
                int updatedRows = stmt.executeUpdate();
                if (updatedRows == 0) {
                    JOptionPane.showMessageDialog(null, "Failed to mark item as sold. Item not found.");
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error marking item as sold: " + e.getMessage());
            return false;
        }
    }

    private static boolean markAsUnsold(int postId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "UPDATE community SET open_for_trading = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, true); // Mark as unsold (open for trading)
                stmt.setInt(2, postId);
                int updatedRows = stmt.executeUpdate();
                if (updatedRows == 0) {
                    JOptionPane.showMessageDialog(null, "Failed to mark item as unsold. Item not found.");
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error marking item as unsold: " + e.getMessage());
            return false;
        }
    }

    private static void addUnsoldButton(JPanel buttonPanel, Post post) {
        JButton unsoldButton = new JButton("Mark as Unsold");
        unsoldButton.setForeground(Color.GREEN.darker());

        // Add action listener to handle unsold marking
        unsoldButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(buttonPanel,
                    "Are you sure you want to mark this item as unsold?",
                    "Confirm Unsold", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = markAsUnsold(post.id);
                if (success) {
                    // Remove this unsold button from the button panel
                    buttonPanel.remove(unsoldButton);

                    // Find and update the soldButton on the button panel
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            JButton btn = (JButton) comp;
                            if ("Sold Already".equals(btn.getText())) {
                                btn.setText("Mark as Sold");
                                btn.setEnabled(true);
                                btn.setForeground(Color.BLUE);
                            }
                        }
                    }

                    post.openForTrading = true; // Update post status

                    buttonPanel.revalidate();
                    buttonPanel.repaint();

                    JOptionPane.showMessageDialog(buttonPanel, "Item marked as unsold and available for trading.");
                }
            }
        });

        buttonPanel.add(unsoldButton); // Add the unsold button to the button panel
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private static void deletePost(int postId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            // First, delete all comments associated with the post
            String deleteCommentsSql = "DELETE FROM comments WHERE post_id = ?";
            try (PreparedStatement deleteCommentsStmt = conn.prepareStatement(deleteCommentsSql)) {
                deleteCommentsStmt.setInt(1, postId);
                deleteCommentsStmt.executeUpdate();
            }

            // Now delete the post
            String deletePostSql = "DELETE FROM community WHERE id = ?";
            try (PreparedStatement deletePostStmt = conn.prepareStatement(deletePostSql)) {
                deletePostStmt.setInt(1, postId);
                deletePostStmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Post deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting post: " + e.getMessage());
        }
    }

    private static void showProductDetails(Post post) {
        JFrame frame = new JFrame("Product Details");
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 500);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel name = new JLabel("Product: " + post.title);
        name.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea description = new JTextArea(post.description);
        description.setEditable(false);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel imageLabel = new JLabel();
        ImageIcon icon = getImageIcon(post.imagePath);
        if (icon != null) {
            imageLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
        }

        JButton tradeButton = new JButton(post.openForTrading ? "Okay for Trading" : "Already Okay for Trading");

        // Show and enable trade button only if current user is NOT the owner
        if (Land.currentUserId == post.userId) {
            tradeButton.setVisible(false);
        } else {
            tradeButton.setEnabled(post.openForTrading);
            tradeButton.addActionListener(e -> {
                updateTradeStatus(post);
                tradeButton.setText("Already Okay for Trading");
                tradeButton.setEnabled(false); // Disable the button after pressing

                // Redirect to the Messages page
                Messages messagesPage = new Messages(Land.currentUserId, post.username); // Pass current user ID and
                                                                                         // trader's username
                JFrame messagesFrame = new JFrame("Messages");
                messagesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                messagesFrame.setSize(600, 400);
                messagesFrame.add(messagesPage.getPanel()); // Call getPanel to display the messaging interface
                messagesFrame.setVisible(true);
            });
        }

        JLabel traderName = new JLabel("Trader: " + post.username);
        traderName.setFont(new Font("Arial", Font.ITALIC, 12));

        panel.add(name);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(description));
        panel.add(Box.createVerticalStrut(10));
        panel.add(tradeButton); // This is hidden if owner
        panel.add(Box.createVerticalStrut(10));
        panel.add(traderName);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void createMessageDialog(String traderUsername) {
        JDialog messageDialog = new JDialog();
        messageDialog.setTitle("Send Message to " + traderUsername);
        messageDialog.setSize(300, 200);
        messageDialog.setLayout(new BorderLayout());

        JTextArea messageArea = new JTextArea();
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String message = messageArea.getText();
            if (!message.isEmpty()) {
                // Here you would implement the logic to send the message
                // For example, save it to the database or send it via email
                JOptionPane.showMessageDialog(messageDialog, "Message sent to " + traderUsername);
                messageDialog.dispose(); // Close the dialog after sending
            } else {
                JOptionPane.showMessageDialog(messageDialog, "Please enter a message before sending.");
            }
        });

        messageDialog.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        messageDialog.add(sendButton, BorderLayout.SOUTH);
        messageDialog.setVisible(true);
    }

    private static void updateTradeStatus(Post post) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "UPDATE community SET open_for_trading = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, true);
            stmt.setInt(2, post.id);
            stmt.executeUpdate();
            post.openForTrading = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ImageIcon getImageIcon(String relativePath) {
        try {
            if (relativePath != null && !relativePath.isEmpty()) {
                // Support multiple comma-separated images
                String[] parts = relativePath.split(",");
                File file = new File("src/img/posts/" + parts[0].trim());
                if (file.exists()) {
                    return new ImageIcon(file.getAbsolutePath());
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}