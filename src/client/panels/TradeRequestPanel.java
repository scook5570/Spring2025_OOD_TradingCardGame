package client.panels;

import client.frames.MainFrame;
import shared.Card;
import javax.swing.*;
import java.awt.*;

/**
 * TradeRequestPanel represents a panel that prompts the user with a trade request.
 * It shows who is requesting the trade, the proposed card, and provides Accept/Deny buttons.
 */
public class TradeRequestPanel extends JPanel {

    public TradeRequestPanel(MainFrame parentFrame, String username, Card proposedCard) {

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        // Set up constraints for layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Create and add the label that shows the trade requester
        JLabel usernameLabel = new JLabel(username + " wants to trade:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(usernameLabel, gbc);  // Add to panel at position (0, 0)

        // Create an image label using the proposed card's image
        ImageIcon cardImage = new ImageIcon(proposedCard.getImage());
        JLabel cardLabel = new JLabel(cardImage);

        // Move card image label to the next row (position 0, 1)
        gbc.gridy = 1;
        add(cardLabel, gbc);

        // Create a panel to hold the Accept and Deny buttons
        JPanel buttonPanel = new JPanel();
        JButton acceptButton = new JButton("Accept");
        JButton denyButton = new JButton("Deny");

        buttonPanel.add(acceptButton);   // Add Accept button to button panel
        buttonPanel.add(denyButton);     // Add Deny button to button panel

        // Move buttons to the next row (position 0, 2)
        gbc.gridy = 2;
        add(buttonPanel, gbc);

        // Set up action listener for the Accept button
        acceptButton.addActionListener(e -> {
            // Show confirmation popup
            JOptionPane.showMessageDialog(this, "Trade Accepted!");

            // Switch to the trade panel (placeholder for further trade logic)
            TradePanel tradePanel = new TradePanel(parentFrame, username);
            parentFrame.setPanel(tradePanel);
        });

        // Set up action listener for the Deny button
        denyButton.addActionListener(e -> {
            // Show rejection popup
            JOptionPane.showMessageDialog(this, "Trade Denied!");

            // Return to the trade panel (could be enhanced to remove pending trade)
            TradePanel tradePanel = new TradePanel(parentFrame, username);
            parentFrame.setPanel(tradePanel);
        });
    }
}
