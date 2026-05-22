package com.ecotrade;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
// import java.io.*;
import java.sql.*;

public class Profile {
    private JLabel profilePicLabel;
    private ImageIcon defaultProfilePic = new ImageIcon(
            new ImageIcon("src/img/default_profile.png").getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));

    private JFrame frame;
    private boolean sidebarVisible = true;
    private JPanel navPanel;
    private String username;
    private static final Color PRIMARY_COLOR = new Color(58, 162, 64);
    private static final Color SECONDARY_COLOR = new Color(76, 175, 80);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    public Profile(String username, JFrame frame) {
        this.username = username;
        this.frame = frame;
        createHomeUI(username);
    }

    private void createHomeUI(String username) {
        frame.getContentPane().removeAll();
        frame.setTitle("EcoTrade - Profile");
        frame.setLayout(new BorderLayout());

        navPanel = createNavBar(username);
        JPanel mainPanel = new JPanel(new BorderLayout());

        if (sidebarVisible) {
            mainPanel.add(navPanel, BorderLayout.WEST);
        }

        JTabbedPane tabs = createProfileTabs(username);
        mainPanel.add(tabs, BorderLayout.CENTER);

        frame.add(createHeader(username), BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }

    private void refreshProfileDisplay(String username) {
        SwingUtilities.invokeLater(() -> {
            JPanel profileContent = createProfileContent(username);
            frame.getContentPane().removeAll();
            frame.add(createHeader(username), BorderLayout.NORTH);
            frame.add(profileContent, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        });
    }

    private JTabbedPane createProfileTabs(String username) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));
        tabs.setBackground(Color.WHITE);

        tabs.addTab("Profile", createProfileContent(username));

        return tabs;
    }

    JPanel createHeader(String username) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(46, 125, 50));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton toggleSidebar = new JButton("\u2261");
        toggleSidebar.setFont(new Font("Arial", Font.BOLD, 18));
        toggleSidebar.setFocusPainted(false);
        toggleSidebar.setForeground(Color.WHITE);
        toggleSidebar.setBackground(new Color(46, 125, 50));
        toggleSidebar.setBorderPainted(false);
        toggleSidebar.setContentAreaFilled(false);
        toggleSidebar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleSidebar.addActionListener(e -> {
            sidebarVisible = !sidebarVisible;
            createHomeUI(username);
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

        JPopupMenu userDropdown = new JPopupMenu();
        JMenuItem profileItem = new JMenuItem("\uD83D\uDC64 Profile");
        JMenuItem ordersItem = new JMenuItem("\uD83D\uDED2 My Orders");
        JMenuItem logoutItem = new JMenuItem("\uD83D\uDCBE Log Out");

        profileItem.addActionListener(e -> new Profile(username, frame));
        logoutItem.addActionListener(e -> {
            if (confirmLogout()) {
                frame.dispose();
                new Land(frame);
            }
        });

        userDropdown.add(profileItem);
        userDropdown.add(ordersItem);
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

    private JPanel createProfileContent(String username) {
        String fullName = "";
        String email = "";
        int age = 0;
        String address = "";
        String contactNumber = "";
        String profilePicturePath = ""; // Variable to hold the profile picture path

        // Load user info from database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "SELECT fullname, email, age, address, contact_number, profile_picture FROM ecoregister WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                fullName = rs.getString("fullname");
                email = rs.getString("email");
                age = rs.getInt("age");
                address = rs.getString("address");
                contactNumber = rs.getString("contact_number");
                profilePicturePath = rs.getString("profile_picture"); // Get the profile picture path
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Info labels
        JLabel nameLabel = new JLabel("Full Name: " + fullName);
        JLabel userLabel = new JLabel("Username: " + username);
        JLabel emailLabel = new JLabel("Email: " + email);
        JLabel ageLabel = new JLabel("Age: " + age);
        JLabel addressLabel = new JLabel("<html>Address: " + address + "</html>");
        JLabel contactLabel = new JLabel("Contact Number: " + contactNumber);

        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contactLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Profile picture section
        profilePicLabel = new JLabel();
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            // Load the profile picture from the path
            ImageIcon profilePicIcon = new ImageIcon(profilePicturePath);
            Image image = profilePicIcon.getImage();
            Image resizedImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            profilePicLabel.setIcon(new ImageIcon(resizedImage));
        } else {
            // Set default profile picture if no picture is found
            profilePicLabel.setIcon(defaultProfilePic);
        }
        profilePicLabel.setPreferredSize(new Dimension(150, 150));
        profilePicLabel.setHorizontalAlignment(JLabel.CENTER);
        profilePicLabel.setVerticalAlignment(JLabel.CENTER);

        JButton uploadPicBtn = new JButton("Upload Photo");
        uploadPicBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        uploadPicBtn.setBackground(new Color(76, 175, 80));
        uploadPicBtn.setForeground(Color.WHITE);
        uploadPicBtn.setFocusPainted(false);
        uploadPicBtn.setMaximumSize(new Dimension(150, 30));
        uploadPicBtn.addActionListener(e -> uploadProfilePicture());

        JPanel picturePanel = new JPanel();
        picturePanel.setLayout(new BoxLayout(picturePanel, BoxLayout.Y_AXIS));
        picturePanel.setBackground(Color.WHITE);
        picturePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        picturePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        picturePanel.add(profilePicLabel);
        picturePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        picturePanel.add(uploadPicBtn);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(userLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(emailLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(ageLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(addressLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(contactLabel);

        RoundedButton editProfileBtn = new RoundedButton("Edit");

        // Inside the createProfileContent method after creating the editProfileBtn
        editProfileBtn.addActionListener(e -> openEditProfileDialog(username));
        RoundedButton changePasswordBtn = new RoundedButton("Password");
        changePasswordBtn.addActionListener(e -> openChangePasswordDialog(username));
        Font buttonFont = new Font("Arial", Font.PLAIN, 12);
        editProfileBtn.setFont(buttonFont);
        changePasswordBtn.setFont(buttonFont);

        editProfileBtn.setFocusPainted(false);
        changePasswordBtn.setFocusPainted(false);
        editProfileBtn.setBackground(new Color(76, 175, 80));
        changePasswordBtn.setBackground(new Color(76, 175, 80));
        editProfileBtn.setForeground(Color.WHITE);
        changePasswordBtn.setForeground(Color.WHITE);

        editProfileBtn.setMaximumSize(new Dimension(100, 30));
        changePasswordBtn.setMaximumSize(new Dimension(100, 30));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        buttonPanel.add(editProfileBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(changePasswordBtn);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        wrapper.add(picturePanel);
        wrapper.add(Box.createRigidArea(new Dimension(20, 0)));
        wrapper.add(infoPanel);
        wrapper.add(Box.createRigidArea(new Dimension(20, 0)));
        wrapper.add(buttonPanel);

        return wrapper;
    }

    private void openChangePasswordDialog(String username) {
        JDialog passwordDialog = new JDialog(frame, "Change Password", true);
        passwordDialog.setSize(300, 200);

        JPanel panel = new JPanel(new GridLayout(4, 2));

        JPasswordField currentPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmNewPasswordField = new JPasswordField();

        panel.add(new JLabel("Current Password:"));
        panel.add(currentPasswordField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmNewPasswordField);

        JButton saveButton = new JButton("Change Password");
        saveButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmNewPasswordField.getPassword());

            if (newPassword.equals(confirmPassword)) {
                if (changePassword(username, currentPassword, newPassword)) {
                    JOptionPane.showMessageDialog(passwordDialog, "Password changed successfully!");
                    passwordDialog.dispose(); // Close the dialog
                } else {
                    JOptionPane.showMessageDialog(passwordDialog, "Failed to change password.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(passwordDialog, "New passwords do not match.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(saveButton);
        passwordDialog.add(panel);
        passwordDialog.setLocationRelativeTo(frame);
        passwordDialog.setVisible(true);
    }

    private boolean changePassword(String username, String currentPassword, String newPassword) {
        // Check if the current password is correct
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "SELECT password FROM ecoregister WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getString("password").equals(currentPassword)) {
                // Update the password in the database
                String updateSql = "UPDATE ecoregister SET password = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, username);
                updateStmt.executeUpdate();
                return true; // Password changed successfully
            } else {
                return false; // Current password is incorrect
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Error occurred
        }
    }

    private void openEditProfileDialog(String username) {
        JDialog editDialog = new JDialog(frame, "Edit Profile", true);
        editDialog.setSize(400, 300);

        // Create panel for edit dialog
        JPanel panel = new JPanel(new GridLayout(6, 2));
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField contactField = new JTextField();

        // Load current values
        loadCurrentUserData(username, nameField, emailField, ageField, addressField, contactField);

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Contact Number:"));
        panel.add(contactField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Update the database with new values
            if (updateUserProfile(username, nameField, emailField, ageField, addressField, contactField)) {
                JOptionPane.showMessageDialog(editDialog, "Profile updated successfully!");
                editDialog.dispose(); // Close the dialog
                refreshProfileDisplay(username); // Refresh profile info
            }
        });

        panel.add(saveButton);

        editDialog.add(panel);
        editDialog.setLocationRelativeTo(frame);
        editDialog.setVisible(true);
    }

    private void loadCurrentUserData(String username, JTextField nameField, JTextField emailField, JTextField ageField,
            JTextField addressField, JTextField contactField) {
        // Load current values from the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "SELECT fullname, email, age, address, contact_number FROM ecoregister WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("fullname"));
                emailField.setText(rs.getString("email"));
                ageField.setText(String.valueOf(rs.getInt("age")));
                addressField.setText(rs.getString("address"));
                contactField.setText(rs.getString("contact_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updateUserProfile(String username, JTextField nameField, JTextField emailField, JTextField ageField,
            JTextField addressField, JTextField contactField) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "UPDATE ecoregister SET fullname = ?, email = ?, age = ?, address = ?, contact_number = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameField.getText());
            stmt.setString(2, emailField.getText());
            stmt.setInt(3, Integer.parseInt(ageField.getText()));
            stmt.setString(4, addressField.getText());
            stmt.setString(5, contactField.getText());
            stmt.setString(6, username);
            stmt.executeUpdate();
            return true; // Successfully updated
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            return false; // Failed to update
        }
    }

    

    private void uploadProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Get the selected file
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                ImageIcon uploadedIcon = new ImageIcon(filePath);
                Image image = uploadedIcon.getImage();

                // Resize the image to fit the profile picture label
                Image resizedImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(resizedImage)); // Update the profile picture label

                // Save the image path to the database
                saveProfilePicturePath(filePath);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to load image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveProfilePicturePath(String filePath) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root",
                "ViewAl3x@M3nd0z@")) {
            String sql = "UPDATE ecoregister SET profile_picture = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, filePath);
            stmt.setString(2, username); // Use the instance variable username
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createNavBar(String username) {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(180, 0));
        nav.setBackground(new Color(58, 162, 64));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        String[] navItems = { "Home", "Shop", "Community", "Message" };

        for (String itemName : navItems) {
            JButton btn = new JButton(itemName);
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
                frame.getContentPane().removeAll();
                frame.setTitle("EcoTrade - " + itemName);

                if (itemName.equals("Home")) {
                    new Home(username, frame); // This assumes you have a Home class constructor
                    return;
                }

                JPanel contentPanel = null; // Ensure it's initialized

                switch (itemName) {
                    case "Shop" -> contentPanel = Shop.getShopPanel(); // ← Now it assigns to contentPanel
                    case "Community" -> contentPanel = Community.getPanel();
                    case "Message" -> {
                        int currentUserId = Land.currentUserId;
                        int postOwnerId = getPostOwnerId(); // You need to fix this method too
                        contentPanel = Messages.getPanel(currentUserId, postOwnerId);
                    }
                    case "Contact" -> contentPanel = Contacts.getPanel();
                    default -> contentPanel = createPlaceholderPanel(itemName);
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
            });

            nav.add(btn);
            nav.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return nav;
    }

    // KUWANG PANIG MGA POSTS SA USER

    public Integer getPostOwnerId(int postId) {
        Integer ownerId = null; // Use Integer to allow null values
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Database.getConnection(); // Make sure Database.getConnection() is implemented correctly
            if (conn == null) {
                throw new SQLException("Failed to establish a database connection.");
            }
            String query = "SELECT owner_id FROM posts WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, postId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                ownerId = rs.getInt("owner_id"); // Assuming owner_id is an INT in your DB
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ownerId;
    }

    private int getPostOwnerId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPostOwnerId'");
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title + " page under construction...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.ITALIC, 18));
        panel.add(label, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private boolean confirmLogout() {
        int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }
}
