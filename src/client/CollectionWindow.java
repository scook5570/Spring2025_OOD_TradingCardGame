package client;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CollectionWindow extends JFrame {
    private JPanel userInfoPanel, collectionPanel;
    private JScrollPane collectionScrollPane;
    private JButton tradeButton;
    private JLabel statusLabel;
    private JButton galleryButton;
    private JButton homeButton;
    private JPanel bottomPanel;
    private String username;

    public CollectionWindow(String username) {
        this.username = username;
        setTitle("Home");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Sets the frame the full screen size
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Makes it none rezisable after its adjusted to not overtake the tool bar
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setResizable(false);
            }
        });
        setLayout(new GridBagLayout());

        userInfoPanel = new JPanel(new BorderLayout());
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

        // temporary until we can obtain user from database

        // I think we shoudl just obtain the username when we log in and just keep
        // passing it arund the frames
        // we wouldnt even need to validete it anymre and since all usernames should be
        // unique it should be easier to find
        JLabel usernameLabel = new JLabel(this.username);
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
        userInfoPanel.add(usernameLabel);

        // Panel where the cards will be displayed
        collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY);
        collectionPanel.setLayout(new GridBagLayout());
        collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);
        // Stops it from streaching horizontally
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Change scroll speed
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Panel with buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 100, 10));
        bottomPanel.setBackground(getBackground());

        galleryButton = new JButton("Gallery");
        galleryButton.setPreferredSize(new Dimension(100, 50));
        galleryButton.setFocusPainted(false); // Stops the thin square form being drawend around the text

        homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 50));
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(e -> {
            dispose();
            try {
                GameWindow g = new GameWindow();
                g.setVisible(true);
            } catch (IOException gameWindowException) {
                gameWindowException.printStackTrace();
            }
        });

        tradeButton = new JButton("Trade");
        tradeButton.setPreferredSize(new Dimension(100, 50));
        tradeButton.setFocusPainted(false);

        bottomPanel.add(galleryButton);
        bottomPanel.add(homeButton);
        bottomPanel.add(tradeButton);

        GridBagConstraints gbc = new GridBagConstraints();

        // Top panel aligned to the top right
        gbc.gridx = 3; // right side
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(userInfoPanel, gbc);

        // Centered collection panel
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // last item on row
        gbc.weighty = 1; // allocates extra space so nicely cnetered
        gbc.anchor = GridBagConstraints.CENTER;
        add(collectionScrollPane, gbc);

        // Botton panel full width
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4; // spans all columns
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(bottomPanel, gbc);

        setVisible(true);
        // Had to set size after settting visible to get get frame width and height
        // other wise it thinks its 0
        collectionScrollPane.setPreferredSize(new Dimension((int) (getWidth() * 0.7), (int) (getHeight() * 0.7)));
    }

    private void addCards() {
        // TODO: add the cards a user has to collection scroll pane
    }
}
