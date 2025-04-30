package client.panels;

import java.awt.*;

import javax.swing.*;

import client.frames.MainFrame;
import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;
import client.utils.TCGUtils;

/**
 * TCGPanel is a base class for panels in the Trading Card Game (TCG)
 * application.
 * It sets up the general layout and includes common components like the user's
 * information and the bottom menu.
 */
public class TCGPanel extends JPanel {
    public MainFrame parentFrame;
    public String username;
    public GridBagConstraints gbc;
    public Rectangle rect;

    /**
     * Constructor for TCGPanel
     * 
     * @param parentFrame The main frame that holds the panel
     * @param username    The username of the logged-in player
     */
    public TCGPanel(MainFrame parentFrame, String username) {
        super();
        this.parentFrame = parentFrame;
        this.username = username;
        this.rect = this.parentFrame.getRect();
        setLayout(new GridBagLayout());
        setBackground(TCGUtils.BACKGROUND_COLOR);

        // Create the user information panel (displays username and profile picture)
        PlayerInfoPanel userInfoPanel = new PlayerInfoPanel(this.username);

        // Create the bottom menu with navigation buttons
        MenuButtons bottomPanel = new MenuButtons(this.parentFrame);

        this.gbc = new GridBagConstraints(); // Initialize the GridBagConstraints object

        // Position the user info panel at the top right of the layout
        this.gbc.gridx = 3;
        this.gbc.anchor = GridBagConstraints.NORTHEAST;
        add(userInfoPanel, this.gbc);

        // Position the bottom menu to span the entire width at the bottom
        this.gbc.gridx = 0;
        this.gbc.gridy = 2;
        this.gbc.gridwidth = 4;
        this.gbc.weightx = 1;
        this.gbc.fill = GridBagConstraints.HORIZONTAL;
        add(bottomPanel, this.gbc);

        // Reset the GridBagConstraints object to avoid bugs in future layout
        // modifications
        resetGBC();
    }

    /**
     * Adds a main component (e.g., collection, carousel) to the panel.
     * The component is centered and sized relative to the screen size.
     * 
     * @param component The component to be added (JComponent)
     */
    public void addMainComponent(JComponent component) {
        // Set the component's preferred size to a percentage of the screen size
        component.setPreferredSize(new Dimension((int) (this.rect.width * 0.7), (int) (this.rect.height * 0.7)));
        component.setVisible(true);

        // Position the component in the layout, centered
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1; // Let it take up vertical space for centering
        gbc.anchor = GridBagConstraints.CENTER;
        add(component, gbc);

        resetGBC();
    }

    /**
     * Adds a main component (e.g., collection, carousel) to the panel.
     * The component is centered and sized relative to the screen size.
     * 
     * @param component The component to be added (JComponent)
     * @param fill      wheather it should fill horizonatally
     */
    public void addMainComponent(JComponent component, boolean fill) {
        // Set the component's preferred size to a percentage of the screen size
        component.setPreferredSize(new Dimension((int) (this.rect.width * 0.7), (int) (this.rect.height * 0.7)));
        component.setVisible(true);   
        
        if (fill) {
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }

        // Position the component in the layout, centered
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1; // Let it take up vertical space for centering
        gbc.anchor = GridBagConstraints.CENTER;
        add(component, gbc);

        resetGBC();
    }

    /**
     * Getter method for the username
     * 
     * @return The username of the player
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Resets the GridBagConstraints object to default values
     */
    private void resetGBC() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 0); // No padding
        gbc.ipadx = 0;
        gbc.ipady = 0;
    }

}