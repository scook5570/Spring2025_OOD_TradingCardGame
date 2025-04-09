package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameWindow extends JFrame {
    private JPanel userInfoPanel;
    private JButton tradeButton;
    private JLabel statusLabel;

    public GameWindow() throws IOException {
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
        userInfoPanel.setBackground(new Color(217, 217, 217));
        add(userInfoPanel);


        BufferedImage myPicture = ImageIO.read(new File("assets/user.png"));
        Image scaledImage = myPicture.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel picLabel = new JLabel(new ImageIcon(scaledImage));
        userInfoPanel.add(picLabel, BorderLayout.WEST);
    }
}

