package com.ecotrade;
import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Home {
    private JFrame frame;
    private boolean sidebarVisible = true;
    private JPanel navPanel;


    public Home(String username, JFrame frame) {
        this.frame = frame;
        createHomePage(username);
    }
    public Integer getPostOwnerId(int postId) {
        
    
        try (Connection conn = Database.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM posts WHERE id = ?")) {
        stmt.setInt(1, postId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("user_id");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    
        return postId; // Return Integer, which can be null if not found
    }


    private void createHomePage(String username) {
        frame.getContentPane().removeAll();
        frame.setTitle("EcoTrade - Home");
        frame.setLayout(new BorderLayout());
    
        navPanel = createNavBar(username);
        JPanel contentPanel = createWelcomePanel(username);
    
        JScrollPane scrollPane = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);
    
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    
        // Create a BackgroundPanel and set the background image
        BackgroundPanel backgroundPanel = new BackgroundPanel("src/img/bg.jpg");
        backgroundPanel.setLayout(new BorderLayout());
    
        JPanel mainPanel = new JPanel(new BorderLayout());
        if (sidebarVisible) {
            mainPanel.add(navPanel, BorderLayout.WEST);
        }
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    
        backgroundPanel.add(createHeader(username), BorderLayout.NORTH);
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
    
        frame.add(backgroundPanel, BorderLayout.CENTER);
    
        frame.revalidate();
        frame.repaint();
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
    
        public BackgroundPanel(String imagePath) {
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

    JPanel createHeader(String username) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(46, 125, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton toggleSidebar = new JButton("\u2261"); // Unicode for ≡
        toggleSidebar.setFont(new Font("Arial", Font.BOLD, 18));
        toggleSidebar.setFocusPainted(false);
        toggleSidebar.setForeground(Color.WHITE);
        toggleSidebar.setBackground(new Color(46, 125, 50));
        toggleSidebar.setBorderPainted(false);
        toggleSidebar.setContentAreaFilled(false);
        toggleSidebar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleSidebar.addActionListener(e -> {
            sidebarVisible = !sidebarVisible;
            createHomePage(username);
        });

        JLabel logo = new JLabel(new ImageIcon(
                new ImageIcon("src/img/logo1.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        JLabel title = new JLabel("EcoTrade");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(toggleSidebar);
        left.add(logo);
        left.add(title);

        ImageIcon userIcon = new ImageIcon("src/img/usericon.png");

        JButton userMenuButton = new JButton(userIcon);

        userMenuButton.setText(" Account: " + username);
        userMenuButton.setOpaque(true); // Set opaque to true to see background color
        userMenuButton.setBackground(new Color(34, 139, 34));
        userMenuButton.setContentAreaFilled(false);
        userMenuButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        userMenuButton.setForeground(Color.WHITE);
        userMenuButton.setFont(new Font("Arial", Font.PLAIN, 14));
        userMenuButton.setFocusPainted(false);
        userMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        userMenuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                userMenuButton.setBackground(new Color(100, 150, 100)); // Change to a lighter green on hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userMenuButton.setBackground(new Color(34, 139, 34)); // Revert to original color
            }
        });

        // Create dropdown
        JPopupMenu userDropdown = new JPopupMenu();
        JMenuItem profileItem = new JMenuItem("\uD83D\uDC64 Profile");
        JMenuItem logoutItem = new JMenuItem("\uD83D\uDCBE Log Out");

        // ➤ FIXED: Use new Profile instead of undefined getPanel()
        profileItem.addActionListener(e -> {
            new Profile(username, frame); // ✅ Correct way based on your Profile class
        });


        // ➤ Log out and go back to Land page
        logoutItem.addActionListener(e -> {
            if (confirmLogout()) {
                frame.dispose();
                new Land(frame);
            }
        });

        userDropdown.add(profileItem);
        userDropdown.addSeparator();
        userDropdown.add(logoutItem);

        userMenuButton.addActionListener(e -> userDropdown.show(userMenuButton, 0, userMenuButton.getHeight()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(userMenuButton);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }


    private JPanel createNavBar(String username) {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(180, 0));
        nav.setBackground(new Color(58, 162, 64));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        String[] navItems = { "Home", "Shop", "Community", "Message" };

        for (String name : navItems) {
            JButton btn = new JButton(name);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setBackground(new Color(58, 162, 64));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setForeground(Color.YELLOW);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setForeground(Color.WHITE);
                }
            });

            btn.addActionListener(e -> {
                int postId = 1; // Replace with the actual post ID you want to retrieve
                Integer ownerId = getPostOwnerId(postId);
                if (ownerId == null) {
                    System.err.println("Error: ownerId is null for postId: " + postId);
                    return;
                }
                int id = ownerId.intValue();
    // Use postOwnerId as needed
                if (name.equals("Home")) {
                    createHomePage(username);
                } else {
                    frame.getContentPane().removeAll();
                    frame.setTitle("EcoTrade - " + name);

                    JPanel contentPanel;
                    switch (name) {
                        case "Shop" -> contentPanel = Shop.getShopPanel();
                        // case "Community" -> contentPanel = Community.getPanel(0);
                        // In Home.java or Profile.java
                        case "Community" -> contentPanel = Community.getPanel(); // No arguments

                        // In Home.java or Profile.java
                        case "Message" -> {
                            int currentUserId = Land.currentUserId; // Assuming this is how you get the current user's ID
                            
                            contentPanel = Messages.getPanel(currentUserId, postId);
                        }
                        case "Contact" -> contentPanel = Contacts.getPanel();
                        default -> contentPanel = createPlaceholderPanel(name);
                    }

                    JScrollPane contentScroll = new JScrollPane(contentPanel,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    contentScroll.setBorder(null);
                    contentScroll.getVerticalScrollBar().setUnitIncrement(16);

                    JPanel mainPanel = new JPanel(new BorderLayout());
                    if (sidebarVisible) {
                        mainPanel.add(createNavBar(username), BorderLayout.WEST);
                    }
                    mainPanel.add(contentScroll, BorderLayout.CENTER);

                    frame.add(createHeader(username), BorderLayout.NORTH);
                    frame.add(mainPanel, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();
                }
            });

            nav.add(btn);
            nav.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return nav;
    }

    private int getPostOwnerId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPostOwnerId'");
    }

    private JPanel createWelcomePanel(String username) {
        JPanel panel = new JPanel() {
            private Image backgroundImage;
    
            {
                backgroundImage = new ImageIcon("src/img/bg.jpg").getImage();
            }
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
    
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
    
        panel.add(Box.createVerticalStrut(14));
        panel.add(centeredTextPane("EcoTrade: A Sustainable Trading System", 24, Font.BOLD, new Color(46, 125, 50)));
        panel.add(Box.createVerticalStrut(14));
        panel.add(centeredTextPane("Trade Green, Live Clean.", 18, Font.ITALIC, new Color(34, 100, 34)));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createImagePanel());
    
        panel.add(Box.createVerticalStrut(20));
    
        JPanel descriptionRow = new JPanel();
        descriptionRow.setLayout(new BoxLayout(descriptionRow, BoxLayout.X_AXIS));
        descriptionRow.setOpaque(false);
        descriptionRow.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        JPanel desc1 = new AnimatedDescriptionPanel(createDescriptionBox(getDescription1(), 350));
        JPanel desc2 = new AnimatedDescriptionPanel(createDescriptionBox(getDescription2(), 350));
    
        desc1.setMaximumSize(new Dimension(370, 350));
        desc2.setMaximumSize(new Dimension(370, 350));
    
        descriptionRow.add(desc1);
        descriptionRow.add(Box.createHorizontalStrut(20)); // space between
        descriptionRow.add(desc2);
    
        panel.add(descriptionRow);
        panel.add(Box.createVerticalStrut(60));
    
        return panel;
    }

    class SlidingGradientPanel extends JPanel {
        private Color startColor = new Color(0, 100, 0); // Light color at the top
        private Color endColor = new Color(235, 255, 235); // Dark color at the bottom
        private float gradientPosition = 1.0f; // Start with the gradient at the bottom
        private boolean hovering = false;
        private Timer animationTimer;
        private int cornerRadius = 30; // Radius for rounded corners
        private Color borderColor = new Color(34, 139, 34); // Border color

        public SlidingGradientPanel() {
            setOpaque(false);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hovering = true;
                    startAnimation();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hovering = false;
                    startAnimation();
                }
            });
        }

        private void startAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            animationTimer = new Timer(30, e -> {
                if (hovering) {
                    gradientPosition -= 0.05f; // Move gradient up
                    if (gradientPosition < 0) {
                        gradientPosition = 0; // Clamp to 0
                        animationTimer.stop();
                    }
                } else {
                    gradientPosition += 0.05f; // Move gradient down
                    if (gradientPosition > 1) {
                        gradientPosition = 1; // Clamp to 1
                        animationTimer.stop();
                    }
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            // Create the gradient paint
            GradientPaint gp = new GradientPaint(0, getHeight() * gradientPosition, endColor, 0, 0, startColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius); // Fill with gradient

            // Draw the rounded border
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2)); // Set border thickness
            g2.drawRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius); // Draw border

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JTextPane centeredTextPane(String text, int size, int style, Color color) {
        JTextPane pane = new JTextPane();
        pane.setText(text);
        pane.setFont(new Font("Arial", style, size));
        pane.setForeground(color);
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setMaximumSize(new Dimension(700, 50));
        pane.setAlignmentX(Component.CENTER_ALIGNMENT);

        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        return pane;
    }

    private JPanel createImagePanel() {
        String[] imagePaths = { "src/img/products.png", "src/img/tools.jpg", "src/img/service.jpeg" };
        String[] captions = { "Eco-friendly Goods", "Tools and Appliances", "Services" };
    
        JPanel panel = new JPanel() {
            private float alpha = 1.0f;
            private int currentIndex = 0;
            private ImageIcon[] images = new ImageIcon[imagePaths.length];
            private String currentCaption = captions[0];
    
            {
                setPreferredSize(new Dimension(800, 400)); // Increased height
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
                setOpaque(false); // Set opaque to false to make the background transparent
    
                // Load and scale images to fit the panel width while preserving aspect ratio
                for (int i = 0; i < imagePaths.length; i++) {
                    ImageIcon original = new ImageIcon(imagePaths[i]);
                    Image scaled = getScaledImage(original.getImage(), 720, 300);
                    images[i] = new ImageIcon(scaled);
                }
    
                Timer fadeTimer = new Timer(50, null);
                Timer switchTimer = new Timer(3000, e -> fadeTimer.start());
    
                fadeTimer.addActionListener(e -> {
                    alpha -= 0.05f;
                    if (alpha <= 0) {
                        currentIndex = (currentIndex + 1) % images.length;
                        currentCaption = captions[currentIndex];
                        alpha = 1.0f;
                        fadeTimer.stop();
                    }
                    repaint();
                });
    
                switchTimer.start();
            }
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
    
                int imgWidth = images[currentIndex].getIconWidth();
                int imgHeight = images[currentIndex].getIconHeight();
                int x = (getWidth() - imgWidth) / 2;
                int y = (getHeight() - imgHeight) / 2 - 20;
    
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.drawImage(images[currentIndex].getImage(), x, y, null);
                g2.dispose();
    
                // Caption below image
                String caption = currentCaption;
                Font font = new Font("Arial", Font.BOLD, 18);
                g.setFont(font);
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(caption);
                int captionX = (getWidth() - textWidth) / 2;
                int captionY = getHeight() - 30;
    
                // Enable anti-aliasing
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
                // Outline (dark green for contrast)
                g2d.setColor(new Color(0, 100, 0)); // Dark green
                g2d.drawString(caption, captionX - 1, captionY - 1);
                g2d.drawString(caption, captionX + 1, captionY - 1);
                g2d.drawString(caption, captionX - 1, captionY + 1);
                g2d.drawString(caption, captionX + 1, captionY + 1);
    
                // Foreground text (light mint)
                g2d.setColor(new Color(235, 255, 235)); // Minty white
                g2d.drawString(caption, captionX, captionY);
    
            }
        };
    
        return panel;
    }

    private Image getScaledImage(Image srcImg, int maxW, int maxH) {
        int width = srcImg.getWidth(null);
        int height = srcImg.getHeight(null);

        double scale = Math.min((double) maxW / width, (double) maxH / height);
        int scaledW = (int) (width * scale);
        int scaledH = (int) (height * scale);

        return srcImg.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
    }

    private JPanel createDescriptionBox(String text, int height) {
        if (height < 300) {
            height = 300; // Minimum height
        }

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Arial", Font.PLAIN, 15));
        area.setForeground(new Color(21, 21, 21));
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setAlignmentX(Component.CENTER_ALIGNMENT);
        area.setMaximumSize(new Dimension(700, height));

        SlidingGradientPanel box = new SlidingGradientPanel();
        box.setLayout(new BorderLayout());
        box.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        box.add(area, BorderLayout.CENTER);
        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.setMaximumSize(new Dimension(750, height)); // Set the maximum size based on the height parameter
        box.setOpaque(false);

        return box;
    }

    class AnimatedDescriptionPanel extends JPanel {
        private float alpha = 0f;
        private float scale = 1.0f;
        private Timer animationTimer;
        private boolean hovering = false;

        public AnimatedDescriptionPanel(JPanel content) {
            setLayout(new BorderLayout());
            setOpaque(false);
            add(content, BorderLayout.CENTER);

            animationTimer = new Timer(15, e -> {
                if (hovering) {
                    if (scale < 1.1f) { // Scale up to 10%
                        scale += 0.05f; // Increase scale
                    }
                } else {
                    if (scale > 1.0f) { // Scale back to original size
                        scale -= 0.05f; // Decrease scale
                    }
                }
                repaint();
            });

            animationTimer.start();

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hovering = true;
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hovering = false;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Apply scaling
            g2.translate(getWidth() / 2, getHeight() / 2);
            g2.scale(scale, scale);
            g2.translate(-getWidth() / 2, -getHeight() / 2);

            // Background fill based on hover state
            g2.setColor(hovering ? new Color(200, 255, 200) : new Color(255, 255, 255));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private String getDescription1() {
        return "EcoTrade is a modern, sustainable marketplace where users can barter, buy, or sell goods, services, and equipment.\n\n"
                + "It is more than just a platform, it's a movement. By choosing to reuse, repair, and share, you help reduce environmental impact and conserve valuable resources.\n\n"
                + "Join our community and be a part of the greener future of trade.\n\n"
                + "We support a circular economy, encourage ethical trading, and make it easy to connect with like-minded people who care about the planet.";
    }

    private String getDescription2() {
        return "Why EcoTrade?\n\n"
                + "We believe in empowering people to make environmentally conscious choices through trade.\n\n"
                + "By using EcoTrade, you reduce waste, support local communities, and extend the lifecycle of products that would otherwise be discarded.\n\n"
                + "EcoTrade promotes a circular economy, where nothing is wasted and everything finds a new purpose.\n\n"
                + "Your small actions lead to big changes. Together, we can trade smart, live green, and inspire others to do the same.";
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(title + " page coming soon!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(new Color(46, 125, 50));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    private boolean confirmLogout() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Log Out",
                JOptionPane.YES_NO_OPTION);
        return confirm == JOptionPane.YES_OPTION;
    }
}
