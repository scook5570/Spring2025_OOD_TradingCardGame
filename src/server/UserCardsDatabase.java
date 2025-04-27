package server;

import java.io.File;
import java.io.InvalidObjectException;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock; // description in TradeDatabase.java
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List; 
import java.util.ArrayList; 

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
            // Step 1: Verify both users exist
            if (!cards.containsKey(fromUser) || !cards.containsKey(toUser)) {
                return false;
            }
            
            // Step 2: Get current card collections (make deep copies to avoid reference issues)
            JSONArray currentSenderCards = deepCopyCardArray(cards.get(fromUser));
            JSONArray currentRecipientCards = deepCopyCardArray(cards.get(toUser));
            
            // Step 3: Setup data structures for card tracking
            Set<String> cardIdsToTransfer = new HashSet<>();
            Map<String, JSONObject> senderCardMap = new HashMap<>();
            
            // Map all sender's cards for efficient lookup
            for (int i = 0; i < currentSenderCards.size(); i++) {
                JSONObject card = (JSONObject) currentSenderCards.get(i);
                senderCardMap.put(card.getString("cardID"), card);
            }
            
            // Step 4: Verify all cards exist in sender's collection
            for (int i = 0; i < cardsToExchange.size(); i++) {
                JSONObject cardToExchange = (JSONObject) cardsToExchange.get(i);
                String cardId = cardToExchange.getString("cardID");
                
                if (!senderCardMap.containsKey(cardId)) {
                    System.err.println("Card " + cardId + " not found in sender's collection");
                    return false;
                }
                
                cardIdsToTransfer.add(cardId);
            }
            
            // Step 5: Create new collections for atomic update
            JSONArray newSenderCards = new JSONArray();
            JSONArray newRecipientCards = new JSONArray();
            
            // Copy recipient's existing cards
            for (int i = 0; i < currentRecipientCards.size(); i++) {
                newRecipientCards.add(deepCopyCard((JSONObject) currentRecipientCards.get(i)));
            }
            
            // Copy sender's cards, excluding those being transferred
            for (int i = 0; i < currentSenderCards.size(); i++) {
                JSONObject card = (JSONObject) currentSenderCards.get(i);
                if (!cardIdsToTransfer.contains(card.getString("cardID"))) {
                    newSenderCards.add(deepCopyCard(card));
                }
            }
            
            // Step 6: Add transferred cards to recipient
            for (String cardId : cardIdsToTransfer) {
                newRecipientCards.add(deepCopyCard(senderCardMap.get(cardId)));
            }
            
            // Step 7: Atomic update of both collections
            cards.put(fromUser, newSenderCards);
            cards.put(toUser, newRecipientCards);
            
            // Step 8: Persist changes
            save();
            
            // Step 9: Verify integrity
            if (!verifyCardExchangeIntegrity(fromUser, toUser, cardIdsToTransfer)) {
                // If verification fails, roll back to previous state
                cards.put(fromUser, currentSenderCards);
                cards.put(toUser, currentRecipientCards);
                save();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Card exchange failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    // New helper method to verify integrity
    private boolean verifyCardExchangeIntegrity(String fromUser, String toUser, Set<String> transferredCardIds) {
        try {
            // Verify no duplicate occurrences
            JSONArray allUserCards = new JSONArray();
            for (String username : cards.keySet()) {
                allUserCards.addAll(cards.get(username));
            }
            
            Map<String, Integer> cardCounts = new HashMap<>();
            for (int i = 0; i < allUserCards.size(); i++) {
                JSONObject card = (JSONObject) allUserCards.get(i);
                String cardId = card.getString("cardID");
                cardCounts.put(cardId, cardCounts.getOrDefault(cardId, 0) + 1);
            }
            
            for (String cardId : cardCounts.keySet()) {
                if (cardCounts.get(cardId) > 1) {
                    System.err.println("Integrity violation: Card " + cardId + " appears " + 
                                      cardCounts.get(cardId) + " times after exchange");
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error in integrity verification: " + e.getMessage());
            return false;
        }
    }

    public void validateAndRepairDatabaseIntegrity() {
        rwLock.writeLock().lock();
        try {
            System.out.println("Starting database integrity validation and repair...");
            
            // Step 1: Build a map of all card IDs to their owners
            Map<String, List<String>> cardOwnerMap = new HashMap<>();
            
            for (String username : cards.keySet()) {
                JSONArray userCards = cards.get(username);
                for (int i = 0; i < userCards.size(); i++) {
                    JSONObject card = (JSONObject) userCards.get(i);
                    String cardId = card.getString("cardID");
                    
                    if (!cardOwnerMap.containsKey(cardId)) {
                        cardOwnerMap.put(cardId, new ArrayList<>());
                    }
                    cardOwnerMap.get(cardId).add(username);
                }
            }
            
            // Step 2: Check for and fix duplicated cards
            boolean repairsNeeded = false;
            for (String cardId : cardOwnerMap.keySet()) {
                List<String> owners = cardOwnerMap.get(cardId);
                if (owners.size() > 1) {
                    System.out.println("Found duplicated card: " + cardId + " owned by " + owners);
                    repairsNeeded = true;
                    
                    // Keep the card with the first owner alphabetically (consistent approach)
                    String keepOwner = owners.stream().sorted().findFirst().get();
                    
                    // Remove the card from all other owners' collections
                    for (String owner : owners) {
                        if (!owner.equals(keepOwner)) {
                            removeCardFromUser(owner, cardId);
                        }
                    }
                }
            }
            
            if (repairsNeeded) {
                save();
                System.out.println("Database integrity repairs completed");
            } else {
                System.out.println("No integrity issues found");
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    private void removeCardFromUser(String username, String cardId) {
        JSONArray userCards = cards.get(username);
        if (userCards == null) {
            return;
        }
        
        JSONArray updatedCards = new JSONArray();
        for (int i = 0; i < userCards.size(); i++) {
            JSONObject card = (JSONObject) userCards.get(i);
            if (!card.getString("cardID").equals(cardId)) {
                updatedCards.add(deepCopyCard(card));
            } else {
                System.out.println("Removing duplicate card " + cardId + " from user " + username);
            }
        }
        
        cards.put(username, updatedCards);
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
