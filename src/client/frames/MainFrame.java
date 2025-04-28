package client.frames;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import client.panels.CollectionPanel;
import client.panels.HomePanel;
import client.panels.TCGPanel;
import client.utils.TCGUtils;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    public String username;
    public Rectangle rect;

    /**
     * 
     * The main farme that loads after login and shows the app UI
     */
    public MainFrame(String username) {
        this.username = username;
        setTitle("Traiding Card Game");
        setLocationRelativeTo(null);
        setBackground(TCGUtils.BACKGROUND_COLOR);
        // Sets the frame the full screen size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(this.rect);
        // Makes it none rezisable after its adjusted to not overtake the tool bar
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setResizable(false);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                System.exit(0);
            }
        });

        // Initialize CardLayout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add panels to the CardLayout
        mainPanel.add(new HomePanel(this, this.username), TCGUtils.HOME);
        mainPanel.add(new CollectionPanel(this, this.username), TCGUtils.COLLECTION);

        // Add the main panel to the frame
        add(mainPanel);

        // Show the home page by default
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

    public String getUsername() {
        return this.username;
    }

    public Rectangle getRect() {
        return this.rect;
    }
}
