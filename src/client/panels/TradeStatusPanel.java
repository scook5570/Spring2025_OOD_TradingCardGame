package client.panels;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import merrimackutil.json.types.JSONArray;
import shared.Card;
import shared.MessageSocket;
import shared.messages.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class TradeStatusPanel extends TCGPanel {

    public TradeStatusPanel(MainFrame parentFrame, String username) {
        super(parentFrame, username);
        setLayout(new BorderLayout());

        // 🔙 Back Button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> parentFrame.reloadPanel(TCGUtils.TRADE)); // ✅ reload TradePanel too

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // 📨 Incoming Requests
        JPanel incomingPanel = new JPanel();
        incomingPanel.setLayout(new BoxLayout(incomingPanel, BoxLayout.Y_AXIS));
        incomingPanel.setBorder(BorderFactory.createTitledBorder("Incoming Requests"));

        // 📤 Outgoing Confirmations
        JPanel outgoingPanel = new JPanel();
        outgoingPanel.setLayout(new BoxLayout(outgoingPanel, BoxLayout.Y_AXIS));
        outgoingPanel.setBorder(BorderFactory.createTitledBorder("Pending Confirmations"));

        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new ViewTradesRequest(username));
            Message response = ms.getMessage();

            if (response instanceof ViewTradesResponse vr) {
                JSONArray trades = vr.getTrades();

                for (int i = 0; i < trades.size(); i++) {
                    JSONArray trade = trades.getArray(i);
                    if (trade == null || trade.size() < 4) continue;

                    String type = trade.getString(0);
                    String requesterID = trade.getString(1);
                    String recipientID = trade.getString(2);
                    String offerCardID = trade.getString(3);
                    String responseCardID = trade.size() > 4 ? trade.getString(4) : null;

                    String tradeKey = requesterID + recipientID;

                    if (type.equals("request") && username.equals(recipientID)) {
                        // Recipient side — respond to trade
                        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        panel.add(new JLabel("From: " + requesterID + " | Offered: " + offerCardID));

                        JButton respondBtn = new JButton("Respond");
                        respondBtn.addActionListener(e -> {
                            Card dummyCard = new Card(offerCardID, "", 0, offerCardID);
                            TradeRequestPanel trp = new TradeRequestPanel(
                                    parentFrame,
                                    requesterID,
                                    dummyCard,
                                    tradeKey,
                                    () -> parentFrame.reloadPanel("TradeStatus"),
                                    false
                            );
                            trp.setName("TradeRequest");
                            parentFrame.showDynamicPanel(trp, "TradeRequest");
                        });

                        panel.add(respondBtn);
                        incomingPanel.add(panel);

                    } else if (type.equals("response") && username.equals(requesterID)) {
                        // Requester side — confirm trade
                        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        panel.add(new JLabel("To: " + recipientID + " | Offered: " + offerCardID + " | Response: " +
                                (responseCardID == null ? "None" : responseCardID)));

                        JButton confirmBtn = new JButton("Confirm");
                        confirmBtn.addActionListener(e -> {
                            int choice = JOptionPane.showConfirmDialog(this, "Confirm this trade?",
                                    "Trade Confirmation", JOptionPane.YES_NO_OPTION);

                            if (choice == JOptionPane.YES_OPTION) {
                                boolean success = sendConfirmation(tradeKey);
                                if (success) {
                                    JOptionPane.showMessageDialog(this, "Trade completed!");
                                    SwingUtilities.invokeLater(() -> {
                                        parentFrame.reloadPanel("TradeStatus");
                                        parentFrame.reloadPanel(TCGUtils.TRADE);
                                    });
                                }
                            }
                        });
                        panel.add(confirmBtn);
                        outgoingPanel.add(panel);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load trades.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server.");
        }

        JScrollPane scroll1 = new JScrollPane(incomingPanel);
        JScrollPane scroll2 = new JScrollPane(outgoingPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll1, scroll2);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);
    }

    private boolean sendConfirmation(String tradeKey) {
        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new TradeConfirmation(tradeKey, true));
            Message response = ms.getMessage();
            if (response instanceof ServerTradeStatus sts) {
                JOptionPane.showMessageDialog(this, sts.getMessage(),
                        sts.getStatus() ? "Success" : "Error",
                        sts.getStatus() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                return sts.getStatus();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending trade confirmation.");
        }
        return false;
    }
}
