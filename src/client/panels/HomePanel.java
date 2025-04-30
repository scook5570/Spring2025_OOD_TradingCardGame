package client.panels;

import java.awt.*;
import java.awt.event.*;
import java.net.Socket;

import javax.swing.*;

import client.*;
import client.frames.MainFrame;
import client.frames.PackOpeningWindow;
import client.utils.TCGUtils;
import merrimackutil.json.types.*;
import shared.Card;
import shared.MessageSocket;
import shared.messages.*;

/**
 * HomePanel represents the home screen shown after login.
 * It contains a carousel-like display of card packs.
 */
public class HomePanel extends TCGPanel {

    private JPanel[] packPanels;

    private MyMouseListener[] mouseListeners;

    private JPanel carouselPanel;
    private int currentIndex = 1;

    /**
     * Constructor for HomePanel
     * 
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public HomePanel(MainFrame parentFrame, String username) {
        super(parentFrame, username);


        // Create the card carousel panel and add the entire centerWrapper to the main
        // panel

        addMainComponent(createCarouselPanel(), true);
    }

    /**
     * A method which creates a carousel panel to contain the card carousel
     * 
     * @return Panel fullWrapper
     */
    private JPanel createCarouselPanel() {
        // Create the array of panels for the carousel
        packPanels = new JPanel[3];
        mouseListeners = new MyMouseListener[3];

        Dimension centerPackSize = new Dimension(200, 300);
        Dimension sidePackSize = new Dimension(160, 240);

        packPanels[0] = createPackPanel(sidePackSize, Color.RED);
        packPanels[1] = createPackPanel(centerPackSize, Color.GREEN);
        packPanels[2] = createPackPanel(sidePackSize, Color.BLUE);

        carouselPanel = new JPanel();
        carouselPanel.setLayout(new BoxLayout(carouselPanel, BoxLayout.X_AXIS));

        //carouselPanel.setBackground(new Color(217, 217, 217));
        carouselPanel.setOpaque(false);
        renderCarousel();

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        //centerWrapper.setBackground(new Color(217, 217, 217));
        centerWrapper.setOpaque(false);

        centerWrapper.add(carouselPanel);

        Font largeFont = new Font("Arial", Font.BOLD, 50);

        // These may have to be adjusted based on compatibility, we'll see

        JButton leftButton = new JButton("<");

        createNavButtons(largeFont, leftButton);

        leftButton.addActionListener(e -> {
            rotateLeft();
            renderCarousel();
        });


        JButton rightButton = new JButton(">");
        createNavButtons(largeFont, rightButton);

        rightButton.addActionListener(e -> {
            rotateRight();
            renderCarousel();
        });

        // Create a wrapper panel that holds left button, carousel, and right button
        JPanel fullWrapper = new JPanel(new BorderLayout());

        //fullWrapper.setBackground(new Color(217, 217, 217));
        fullWrapper.setOpaque(false);
        fullWrapper.add(leftButton, BorderLayout.WEST);
        fullWrapper.add(centerWrapper, BorderLayout.CENTER);
        fullWrapper.add(rightButton, BorderLayout.EAST);

        return fullWrapper;

    }

    /**
     * A method which
     * 
     * @param largeFont Font of choice for the aesthetics
     * @param navButton The navigation button either left or right
     */
    private void createNavButtons(Font largeFont, JButton navButton) {
        navButton.setBackground(new Color(217, 217, 217));
        navButton.setBorder(BorderFactory.createEmptyBorder());
        navButton.setContentAreaFilled(false);
        navButton.setFocusPainted(false);
        navButton.setOpaque(false);
        navButton.setPreferredSize(new Dimension(100, 100));
        navButton.setFont(largeFont);
    }

    /**
     * Helper method for carousel rotation to the left
     */
    private void rotateLeft() {
        currentIndex = (currentIndex + 1) % packPanels.length;
    }

    /**
     * Helper method for carousel rotation to the right
     */
    private void rotateRight() {
        currentIndex = (currentIndex - 1 + packPanels.length) % packPanels.length;
    }

    /**
     * A method to render the carousel with specific layout
     */
    private void renderCarousel() {
        for (int i = 0; i < this.packPanels.length; i++) {
            this.packPanels[i].removeMouseListener(this.mouseListeners[i]);
        }
        carouselPanel.removeAll();

        Dimension centerPackSize = new Dimension(250, 375);
        Dimension sidePackSize = new Dimension(210, 315);

        carouselPanel.add(wrapWithContainer(getSizedPack((currentIndex + 2) % 3, sidePackSize), sidePackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0)));
        carouselPanel.add(wrapWithContainer(getSizedPack(currentIndex, centerPackSize), centerPackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0)));
        carouselPanel.add(wrapWithContainer(getSizedPack((currentIndex + 1) % 3, sidePackSize), sidePackSize));

        carouselPanel.revalidate();
        carouselPanel.repaint();
    }

    /**
     * The sizing of the pack is determined by its index, center pack should always
     * be the biggest
     * 
     * @param index Index of the current pack
     * @param size  Necessary size of the pack
     * @return Panel panel
     */
    private JPanel getSizedPack(int index, Dimension size) {
        JPanel packPanel = packPanels[index];
        packPanel.setPreferredSize(size);
        // Each packPanel basically works as a button for the user to press
        this.mouseListeners[index] = new MyMouseListener(this, index);
        packPanel.addMouseListener(this.mouseListeners[index]);
        return packPanel;
    }

    /**
     * A method for opening and rendering packs
     * 
     * @param packIndex The current index of the selected pack
     */
    public void openPack(int packIndex) {
        System.out.println("openPack called with pack index: " + packIndex);
        try (MessageSocket messageSocket = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            System.out.println("Opening a pack...");
            PackRequest packRequest = new PackRequest(username, "PackNamePlaceholder", 5);
            messageSocket.sendMessage(packRequest);
            Message response = messageSocket.getMessage();
            if (response instanceof PackResponse packResponse) {
                JSONArray cards = packResponse.getCards();
                System.out.println("You opened a pack with the following cards:");
                // Create an array of Cards
                Card[] openedCards = new Card[cards.size()];
                for (int i = 0; i < cards.size(); i++) {
                    JSONObject card = (JSONObject) cards.get(i);
                    String cardID = card.getString("cardID");
                    String name = card.getString("name");
                    int rarity = card.getInt("rarity");
                    String image = card.getString("imageLink");
                    openedCards[i] = new Card(cardID, name, rarity, image);
                }
                new PackOpeningWindow(openedCards);
            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error while opening pack: " + ex.getMessage());
        }
    }

    /**
     * A method to create the panel for a pack
     * 
     * @param size  The size of the pack
     * @param color The color of the pack (for debugging)
     * @return The pack panel
     */

    private JPanel createPackPanel(Dimension size, Color color) {
        JPanel pack = new JPanel();
        pack.setPreferredSize(size);
        pack.setBackground(color);
        pack.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return pack;
    }

    /**
     * Helper method to set the spacing of certain panels
     * 
     * @param content Panel that needs to be wrapped
     * @param size    The size of the container
     * @return A wrapper container
     */
    private JPanel wrapWithContainer(JPanel content, Dimension size) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setPreferredSize(size);
        container.setLayout(new GridBagLayout());
        container.add(content);
        return container;
    }
}

class MyMouseListener extends MouseAdapter {
    private HomePanel parentPanel;
    private int index;

    public MyMouseListener(HomePanel parentPanel, int index) {
        this.parentPanel = parentPanel;
        this.index = index;
    }

    // Each packPanel basically works as a button for the user to press
    public void mouseClicked(MouseEvent e) {
        // Prompt user to confirm opening the pack
        int response = JOptionPane.showConfirmDialog(this.parentPanel.parentFrame, "Would you like to open this pack?",
                "Open Pack", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            this.parentPanel.openPack(index);
        }
    }
}
