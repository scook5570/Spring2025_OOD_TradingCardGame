package client.panels;

import java.awt.*;

import javax.swing.*;

import client.frames.MainFrame;
import client.utils.TCGUtils;

/**
 * HomePanel represents the home screen shown after login.
 * It contains a carousel-like display of card packs.
 */
public class HomePanel extends TCGPanel {

    /**
     * Constructor for HomePanel
     * 
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public HomePanel(MainFrame parentFrame, String username) {
        super(parentFrame, username);

        // Create the card carousel panel
        JPanel carouselPanel = new JPanel();
        carouselPanel.setLayout(new BoxLayout(carouselPanel, BoxLayout.X_AXIS));
        carouselPanel.setBackground(TCGUtils.BACKGROUND_COLOR);

        // Define dimensions for center and side packs (3.5 x 2.5 ratio)
        Dimension centerPackSize = new Dimension(250, 375); // Center card is bigger
        Dimension sidePackSize = new Dimension(210, 315); // Side cards are smaller

        // Add side pack
        carouselPanel.add(wrapWithContainer(createPackPanel(sidePackSize), sidePackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0))); // Spacing between packs

        // Add center pack
        carouselPanel.add(wrapWithContainer(createPackPanel(centerPackSize), centerPackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0))); // Spacing between packs

        // Add another side pack
        carouselPanel.add(wrapWithContainer(createPackPanel(sidePackSize), sidePackSize));

        // Create a center wrapper to center the carousel panel in the main area
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(TCGUtils.BACKGROUND_COLOR);
        centerWrapper.add(carouselPanel);

        // Add the entire centerWrapper to the main panel
        addMainComponent(centerWrapper);
    }

    /**
     * Helper method to create a single pack panel (the "card pack" in the carousel)
     * 
     * @param size Size of the pack panel
     * @return A styled JPanel representing a card pack
     */
    private JPanel createPackPanel(Dimension size) {
        JPanel pack = new JPanel();
        pack.setPreferredSize(size);
        pack.setBackground(new Color(100, 100, 100));
        pack.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Black border around the pack
        return pack;
    }

    /**
     * Helper method to wrap a content panel inside a container with fixed size
     * 
     * @param content The content panel to wrap
     * @param size    The size to enforce
     * @return A transparent container panel holding the content panel
     */
    private JPanel wrapWithContainer(JPanel content, Dimension size) {
        JPanel container = new JPanel();
        container.setOpaque(false); // Make container transparent
        container.setPreferredSize(size);
        container.setMaximumSize(size);
        container.setMinimumSize(size);
        container.setLayout(new GridBagLayout()); // Center the content
        container.add(content);
        return container;
    }
}