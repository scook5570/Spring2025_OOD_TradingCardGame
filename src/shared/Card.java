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
    private String cardID;
    private String image;
    private String pack; // Pack the card belongs to

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
        this.image = image;
        setPreferredSize(dimension);
        setVisible(true);
        // System.out.println(image);
    }

    public Card(String cardID, String name, int rarity, String image, String pack) {
        this(cardID, name, rarity, null);
        this.pack = pack;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            // get image
            File imageFile = Paths.get(System.getProperty("user.dir"), "src/server/cardinfo/images/", this.name + ".png").toFile();
            Image img = ImageIO.read(imageFile);
            if (img == null) {
                throw new IOException("ImageIO.read returned null for file: " + imageFile);
            }

            double scaleWidth = (double) this.getWidth() / img.getWidth(null);
            double scaleHeight = (double) this.getHeight() / img.getHeight(null);
            double scale = Math.min(scaleWidth, scaleHeight);

            int newWidth = (int) (img.getWidth(null) * scale);
            int newHeight = (int) (img.getHeight(null) * scale);

            int x = (this.getWidth() - newWidth) / 2;
            int y = (this.getHeight() - newHeight) / 2;
            g.drawImage(img, x, y, newWidth, newHeight, this);
        } catch (IOException e) {
            g.setColor(Color.WHITE);
            // add font
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(new Color(0, 0, 0));
            FontMetrics metrics = getFontMetrics(g.getFont());
            String message = this.name + " Not Found";
            // Display Image Not Found
            g.drawString(message, ((int) dimension.getWidth() - metrics.stringWidth(message)) / 2,
                    (int) dimension.getHeight() / 12);
            // Print error
            e.printStackTrace();
        }

    }

    /**
     * Get the ID of the card
     * 
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
     * 
     * @return image of the card
     */
    public String getImage() {
        return image;
    }

    /**
     * Get the pack of the card
     * 
     * @return pack of the card
     */
    public String getPack() {
        return pack;
    }
}