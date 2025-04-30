package client.panels;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import shared.Card;
import client.utils.ArrowHelper;

import javax.swing.*;
import java.awt.*;


/**
 * TradePanel represents the screen where users can initiate and manage trade actions.
 */
public class TradePanel extends TCGPanel {

    private final ArrowHelper arrowHelper;

    /**
     * Constructor for the TradePanel
     *
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public TradePanel(MainFrame parentFrame, String username) {
        super(parentFrame, username);
        arrowHelper = new ArrowHelper();

        // Create the panels for trade history, trade area, and trade requests
        JPanel chatPanel = createSidePanel("Trade History", new Dimension(300, 0), Color.DARK_GRAY);
        JPanel tradeArea = createTradeArea();
        tradeArea.setPreferredSize(new Dimension(500, 0));
        tradeArea.setMinimumSize(new Dimension(500, 0));
        JPanel requestPanel = createSidePanel("Trade Requests", new Dimension(300, 0), Color.LIGHT_GRAY);

        // Combine the side panels and trade area into a container
        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.add(chatPanel, BorderLayout.WEST);
        container.add(tradeArea, BorderLayout.CENTER);
        container.add(requestPanel, BorderLayout.EAST);

        // Add Trade button at the bottom to initiate a trade
        JPanel tradeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton tradeButton = new JButton("Initiate Trade");
        tradeButton.addActionListener(e -> initiateTrade());
        tradeButtonPanel.add(tradeButton);
        container.add(tradeButtonPanel, BorderLayout.SOUTH);

        // Add the entire container as the main component
        addMainComponent(container, true);
    }

    /**
     * Creates a side panel with a title and specified background color and size.
     *
     * @param title The title of the panel
     * @param size  The preferred size of the panel
     * @param bg    The background color of the panel
     * @return A JPanel with the specified title, size, and background color
     */
    private JPanel createSidePanel(String title, Dimension size, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setPreferredSize(size);

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the trade area where cards are displayed for trade.
     * The cards are represented as two Card objects, with arrows to indicate trade offers.
     *
     * @return A JPanel containing the trade area with cards and arrows
     */
    private JPanel createTradeArea() {
        JLayeredPane layered = new JLayeredPane();
        Dimension areaSize = new Dimension(500, 500);
        layered.setPreferredSize(areaSize);

        // Create two sample cards for trade
        Card cardA = new Card("card001", "Fire Dragon", 3, "src/server/cardinfo/images/Fire Dragon.png");
        Card cardB = new Card("card002", "Water Sprite", 2, "src/server/cardinfo/images/Water Sprite.png");

        int cardW = 200, cardH = 280;
        int padX = 50, padY = 20, yGap = 150;

        // Set card positions
        cardA.setBounds(padX, padY, cardW, cardH);
        cardB.setBounds(padX + cardW + 50, padY + yGap, cardW, cardH);

        // Add cards to the layered pane
        layered.add(cardA, JLayeredPane.DEFAULT_LAYER);
        layered.add(cardB, JLayeredPane.DEFAULT_LAYER);

        // Add arrows to indicate the trade offers
        arrowHelper.addArrow(layered, cardA, 225);
        arrowHelper.addArrow(layered, cardB, 45);

        // Wrap the layered pane in a wrapper panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(layered);
        return wrapper;
    }

    /**
     * Initiates the trade process by prompting the user to select a trade partner.
     * After selection, the CollectionPanel is displayed for card selection.
     */
    private void initiateTrade() {
        // Prompt user to select a trading partner
        String[] activeUsers = {"User1", "User2", "User3"};
        String selectedUser = (String) JOptionPane.showInputDialog(
                this,
                "Select a user to trade with:",
                "Choose Trade Partner",
                JOptionPane.PLAIN_MESSAGE,
                null,
                activeUsers,
                activeUsers[0]
        );

        if (selectedUser != null) {
            // Switch to the CollectionPanel for card selection
            CollectionPanel collectionPanel = new CollectionPanel(parentFrame, username, true);
            parentFrame.setPanel(collectionPanel);
            parentFrame.showPanel(TCGUtils.COLLECTION);

            // Optionally store the selected user for further trade logic
            System.out.println("Selected trading partner: " + selectedUser);
        }
    }

    /**
     * Placeholder for obtaining the selected card from the CollectionPanel.
     * In a real implementation, this should be replaced with logic to retrieve the actual selected card.
     *
     * @return The name of the selected card
     */
    private String getSelectedCardFromCollection() {
        return "Fire Dragon"; // Placeholder for actual card selection logic
    }

    /**
     * Simulates waiting for the other user to confirm the trade.
     * This method is a placeholder for future trade confirmation logic.
     */
    private void initiateTradeWaiting() {
        JOptionPane.showMessageDialog(this, "Waiting for the other user to confirm the trade...");
    }
}
