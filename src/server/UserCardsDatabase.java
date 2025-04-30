package server;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.HashMap;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import shared.Card;

public class UserCardsDatabase implements JSONSerializable {
    private HashMap<String, JSONArray> cards; // username -> cardID
    private File file;

    public UserCardsDatabase(File file) {
        this.file = file;
        if (!file.exists() || file.length() == 0) {
            try {
                file.getParentFile().mkdir(); // Ensure directory exists
                if (!file.createNewFile()) {
                    System.err.println("Could not create users.json file");
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                System.err.println("Error creating users file: " + e.getMessage());
            }
            cards = new HashMap<>();
            return;
        }

        try {
            // Read and deserialize JSON data
            deserialize(JsonIO.readArray(this.file));
        } catch (Exception e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
    }

    /**
     * Add a user to the database with empty cards
     * 
     * @param username
     * @throws InvalidObjectException
     */
    public void addUser(String username) throws InvalidObjectException {
        if (cards == null) {
            cards = new HashMap<>();
        }
        if (cards.containsKey(username)) {
            throw new InvalidObjectException("User already exists in the database");
        }
        cards.put(username, new JSONArray());
        save(); // Save the credentials to the file
    }

    /**
     * Add cards to a user
     * 
     * @param username
     * @throws InvalidObjectException
     */
    public void addCards(String username, JSONArray cardIDs) throws InvalidObjectException {
        if (cards == null) {
            cards = new HashMap<>();
        }
        if (!cards.containsKey(username)) {
            throw new InvalidObjectException("User does not exist in the database");
        }
        cards.get(username).addAll(cardIDs); // i love this function
        save(); // Save the credentials to the file
    }

    /**
     * Removes a user from the database
     * 
     * @param username
     * @throws InvalidObjectException
     */
    public void removeUser(String username) throws InvalidObjectException {
        if (cards == null) {
            return;
        }
        if (!cards.containsKey(username)) {
            throw new InvalidObjectException("User does not exist in the database");
        }
        cards.remove(username);
        save(); // Save the credentials to the file
    }

    /**
     * Removes a card from a user
     */
    public void removeCard(String username, String cardID) throws InvalidObjectException {
        if (cards == null) {
            return;
        }
        if (!cards.containsKey(username)) {
            throw new InvalidObjectException("User does not exist in the database");
        }
        JSONArray userCards = cards.get(username);
        System.out.println("User's current cards: " + userCards); // Debugging line
        System.out.println("Card to remove: " + cardID); // Debugging line

        boolean cardFound = false;

        // Iterate backwards to avoid issues with modifying the list during iteration
        for (int i = userCards.size() - 1; i >= 0; i--) {
            Object card = userCards.get(i);
            if (card instanceof JSONObject) {
                JSONObject cardObject = (JSONObject) card;
                if (cardObject.getString("cardID").equals(cardID)) {
                    userCards.remove(i); // Remove the card
                    cardFound = true;
                    break;
                }
            } else if (card instanceof String && card.equals(cardID)) {
                userCards.remove(i); // Remove the card
                cardFound = true;
                break;
            }
        }

        if (!cardFound) {
            System.out.println("Card " + cardID + " does not exist for user " + username); // Debugging line
            throw new InvalidObjectException("Card does not exist for user");
        }

        save(); // Save the credentials to the file
    }

    /**
     * Get the cards of a user
     * 
     * @param username
     * @return JSONArray of cardIDs
     * @throws InvalidObjectException
     */
    public JSONArray getUserCards(String username) throws InvalidObjectException {
        if (cards == null) {
            return new JSONArray();
        }
        if (!cards.containsKey(username)) {
            throw new InvalidObjectException("User does not exist in the database");
        }
        return cards.get(username);
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (jsonType instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonType;
            cards = new HashMap<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray userArray = (JSONArray) jsonArray.get(i);
                String username = userArray.getString(0);
                JSONArray cardIDs = userArray.getArray(1);
                cards.put(username, cardIDs);
            }
        } else {
            throw new InvalidObjectException("Invalid JSON type for UserCardsDatabase");
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONArray jsonArray = new JSONArray();
        for (String username : cards.keySet()) {
            JSONArray userArray = new JSONArray();
            userArray.add(username);
            userArray.add(cards.get(username));
            jsonArray.add(userArray);
        }
        return jsonArray;
    }

    /**
     * Writes the database to the file
     */
    public void save() {
        try {
            JsonIO.writeFormattedObject(this, file);
        } catch (Exception e) {
            System.err.println("Error writing users file: " + e.getMessage());
        }
    }

    public void addCard(String requesterID, String responseCardID, String name, int rarity, String imageLink) throws InvalidObjectException {
        if (cards == null) {
            cards = new HashMap<>();
        }

        // Check if the user exists, if not, create an empty list of cards for them
        if (!cards.containsKey(requesterID)) {
            cards.put(requesterID, new JSONArray());
        }

        JSONArray userCards = cards.get(requesterID);

        // Create a new card JSONObject with the appropriate fields
        JSONObject cardObject = new JSONObject();
        cardObject.put("cardID", responseCardID);
        cardObject.put("name", name);
        cardObject.put("rarity", rarity);
        cardObject.put("imageLink", imageLink); // Assuming no image link for now, can be updated later

        // Add the card to the user's list
        userCards.add(cardObject);
        save(); // Save the credentials to the file
    }

    public JSONArray getAllUsers() {
        JSONArray users = new JSONArray();
        if (cards == null) {
            return users;
        }
        for (String username : cards.keySet()) {
            users.add(username);
        }
        return users;
    }
}
