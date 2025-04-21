package client;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.messages.*;

public class TradingTest {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: TradingTest <username> <password> <action> [recipient] [cardIds]");
            System.exit(1);
        }

        String username = args[0];
        String password = args[1];
        String action = args[2];

        ClientConnectionHandler connectionHandler = ClientConnectionHandler.getInstance();
        connectionHandler.connect();

        // Login first
        final BooleanWrapper loggedIn = new BooleanWrapper();
        loggedIn.value = false;
        
        connectionHandler.login(username, password, response -> {
            if (response.isSuccess()) {
                System.out.println("Login successful");
                connectionHandler.setUsername(username);
                loggedIn.value = true;
            } else {
                System.out.println("Login failed");
                System.exit(1);
            }
        });

        // Wait for login
        waitForCondition(() -> loggedIn.value);

        // Handle different actions
        switch (action) {
            case "initiate":
                if (args.length < 5) {
                    System.out.println("Missing recipient or card IDs");
                    System.exit(1);
                }
                String recipient = args[3];
                String cardIdsStr = args[4];
                
                // Get user collection
                final JSONArray[] collection = new JSONArray[1];
                connectionHandler.getCollection(username, response -> {
                    collection[0] = response.getCollection();
                    System.out.println("Got collection with " + collection[0].size() + " cards");

                    for (int i = 0; i < collection[0].size(); i++) {
                        JSONObject card = (JSONObject) collection[0].get(i);
                        System.out.println("Card: " + card.getString("cardID") + " - " + card.getString("name"));
                    }
                });
                
                // Wait for collection
                waitForCondition(() -> collection[0] != null);
                
                // Select cards
                String[] cardIds = cardIdsStr.split(",");
                JSONArray selectedCards = new JSONArray();
                for (String cardId : cardIds) {
                    cardId = cardId.trim();
                    for (int i = 0; i < collection[0].size(); i++) {
                        JSONObject card = (JSONObject) collection[0].get(i);
                        if (card.getString("cardID").equals(cardId)) {
                            selectedCards.add(card);
                            System.out.println("Selected card: " + card.getString("name"));
                            break;
                        }
                    }
                }
                
                // Initiate trade
                System.out.println("Initiating trade with " + recipient);
                connectionHandler.initiateTrade(recipient, selectedCards, tradeId -> {
                    System.out.println("Trade initiated with ID: " + tradeId);
                });
                
                // Wait a bit for server to process
                System.out.println("Keeping connection alive to receive notifications...");
                sleep(15000);
                break;
                
            case "accept":
                final BooleanWrapper tradeProcessed = new BooleanWrapper();
                tradeProcessed.value = false;
                
                // Set up trade notification handler
                connectionHandler.setTradeOfferCallback(offer -> {
                    System.out.println("Received trade offer from: " + offer.getSenderUsername());
                    System.out.println("Cards offered: " + offer.getOfferedCards().size());
                    
                    // Accept the trade
                    connectionHandler.respondToTrade(offer.getTradeID(), true, response -> {
                        System.out.println("Trade " + (response.isAccepted() ? "accepted" : "rejected"));
                        tradeProcessed.value = true;
                    });
                });
                
                // Wait for trade to be processed (or timeout)
                waitForCondition(() -> tradeProcessed.value, 10000);
                break;
                
            default:
                System.out.println("Unknown action: " + action);
                System.exit(1);
        }
        
        // Wait a bit more and exit
        System.out.println("Keeping connection alive to receive notifications...");
        sleep(15000);
        connectionHandler.shutdown();
        System.out.println("Test completed for " + username);
    }
    
    private static void waitForCondition(Condition condition) {
        waitForCondition(condition, 5000);
    }
    
    private static void waitForCondition(Condition condition, long timeout) {
        long startTime = System.currentTimeMillis();
        while (!condition.check()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("Timeout waiting for condition");
                break;
            }
            sleep(100);
        }
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    interface Condition {
        boolean check();
    }
}