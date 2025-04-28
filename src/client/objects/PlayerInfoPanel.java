package client.objects;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import client.utils.TCGUtils;

public class PlayerInfoPanel extends JPanel{
    private String username;
    
    public PlayerInfoPanel(String username){
        super();
        this.username = username;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(new Dimension(375, 50)));
        setBackground(TCGUtils.USER_INFO_BC);

        try {
            // TODO: Get correct profile image for user
            BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));
            Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(picLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel usernameLabel = new JLabel(this.username);
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
        add(usernameLabel);
    }

    public String getUsername(){
        return this.username;
    }
}
