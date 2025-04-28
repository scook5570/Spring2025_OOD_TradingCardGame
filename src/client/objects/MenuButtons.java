package client.objects;

import java.awt.*;

import javax.swing.*;

import client.frames.MainFrame;
import client.utils.TCGUtils;

/**
 * MenuButtons is a panel containing navigation buttons for the app
 */
public class MenuButtons extends JPanel {
    private MainFrame parentFrame; // Reference to the main frame to control panel switching

    /**
     * Constructor for MenuButtons
     * 
     * @param parentFrame the main application frame
     */
    public MenuButtons(MainFrame parentFrame) {
        super();
        this.parentFrame = parentFrame;

        // Set layout and background color
        setLayout(new FlowLayout(FlowLayout.CENTER, 100, 10)); // Center buttons with spacing
        setBackground(TCGUtils.BACKGROUND_COLOR);

        // Create "Gallery" button (collection panel)
        JButton collectionButton = new JButton("Gallery");
        collectionButton.setPreferredSize(new Dimension(100, 50));
        collectionButton.setFocusPainted(false); // Remove focus border around text
        // Switch to Collection panel
        collectionButton.addActionListener(e -> this.parentFrame.showPanel(TCGUtils.COLLECTION));

        // Create "Home" button
        JButton homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 50));
        homeButton.setFocusPainted(false);
        // Switch to Home panel
        homeButton.addActionListener(e -> this.parentFrame.showPanel(TCGUtils.HOME)); 

        // Create "Trade" button
        JButton tradeButton = new JButton("Trade");
        tradeButton.setPreferredSize(new Dimension(100, 50));
        tradeButton.setFocusPainted(false);
        tradeButton.addActionListener(e -> {
            // TODO: Change to Trading panel when implemented
        });

        // Add all buttons to the panel
        add(collectionButton);
        add(homeButton);
        add(tradeButton);
    }
}