package server;

import java.util.concurrent.ConcurrentHashMap; 
import java.util.Set;

public class CardLockManager {

    private static CardLockManager instance; 
    private final ConcurrentHashMap<String, String> lockedCards = new ConcurrentHashMap<>();

    // get singleton instance 
    public static synchronized CardLockManager getInstance() {
        if (instance == null) {
            instance = new CardLockManager();
        }
        return instance;
    }

    // attempt to acquire locks on all cards for a transaction
    public synchronized boolean acquireLocks(Set<String> cardIds, String transactionId) {

        // check if any cards are already locked by another transaction
        for (String cardId : cardIds) {
            String existingLock = lockedCards.get(cardId);
            if (existingLock != null && !existingLock.equals(transactionId)) {
                return false; 
            }
        }

        // lock all cards for this transaction
        for (String cardId : cardIds) {
            lockedCards.put(cardId, transactionId);
        }
        return true; 
    }

    // release locks for a transaction
    public synchronized void releaseLocks(String transactionId) {
        lockedCards.entrySet().removeIf(entry -> transactionId.equals(entry.getValue()));
    }

}