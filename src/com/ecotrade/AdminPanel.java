package com.ecotrade;

import javax.swing.*;
import java.awt.*;

public class AdminPanel {
    public AdminPanel(String username, JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("Welcome to the Admin Panel, " + username, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.CENTER);
        
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }
}