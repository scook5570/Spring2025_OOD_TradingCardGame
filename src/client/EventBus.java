package client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    
    // singleton pattern
    private static EventBus instance; 

    public enum EventType {
        LOGIN_RESPONSE,
        COLLECTION_RESPONSE,
        PACK_RESPONSE,
        TRADE_OFFER,
        TRADE_RESPONSE,
        COUNTER_OFFER,
        COUNTER_OFFER_RESPONSE,
        AVAILABLE_USERS_RESPONSE
    }

    // thread safe collections for listeners 
    private final ConcurrentHashMap<EventType, 
                  CopyOnWriteArrayList<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {
        // initialize event types 
        for (EventType type : EventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
        }
    }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public <T> void  subscribe(EventType type, Consumer<T> listener) {
        @SuppressWarnings("unchecked")
        Consumer<Object> objectListener = (Consumer<Object>) listener;
        listeners.get(type).add(objectListener); 
    }

    public <T> void unsubscribe(EventType type, Consumer<T> listener) {
       @SuppressWarnings("unchecked")
       Consumer<Object> objectListener = (Consumer<Object>) listener;
       listeners.get(type).remove(objectListener);
    }

    public <T> void publish(EventType type, T event) {
        for (Consumer<Object> listener : listeners.get(type)) {
            listener.accept(event);
        }
    }

}

