package server;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;


/**
 * Coordinates trade processes
 */
public class TradeTransactionCoordinator {
    private static TradeTransactionCoordinator instance; 
    private final Object transactionLock = new Object();

    private TradeTransactionCoordinator() {}

    public static synchronized TradeTransactionCoordinator getInstance() {
        if (instance == null) {
            instance = new TradeTransactionCoordinator();
        }
        return instance; 
    }

    public boolean executeTradeTransaction(String tradeId, TradeDatabase tradeDB, UserCardsDatabase userCardsDB) {
        synchronized(transactionLock) {
            // get trade details
            JSONObject trade = tradeDB.getTrade(tradeId);
            if (trade == null || !"accepted".equals(trade.getString("status"))) {
                return false; 
            }

            String initiator = trade.getString("initiator");
            String recipient = trade.getString("recipient");
            JSONArray offeredCards = trade.getArray("offeredCards");

            // lock cards for this trade
            if (!tradeDB.lockCardsForTrade(offeredCards, tradeId)) {
                tradeDB.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Cards are invloved in another trade");
                return false; 
            } 

            try {
                // execute card exchange atomically 
                boolean success = userCardsDB.exchangeCards(initiator, recipient, offeredCards);

                if (success) {
                    // update trade status
                    tradeDB.updateTradeStatus(tradeId, "comleted");
                    TradeLogger.getInstance().logTradeCompletion(tradeId, initiator, recipient);
                    return true;
                } else {
                    tradeDB.updateTradeStatus(tradeId, "failed");
                    TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Card exhange failed");
                    return false; 
                }
            } catch (Exception e) {
                tradeDB.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, e.getMessage());
                return false;
            } finally {
                // always unlock cards
                tradeDB.unlockCards(offeredCards);
            }

        }
    }
}
