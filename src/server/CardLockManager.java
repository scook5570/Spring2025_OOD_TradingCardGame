package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CardLockManager {
    private static CardLockManager instance;
    private final ConcurrentHashMap<String, String> lockedCards = new ConcurrentHashMap<>();
    //private final Object lockObject = new Object();
    private final ReentrantReadWriteLock managerLock = new ReentrantReadWriteLock();


    public static synchronized CardLockManager getInstance() {
        if (instance == null) {
            instance = new CardLockManager();
        }
        return instance;
    }
    
    // Acquire locks on multiple cards atomically
    public boolean acquireLocks(Set<String> cardIds, String transactionId) {
        // Use a write lock for the entire operation to ensure atomicity
        managerLock.writeLock().lock();
        try {
            // First, check if any cards are already locked by another transaction
            for (String cardId : cardIds) {
                String existingLock = lockedCards.get(cardId);
                if (existingLock != null && !existingLock.equals(transactionId)) {
                    System.out.println("Card " + cardId + " is already locked by trade " + existingLock);
                    return false;
                }
            }
            
            // If we reach here, we can acquire all locks
            for (String cardId : cardIds) {
                lockedCards.put(cardId, transactionId);
                System.out.println("Lock acquired for card " + cardId + " by trade " + transactionId);
            }
            return true;
        } finally {
            managerLock.writeLock().unlock();
        }
    }
    
    // Release all locks held by a transaction
    public void releaseLocks(String transactionId) {
        managerLock.writeLock().lock();
        try {
            // Find all locks held by this transaction
            Set<String> cardsToRelease = new HashSet<>();
            for (Map.Entry<String, String> entry : lockedCards.entrySet()) {
                if (transactionId.equals(entry.getValue())) {
                    cardsToRelease.add(entry.getKey());
                }
            }
            
            // Release the locks
            for (String cardId : cardsToRelease) {
                lockedCards.remove(cardId);
                System.out.println("Lock released for card " + cardId + " by trade " + transactionId);
            }
        } finally {
            managerLock.writeLock().unlock();
        }
    }
}