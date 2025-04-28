package client.objects;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import client.frames.MainFrame;
import client.utils.TCGUtils;

public class MenuButtons extends JPanel {
    private MainFrame parentFrame;

    public MenuButtons(MainFrame parentFrame) {
        super();
        this.parentFrame = parentFrame;
        setLayout(new FlowLayout(FlowLayout.CENTER, 100, 10));
        setBackground(TCGUtils.BACKGROUND_COLOR);

        JButton collectionButton = new JButton("Gallery");
        collectionButton.setPreferredSize(new Dimension(100, 50));
        collectionButton.setFocusPainted(false); // Stops the thin square form being drawend around the text
        // Chnage to collection panel
        collectionButton.addActionListener(e -> this.parentFrame.showPanel(TCGUtils.COLLECTION));

        JButton homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 50));
        homeButton.setFocusPainted(false);
        // Change to home panel
        homeButton.addActionListener(e -> this.parentFrame.showPanel(TCGUtils.HOME));

        JButton tradeButton = new JButton("Trade");
        tradeButton.setPreferredSize(new Dimension(100, 50));
        tradeButton.setFocusPainted(false);
        tradeButton.addActionListener(e -> {
            // TODO: Change to Trading panel
        });

        add(collectionButton);
        add(homeButton);
        add(tradeButton);
    }
}
