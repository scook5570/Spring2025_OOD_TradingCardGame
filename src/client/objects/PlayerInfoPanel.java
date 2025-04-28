package client.objects;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import client.utils.TCGUtils;

/**
 * PlayerInfoPanel displays the user's profile picture and username at the top
 * of the UI
 */
public class PlayerInfoPanel extends JPanel {
    private String username; // Stores the username to display

    /**
     * Constructor for PlayerInfoPanel
     * 
     * @param username The username of the player
     */
    public PlayerInfoPanel(String username) {
        super();
        this.username = username;

        // Set layout to arrange components horizontally
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Set preferred size of the panel
        setPreferredSize(new Dimension(375, 50));

        // Set background color
        setBackground(TCGUtils.USER_INFO_BC);

        try {
            // Load default user profile image
            BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));

            // Scale the image to fit nicely
            Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);

            // Create a label to display the profile picture
            JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align the picture vertically

            // Add the picture label to the panel
            add(picLabel);
        } catch (IOException e) {
            e.printStackTrace(); // Print error if image loading fails
        }

        // Create a label for the username
        JLabel usernameLabel = new JLabel(this.username);
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add spacing between image and username
        add(Box.createRigidArea(new Dimension(10, 0)));

        // Add username label to the panel
        add(usernameLabel);
    }

    /**
     * Getter for the username
     * 
     * @return the username string
     */
    public String getUsername() {
        return this.username;
    }
}