package client;

import javax.swing.*;
import java.awt.*;

import shared.Card;

public class PackOpeningWindow extends JFrame {
    /**
     * Method for rendering a display of openedCards
     * @param openedCards The array of cards that have been opened by the user
     */
    public PackOpeningWindow(Card[] openedCards) {
        setTitle("You Opened a Pack!");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardsPanel.setBackground(new Color(240, 240, 240));

        for (Card card : openedCards) {
            cardsPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
