package client;

import java.awt.*;

import javax.swing.*;

import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;

public class GameWindow extends JFrame {
    private String username;

    public GameWindow(String username) {
        this.username = username;
        setTitle("Home");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sets the frame the full screen size
        Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(rect);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        PlayerInfoPanel userInfoPanel = new PlayerInfoPanel(this.username);

        add(userInfoPanel);

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
