package client.panels;

import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import merrimackutil.json.types.JSONArray;
import shared.Card;
import shared.MessageSocket;
import shared.messages.CollectionRequest;
import shared.messages.CollectionResponse;

/**
 * CollectionPanel represents the screen where a user's card collection is displayed.
 */
public class CollectionPanel extends TCGPanel {

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

            if (response instanceof CollectionResponse cr) {
                JSONArray collection = cr.getCollection();
                for (int i = 0; i < collection.size(); i++) {
                    var obj = collection.getObject(i);
                    String cardID = obj.getString("cardID");
                    String name = obj.getString("name");
                    double rarity = obj.getDouble("rarity");
                    String image = obj.getString("imageLink");
                    cardList.add(new Card(cardID, name, (int) rarity, image));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Failed to load collection.", "Error", JOptionPane.ERROR_MESSAGE);
            });
        }
        return cardList;
    }
}
