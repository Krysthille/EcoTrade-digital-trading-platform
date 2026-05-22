// Shop.java
package com.ecotrade;

import javax.swing.*;
import java.awt.*;

public class Shop {
    public static JPanel getShopPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("Welcome to the EcoTrade Shop!");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(46, 125, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("Browse eco-friendly products and services.");
        message.setFont(new Font("Arial", Font.PLAIN, 16));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(message);

        return panel;
    }

 
}
