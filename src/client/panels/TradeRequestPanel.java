package client.panels;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import shared.Card;
import shared.MessageSocket;
import shared.messages.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

import static client.utils.TCGUtils.PORT;
import static client.utils.TCGUtils.SERVERADDRESS;

public class TradeRequestPanel extends JPanel {

    private final String tradeKey;

    public TradeRequestPanel(
            MainFrame parentFrame,
            String fromUser,
            Card proposedCard,
            String tradeKey,
            Runnable onDecision,
            boolean isListItem
    ) {
        this.tradeKey = tradeKey;
        setName("TradeRequest"); // Important for reloadPanel cleanup

        if (isListItem) {
            // List mode (compact card view)
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.GRAY);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setMaximumSize(new Dimension(300, 80));

            JLabel label = new JLabel(fromUser + " offers " + proposedCard.getCardID());
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(label);

            Card thumb = proposedCard;
            thumb.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(thumb);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    TradeRequestPanel fullView = new TradeRequestPanel(
                            parentFrame, fromUser, proposedCard, tradeKey, onDecision, false
                    );
                    fullView.setName("TradeRequest");
                    parentFrame.showDynamicPanel(fullView, "TradeRequest");
                }
            });

        } else {
            // Fullscreen response view
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            Card realCard = TCGUtils.fetchCardByID(fromUser, proposedCard.getCardID());

            JPanel center = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;

            JLabel lbl = new JLabel(fromUser + " offers: " + realCard.getName() + " (Rarity " + realCard.getRarity() + ")");
            lbl.setFont(new Font("Arial", Font.BOLD, 16));
            center.add(lbl, gbc);

            gbc.gridy = 1;
            ImageIcon icon = new ImageIcon(realCard.getImage());
            center.add(new JLabel(icon), gbc);

            gbc.gridy = 2;
            JPanel buttons = new JPanel();
            JButton accept = new JButton("Accept");
            JButton deny = new JButton("Deny");
            buttons.add(accept);
            buttons.add(deny);
            center.add(buttons, gbc);

            add(center, BorderLayout.CENTER);

            // Back button
            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> parentFrame.reloadPanel("TradeStatus"));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(backButton);
            add(top, BorderLayout.NORTH);

            // Deny trade
            deny.addActionListener(e -> {
                sendTradeResponse(null);
                SwingUtilities.invokeLater(() -> parentFrame.reloadPanel("TradeStatus"));
            });

            // Accept trade
            accept.addActionListener(e -> {
                CollectionPanel collectionPanel = new CollectionPanel(parentFrame, parentFrame.getUsername(), true, null);
                int result = JOptionPane.showConfirmDialog(
                        this,
                        collectionPanel,
                        "Select a card to trade back",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                String selectedCardID = collectionPanel.getSelectedCardID();
                if (result == JOptionPane.OK_OPTION && selectedCardID != null) {
                    sendTradeResponse(selectedCardID);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Trade response sent.");
                        parentFrame.reloadPanel("TradeStatus");
                        parentFrame.reloadPanel(TCGUtils.TRADE);
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "You must select a card to respond with.");
                }
            });

        }
    }

    private void sendTradeResponse(String responseCardID) {
        try (MessageSocket ms = new MessageSocket(new Socket(SERVERADDRESS, PORT))) {
            ms.sendMessage(new TradeResponse(true, tradeKey, responseCardID));
            var msg = ms.getMessage();
            if (msg instanceof ServerTradeStatus sts) {
                JOptionPane.showMessageDialog(this,
                        sts.getMessage(),
                        sts.getStatus() ? "Success" : "Error",
                        sts.getStatus() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Trade failed due to server error.");
        }
    }
}
