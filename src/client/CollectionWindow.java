package client;

import shared.Card;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CollectionWindow extends JFrame {
    private JPanel userInfoPanel, collectionPanel;
    private JScrollPane collectionPane;
    private JButton tradeButton;
    private JLabel statusLabel;

    public CollectionWindow() {
        setTitle("Home");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        int panelWidth = 375;
        int panelHeight = 50;

        int panelX = screenSize.width - panelWidth;
        int panelY = 0;

        userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setBounds(panelX, panelY, panelWidth, panelHeight);
        // userInfoPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        userInfoPanel.setBackground(new Color(217, 217, 217));
        add(userInfoPanel);

        try {
            BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));
            Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
            userInfoPanel.add(picLabel, BorderLayout.WEST);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY);
        collectionPanel.setLayout(new GridBagLayout());
        collectionPane = new JScrollPane(collectionPanel);
        collectionPane.setBackground(Color.GRAY);
        collectionPane.setBounds((int) (screenSize.getWidth() * 0.145), (int) (screenSize.getHeight() * 0.1),
                (int) (screenSize.getWidth() * 0.71), (int) (screenSize.getHeight() * 0.7));
        collectionPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Change scroll speed
        collectionPane.getVerticalScrollBar().setUnitIncrement(16);

        for (int i = 0; i < 20; i++) {
            Card c = new Card("" + i, 1);
            gbc.gridy = Math.floorDiv(i, 4);
            collectionPanel.add(c, gbc);
        }

        add(collectionPane);

    }
}
