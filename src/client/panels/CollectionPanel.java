package client.panels;

import java.awt.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import shared.Card;

/**
 * CollectionPanel represents the screen where a user's card collection is
 * displayed.
 */
public class CollectionPanel extends TCGPanel {

    /**
     * Constructor for CollectionPanel
     * 
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public CollectionPanel(MainFrame parentFrame, String username) {
        this(parentFrame, username, false); // default to non-trade mode
    }

    /**
     * Constructor for CollectionPanel (with trade mode option)
     *
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     * @param isTradeMode Whether this panel is being used to select a card for trade
     */
    public CollectionPanel(MainFrame parentFrame, String username, boolean isTradeMode) {
        super(parentFrame, username); // Call the parent TCGPanel constructor

        // Create a panel where the collection cards will be displayed
        JPanel collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY); 
        collectionPanel.setLayout(new GridBagLayout()); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between cards

        // Add 9 dummy cards in a 3x3 grid
        int cardCount = 9;
        int columns = 3;

        for (int i = 0; i < cardCount; i++) {
            String dummyName = "Card" + (i + 1);
            Card card = new Card("ID" + i, dummyName, (i % 5) + 1, dummyName);

            if (isTradeMode) {
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(
                                null,
                                "Trade this card?",
                                "Confirm Trade",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            parentFrame.showPanel(TCGUtils.TRADE);
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Waiting for other user to accept trade..."
                            );
                        }
                    }
                });
            }

            gbc.gridx = i % columns;
            gbc.gridy = i / columns;
            collectionPanel.add(card, gbc);
        }

        // Wrap the collection panel in a scroll pane for scrolling functionality
        JScrollPane collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);

        // Disable horizontal scrolling to keep the layout clean
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Increase vertical scroll speed for smoother scrolling
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add the scroll pane to the main content area of the panel
        addMainComponent(collectionScrollPane);
    }
}