package client.utils;

import shared.Card;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ArrowHelper is a utility class responsible for creating and positioning arrows
 * that indicate trade offers between cards in the trade panel.
 * It rotates and scales an arrow image based on the specified angle and card positions.
 */
public class ArrowHelper {

    /**
     * Adds a rotated arrow to the provided layered pane.
     *
     * @param layered       The JLayeredPane to which the arrow will be added
     * @param card          The card associated with the arrow
     * @param angleDegrees  The angle in degrees at which to rotate the arrow
     */
    public void addArrow(JLayeredPane layered, Card card, double angleDegrees) {
        // Create the rotated arrow and add it to the layered pane at the PALETTE_LAYER
        JLabel arrowLabel = createRotatedArrow("assets/top_arrow.png", angleDegrees, card);
        layered.add(arrowLabel, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Creates a rotated and scaled arrow image and returns a JLabel containing the image.
     *
     * @param path          The path to the arrow image file
     * @param angleDegrees  The angle in degrees to rotate the arrow
     * @param card          The card to position the arrow relative to
     * @return A JLabel containing the rotated and scaled arrow image
     */
    private JLabel createRotatedArrow(String path, double angleDegrees, Card card) {
        try {
            // Load the original arrow image from the specified file path
            BufferedImage src = ImageIO.read(new File(path));

            // Set the new desired size for the arrow and scale it
            int newW = 250, newH = 200;
            BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, newW, newH, null);
            g.dispose();

            // Perform the rotation of the image based on the specified angle
            double rad = Math.toRadians(angleDegrees);
            double sin = Math.abs(Math.sin(rad)), cos = Math.abs(Math.cos(rad));
            int rotW = (int) Math.floor(newW * cos + newH * sin);
            int rotH = (int) Math.floor(newH * cos + newW * sin);

            // Create a new image for the rotated arrow
            BufferedImage rotated = new BufferedImage(rotW, rotH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rotated.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.translate(rotW / 2.0, rotH / 2.0);
            g2.rotate(rad);
            g2.translate(-newW / 2.0, -newH / 2.0);
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();

            // Create a JLabel to display the rotated arrow image
            JLabel lbl = new JLabel(new ImageIcon(rotated));
            lbl.setPreferredSize(new Dimension(rotW, rotH));

            // Position the arrow relative to the card based on the angle
            positionArrow(lbl, card, angleDegrees);

            return lbl;
        } catch (IOException e) {
            e.printStackTrace();
            return new JLabel("X");
        }
    }

    /**
     * Positions the arrow label relative to the card based on the rotation angle and size differences.
     *
     * @param label         The JLabel containing the rotated arrow image
     * @param card          The card to which the arrow is attached
     * @param angleDegrees  The rotation angle used for positioning the arrow
     */
    private void positionArrow(JLabel label, Card card, double angleDegrees) {
        // Get the size of the arrow
        Dimension arrowSize = label.getPreferredSize();

        // Define original card dimensions
        int originalCardWidth = 200;
        int originalCardHeight = 280;

        // Get the actual dimensions of the card
        int cardWidth = card.getWidth();
        int cardHeight = card.getHeight();

        // Calculate the difference in size between the original and resized cards
        int widthDifference = cardWidth - originalCardWidth;
        int heightDifference = cardHeight - originalCardHeight;

        // Calculate the new position of the arrow based on the angle and size differences
        int x = card.getX() + (angleDegrees == 225 ? card.getWidth() - arrowSize.width / 2 - 100 - widthDifference
                : -arrowSize.width / 2 + 100 + widthDifference);

        int y = card.getY() + (angleDegrees == 225 ? card.getHeight() - arrowSize.height / 2 + 100 + heightDifference
                : -arrowSize.height / 2 - 100 - heightDifference);

        // Set the position of the arrow label
        label.setBounds(x, y, arrowSize.width, arrowSize.height);
    }
}
