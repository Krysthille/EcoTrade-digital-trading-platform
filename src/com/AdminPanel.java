package com.ecotrade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminPanel{
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel contentPanel; // Moved contentPanel to class level for access in nav buttons

    public AdminPanel(String username) {
        frame = new JFrame("Admin Panel");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create navigation bar
        JPanel navPanel = createNavBar();
        mainPanel.add(navPanel, BorderLayout.WEST);

        // Create content area
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.add(createUserManagementPanel(), "User  Management");
        contentPanel.add(createProductManagementPanel(), "Product Management");
        contentPanel.add(createTransactionManagementPanel(), "Transaction Management");
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel();
        navBar.setLayout(new BoxLayout(navBar, BoxLayout.Y_AXIS));
        navBar.setPreferredSize(new Dimension(200, 0));
        navBar.setBackground(new Color(58, 162, 64));

        String[] navItems = {"User  Management", "Product Management", "Transaction Management"};
        for (String item : navItems) {
            JButton btn = new JButton(item);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setBackground(new Color(58, 162, 64));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, item);
                }
            });
            navBar.add(btn);
            navBar.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return navBar;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("User  Management"));

        // Sample table for user data
        String[] columnNames = {"User  ID", "Username", "Email", "Actions"};
        Object[][] data = {
                {1, "john_doe", "john@example.com", "Edit | Delete"},
                {2, "jane_doe", "jane@example.com", "Edit | Delete"},
                // Add more sample data as needed
        };

        JTable userTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProductManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Product Management"));

        // Sample table for product data
        String[] columnNames = {"Product ID", "Product Name", "Price", "Actions"};
        Object[][] data = {
                {1, "Eco-Friendly Bag", "$10", "Edit | Delete"},
                {2, "Solar Panel", "$200", "Edit | Delete"},
                // Add more sample data as needed
        };

        JTable productTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Transaction Management"));

        // Sample table for transaction data
        String[] columnNames = {"Transaction ID", "User ", "Amount", "Date", "Status"};
        Object[][] data = {
                {1, "john_doe", "$50", "2023-10-01", "Completed"},
                {2, "jane_doe", "$100", "2023-10-02", "Pending"},
                // Add more sample data as needed
        };

        JTable transactionTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}