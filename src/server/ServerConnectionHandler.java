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
    private TradeDatabase tradeDatabase;

    /**
     * 
     * @param port
     * @param userCreds
     * @param userCardsDatabase
     * @param tradeDatabase
     */
    public void start(int port, UserCredentials userCreds, UserCardsDatabase userCardsDatabase, TradeDatabase tradeDatabase) {
        this.userCreds = userCreds;
        this.userCardsDatabase = userCardsDatabase;
        this.tradeDatabase = tradeDatabase;

        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param userCredRequest
     * @return
     */
    public boolean handleLogin(UserCredRequest userCredRequest) {
        if (!(userCreds.checkUser(userCredRequest.getUsername()))) {
            return false;
        }

        if (userCreds.checkPassword(userCredRequest.getUsername(), userCredRequest.getPassword())) {
            return true;
        }

        return false;
    }

    /**
     * 
     * @param userCredRequest
     * @return
     */
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

    /**
     * 
     * @param request
     * @return
     */
    public String handleTradeInitiation(TradeInitiateRequest request) {

        System.out.println("DEBUG: Trade initiation from " + request.getSenderUsername() + " to " + request.getRecipientUsername());

        String sender = request.getSenderUsername();
        String recipient = request.getRecipientUsername();
        JSONArray offeredCards = request.getOfferedCards();

        if (!userCreds.checkUser(sender) || !userCreds.checkUser(recipient)) {
            return null;
        }

        try {
            JSONArray senderCards = userCardsDatabase.getUserCards(sender);
            for (int i = 0; i < offeredCards.size(); i++) {
                JSONObject offeredCard = (JSONObject) offeredCards.get(i);
                boolean cardFound = false;

                for (int j = 0; j < senderCards.size(); j++) {
                    JSONObject userCard = (JSONObject) senderCards.get(j);
                    if (userCard.getString("cardID").equals(offeredCard.getString("cardID"))) {
                        cardFound = true;
                        break;
                    }
                }

                if (!cardFound) {
                    System.out.println("Card not found");
                    return null;
                }
            } 
        } catch (InvalidObjectException e) {
            System.err.println("Error verifying card ownership: " + e.getMessage());
            return null;
        }

        String tradeId = tradeDatabase.createTrade(sender, recipient, offeredCards);
        TradeLogger.getInstance().logTradeCreation(tradeId, sender, recipient, offeredCards);

        ClientHandler recipientHandler  = clients.get(recipient);
        if (recipientHandler != null) {
            System.out.println("DEBUG: Sending notification to" + recipient);
            TradeOfferNotification notification = new TradeOfferNotification(tradeId, sender, offeredCards);
            recipientHandler.sendMessage(notification);
        } else {
            System.out.println("DEBUG: Recipient handler is null, cannot send notification");
        }
        return tradeId; 
    }
    
    /**
     * 
     * @param response
     * @return
     */
    public boolean handleTradeResponse(TradeResponse response) {
        String tradeId = response.getTradeId();
        boolean accepted = response.isAccepted();

        JSONObject trade = tradeDatabase.getTrade(tradeId);
        if (trade == null) {
            return false;
        }

        String initiator = trade.getString("initiator");
        String recipient = trade.getString("recipient");

        // update trade status 
        tradeDatabase.updateTradeStatus(tradeId, accepted ? "accepted" : "rejected");

        // log result
        if (accepted) {
            TradeLogger.getInstance().logTradeAcceptance(tradeId, initiator, recipient);
        } else {
            TradeLogger.getInstance().logTradeRejection(tradeId, initiator, recipient);
        }

        // execute the trade 
        if (accepted) {
            executeTradeTransaction(tradeId);
        }

        ClientHandler initiatorHandler = clients.get(initiator);
        if (initiatorHandler != null) {
            initiatorHandler.sendMessage(response);
        }
        return true; 
    }

    /**
     * 
     * @param packRequest
     * @return
     */
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

    /**
     * handles th card transfer 
     * @param tradeId - id of the specifc trade
     */
    private synchronized void executeTradeTransaction(String tradeId) {

        JSONObject trade = tradeDatabase.getTrade(tradeId);
        if (trade == null || !"accepted".equals(trade.getString("status"))) {
            return;
        }

        String initiator = trade.getString("initiator");
        String recipient = trade.getString("recipient");
        JSONArray offeredCards = trade.getArray("offeredCards");

        try {

            // added to help with debuggin duplication issues, kept because of convenience 
            int cardsBefore = userCardsDatabase.countTotalCards();

            // start transaction
            // get current collections 
            JSONArray initiatorCollection = new JSONArray();
            JSONArray recipientCollection = new JSONArray();
            
            // create new collections 
            JSONArray newInitiatorCollection = new JSONArray();
            JSONArray cardsToTransfer = new JSONArray();

            // identify cards to keep and transfer 
            for (int i = 0; i < initiatorCollection.size(); i++) {
                JSONObject card = (JSONObject) initiatorCollection.get(i);
                boolean shouldTransfer = false; 

                for (int j = 0; j < offeredCards.size(); j++) {
                    JSONObject offeredCard = (JSONObject) offeredCards.get(j);
                    if (card.getString("cardID").equals(offeredCard.getString("cardID"))) {
                        shouldTransfer = true;
                        cardsToTransfer.add(deepCopyJSONObject(card));
                        break; 
                    }
                }

                if (!shouldTransfer) {
                    newInitiatorCollection.add(deepCopyJSONObject(card));
                } 
            }

                // verfiy all cards were found 
            if (cardsToTransfer.size() != offeredCards.size()) {
                throw new InvalidObjectException("Not all cards found in initiator's collection");
            }

            // create new collections atomically 
            userCardsDatabase.replaceUserCards(initiator, newInitiatorCollection);
            userCardsDatabase.addCards(recipient, cardsToTransfer);

            // complete transaction
            tradeDatabase.updateTradeStatus(tradeId, "completed");

            int cardsAfter = userCardsDatabase.countTotalCards();
            if (cardsAfter != cardsBefore) {
                throw new InvalidObjectException("Card count mismatch before and after trade");
            }

        } catch (Exception e) {
            tradeDatabase.updateTradeStatus(tradeId, "failed");
            TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, e.getMessage());
        }
    }

    /**
     * Helper method for deep copying JSONObjects
     * @param original
     * @return
     */
    private JSONObject deepCopyJSONObject(JSONObject original) {
        JSONObject copy = new JSONObject();
        for (String key : original.keySet()) {
            copy.put(key, original.get(key));
        }
        return copy; 
    } 

    /**
     * 
     * @param username
     * @param clientHandler
     */
    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    /**
     * 
     * @param username
     */
    public void removeClient(String username) {
        clients.remove(username);
    }

    /**
     * 
     * @param collectionRequest
     * @return
     */
    public JSONArray handleCollectionRequest(CollectionRequest collectionRequest) {
        JSONArray collection = new JSONArray();
        try {
            collection = userCardsDatabase.getUserCards(collectionRequest.getUsername());
        } catch (InvalidObjectException e) {
            System.err.println("Error retrieving user cards: " + e.getMessage());
        }
        return collection;
    }

    /**
     * Helper method to remove cards during the trade process
     * @param username
     * @param cards
     * @throws InvalidObjectException
     */
    private void removeCardsFromUser(String username, JSONArray cards) throws InvalidObjectException {
        userCardsDatabase.removeCards(username, cards);
    }

    /**
     * Helper method to add cards during the trade process
     * @param username
     * @param cards
     * @throws InvalidObjectException
     */
    private void addCardsToUser(String username, JSONArray cards) throws InvalidObjectException {
        userCardsDatabase.addCards(username, cards);
    }
}
