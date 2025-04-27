package server;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.util.HashSet; 
import java.util.Set; 


/**
 * Coordinates trade processes
 */
public class TradeTransactionCoordinator {
    private static TradeTransactionCoordinator instance; 

    private TradeTransactionCoordinator() {}

    public static synchronized TradeTransactionCoordinator getInstance() {
        if (instance == null) {
            instance = new TradeTransactionCoordinator();
        }
        return instance; 
    }

    public boolean executeTradeTransaction(String tradeId, TradeDatabase tradeDB, UserCardsDatabase userCardsDB) {
        JSONObject trade = null;
        String initiator = null;
        String recipient = null;
        JSONArray offeredCards = null;
        
        // Step 1: Get trade details (outside synchronized block to minimize lock time)
        trade = tradeDB.getTrade(tradeId);
        if (trade == null || !"accepted".equals(trade.getString("status"))) {
            return false;
        }
        
        initiator = trade.getString("initiator");
        recipient = trade.getString("recipient");
        offeredCards = trade.getArray("offeredCards");
        
        // Step 2: Create a set of card IDs for locking
        Set<String> cardIds = new HashSet<>();
        for (int i = 0; i < offeredCards.size(); i++) {
            JSONObject card = (JSONObject) offeredCards.get(i);
            cardIds.add(card.getString("cardID"));
        }
        
        // Step 3: Acquire locks on all involved cards atomically
        if (!CardLockManager.getInstance().acquireLocks(cardIds, tradeId)) {
            tradeDB.updateTradeStatus(tradeId, "failed");
            TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, 
                    "Failed to lock cards - cards may be involved in another trade");
            return false;
        }
        
        try {
            // Step 4: Use a synchronized block to ensure atomicity across databases
            synchronized(this) {
                // Verify card ownership AGAIN right before exchange - critical step!
                if (!verifyCardsForTrade(initiator, offeredCards, userCardsDB)) {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, 
                            "Cards no longer available for trade");
                    return false;
                }
                
                // Perform card exchange with database write lock
                boolean success = userCardsDB.exchangeCards(initiator, recipient, offeredCards);
                
                if (success) {
                    tradeDB.updateTradeStatus(tradeId, "completed");
                    TradeLogger.getInstance().logTradeCompletion(tradeId, initiator, recipient);
                    return true;
                } else {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, 
                            "Card exchange failed during database update");
                    return false;
                }
            }
        } finally {
            // Step 5: Always release locks
            CardLockManager.getInstance().releaseLocks(tradeId);
        }
    }
    
    private boolean verifyCardsForTrade(String initiator, JSONArray offeredCards, UserCardsDatabase userCardsDB) {
        try {
            JSONArray initiatorCards = userCardsDB.getUserCards(initiator);
            Set<String> cardIds = new HashSet<>();
            
            // Get all cards owned by the initiator
            for (int i = 0; i < initiatorCards.size(); i++) {
                JSONObject card = (JSONObject) initiatorCards.get(i);
                cardIds.add(card.getString("cardID"));
            }
            
            // Verify all offered cards are still owned by the initiator
            for (int i = 0; i < offeredCards.size(); i++) {
                JSONObject card = (JSONObject) offeredCards.get(i);
                String cardId = card.getString("cardID");
                
                if (!cardIds.contains(cardId)) {
                    System.out.println("Card " + cardId + " no longer owned by " + initiator);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error verifying cards for trade: " + e.getMessage());
            return false;
        }
    }
}
