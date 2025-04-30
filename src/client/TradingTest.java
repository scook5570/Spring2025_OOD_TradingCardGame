package client;

//import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.messages.*;

public class TradingTest {

    private static final int DEFAULT_TIMEOUT = 10;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: TradingTest <username> <password> <action> [recipient] [cardIds]");
            System.exit(1);
        }

        String username = args[0];
        String password = args[1];
        String action = args[2];

        ClientConnectionHandler connectionHandler = ClientConnectionHandler.getInstance();
        if (!connectionHandler.connect()) {
            System.err.println("Failed to connect to server");
            System.exit(1);
        }

        try{
            // login first
            boolean loggedIn = connectionHandler.login(username, password).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);       
        
            if (!loggedIn) {
                System.err.println("Login failed");
                System.exit(1);
            } 

            switch (action) {
                case "initiate":
                    handleInitiateTrade(connectionHandler, args);
                    break;
                case "accept":
                    handleAcceptTrade(connectionHandler);
                    break;
                default:
                    System.err.println("Unkown action: " + action);
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // keep the connection alive for a bit to process any final messages 
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
                System.out.println("Interrupted Exception thrown");
            }
            connectionHandler.shutdown();
        }
    }
    
    private static void handleInitiateTrade(ClientConnectionHandler connectionHandler, String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("Missing recipient or card IDs");
            System.exit(1);
        }

        String username = args[0];
        String recipient = args[3];
        String cardIdsStr = args[4];

        // get user's card collection
        JSONArray collection = connectionHandler.getCollection(username).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        System.out.println("Got collection with " + collection.size() + " cards");
        displayCards(collection);

        String[] cardIds = cardIdsStr.split(",");
        JSONArray selectedCards = new JSONArray();

        for (String cardId : cardIds) {
            cardId = cardId.trim();
            for (int i = 0; i < collection.size(); i++) {
                JSONObject card = (JSONObject) collection.get(i);
                if (card.getString("cardID").equals(cardId)) {
                    selectedCards.add(card);
                    System.out.println("Selected card: " + card.getString("name"));
                    break;
                }
            }
        }

        if (selectedCards.size() == 0) {
            System.err.println("No valid cards selected");
            System.exit(1);
        }

        // initiate the trade
        String result = connectionHandler.initiateTrade(recipient, selectedCards).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        System.out.println("Trade initiation result: " + result);

        // wait for a while to receive any trade response
        System.out.println("Waiting for trade response...");
        Thread.sleep(10000);
    }
    
    private static void handleAcceptTrade(ClientConnectionHandler connectionHandler) throws Exception {
        System.out.println("Waiting for trade offers...");

        try {
            // wait for a trade offer with timeout
            TradeOfferNotification offer = connectionHandler.waitForTradeOffer(30).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

            System.out.println("Trade offer received from: " + offer.getUsername());
            System.out.println("Cards offered:");
            displayCards(offer.getOfferedCards());

            // accept the trade
            boolean success = connectionHandler.respondToTrade(offer.getTradeID(), true).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

            System.out.println("Trade " + (success ? "accepted" : "rejected"));
        } catch (TimeoutException e) {
            System.out.println("No trade offers received within timeout period");
        }
    }

    private static void displayCards(JSONArray cards) {
        if (cards == null || cards.size() == 0) {
            System.out.println("No cards found");
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            JSONObject card = (JSONObject) cards.get(i);
            System.out.println("-----Cards-----");
            System.out.println("CardID: " + card.getString("cardID"));
            System.out.println("Name: " + card.getString("name"));
            System.out.println("Rarity: " + card.getString("rarity"));
            System.out.println("Image Link: " + card.getString("imageLink"));
            System.out.println("--------------");
        }
    }

}