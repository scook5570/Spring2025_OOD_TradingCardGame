package client;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.Card;
import shared.MessageSocket;
import shared.messages.Message;
import shared.messages.PackRequest;
import shared.messages.PackResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class GameWindow extends JFrame {

    private JPanel[] packPanels;
    private JPanel carouselPanel;
    private int currentIndex = 1;

    private final String username;
    String serverAddress = "localhost";
    int port = 5000;

    public GameWindow(String username) throws IOException {
        this.username = username;
        setTitle("Home");
        setupWindow();

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(new Color(217, 217, 217));

        // Add user info panel
        topWrapper.add(createUserInfoPanel(), BorderLayout.EAST);
        add(topWrapper, BorderLayout.NORTH);

        add(createCarouselPanel(), BorderLayout.CENTER);
        // Panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 10));
        bottomPanel.setBackground(new Color(217, 217, 217));

        JButton galleryButton = new JButton("Gallery");
        galleryButton.addActionListener(e -> {
            CollectionWindow collectionWindow = new CollectionWindow(username);
            collectionWindow.setVisible(true);
        });
        galleryButton.setPreferredSize(new Dimension(100, 50));
        galleryButton.setFocusPainted(false);

        JButton homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 50));
        homeButton.setFocusPainted(false);

        JButton tradeButton = new JButton("Trade");
        tradeButton.setPreferredSize(new Dimension(100, 50));
        tradeButton.setFocusPainted(false);

        bottomPanel.add(galleryButton);
        bottomPanel.add(homeButton);
        bottomPanel.add(tradeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * A method to set up the frame
     */
    private void setupWindow() {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(bounds);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    /**
     * A method which creates the user info panel
     * @return Panel userInfoPanel
     */
    // TODO: Make userInfoPanel an object that can be used by collection window and game window
    private JPanel createUserInfoPanel() {
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setPreferredSize(new Dimension(375, 50));
        userInfoPanel.setBackground(new Color(217, 217, 217));
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.X_AXIS));

        try {
            // TODO: Get correct profile image for user
            BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));
            Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            userInfoPanel.add(picLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        userInfoPanel.add(usernameLabel);

        return userInfoPanel;
    }

    /**
     * A method which creates a carousel panel to contain the card carousel
     * @return Panel fullWrapper
     */
    private JPanel createCarouselPanel() {
        // Create the array of panels for the carousel
        packPanels = new JPanel[3];
        Dimension centerPackSize = new Dimension(200, 300);
        Dimension sidePackSize = new Dimension(160, 240);

        packPanels[0] = createPackPanel(sidePackSize, Color.RED);
        packPanels[1] = createPackPanel(centerPackSize, Color.GREEN);
        packPanels[2] = createPackPanel(sidePackSize, Color.BLUE);

        carouselPanel = new JPanel();
        carouselPanel.setLayout(new BoxLayout(carouselPanel, BoxLayout.X_AXIS));
        carouselPanel.setBackground(new Color(217, 217, 217));
        renderCarousel();

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(217, 217, 217));
        centerWrapper.add(carouselPanel);

        Font largeFont = new Font("Arial", Font.BOLD, 50);

        // These may have to be adjusted based on compatibility, we'll see
        JButton leftButton = new JButton("◀");
        createNavButtons(largeFont, leftButton);

        leftButton.addActionListener(e -> {
            rotateLeft();
            renderCarousel();
        });

        JButton rightButton = new JButton("▶");
        createNavButtons(largeFont, rightButton);

        rightButton.addActionListener(e -> {
            rotateRight();
            renderCarousel();
        });

        // Create a wrapper panel that holds left button, carousel, and right button
        JPanel fullWrapper = new JPanel(new BorderLayout());
        fullWrapper.setBackground(new Color(217, 217, 217));
        fullWrapper.add(leftButton, BorderLayout.WEST);
        fullWrapper.add(centerWrapper, BorderLayout.CENTER);
        fullWrapper.add(rightButton, BorderLayout.EAST);

        return fullWrapper;
    }

    /**
     * A method which
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
     * The sizing of the pack is determined by its index, center pack should always be the biggest
     * @param index Index of the current pack
     * @param size Necessary size of the pack
     * @return Panel panel
     */
    private JPanel getSizedPack(int index, Dimension size) {
        JPanel packPanel = packPanels[index];
        packPanel.setPreferredSize(size);
        // Each packPanel basically works as a button for the user to press
        packPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Prompt user to confirm opening the pack
                int response = JOptionPane.showConfirmDialog(GameWindow.this, "Would you like to open this pack?", "Open Pack", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    openPack(index);
                }
            }
        });
        return packPanel;
    }

    /**
     * A method for opening and rendering packs
     * @param packIndex The current index of the selected pack
     */
    private void openPack(int packIndex) {
        System.out.println("openPack called with pack index: " + packIndex);

        try {
            System.out.println("Opening a pack...");

            MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
            System.out.println("Connected to server at " + serverAddress + ":" + port);

            PackRequest packRequest = new PackRequest(username, "PackNamePlaceholder", 5);
            messageSocket.sendMessage(packRequest);
            System.out.println("Pack request sent to server");

            Message response = messageSocket.getMessage();
            System.out.println("Received response: " + response);

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

                    System.out.println("Card ID: " + cardID);
                    System.out.println("Name: " + name);
                    System.out.println("Rarity: " + rarity);
                    System.out.println("Image Link: " + image);

                    openedCards[i] = new Card(cardID, name, rarity, image);
                }

                SwingUtilities.invokeLater(() -> new PackOpeningWindow(openedCards));

            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error while opening pack: " + ex.getMessage());
        }
    }

    /**
     * Helper method to set the spacing of certain panels
     * @param content Panel that needs to be wrapped
     * @param size The size of the container
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

    /**
     * A method to create the panel for a pack
     * @param size The size of the pack
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
}
