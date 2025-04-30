package client.panels;

import java.awt.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import client.frames.MainFrame;
import client.utils.TCGUtils;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;
import shared.*;
import shared.messages.*;

/**
 * CollectionPanel represents the screen where a user's card collection is displayed.
 */
public class CollectionPanel extends TCGPanel {
    private JPanel display;
    private ArrayList<String> collection;
    private GridBagConstraints gbcCards;

    private final boolean isTradeMode;
    private final String tradePartner;
    private Card selectedCard = null;

    public CollectionPanel(MainFrame parentFrame, String username) {
        this(parentFrame, username, false, null);
    }

    public CollectionPanel(MainFrame parentFrame, String username, boolean isTradeMode, String tradePartner) {
        super(parentFrame, username);
        this.isTradeMode = isTradeMode;
        this.tradePartner = tradePartner;
        setName("Collection");

        List<Card> userCards = fetchUserCards(username);

        JPanel collectionPanel = new JPanel(new GridBagLayout());
        collectionPanel.setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        int columns = 3;

        for (int i = 0; i < userCards.size(); i++) {
            Card card = userCards.get(i);
            card.setFocusable(true);

            if (isTradeMode) {
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectedCard = card;
                        highlightSelectedCard(collectionPanel, card);

                        // If initiating trade
                        if (tradePartner != null) {
                            SwingUtilities.invokeLater(() -> {
                                int confirm = JOptionPane.showConfirmDialog(
                                        CollectionPanel.this,
                                        "Trade this card?",
                                        "Confirm Trade",
                                        JOptionPane.YES_NO_OPTION
                                );
                                if (confirm == JOptionPane.YES_OPTION) {
                                    parentFrame.sendTradeRequest(tradePartner, card.getCardID());
                                    parentFrame.reloadPanel(TCGUtils.TRADE);
                                    JOptionPane.showMessageDialog(
                                            CollectionPanel.this,
                                            "Waiting for other user to accept trade..."
                                    );
                                }
                            });
                        }
                    }
                });
            }

            gbc.gridx = i % columns;
            gbc.gridy = i / columns;
            collectionPanel.add(card, gbc);
        }

        JScrollPane scrollPane = new JScrollPane(collectionPanel);
        scrollPane.setBackground(Color.GRAY);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Wrap scroll + confirm (if in selection mode)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);

        if (isTradeMode && tradePartner == null) {
            JButton confirm = new JButton("Confirm Selection");
            confirm.addActionListener(e -> {
                if (selectedCard == null) {
                    JOptionPane.showMessageDialog(this, "Please select a card to respond with.");
                    return;
                }

                // Callback would normally go here
                JOptionPane.showMessageDialog(this,
                        "Selected card: " + selectedCard.getCardID() + " (implement trade callback here)");
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(confirm);
            wrapper.add(bottom, BorderLayout.SOUTH);
        }

        addMainComponent(wrapper);
    }

    /**
     * Highlights the selected card visually (optional enhancement).
     */
    private void highlightSelectedCard(JPanel container, Card selected) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof Card) {
                comp.setBackground(comp == selected ? Color.YELLOW : Color.LIGHT_GRAY);
            }
        }
    }

    public String getSelectedCardID() {
        return selectedCard != null ? selectedCard.getCardID() : null;
    }

    private List<Card> fetchUserCards(String username) {
        List<Card> cardList = new ArrayList<>();
        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new CollectionRequest(username));
            var response = ms.getMessage();
        // Add the scroll pane to the main content area of the panel
        addMainComponent(collectionScrollPane);

        loadCards();
    }

    private void loadCards() {
        try {
            System.out.println("Retrieving collection...");
            MessageSocket messageSocket = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT));
            CollectionRequest collectionRequest = new CollectionRequest(username);
            messageSocket.sendMessage(collectionRequest);

            Message response = messageSocket.getMessage();
            if (response instanceof CollectionResponse) {
                CollectionResponse collectionResponse = (CollectionResponse) response;
                JSONArray cards = collectionResponse.getCollection();
                System.out.println("Your collection contains the following cards:");
                for (int i = 0; i < cards.size(); i++) {
                    JSONObject card = (JSONObject) cards.get(i);
                    String cardId = card.getString("cardID");
                    String name = card.getString("name");
                    int rarity = card.getInt("rarity");
                    String imageLink = card.getString("imageLink");

                    if (!collection.contains(imageLink)) {
                        collection.add(imageLink);
                        this.gbcCards.gridy = Math.floorDiv(collection.size() - 1, 4);
                        this.display.add(new Card(cardId, name, rarity, imageLink), this.gbcCards);
                    }
                }
            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }
            messageSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshCollection() {
        this.display.removeAll();
        this.collection = new ArrayList<>();
        this.loadCards();
    }

}
