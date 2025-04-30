package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.messages.*;
import shared.Card;

public class ServerConnectionHandler {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private UserCredentials userCreds;
    private UserCardsDatabase userCardsDatabase;
    private TradeRequestDatabase tradeRequestDatabase;

    public void start(int port, UserCredentials userCreds, UserCardsDatabase userCardsDatabase,
            TradeRequestDatabase tradeRequestDatabase) {
        this.userCreds = userCreds;
        this.userCardsDatabase = userCardsDatabase;
        this.tradeRequestDatabase = tradeRequestDatabase;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port); // Add this line
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress()); // Add this line
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean handleLogin(UserCredRequest userCredRequest) {
        if (!(userCreds.checkUser(userCredRequest.getUsername()))) {
            return false;
        }

        if (userCreds.checkPassword(userCredRequest.getUsername(), userCredRequest.getPassword())) {
            return true;
        }

        return false;
    }

    public boolean handleRegistration(UserCredRequest userCredRequest) {
        if (userCreds.checkUser(userCredRequest.getUsername())) {
            return false;
        }

        userCreds.addUser(userCredRequest.getUsername(), userCredRequest.getPassword());
        try {
            userCardsDatabase.addUser(userCredRequest.getUsername());
        } catch (InvalidObjectException e) {
            System.err.println("Error adding user to database: " + e.getMessage());
            return false;
        }
        return true;
    }

    public JSONArray handlePackRequest(PackRequest packRequest) {
        HashMap<String, Card> cardsMap = new HashMap<>();
        JSONArray cardArray = null;

        try {
            cardArray = JsonIO.readArray(new File("src/server/cardinfo/cards.json"));
        } catch (FileNotFoundException e) {
            System.err.println("cards.json file not found: " + e.getMessage());
            return new JSONArray(); // Return an empty array if the file is missing
        }

        // Populate the cardsMap
        for (int i = 0; i < cardArray.size(); i++) {
            JSONObject card = (JSONObject) cardArray.get(i);
            String cardID = card.getString("cardID");
            String name = card.getString("name");
            int rarity = card.getInt("rarity");
            String imageLink = card.getString("imageLink");
            cardsMap.put(cardID, new Card(cardID, name, rarity, imageLink));
        }

        JSONArray cardPack = new JSONArray();
        int cardCount = packRequest.getCardCount();

        // Randomly select cards
        for (int i = 0; i < cardCount; i++) {
            int randomIndex = (int) (Math.random() * cardsMap.size());
            String cardID = (String) cardsMap.keySet().toArray()[randomIndex];
            Card card = cardsMap.get(cardID);

            if (card != null) {
                JSONObject cardJSON = new JSONObject();
                cardJSON.put("cardID", card.getCardID());
                cardJSON.put("name", card.getName());
                cardJSON.put("rarity", card.getRarity());
                cardJSON.put("imageLink", card.getImage());
                cardPack.add(cardJSON);
            } else {
                System.err.println("Card with ID " + cardID + " is null.");
            }
        }

        // Add the cards to the user's database
        try {
            userCardsDatabase.addCards(packRequest.getUsername(), cardPack);
        } catch (InvalidObjectException e) {
            System.err.println("Error adding cards to database: " + e.getMessage());
        }

        return cardPack;
    }

    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }

    public JSONArray handleCollectionRequest(CollectionRequest collectionRequest) {
        JSONArray collection = new JSONArray();
        try {
            collection = userCardsDatabase.getUserCards(collectionRequest.getUsername());
        } catch (InvalidObjectException e) {
            System.err.println("Error retrieving user cards: " + e.getMessage());
        }
        return collection;
    }

    public void handleTradeRequest(TradeRequest tradeRequest) {
        System.out.println(
                "Trade request from " + tradeRequest.getRequesterID() + " to " + tradeRequest.getRecipientID());
        try {
            tradeRequestDatabase.addTradeRequest(tradeRequest.getRequesterID(), tradeRequest.getRecipientID(),
                    tradeRequest.getOfferCardID(), null);
        } catch (InvalidObjectException e) {
            System.out.println("Error adding trade request to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleTradeResponse(TradeResponse tradeResponse) {
        System.out
                .println("Trade response status: " + tradeResponse.getStatus() + " for " + tradeResponse.getTradeKey());
        if (!tradeResponse.getStatus()) {
            System.out.println("Trade for " + tradeResponse.getTradeKey() + " was not accepted.");
            try {
                tradeRequestDatabase.removeTradeRequest(tradeResponse.getTradeKey());
            } catch (InvalidObjectException e) {
                System.out.println("Error removing trade request from database: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        try {
            tradeRequestDatabase.updateTradeRequest(tradeResponse.getTradeKey(), tradeResponse.getCardID());
        } catch (InvalidObjectException e) {
            System.out.println("Error updating trade request in database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the view trades request by retrieving all trade requests and
     * responses for the user.
     *
     * @param viewTradesRequest The request containing the username of the user.
     * @return A JSONArray containing all trade requests and responses for the user.
     */
    public JSONArray handleViewTradesRequest(ViewTradesRequest viewTradesRequest) {
        JSONArray flatTrades = new JSONArray();

        // Flatten trade requests
        JSONArray requests = tradeRequestDatabase.getTradeRequests(viewTradesRequest.getUsername());
        for (int i = 0; i < requests.size(); i++) {
            flatTrades.add(requests.get(i)); // each entry is a JSONArray
        }

        // Flatten trade responses
        JSONArray responses = tradeRequestDatabase.getTradeResponses(viewTradesRequest.getUsername());
        for (int i = 0; i < responses.size(); i++) {
            flatTrades.add(responses.get(i));
        }

        System.out.println("Flat trade list for " + viewTradesRequest.getUsername() + ": " + flatTrades.toString());
        return flatTrades;
    }

    public void handleTradeConfirmation(TradeConfirmation tradeConfirmation) {
        if (tradeConfirmation.getStatus()) {
            System.out.println("Trade confirmed for " + tradeConfirmation.getTradeKey());
            try {
                JSONArray tradeInfo = tradeRequestDatabase.getTradeRequest(tradeConfirmation.getTradeKey());
                String requesterID = tradeInfo.getString(1);
                String recipientID = tradeInfo.getString(2);
                String offerCardID = tradeInfo.getString(3);
                String responseCardID = tradeInfo.getString(4);

                // swap cards
                System.out.println("Swapping cards: " + requesterID + " -> " + recipientID);
                System.out.println("Offer card ID: " + offerCardID + ", Response card ID: " + responseCardID);
                userCardsDatabase.removeCard(requesterID, offerCardID);
                userCardsDatabase.removeCard(recipientID, responseCardID);

                // Load cards from cards.json
                JSONArray cardArray = JsonIO.readArray(new File("src/server/cardinfo/cards.json")); // hard coded path cause im lazy
                HashMap<String, Card> cardsMap = new HashMap<>();

                // Populate the cardsMap
                for (int i = 0; i < cardArray.size(); i++) {
                    JSONObject card = (JSONObject) cardArray.get(i);
                    String cardID = card.getString("cardID");
                    String name = card.getString("name");
                    int rarity = card.getInt("rarity");
                    String imageLink = card.getString("imageLink");
                    cardsMap.put(cardID, new Card(cardID, name, rarity, imageLink));
                }

                // Query card info from cardsMap
                Card offerCard = cardsMap.get(offerCardID);
                Card responseCard = cardsMap.get(responseCardID);

                userCardsDatabase.addCard(requesterID, responseCard.getCardID(), responseCard.getName(),
                        responseCard.getRarity(), responseCard.getImage());
                userCardsDatabase.addCard(recipientID, offerCard.getCardID(), offerCard.getName(),
                        offerCard.getRarity(), offerCard.getImage());

                // remove trade request from database
                tradeRequestDatabase.removeTradeRequest(tradeConfirmation.getTradeKey());

            } catch (InvalidObjectException e) {
                System.out.println("Error removing trade request from database: " + e.getMessage());
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Trade not confirmed for " + tradeConfirmation.getTradeKey());
        }
    }

    public JSONArray handleUserListRequest(UserListRequest userListRequest) {
        JSONArray userList = new JSONArray();
        userList = userCardsDatabase.getAllUsers();
        return userList;
    }
}
