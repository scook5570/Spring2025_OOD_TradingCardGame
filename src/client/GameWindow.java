package client;

import javax.imageio.ImageIO;
import javax.swing.*;

import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameWindow extends JFrame {
    private JPanel userInfoPanel;
    private JButton tradeButton;
    private JButton galleryButton;
    private JButton homeButton;
    private JPanel bottomPanel;

    public GameWindow() throws IOException {
        setTitle("Home");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sets the frame the full screen size
        Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(rect);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        PlayerInfoPanel userInfoPanel = new PlayerInfoPanel();
        add(userInfoPanel);

        BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));
        Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel picLabel = new JLabel(new ImageIcon(scaledImage));

        // temporary until we can obtain user from database
        JLabel usernameLabel = new JLabel("username");
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));

        picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfoPanel.add(picLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        userInfoPanel.add(usernameLabel);

        // Card carousel
        JPanel carouselPanel = new JPanel();
        carouselPanel.setLayout(new BoxLayout(carouselPanel, BoxLayout.X_AXIS));
        carouselPanel.setBackground(Color.WHITE);

        // 3.5 x 2.5 ratio
        Dimension centerPackSize = new Dimension(250, 375);
        Dimension sidePackSize = new Dimension(210, 315);

        carouselPanel.add(wrapWithContainer(createPackPanel(sidePackSize), sidePackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0)));
        carouselPanel.add(wrapWithContainer(createPackPanel(centerPackSize), centerPackSize));
        carouselPanel.add(Box.createRigidArea(new Dimension(75, 0)));
        carouselPanel.add(wrapWithContainer(createPackPanel(sidePackSize), sidePackSize));

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(carouselPanel);
        add(centerWrapper, BorderLayout.CENTER);

        MenuButtons bottomPanel = new MenuButtons(this);
        
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     * Method to make the pack panel
     * @param size
     * @return
     */
    private JPanel createPackPanel(Dimension size) {
        JPanel pack = new JPanel();
        pack.setPreferredSize(size);
        pack.setBackground(new Color(100, 100, 100));
        pack.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return pack;
    }

    private JPanel wrapWithContainer(JPanel content, Dimension size) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setPreferredSize(size);
        container.setMaximumSize(size);
        container.setMinimumSize(size);
        container.setLayout(new GridBagLayout());
        container.add(content);
        return container;
    }
}
