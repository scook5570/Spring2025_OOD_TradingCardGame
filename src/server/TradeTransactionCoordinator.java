package server;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.io.InvalidObjectException;
import java.util.HashSet; 
import java.util.Set; 


/**
 * Coordinates trade processes
 */
public class TradeTransactionCoordinator {
    private static TradeTransactionCoordinator instance; 
    private final Object globalTradeLock = new Object();

    private TradeTransactionCoordinator() {}

    public static synchronized TradeTransactionCoordinator getInstance() {
        if (instance == null) {
            instance = new TradeTransactionCoordinator();
        }
        return instance; 
    }

    public boolean executeTradeTransaction(String tradeId, TradeDatabase tradeDB, UserCardsDatabase userCardsDB) {
        // global trae lock for this specific trade 
        synchronized(globalTradeLock) {
            // step 1: get and validate trade details
            JSONObject trade = tradeDB.getTrade(tradeId);
            if (trade == null) {
                System.err.println("Trade " + tradeId + " not found");
                return false; 
            }

            if (!"accepted".equals(trade.getString("status"))) {
                System.err.println("Trade " + tradeId + " is not in accepted state");
                return false; 
            }

            String initiator = trade.getString("initiator");
            String recipient = trade.getString("recipient");
            JSONArray offeredCards = trade.getArray("offeredCards");

            // step 2: lock all cards for this trade to prevent race conditions
            if (!tradeDB.lockCardsForTrade(offeredCards, tradeId)) {
                tradeDB.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Failed to lock cards - cards may be invloved in another trade");
                return false; 
            } 

            try {

                // step 3: verify that both users still exist
                try {
                    userCardsDB.getUserCards(initiator);
                    userCardsDB.getUserCards(recipient);
                } catch (InvalidObjectException e) {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "User no longer exists: " + e.getMessage());
                    return false; 
                }

                // step 4: verify initiator still owns all cards being offered 
                boolean verificationPassed = verifyCardOwnership(userCardsDB, recipient, offeredCards);
                if (!verificationPassed) {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Initiator no longer owns all offered cards");
                    return false;
                }

                // step 5: execute the actual card exchange as an atomis operation
                boolean success = userCardsDB.exchangeCards(initiator, recipient, offeredCards);

                if (success) {
                    // step 6: update trade status to completed 
                    tradeDB.updateTradeStatus(tradeId, "comleted");
                    TradeLogger.getInstance().logTradeCompletion(tradeId, initiator, recipient);
                    
                    // step 7: log individual card transfers for audit purposes
                    for (int i = 0; i < offeredCards.size(); i++) {
                        JSONObject card = (JSONObject) offeredCards.get(i);
                        TradeLogger.getInstance().logCardTransfer(tradeId, initiator, recipient, card.getString("cardID"), card.getString("name"));
                    }
                    return true;
                } else {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Card exchange failed");
                    return false; 
                }
            } catch (Exception e) {
                tradeDB.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, e.getMessage());
                return false;
            } finally {
                // always unlock cards regardless of success/failure 
                tradeDB.unlockCardsForTrade(tradeId);
            }

        }
    }

    /**
     * verifies that a user still owns all card in a trade offer
     * @param userCardsDB
     * @param username
     * @param offeredCards
     * @return
     */
    private boolean verifyCardOwnership(UserCardsDatabase userCardsDB, String username, JSONArray offeredCards) {
        try {
            JSONArray userCards = userCardsDB.getUserCards(username);
            Set<String> userCardIds = new HashSet<>();

            // create a set of all card IDs owned by the user
            for (int i = 0; i < userCards.size(); i++) {
                JSONObject card = (JSONObject) userCards.get(i);
                userCardIds.add(card.getString("cardID"));
            }

            // check if all offered cards are in the user's collection
            for (int i = 0; i < offeredCards.size(); i++) {
                JSONObject offeredCard = (JSONObject) offeredCards.get(i);
                String cardId = offeredCard.getString("cardID");

                if (!userCardIds.contains(cardId)) {
                    System.err.println("Card " + cardId + " is no longer in user's collection");
                    return false;
                }
            }

            return true; 
        } catch (Exception e) {
            System.err.println("Error verifying card ownership: " + e.getMessage());
            return false; 
        }
    }
}
