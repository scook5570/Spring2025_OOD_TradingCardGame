package client.utils;

import merrimackutil.json.types.JSONArray;
import shared.Card;
import shared.MessageSocket;
import shared.messages.CollectionRequest;
import shared.messages.CollectionResponse;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for shared constants and configurations used across the TCG client application.
 */
public class TCGUtils {

    public static final String SERVERADDRESS = "localhost";
    public static final int PORT = 5000;

    public static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    public static final Color USER_INFO_BC = new Color(217, 217, 217);

    public static final String HOME = "Home";
    public static final String COLLECTION = "collection";
    public static final String TRADE = "Trade";

    /**
     * Fetches the full card collection for a given user from the server.
     *
     * @param username the user whose cards to fetch
     * @return list of Card objects
     */
    public static List<Card> fetchUserCards(String username) {
        List<Card> cardList = new ArrayList<>();
        try (MessageSocket ms = new MessageSocket(new Socket(SERVERADDRESS, PORT))) {
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

    /**
     * Fetches a specific card by ID from the given user's collection.
     *
     * @param username the user whose collection to search
     * @param cardID   the ID of the card to find
     * @return the Card object if found, or a fallback "Unknown" card
     */
    public static Card fetchCardByID(String username, String cardID) {
        return fetchUserCards(username).stream()
                .filter(c -> c.getCardID().equals(cardID))
                .findFirst()
                .orElse(new Card(cardID, "Unknown", 0, ""));
    }
}
