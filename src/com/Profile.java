package com.ecotrade;

import javax.swing.*;
import java.awt.*;
// import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Profile {
    private JFrame frame;
    private boolean sidebarVisible = true;
    private JPanel navPanel;

    public Profile(String username, JFrame frame) {
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

        JPanel profileContent = createProfileContent(username);
        mainPanel.add(profileContent, BorderLayout.CENTER);

        frame.add(createHeader(username), BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
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

        JButton userMenuButton = new JButton("\uD83D\uDC64 Account: " + username);
        userMenuButton.setContentAreaFilled(false);
        userMenuButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        userMenuButton.setForeground(Color.WHITE);
        userMenuButton.setFont(new Font("Arial", Font.PLAIN, 14));
        userMenuButton.setFocusPainted(false);
        userMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu userDropdown = new JPopupMenu();
        JMenuItem profileItem = new JMenuItem("\uD83D\uDC64 Profile");
        JMenuItem ordersItem = new JMenuItem("\uD83D\uDED2 My Orders");
        JMenuItem logoutItem = new JMenuItem("\uD83D\uDCBE Log Out");

        profileItem.addActionListener(e -> new Profile(username, frame));
        logoutItem.addActionListener(e -> {
            if (confirmLogout()) {
                frame.dispose();
                new Land();
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
    
        // Load user info from database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ecotrade", "root", "ViewAl3x@M3nd0z@")) {
            String sql = "SELECT fullname, email FROM ecoregister WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                fullName = rs.getString("fullname");
                email = rs.getString("email");
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Info labels
        JLabel nameLabel = new JLabel("Full Name: " + fullName);
        JLabel userLabel = new JLabel("Username: " + username);
        JLabel emailLabel = new JLabel("Email: " + email);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    
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
    
        JButton editProfileBtn = new JButton("Edit");
        JButton changePasswordBtn = new JButton("Password");
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
        wrapper.add(infoPanel);
        wrapper.add(buttonPanel);
    
        return wrapper;
    }
    
    

    private JPanel createNavBar(String username) {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(180, 0));
        nav.setBackground(new Color(58, 162, 64));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        String[] navItems = {"Home", "Shop", "Community", "Message", "Contact"};

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
        
                JPanel contentPanel;
                switch (itemName) {
                    case "Shop" -> contentPanel = Shop.getShopPanel();
                    // In Home.java or Profile.java
                    case "Community" -> contentPanel = Community.getPanel(); // No arguments
                    // In Home.java or Profile.java
                    case "Message" -> {
                        int currentUserId = Land.currentUserId; // Assuming this is how you get the current user's ID
                        int postOwnerId = getPostOwnerId(); // Retrieve the post owner's ID from your context (e.g., from a selected post)
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








    
    private int getPostOwnerId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPostOwnerId'");
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

    private boolean confirmLogout() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
        return confirm == JOptionPane.YES_OPTION;
    }
}
