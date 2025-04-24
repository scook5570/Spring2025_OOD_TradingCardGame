package server;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock; // description in TradeDatabase.java

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.types.JSONObject;

/**
 * stores/tracks cards for each user 
 */
public class UserCardsDatabase implements JSONSerializable {
    private HashMap<String, JSONArray> cards; // username -> cardID
    private File file;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Object fileLock = new Object();

    /**
     * constructor 
     * @param file
     */
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
     * @param username
     * @throws InvalidObjectException
     */
    public void addCards(String username, JSONArray newCards) throws InvalidObjectException {
        rwLock.writeLock().lock();
        try {
            if (!cards.containsKey(username)) {
                throw new InvalidObjectException("User does not exist in the database");
            }

            // create a deep copy of the cards to avoid potential reference issues 
            JSONArray userCards = cards.get(username);
            for (int i = 0; i < newCards.size(); i++) {
                userCards.add(deepCopyCard((JSONObject) newCards.get(i)));// im sorry little one, the function had to go...a soul for a soul
            }

            save(); // Save the credentials to the file
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * removes cards 
     * @param username
     * @param cardsToRemove
     * @throws InvalidObjectException
     */
    public synchronized void removeCards(String username, JSONArray cardsToRemove) throws InvalidObjectException {
        rwLock.writeLock().lock();

        try {
            if (!cards.containsKey(username)) {
                throw new InvalidObjectException("User does not exist in the database");
            }

            JSONArray userCards = cards.get(username);
            JSONArray updatedCards = new JSONArray();
            boolean[] cardsFound = new boolean[cardsToRemove.size()];

            // copy cards that aren't being removed
            for (int i = 0; i < userCards.size(); i++) {
                JSONObject userCard = (JSONObject) userCards.get(i);
                boolean shouldKeep = true;

                for (int j = 0; j < cardsToRemove.size(); j++) {
                    JSONObject cardToRemove = (JSONObject) cardsToRemove.get(j);
                    if (userCard.getString("cardID").equals(cardToRemove.getString("cardID"))) {
                        shouldKeep = false;
                        cardsFound[j] = true;
                        break;
                    }
                }

                if (shouldKeep) {
                    updatedCards.add(deepCopyCard(userCard));
                }
            }

            // verify that all cards were found
            for (int i = 0; i < cardsFound.length; i++) {
                if (!cardsFound[i]) {
                    JSONObject card = (JSONObject) cardsToRemove.get(i);
                    throw new InvalidObjectException("Card " + card.getString("cardID") + " not found in user's collection");
                }
            }

            // replace the user's cards with the updated list and save
            cards.put(username, updatedCards);
            save();
        } finally {
            rwLock.writeLock().unlock();
        } 
    }

    /**
     * Removes a user from the database
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
     * Get the cards of a user
     * @param username
     * @return JSONArray of cardIDs
     * @throws InvalidObjectException
     */
    public JSONArray getUserCards(String username) throws InvalidObjectException {
        
        rwLock.readLock().lock();
        
        try {

            if (!cards.containsKey(username)) {
                throw new InvalidObjectException("User does not exist in the database");
            }

            // return a deep copy to prevent external modification
            JSONArray result = new JSONArray();
            JSONArray userCards = cards.get(username);

            for (int i = 0; i < userCards.size(); i++) {
                result.add(deepCopyCard((JSONObject) userCards.get(i)));
            }

            return result;

        } finally {
            rwLock.readLock().unlock();
        }
        
    }

    public synchronized boolean exchangeCards(String fromUser, String toUser, JSONArray cardsToExchange) {
        rwLock.writeLock().lock();

        try {

            // validate that the users exist
            if (!cards.containsKey(fromUser) || !cards.containsKey(toUser)) {
                return false; 
            }

            // validate all cards exist in fromUser's collection
            JSONArray fromUserCards = cards.get(fromUser);
            boolean[] cardsFound = new boolean[cardsToExchange.size()];

            for (int i = 0; i < fromUserCards.size(); i++) {
                JSONObject userCard = (JSONObject) fromUserCards.get(i);

                for (int j = 0; j < cardsToExchange.size(); j++) {
                    JSONObject exchangeCard = (JSONObject) cardsToExchange.get(j);
                    if (userCard.getString("cardID").equals(exchangeCard.getString("cardID"))) {
                        cardsFound[j] = true; 
                    }
                }
            }

            // check to make sure all cards were found
            for (boolean found : cardsFound) {
                if (!found) {
                    return false; 
                }
            }

            // create updated collections
            JSONArray newFromUserCards = new JSONArray();
            for (int i = 0; i < fromUserCards.size(); i++) {
                JSONObject userCard = (JSONObject) fromUserCards.get(i);
                boolean shouldKeep = true;

                for (int j = 0; j < cardsToExchange.size(); j++) {
                    JSONObject exchangeCard = (JSONObject) cardsToExchange.get(j);
                    if (userCard.getString("cardID").equals(exchangeCard.getString("cardID"))) {
                        shouldKeep = true;
                        break; 
                    }
                }

                if (shouldKeep) {
                    newFromUserCards.add(deepCopyCard(userCard));
                }
            }

            // add cards to recipient's collection
            JSONArray toUserCards = cards.get(toUser);
            for (int i = 0; i < cardsToExchange.size(); i++) {
                toUserCards.add(deepCopyCard((JSONObject) cardsToExchange.get(i)));
            }

            // commit changes
            cards.put(fromUser, newFromUserCards);
            save();
            return true; 

        } catch (Exception e) {
            System.err.println("Transaction failed: " + e.getMessage());
            return false; 
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * helper method to create a deep copy of a card 
     * @param original
     * @return
     */
    private JSONObject deepCopyCard(JSONObject original) {
        JSONObject copy = new JSONObject();
        for (String key : original.keySet()) {
            copy.put(key, original.get(key));
        }
        return copy;
    }

    /**
     * 
     */
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

    /**
     * 
     */
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
        synchronized(fileLock) {
            try {
                JsonIO.writeFormattedObject(this, file);
            } catch (Exception e) {
                System.err.println("Error writing users file: " + e.getMessage());
            }
        }
    }

    /**
     * replace all cards for a user atomically 
     * @param username
     * @param newCards
     * @throws InvalidObjectException
     */
    public synchronized void replaceUserCards(String username, JSONArray newCards)  throws InvalidObjectException {
        if (!cards.containsKey(username)) {
            throw new InvalidObjectException("User does not exist in the databse");
        }
        cards.put(username, newCards);
        save();
    }

    /**
     * counts the total number of cards across all users 
     * @return
     */
    public int countTotalCards() {
        int total = 0;
        for (JSONArray userCards : cards.values()) {
            total += userCards.size();
        }

        return total;
    }
}
