package server;

import java.io.File;
import java.io.InvalidObjectException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/** ReentrantReadWriteLock
 * - one person can write at a time
 * - when one person is writing to a file (json) no one else can read or write (changes are being made)
 * - when no one is writing, anybody can read
 */

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * class representing a database for trading data
 */
public class TradeDatabase implements JSONSerializable {
    
    private HashMap<String, JSONObject> pendingTrades;
    private File file;

    // read-write lock for better concurrency
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Object fileLock = new Object();
    private final Set<String> cardsInActiveExchange = new HashSet<>();
    private final Map<String, String> cardToTradeMap = new ConcurrentHashMap<>();
    private final Object cardLockObject = new Object();

    /**
     * constructor 
     * @param file
     */
    public TradeDatabase(File file) {
        this.file = file;
        // makes th file if it does't already exist 
        if (!this.file.exists() || this.file.length() == 0) {
            try {
                this.file.getParentFile().mkdir();
                if (!this.file.createNewFile()) {
                    System.err.println("TARRIF ERROR : Could not create trades.json file");
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                System.err.println("TARRIF ERROR : Error creating trades file: " + e.getMessage());
            }
            pendingTrades = new HashMap<>();
            return;
        }

        try {
            deserialize(JsonIO.readArray(this.file));
        } catch (Exception e) {
            System.err.println("Error reading trades file: " + e.getMessage());
            pendingTrades = new HashMap<>();
        }
    }

    /**
     * locks cards by adding them to a dedicated set to prevent race conditions 
     * @param cards
     * @return
     */
    public boolean lockCardsForTrade(JSONArray cards, String tradeId) {
        synchronized(cardLockObject) {
            // check if any card is already being exchanged
            for (int i = 0; i < cards.size(); i++) {
                JSONObject card = (JSONObject) cards.get(i);
                String cardId = card.getString("cardID");

                if (cardsInActiveExchange.contains(cardId)) {
                    // check if the card is locked by the same trade
                    String lockingTradeId = cardToTradeMap.get(cardId);
                    if (lockingTradeId != null && !lockingTradeId.equals(tradeId)) {
                        System.out.println("Card " + cardId + " is already locked by trade " + lockingTradeId);
                        return false; // cards are already locked
                    }
                }
            }

            // if we get here, cards are good, lock em by adding them to the set 
            for (int i = 0; i < cards.size(); i++) {
                JSONObject card = (JSONObject) cards.get(i);
                String cardId = card.getString("cardID");

                cardsInActiveExchange.add(cardId);
                cardToTradeMap.put(cardId, tradeId);
            }
            return true; 
        }
    }

    /**
     * unlocks cards by taking them out of the dedicated set mentioned in lockCardsForTrade (above)
     * @param cards
     */
    public void unlockCards(JSONArray cards) {
        synchronized(cardLockObject) {
            for (int i = 0; i < cards.size(); i++) {
                JSONObject card = (JSONObject) cards.get(i);
                String cardId = card.getString("cardID");
                cardsInActiveExchange.remove(cardId);
                cardToTradeMap.remove(cardId);
            }
        }
    }

    public void unlockCardsForTrade(String tradeId) {
        synchronized(cardLockObject) {
            // find all cards locked by this trade
            List<String> cardsToUnlock = new ArrayList<>();
            for (Map.Entry<String, String> entry : cardToTradeMap.entrySet()) {
                if (entry.getValue().equals(tradeId)) {
                    cardsToUnlock.add(entry.getKey());
                }
            }

            // unlock the cards
            for (String cardId : cardsToUnlock) {
                cardsInActiveExchange.remove(cardId);
                cardToTradeMap.remove(cardId);
            }
        }
    }

    /**
     * creates a new trade and returns its ID
     * @param initiator
     * @param recipient
     * @param offeredCards
     * @return
     */
    public synchronized String createTrade(String initiator, String recipient, JSONArray offeredCards) {

        // If we're being nit-picky, we should iterate throigh ID's an make sure thje ID generated isn't a duplicate 
        String tradeId = UUID.randomUUID().toString();

        JSONObject trade = new JSONObject();
        trade.put("tradeId", tradeId);
        trade.put("initiator", initiator);
        trade.put("recipient", recipient);
        trade.put("offeredCards", offeredCards);
        trade.put("status", "pending");
        trade.put("timestamp", System.currentTimeMillis());

        // add to the database with lock protection
        rwLock.writeLock().lock();
        try {
            pendingTrades.put(tradeId, trade);
            save();
            return tradeId;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * get a trade by ID with read lock protection
     * @param tradeId
     * @return
     */
    public JSONObject getTrade(String tradeId) {
        rwLock.readLock().lock();
        try {
            return pendingTrades.get(tradeId);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * update trade status with a write lock protection
     * @param tradeId
     * @param status
     */
    public synchronized void updateTradeStatus(String tradeId, String status) {
        rwLock.writeLock().lock();
        try {
            JSONObject trade = pendingTrades.get(tradeId);
            if (trade != null) {
                trade.put("status", status);
                save();
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * get all cards that are currently locked in pending trades for a user
     * (prevents the same card from being included in multiple trades)
     * @param username
     * @return
     */
    public Set<String> getLockedCardIDs(String username) {
        Set<String> lockedCards = new HashSet<>();

        rwLock.readLock().lock();
        try {
            for (JSONObject trade : pendingTrades.values()) {
                if (!"pending".equals(trade.getString("status"))) {
                    continue;
                }

                // check if user is invlolved in this trade 
                if (username.equals(trade.getString("initiator"))) {
                    // add all cards the user if offering to this trade
                    JSONArray cards = trade.getArray("offeredCards");
                    for (int i = 0; i < cards.size(); i++) {
                        JSONObject card = (JSONObject) cards.get(i);
                        lockedCards.add(card.getString("cardID"));
                    }
                }
            }
            return lockedCards; 
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * removes an active trade from the trade database, making it unavailable for interactions
     * @param tradeId
     */
    public synchronized void removeTrade(String tradeId) {
        pendingTrades.remove(tradeId);
        save();
    }

    /**
     * get all trades involving a specific user 
     * @param username
     * @return
     */
    public JSONArray getTradesForUser(String username) {
        JSONArray userTrades = new JSONArray();

        rwLock.readLock().lock();

        try {
            for (JSONObject trade : pendingTrades.values()) {
                if (trade.getString("initiator").equals(username) || 
                    trade.getString("recipient").equals(username)) {
                    userTrades.add(trade);
                }
            }
            return userTrades;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (jsonType instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonType;
            pendingTrades = new HashMap<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject trade = (JSONObject) jsonArray.get(i);
                pendingTrades.put(trade.getString("tradeId"), trade);
            }
        } else {
            throw new InvalidObjectException("Invalid JSON type for TradeDatabase");
        }
    }

    /**
     * adds trades to the database 
     */
    @Override
    public JSONType toJSONType() {
        JSONArray jsonArray = new JSONArray();
        for (JSONObject trade : pendingTrades.values()) {
            jsonArray.add(trade);
        }
        return jsonArray;
    }

    /**
     * cleans up open trades that haven't been interacted with after a certain amount of milliseconds
     * @param maxAgeMillis
     */
    public synchronized void cleanupStaleTrades(long maxAgeMillis) {
        long now = System.currentTimeMillis();

        for (String tradeId : new HashMap<>(pendingTrades).keySet()) {
            JSONObject trade = pendingTrades.get(tradeId);
            if ("pending".equals(trade.getString("status"))) {
                long timestamp = trade.getLong("timestamp");
                if (now - timestamp > maxAgeMillis) {
                    trade.put("status", "expired");
                    save();

                    String initiator = trade.getString("initiator");
                    String recipient = trade.getString("recipient");

                    // log expiration
                    TradeLogger.getInstance().logTradeExpiration(tradeId, initiator, recipient);

                    System.out.println("Trade " + tradeId + " has expired");
                }
            }
        }
    }

    /**
     * saves the current state of the database 
     */
    public void save() {
        synchronized(fileLock) {
            try {
                JsonIO.writeFormattedObject(this, file);
            } catch (Exception e) {
                System.err.println("Error writing trades file: " + e.getMessage());
            }
        }
    }

}
