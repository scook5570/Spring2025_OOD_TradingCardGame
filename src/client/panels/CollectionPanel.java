package client.panels;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

import client.frames.MainFrame;
import client.utils.TCGUtils;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;
import shared.*;
import shared.messages.*;

/**
 * CollectionPanel represents the screen where a user's card collection is
 * displayed.
 */
public class CollectionPanel extends TCGPanel {
    private ArrayList<String> collection;
    private GridBagConstraints gbcCards;

    /**
     * Constructor for CollectionPanel
     * 
     * @param parentFrame The main application frame
     * @param username    The username of the logged-in player
     */
    public CollectionPanel(MainFrame parentFrame, String username) {
        super(parentFrame, username); // Call the parent TCGPanel constructor
        this.collection = new ArrayList<>();
        this.gbcCards = new GridBagConstraints();
        this.gbcCards.insets = new Insets(3, 3, 3, 3);

        // Create a panel where the collection cards will be displayed
        JPanel collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY);
        collectionPanel.setLayout(new GridBagLayout());

        // Wrap the collection panel in a scroll pane for scrolling functionality
        JScrollPane collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);

        // Disable horizontal scrolling to keep the layout clean
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Increase vertical scroll speed for smoother scrolling
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

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
                // for (int i = 0; i < cards.size(); i++) {
                for (int i = 0; i < 10; i++) {
                    JSONObject card = (JSONObject) cards.get(i);
                    String cardId = card.getString("cardID");
                    String name = card.getString("name");
                    int rarity = card.getInt("rarity");
                    String imageLink = card.getString("imageLink");

                    if (!collection.contains(imageLink)) {
                        collection.add(imageLink);
                        this.gbcCards.gridy = Math.floorDiv(i, 4);
                        collectionPanel.add(new Card(cardId, name, rarity, imageLink), this.gbcCards);
                    }

                    // System.out.println("Card ID: " + card.getString("cardID"));
                    // System.out.println("Name: " + card.getString("name"));
                    // System.out.println("Rarity: " + card.getInt("rarity"));
                    // System.out.println("Image Link: " + card.getString("imageLink"));
                }
            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }
            messageSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the scroll pane to the main content area of the panel
        addMainComponent(collectionScrollPane);
    }
}