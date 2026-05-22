package com.ecotrade;

import javax.swing.*;
import java.awt.*;

public class ProductPolicyFrame extends JFrame {
    public ProductPolicyFrame() {
        setTitle("Product Policies");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Simple content
        JTextArea policyText = new JTextArea("Here are the product policies...");
        policyText.setEditable(false);
        policyText.setWrapStyleWord(true);
        policyText.setLineWrap(true);
        policyText.setMargin(new Insets(10, 10, 10, 10));

        add(new JScrollPane(policyText), BorderLayout.CENTER);
        setVisible(true);
    }
}
