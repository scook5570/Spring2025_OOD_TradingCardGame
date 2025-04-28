package client.objects;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import client.CollectionWindow;
import client.GameWindow;
import client.utils.TCGUtils;

public class MenuButtons extends JPanel {
    private JButton galleryButton, homeButton, tradeButton;
    private Frame parentFrame;

    public MenuButtons(Frame parentFrame) {
        super();
        setLayout(new FlowLayout(FlowLayout.CENTER, 100, 10));
        setBackground(TCGUtils.BACKGROUND_COLOR);

        galleryButton = new JButton("Gallery");
        galleryButton.setPreferredSize(new Dimension(100, 50));
        galleryButton.setFocusPainted(false); // Stops the thin square form being drawend around the text
        galleryButton.addActionListener(e -> {
            // TODO: Change to gallery panel
            new CollectionWindow("Username");
            parentFrame.dispose();

        });

        homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 50));
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(e -> {
            // TODO: Change to Home panel
            try {
                new GameWindow();
                parentFrame.dispose();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
        });

        tradeButton = new JButton("Trade");
        tradeButton.setPreferredSize(new Dimension(100, 50));
        tradeButton.setFocusPainted(false);
        tradeButton.addActionListener(e -> {
            // TODO: Change to Trading panel
        });

        add(galleryButton);
        add(homeButton);
        add(tradeButton);
    }
}
