package client.panels;

import client.frames.MainFrame;
import client.utils.ArrowHelper;
import client.utils.TCGUtils;
import merrimackutil.json.types.JSONArray;
import shared.Card;
import shared.MessageSocket;
import shared.messages.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TradePanel extends TCGPanel {

    private final ArrowHelper arrowHelper;
    private final JPanel requestPanel;
    private final List<TradeRequest> activeRequests = new ArrayList<>();
    private String pendingTradePartner;

    public TradePanel(MainFrame parentFrame, String username) {
        super(parentFrame, username);
        arrowHelper = new ArrowHelper();

        JPanel chatPanel = createSidePanel("Trade History", new Dimension(300, 0), Color.DARK_GRAY);
        JPanel tradeArea = createTradeArea();
        tradeArea.setPreferredSize(new Dimension(500, 0));

        requestPanel = new JPanel();
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
        requestPanel.setBackground(Color.LIGHT_GRAY);
        requestPanel.setPreferredSize(new Dimension(300, 0));

        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.add(chatPanel, BorderLayout.WEST);
        container.add(tradeArea, BorderLayout.CENTER);
        container.add(requestPanel, BorderLayout.EAST);
        container.setOpaque(false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton tradeButton = new JButton("Initiate Trade");
        JButton statusButton = new JButton("View Trade Status");

        tradeButton.addActionListener(e -> initiateTrade());

        statusButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> parentFrame.reloadPanel("TradeStatus"));
        });

        buttonPanel.add(tradeButton);
        buttonPanel.add(statusButton);
        buttonPanel.setOpaque(false);

        container.add(buttonPanel, BorderLayout.SOUTH);

        addMainComponent(container, true);

        loadIncomingRequests(); // ⬅ loads "Incoming Requests" panel on the right
    }

    private JPanel createSidePanel(String title, Dimension size, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setPreferredSize(size);
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(new JPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTradeArea() {
        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(500, 500));

        List<Card> userCards = fetchUserCards(getUsername());

        if (userCards.size() < 2) {
            JLabel noCards = new JLabel("Not enough cards to show sample trade.");
            JPanel wrapper = new JPanel();
            wrapper.add(noCards);
            return wrapper;
        }

        Card cardA = userCards.get(0);
        Card cardB = userCards.get(1);

        cardA.setBounds(50, 20, 200, 280);
        cardB.setBounds(300, 170, 200, 280);

        layered.add(cardA, JLayeredPane.DEFAULT_LAYER);
        layered.add(cardB, JLayeredPane.DEFAULT_LAYER);
        arrowHelper.addArrow(layered, cardA, 225);
        arrowHelper.addArrow(layered, cardB, 45);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(layered);
        return wrapper;
    }

    private void initiateTrade() {
        String[] partners = fetchActiveUsers(getUsername());
        if (partners.length == 0) {
            JOptionPane.showMessageDialog(this, "No other users online.");
            return;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this, "Select a user to trade with:", "Choose Trade Partner",
                JOptionPane.PLAIN_MESSAGE, null, partners, partners[0]);

        if (selected == null) return;

        pendingTradePartner = selected;

        CollectionPanel tradePanel = new CollectionPanel(parentFrame, getUsername(), true, pendingTradePartner);
        parentFrame.showDynamicPanel(tradePanel, "TradeCollection_" + pendingTradePartner);
    }

    private void loadIncomingRequests() {
        activeRequests.clear();
        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new ViewTradesRequest(getUsername()));
            var r = ms.getMessage();
            if (r instanceof ViewTradesResponse vr) {
                JSONArray arr = vr.getTrades();
                for (int i = 0; i < arr.size(); i++) {
                    JSONArray t = arr.getArray(i);
                    if (t.size() >= 4 && "request".equals(t.getString(0))
                            && getUsername().equals(t.getString(2))) {
                        activeRequests.add(new TradeRequest(t.getString(1), t.getString(2), t.getString(3)));
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        refreshRequestPanel();
    }

    private void refreshRequestPanel() {
        requestPanel.removeAll();
        for (TradeRequest req : activeRequests) {
            String tradeKey = req.getRequesterID() + req.getRecipientID();
            TradeRequestPanel item = new TradeRequestPanel(
                    parentFrame,
                    req.getRequesterID(),
                    new Card(req.getOfferCardID(), "", 0, req.getOfferCardID()),
                    tradeKey,
                    () -> {
                        loadIncomingRequests(); // refresh this list after accept/deny
                        parentFrame.reloadPanel(TCGUtils.TRADE); // ensure panel refreshes visually
                    },
                    true
            );
            requestPanel.add(item);
        }
        requestPanel.revalidate();
        requestPanel.repaint();
    }

    private String[] fetchActiveUsers(String self) {
        try (MessageSocket ms = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT))) {
            ms.sendMessage(new UserListRequest());
            var r = ms.getMessage();
            if (!(r instanceof UserListResponse)) return new String[0];
            JSONArray a = ((UserListResponse) r).getUsers();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < a.size(); i++) {
                String u2 = a.getString(i);
                if (!u2.equals(self)) list.add(u2);
            }
            return list.toArray(new String[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new String[0];
        }
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
        }

        return cardList;
    }
}
