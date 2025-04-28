package client.panels;

import java.awt.*;

import javax.swing.*;

import client.frames.MainFrame;

/**
 * CollectionPanel represents the screen where a user's card collection is
 * displayed.
 */
public class CollectionPanel extends TCGPanel {

    /**
     * Constructor for CollectionPanel
     * 
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public CollectionPanel(MainFrame parentFrame, String username) {
        super(parentFrame, username); // Call the parent TCGPanel constructor

        // Create a panel where the collection cards will be displayed
        JPanel collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY); 
        collectionPanel.setLayout(new GridBagLayout()); 

        // Wrap the collection panel in a scroll pane for scrolling functionality
        JScrollPane collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);

        // Disable horizontal scrolling to keep the layout clean
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Increase vertical scroll speed for smoother scrolling
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add the scroll pane to the main content area of the panel
        addMainComponent(collectionScrollPane);
    }
}