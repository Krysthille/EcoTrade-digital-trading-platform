package com.ecotrade;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Contacts {
    
    public static JPanel getPanel() {
        // Create a panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Create the contact list area and populate with sample contacts
        DefaultListModel<String> contactListModel = new DefaultListModel<>();
        contactListModel.addElement("Alice");
        contactListModel.addElement("Bob");
        contactListModel.addElement("Charlie");
        JList<String> contactList = new JList<>(contactListModel);
        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setPreferredSize(new Dimension(200, 300));
        
        // Create the message area
        JTextArea messageArea = new JTextArea();
        messageArea.setPreferredSize(new Dimension(400, 300));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        
        // Create the send button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedContact = contactList.getSelectedValue();
                String message = messageArea.getText();
                if (selectedContact != null && !message.trim().isEmpty()) {
                    // Handle message sending logic here
                    System.out.println("Sending message to " + selectedContact + ": " + message);
                    messageArea.setText(""); // Clear text area after sending
                } else {
                    JOptionPane.showMessageDialog(panel, "Please select a contact and enter a message.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Top panel for contact list
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel("Select a contact:"), BorderLayout.NORTH);
        topPanel.add(contactScrollPane, BorderLayout.CENTER);
        
        // Bottom panel for message area and send button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(new JLabel("Write a message:"), BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.SOUTH);
        
        // Add all panels to the main panel
        panel.add(topPanel, BorderLayout.WEST);
        panel.add(bottomPanel, BorderLayout.CENTER);
        
        // Return the constructed panel
        return panel;
    }
}