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
    private File image; // Image of the card

    /**
     * Trading Card Constructor
     * @param name   name of the card.
     *               Sould be the same as the name for its texture .png
     * @param rarity how rare is the card is
     */
    public Card(String name, int rarity) {
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
            // display
            System.out.println("Exception");
            g.setColor(Color.WHITE);
            // add font
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(new Color(0, 0, 0));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Image Not Found", ((int) dimension.getWidth() - metrics.stringWidth("Image Not Found")) / 2,
                    (int) dimension.getHeight() / 12);
        }

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
    public File getImage() {
        return image;
    }
}