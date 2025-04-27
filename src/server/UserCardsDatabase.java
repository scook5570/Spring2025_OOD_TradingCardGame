package server;

import java.io.File;
import java.io.InvalidObjectException;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock; // description in TradeDatabase.java
import java.util.Set;
import java.util.HashSet;
import java.util.UUID; 

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
        // lock for the entire op
        rwLock.writeLock().lock();
    
        try {
            // step 1: create a transaction ID for tracking 
            String transactionId = UUID.randomUUID().toString();
            System.out.println("Starting transaction " + transactionId);

            // step 2:L verify all cards are ownded by the sender and collect card IDs
            Set<String> cardIdsToTransfer = new HashSet<>();
            JSONArray senderCards = cards.get(fromUser);

            if (senderCards == null) {
                System.err.println("Transaction " + transactionId + " failed: Sender not found");
                return false;
            }

            // verify each card exists in sender's collection
            for (int i = 0; i < cardsToExchange.size(); i++) {
                JSONObject cardToExchange = (JSONObject) cardsToExchange.get(i);
                String cardId = cardToExchange.getString("cardID");

                boolean cardFound = false; 
                for (int j = 0; j < senderCards.size(); j++) {
                    JSONObject senderCard = (JSONObject) senderCards.get(j);
                    if (senderCard.getString("cardID").equals(cardId)) {
                        cardFound = true; 
                        break;
                    }
                }

                if (!cardFound) {
                    System.err.println("Transaction " + transactionId + " failed: Card " + cardId + " not found in sender's collection");
                    return false; 
                }

                // track card IDs to ensure we don't have duplicates in the request
                if (!cardIdsToTransfer.add(cardId)) {
                    System.err.println("transaction " + transactionId + " failed: Duplicate card " + cardId + " in exchange request");
                    return false;
                }
            }

            // step 3: create new collection arrays for both users while avoiding in place modifications
            JSONArray newSenderCards = new JSONArray();
            JSONArray newRecipientCards = new JSONArray();
            JSONArray recipientCards = cards.get(toUser);

            if (recipientCards == null) {
                System.err.println("Transaction " + transactionId + " failed: Recipient not found");
                return false; 
            }

            // step 4: copy recipient's existing cards
            for (int i = 0; i < recipientCards.size(); i++) {
                newRecipientCards.add(deepCopyCard((JSONObject) recipientCards.get(i)));
            } 

            // step 5: filter out transferred cards from sender and add remainig ones to ne collection
            for (int i = 0; i < senderCards.size(); i++) {
                JSONObject card = (JSONObject) senderCards.get(i);
                String cardId = card.getString("cardID");

                if (!cardIdsToTransfer.contains(cardId)) {
                    newSenderCards.add(deepCopyCard(card));
                } else {
                    // add transferred card to recipient
                    newRecipientCards.add(deepCopyCard(card));
                    System.out.println("Transaction " + transactionId + ": Transferring card " + cardId + " from " + fromUser + " to " + toUser);
                }
            }

            // step 6: perform the actual update atomically
            cards.put(fromUser, newSenderCards);
            cards.put(toUser, newRecipientCards);

            // step 7: verify integrity after the transaction
            if (!verifyCardIntegrity(fromUser, toUser, cardIdsToTransfer, transactionId)) {

                // shouldn't happen if our logic is correct 
                System.err.println("Transaction " + transactionId + " failed iontegrity verification");
                return false;
            }

            // step 8: save the changes to persistent storage
            save();
            System.out.println("Transaction " + transactionId + " completed successfully");
            return true; 

        } catch (Exception e) {
            System.err.println("transaction failed with exception: " + e.getMessage());
            e.printStackTrace();
            return false; 
        } finally {
            rwLock.writeLock().unlock();
        }
           
    }

    /**
     * verify that all cards in the exchange are valid for transfer
     * @param fromUser
     * @param cardsToExchange
     * @return
     */
    private boolean verifyCardsForExchange(String fromUser, JSONArray cardsToExchange) {
        // check if sender exists
        if (!cards.containsKey(fromUser)) {
            System.err.println("Sender does not exist: " + fromUser);
            return false; 
        }

        // get sender's cards
        JSONArray senderCards = cards.get(fromUser);

        // create a set of all the sender's card IDs for efficient lookup
        Set<String> senderCardIds = new HashSet<>();
        for (int i = 0; i < senderCards.size(); i++) {
            JSONObject card = (JSONObject) senderCards.get(i);
            senderCardIds.add(card.getString("cardID"));
        }

        // check that each card to be exchanged exists in sender's collection and is only being transfered once
        Set<String> transferCardIds = new HashSet<>();
        for (int i = 0; i < cardsToExchange.size(); i++) {
            JSONObject card = (JSONObject) cardsToExchange.get(i);
            String cardId = card.getString("cardID");

            // check for existence in senders collection
            if (!senderCardIds.contains(cardId)) {
                System.err.println("Card " + cardId + " not found in sender's collection");
                return false; 
            }

            // check if we're trying to transfer the same card twice
            if (!transferCardIds.add(cardId)) {
                System.err.println("Duplicate card in exchange request: " + cardId);
                return false; 
            }
        }

        return true; 

    }

    /**
     * verifies the integrity of a card exchange transaction
     * @param fromUser
     * @param toUser
     * @param transferredCardIds
     * @param transactionId
     * @return
     */
    private boolean verifyCardIntegrity(String fromUser, String toUser, Set<String> transferredCardIds, String transactionId) {
        try {
            JSONArray senderCards = cards.get(fromUser);
            JSONArray recipientCards = cards.get(toUser);

            // check 1 : sender should no longer have any of the transferred cards
            for (int i = 0; i < senderCards.size(); i++) {
                JSONObject card = (JSONObject) senderCards.get(i);
                String cardId = card.getString("cardID");

                if (transferredCardIds.contains(cardId)) {
                    System.err.println("Integrity error: Sender still has the transferred card " + cardId);
                    return false; 
                }
            }

            // check 2: recipient should have all trqansferred cards exactly once 
            Set<String> foundCardIds = new HashSet<>();
            for (int i = 0; i < recipientCards.size(); i++) {
                JSONObject card = (JSONObject) recipientCards.get(i);
                String cardId = card.getString("cardID");

                if (transferredCardIds.contains(cardId)) {
                    if (foundCardIds.contains(cardId)) {
                        System.err.println("Integrity error: Recipient has duplicate card " + cardId);
                        return false; 
                    }
                    foundCardIds.add(cardId);
                }
            }

            // check 3: all transferred cards should be found in recipient's collection
            if (foundCardIds.size() != transferredCardIds.size()) {
                System.err.println("Integrity error: not all trsnferred cards found in recipient's collection");
                return false; 
            }
            return true; 
        } catch (Exception e) {
            System.err.println("Error during integrity verification: " + e.getMessage());
            return false; 
        }
    }

    /**
     * helper method : creates a deep copy of a card array
     * @param original
     * @return
     */
    private JSONArray deepCopyCardArray(JSONArray original) {
        JSONArray copy = new JSONArray();
        for (int i = 0; i < original.size(); i++) {
            copy.add(deepCopyCard((JSONObject) original.get(i)));
        }
        return copy; 
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

    private boolean validateCardUniqueness() {
        // create a set to track all card IDs
        Set<String> allCardIds = new HashSet<>();

        // check each user's collection
        for (String username : cards.keySet()) {
            JSONArray userCards = cards.get(username);
            for (int i = 0; i < userCards.size(); i++) {
                JSONObject card = (JSONObject) userCards.get(i);
                String cardId = card.getString("cardID");

                // if we've seen this card before it's a duplicate
                if (allCardIds.contains(cardId)) {
                    return false;
                }

                allCardIds.add(cardId);
            }
        }
        return true; 
    }

    /**
     * rolls back a transaction
     * @param transactionLog
     */
    private void rollbackTransaction(JSONArray transactionLog) {
        // process the log in reverse order
        for (int i = transactionLog.size() - 1; i >= 0; i--) {
            JSONObject entry = (JSONObject) transactionLog.get(i);
            String action = entry.getString("action");
            String user = entry.getString("user");
            JSONObject card = entry.getObject("card");

            if ("add".equals(action)) {
                // reverse and add - remove the card
                removeCardFromUser(user, card);
            } else if ("remove".equals(action)) {
                // vice versa 
                addCardToUser(user, card);
            }
        }
    }

    /**
     * helper method to remove a specific card from a user 
     * @param username
     * @param cardToRemove
     */
    private void removeCardFromUser(String username, JSONObject cardToRemove) {
        if (!cards.containsKey(username)) {
            System.out.println("Username not found in database");
            return; 
        }

         JSONArray userCards = cards.get(username);
         JSONArray updatedCards = new JSONArray();
         String cardIdToRemove = cardToRemove.getString("cardID");

         // keep all cards except the one to remove 
         for (int i = 0; i < userCards.size(); i++) {
            JSONObject card = (JSONObject) userCards.get(i);
            if (!card.getString("cardID").equals(cardIdToRemove)) {
                updatedCards.add(deepCopyCard(card));
            }
         }

         cards.put(username, updatedCards);
    }

    /**
     * helper method to adda card to user's collection
     * @param username
     * @param cardtoAdd
     */
    private void addCardToUser(String username, JSONObject cardtoAdd) {
        if (!cards.containsKey(username)) {
            System.out.println("Username not found in database");
            return;
        }

        JSONArray userCards = cards.get(username);
        JSONArray updatedCards = deepCopyCardArray(userCards);
        updatedCards.add(deepCopyCard(cardtoAdd));

        cards.put(username, updatedCards);
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
     * retrieves all the usernames in the database
     * @return
     */
    public Set<String> getAllUsernames() {
        rwLock.readLock().lock();
        try {
            return new HashSet<>(cards.keySet());
        } finally {
            rwLock.readLock().unlock();
        }
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
