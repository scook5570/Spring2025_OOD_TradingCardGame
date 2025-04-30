package client.frames;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;

import client.panels.*;
import client.utils.TCGUtils;
import merrimackutil.json.types.JSONArray;
import shared.Card;
import shared.MessageSocket;
import shared.messages.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final String currentUser;
    private final Rectangle screenBounds;

    public MainFrame(String username) {
        this.currentUser = username;
        this.screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        setTitle("Trading Card Game");
        setBackground(TCGUtils.BACKGROUND_COLOR);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBounds(screenBounds);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Named panels (added once)
        mainPanel.add(new HomePanel(this, currentUser), TCGUtils.HOME);
        mainPanel.add(new CollectionPanel(this, currentUser), TCGUtils.COLLECTION);
        mainPanel.add(new TradePanel(this, currentUser), TCGUtils.TRADE);
        mainPanel.add(new TradeStatusPanel(this, currentUser), "TradeStatus");

        setContentPane(mainPanel);
        showPanel(TCGUtils.HOME);
        setVisible(true);

        checkIncomingTradeRequests();
    }

    // Named panels (preloaded)
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        revalidate();
        repaint();
    }

    // For dynamic, one-time-use panels (like TradeRequest full-screen view)
    public void showDynamicPanel(JPanel panel, String uniqueName) {
        mainPanel.add(panel, uniqueName);
        cardLayout.show(mainPanel, uniqueName);
        revalidate();
        repaint();
    }

    public String getUsername() {
        return currentUser;
    }

    public Rectangle getRect() {
        return screenBounds;
    }

    public void sendTradeRequest(String recipient, String offeredCardId) {
        if (currentUser == null) return;

        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new TradeRequest(currentUser, recipient, offeredCardId));
            Message response = ms.getMessage();

            if (response instanceof ServerTradeStatus tradeStatus) {
                JOptionPane.showMessageDialog(this, tradeStatus.getMessage(), "Trade Status", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send trade request.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openCollectionInTradeMode(String tradePartner) {
        CollectionPanel tradeCollectionPanel = new CollectionPanel(this, currentUser, true, tradePartner);
        showDynamicPanel(tradeCollectionPanel, "TradeCollection_" + tradePartner);
    }

    private void checkIncomingTradeRequests() {
        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new ViewTradesRequest(currentUser));
            Message response = ms.getMessage();

            if (response instanceof ViewTradesResponse viewResp) {
                JSONArray trades = viewResp.getTrades();

                boolean hasIncoming = false;

                for (int i = 0; i < trades.size(); i++) {
                    JSONArray trade = trades.getArray(i);
                    if (trade != null && trade.size() >= 4) {
                        String type = trade.getString(0);
                        String recipientID = trade.getString(2);
                        if ("request".equals(type) && currentUser.equals(recipientID)) {
                            hasIncoming = true;
                            break;
                        }
                    }
                }

                if (hasIncoming) {
                    JOptionPane.showMessageDialog(this,
                            "You have new trade requests! Go to the Trade screen to view them.",
                            "Trade Notification", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadPanel(String panelName) {
        Component existing = findComponentByName(panelName);
        if (existing != null) {
            mainPanel.remove(existing);
        }

        JPanel refreshed = switch (panelName) {
            case TCGUtils.TRADE -> new TradePanel(this, currentUser);
            case "TradeStatus" -> new TradeStatusPanel(this, currentUser);
            default -> null;
        };

        if (refreshed != null) {
            refreshed.setName(panelName);
            mainPanel.add(refreshed, panelName);
            cardLayout.show(mainPanel, panelName);
            revalidate();
            repaint();
        } else {
            System.err.println("[reloadPanel] Unknown panel: " + panelName);
        }
    }

    private Component findComponentByName(String name) {
        for (Component comp : mainPanel.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }

}
