package shared;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Card extends JPanel {
    private Dimension dimension = new Dimension(250, 350);
    private String name; // Name of the card
    private int rarity; // Rarity of the card (1-5)

    /**
     * Trading Card Constructor
     * 
     * @param name   name of the card.
     *               Sould be the same as the name for its texture .png
     * @param rarity how rare is the card is
     */

    public Card(String cardID, String name, int rarity, String image) {
        this.cardID = cardID;
        this.name = name;
        this.rarity = rarity;
        // this.image = image;
        setPreferredSize(dimension);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            // get image
            File imageFile = Paths.get(System.getProperty("user.dir"), "assets", this.name + ".png").toFile();
            g.drawImage(ImageIO.read(imageFile), 0, 0, this);
        } catch (IOException e) {
            g.setColor(Color.WHITE);
            // add font
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(new Color(0, 0, 0));
            FontMetrics metrics = getFontMetrics(g.getFont());
            // Display Image Not Found
            g.drawString("Image Not Found", ((int) dimension.getWidth() - metrics.stringWidth("Image Not Found")) / 2,
                    (int) dimension.getHeight() / 12);
            // Print error
            e.printStackTrace();        
        }

    }

    /**
     * Get the ID of the card
     * @return ID of the card
     */
    public String getCardID() {
        return cardID;
    }

    /**
     * Get the name of the card
     * 
     * @return name of the card
     */
    public String getName() {
        return name;
    }

    /**
     * Get the rarity of the card
     * 
     * @return rarity of the card
     */
    public int getRarity() {
        return rarity;
    }

    /**
     * Get the image of the card
     * @return image of the card
     */
    public String getImage() {
        return image;
    }

}