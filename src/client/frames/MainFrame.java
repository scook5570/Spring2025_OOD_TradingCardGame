package client.frames;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import client.panels.CollectionPanel;
import client.panels.HomePanel;
import client.utils.TCGUtils;

/**
 * The main frame that loads after login and shows the app UI
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout; // Manages switching between different panels
    private JPanel mainPanel;
    public String username;
    public Rectangle rect; 

    /**
     * Constructor for MainFrame
     * 
     * @param username the username of the logged-in user
     */
    public MainFrame(String username) {
        this.username = username;

        // Set window properties
        setTitle("Trading Card Game");
        setLocationRelativeTo(null); // Center the window
        setBackground(TCGUtils.BACKGROUND_COLOR);

        // Set the window to full screen size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(this.rect);

        // After window is shown, prevent resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setResizable(false);
            }

            // Exit program if window is closed
            @Override
            public void componentHidden(ComponentEvent e) {
                System.exit(0);
            }
        });

        // Initialize CardLayout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add Home and Collection panels to the main panel with identifiers
        mainPanel.add(new HomePanel(this, this.username), TCGUtils.HOME);
        mainPanel.add(new CollectionPanel(this, this.username), TCGUtils.COLLECTION);

        // Add the main panel to the frame
        add(mainPanel);

        // Show the Home panel by default
        cardLayout.show(mainPanel, "Home");

        setVisible(true);
    }

    /**
     * Switches the visible panel to the one with the given name
     * 
     * @param panelName the panel name to change to
     */
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    /**
     * Returns the username of the current user
     * 
     * @return username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns the rectangle representing screen bounds
     * 
     * @return rectangle of the screen size
     */
    public Rectangle getRect() {
        return this.rect;
    }
}