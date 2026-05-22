package com.ecotrade;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Land {

    public static int currentUserId;
    public static int currentUser;
    private String currentUsername;
    private JFrame frame;
    private JPanel contentPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("EcoTrade - Sustainable Trading System");
            mainFrame.setSize(1000, 650);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            new Land(mainFrame);
        });
    }

    public Land(JFrame existingFrame) {
        this.frame = existingFrame;
        init(frame); // Initialize the frame
    }

    public void init(JFrame existingFrame) {
        this.frame = existingFrame;
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        frame.add(createHeader(), BorderLayout.NORTH);

        contentPanel = createHomePanel();
        contentPanel.setPreferredSize(new Dimension(0, contentPanel.getPreferredSize().height));

        JScrollPane scrollPane = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        frame.add(scrollPane, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(34, 139, 34)); // Forest Green color
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Logo & Title
        ImageIcon scaledIcon = new ImageIcon(new ImageIcon("src/img/logo1.png")
                .getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
        JLabel logo = new JLabel(scaledIcon);

        JLabel title = new JLabel("EcoTrade");
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(logo);
        leftPanel.add(title);

        // User Account Button
        // Load the icon image
        ImageIcon userIcon = new ImageIcon("src/img/usericon.png"); // Replace with your icon path

        // Create the button with the icon
        JButton userButton = new JButton(userIcon); // Set the icon as the button's icon
        userButton.setText("  Account"); // Add text next to the icon (add spaces for padding)
        userButton.setOpaque(true); // Set opaque to true to see background color
        userButton.setBackground(new Color(34, 139, 34)); // Initial background color
        userButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        userButton.setForeground(Color.WHITE);
        userButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userButton.setFocusPainted(false);

        // Add mouse listener for hover effect
        userButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new Color(100, 150, 100)); // Change to a lighter green on hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new Color(34, 139, 34)); // Revert to original color
            }
        });

        // Dropdown Menu
        // Create the popup menu
        JPopupMenu userMenu = new JPopupMenu();

        // Load and scale the icons for login and register
        ImageIcon loginIcon = new ImageIcon(new ImageIcon("src/img/login.png")
                .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)); // Adjust width and height as needed
        ImageIcon registerIcon = new ImageIcon(new ImageIcon("src/img/reg.png")
                .getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)); // Adjust width and height as needed
        // Create menu items with icons
        JMenuItem loginItem = new JMenuItem("Login", loginIcon); // Set the icon for the login item
        JMenuItem registerItem = new JMenuItem("Register", registerIcon); // Set the icon for the register item

        // Set font for menu items
        loginItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Add action listeners
        loginItem.addActionListener(e -> showLoginDialog());
        registerItem.addActionListener(e -> showRegisterDialog());

        // Add menu items to the popup menu
        userMenu.add(loginItem);
        userMenu.add(registerItem);

        // Show the popup menu when the user button is clicked
        userButton.addActionListener(e -> userMenu.show(userButton, 0, userButton.getHeight()));

        // Add the user button to the right panel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(userButton);

        // Add the left and right panels to the header
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createHomePanel() {
        // Create an ImagePanel with the path to your background image
        ImagePanel backgroundPanel = new ImagePanel("src/img/bg.jpg"); // Replace with your image path
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));

        // Create the content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // Make sure the content panel is transparent

        // Add components to the content panel
        contentPanel
                .add(centeredTextPane("EcoTrade: A Sustainable Trading System", 26, Font.BOLD, new Color(34, 139, 34)));
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(centeredTextPane("Trade Green, Live Clean", 18, Font.ITALIC, new Color(100, 149, 237)));
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(createImagePanel());
        contentPanel.add(Box.createVerticalStrut(60)); // More spacing before descriptions

        // Description Row Panel (horizontal layout)
        JPanel descriptionRow = new JPanel();
        descriptionRow.setLayout(new BoxLayout(descriptionRow, BoxLayout.X_AXIS));
        descriptionRow.setOpaque(false);
        descriptionRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create description panels

        JPanel desc1 = new AnimatedDescriptionPanel(createDescriptionBox(getDescription1(), 350));
        JPanel desc2 = new AnimatedDescriptionPanel(createDescriptionBox(getDescription2(), 350));

        desc1.setMaximumSize(new Dimension(370, 350));
        desc2.setMaximumSize(new Dimension(370, 350));

        descriptionRow.add(desc1);
        descriptionRow.add(Box.createHorizontalStrut(20)); // space between
        descriptionRow.add(desc2);

        contentPanel.add(descriptionRow);
        contentPanel.add(Box.createVerticalStrut(60));

        // Add the content panel to the background panel
        backgroundPanel.add(contentPanel);

        return backgroundPanel;
    }

    class ImagePanel extends JPanel {
        private Image backgroundImage;

        public ImagePanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
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
                setBackground(new Color(204, 255, 204)); // Light green that blends well
                setOpaque(false); // Let parent panel's gradient show through

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

    // Utility to scale image while preserving aspect ratio
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

    private JDialog loadingDialog;

    private void showLoadingDialog() {
        loadingDialog = new JDialog(frame, "Loading...", true);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(frame);
        loadingDialog.setLayout(new BorderLayout());

        JLabel loadingLabel = new JLabel("Please wait...", SwingConstants.CENTER);
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);

        loadingDialog.setVisible(true);
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dispose();
        }
    }

    private void showLoginDialog() {
        // Load icons
        ImageIcon userIcon = new ImageIcon("icons/user.png");
        ImageIcon passIcon = new ImageIcon("icons/lock.png");

        // Create and style fields
        JTextField usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Create panel with background
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 250, 255)); // light soft blue
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Username row
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:", userIcon, JLabel.LEFT), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password row
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:", passIcon, JLabel.LEFT), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Customize dialog buttons
        UIManager.put("OptionPane.okButtonText", "Login");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));

        // Show dialog
        int option = JOptionPane.showConfirmDialog(frame, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim(); // Trim whitespace
            String password = new String(passwordField.getPassword());

            // Validate input
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter both username and password.");
                return; // Exit if validation fails
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT l.pass, r.salt, r.id FROM login l " +
                                "JOIN ecoregister r ON l.username = r.username WHERE l.username = ?");
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("pass");
                    String storedSalt = rs.getString("salt");
                    String inputHash;
                    if (storedSalt == null || storedSalt.isEmpty()) {
                        // Salt is missing, assume password is stored in plain text (NOT secure, for dev
                        // use only)
                        inputHash = password;
                    } else {
                        // Salt is present, use secure hash
                        inputHash = hashPasswordPBKDF2(password, storedSalt);
                    }

                    if (storedHash.equals(inputHash)) {

                        currentUserId = rs.getInt("id");
                        currentUsername = username;
                        JOptionPane.showMessageDialog(frame, "Login successful! Welcome " + username);
                        new Home(username, frame);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
            }
        }
    }

    private boolean isValidPassword(String password) {
        // Check length
        if (password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // Check for at least one special character (optional)
        // Uncomment the next line if you want to enforce special characters
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        return true;
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPasswordPBKDF2(String password, String salt) throws Exception {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private void showRegisterDialog() {
        while (true) {
            // Create input fields
            JTextField nameField = new JTextField(15);
            JTextField emailField = new JTextField(15);
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

            JTextField[] fields = { nameField, emailField, usernameField };
            for (JTextField field : fields) {
                field.setFont(fieldFont);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            passwordField.setFont(fieldFont);
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            // Panel with GridBagLayout
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(245, 250, 255));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            int row = 0;

            // Add each field to the panel
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Full Name:"), gbc);
            gbc.gridx = 1;
            panel.add(nameField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            panel.add(emailField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            panel.add(usernameField, gbc);

            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);

            // Custom button text and fonts
            UIManager.put("OptionPane.okButtonText", "Register");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));

            int option = JOptionPane.showConfirmDialog(frame, panel, "Register", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION)
                return;

            // Get user input
            String fullName = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Validate password
            if (!isValidPassword(password)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Password must be at least 8 characters long,\n" +
                                "and include at least one uppercase letter,\n" +
                                "one lowercase letter, and one digit.",
                        "Invalid Password",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // Register logic with DB
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                    "ViewAl3x@M3nd0z@")) {
                conn.setAutoCommit(false);

                try {
                    // Password hashing placeholder
                    // String salt = generateSalt();
                    // String hashedPassword = hashPasswordPBKDF2(password, salt);

                    PreparedStatement stmt1 = conn.prepareStatement(
                            "INSERT INTO ecoregister (fullname, email, username, pass, salt) VALUES (?, ?, ?, ?, ?)");
                    stmt1.setString(1, fullName);
                    stmt1.setString(2, email);
                    stmt1.setString(3, username);
                    String salt = generateSalt();
                    String hashedPassword = hashPasswordPBKDF2(password, salt);

                    stmt1.setString(4, hashedPassword);
                    stmt1.setString(5, salt);

                    stmt1.executeUpdate();

                    PreparedStatement stmt2 = conn.prepareStatement(
                            "INSERT INTO login (username, pass) VALUES (?, ?)");
                    stmt2.setString(1, username);
                    stmt2.setString(2, hashedPassword); // Replace with hashedPassword
                    stmt2.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(frame, "Registration successful!");
                    showLoginDialog();
                    return;

                } catch (SQLIntegrityConstraintViolationException ex) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(frame, "Username already exists.");
                } catch (Exception ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database connection error.");
            }
        }
    }
}
