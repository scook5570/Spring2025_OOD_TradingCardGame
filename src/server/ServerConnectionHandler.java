package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;

import java.net.ServerSocket;
import java.net.Socket;

import merrimackutil.json.types.JSONArray; 
import merrimackutil.json.types.JSONObject; 

import shared.messages.*;

import java.util.HashMap;
import java.util.HashSet; 
import java.util.Map;
import java.util.List; 
import java.util.ArrayList; 

import java.util.concurrent.ConcurrentHashMap;
/** ConcurrentHashMap
 * thread-safe version of a HashMap (can handle multiple interactions at once)
 */

import java.util.Set; 

import merrimackutil.json.JsonIO;
import shared.Card;

public class ServerConnectionHandler {
    private ServerSocket serverSocket;
    public ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private UserCredentials userCreds;
    private UserCardsDatabase userCardsDatabase;
    public TradeDatabase tradeDatabase;

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
     * handles user login
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
     * handles user registration
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
     * handles trade initiation
     * @param request
     * @return
     */
    public String handleTradeInitiation(TradeInitiateRequest request) {

        System.out.println("DEBUG: Trade initiation from " + request.getSenderUsername() + " to " + request.getRecipientUsername());

        String sender = request.getSenderUsername();
        String recipient = request.getRecipientUsername();
        JSONArray offeredCards = request.getOfferedCards();

        if (!userCreds.checkUser(sender) || !userCreds.checkUser(recipient)) {
            System.out.println("Trade failed: User doesn't exist");
            return null;
        }

        try { 
            // check if any offered cards are already in pending trades 
            Set<String> lockedCards = tradeDatabase.getLockedCardIDs(sender);
            for (int i = 0; i < offeredCards.size(); i++) {
                JSONObject offeredCard = (JSONObject) offeredCards.get(i);
                String cardId = offeredCard.getString("cardID");

                if (lockedCards.contains(cardId)) {
                    System.out.println("Trade failed: Card " + cardId + " is already in a pending trade");
                    return null;
                }
            }

            // verify that the sender owns all cards being offered 
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

        // create the trade
        String tradeId = tradeDatabase.createTrade(sender, recipient, offeredCards);
        TradeLogger.getInstance().logTradeCreation(tradeId, sender, recipient, offeredCards);

        // notify the recipient if their online
        ClientHandler recipientHandler  = clients.get(recipient);
        if (recipientHandler != null) {
            System.out.println("DEBUG: Sending notification to " + recipient);
            TradeOfferNotification notification = new TradeOfferNotification(tradeId, sender, offeredCards);
            recipientHandler.sendMessage(notification);
        } else {
            System.out.println("DEBUG: Recipient handler is null, cannot send notification");
        }
        return tradeId; 
    }
    
    /**
     * handles trade response with transaction support 
     * @param response
     * @return
     */
    public boolean handleTradeResponse(TradeResponse response) {
        String tradeId = response.getTradeId();
        boolean accepted = response.isAccepted();

        JSONObject trade = tradeDatabase.getTrade(tradeId);
        if (trade == null) {
            System.out.println("Trade response failed: Trade not found");
            return false;
        }

        // verify that the trade is still pending 
        if (!"pending".equals(trade.getString("status"))) {
            System.out.println("Trade response failed: Trade is no longer pending");
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
            boolean success = executeTradeTransaction(tradeId);
            if  (!success) {
                // if execution failed, update status
                tradeDatabase.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Failed to execute card transfer");
                return false; 
            }
        }

        //  notify the initiator of the response
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
     * handles the card transfer with added transaction support 
     * @param tradeId
     */
    private synchronized boolean executeTradeTransaction(String tradeId) {

        JSONObject trade = tradeDatabase.getTrade(tradeId);
        if (trade == null || !"accepted".equals(trade.getString("status"))) {
            return false;
        }

        String initiator = trade.getString("initiator");
        String recipient = trade.getString("recipient");
        JSONArray offeredCards = trade.getArray("offeredCards");

        // try to lock cards before the trade
        if (!tradeDatabase.lockCardsForTrade(offeredCards)) {
            // cards are invlolved in another trade 
            tradeDatabase.updateTradeStatus(tradeId, "failed");
            TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "One or more of the cards requested are currently invlolved in another trade");
            return false; 
        }

        try {

            // log the starting if the transaction
            TradeLogger.getInstance().logTradeCompletion(tradeId, initiator, recipient);

            // log each card transfer
            for (int i = 0; i < offeredCards.size(); i++) {
                JSONObject card  = (JSONObject) offeredCards.get(i);
                TradeLogger.getInstance().logCardTransfer(tradeId, initiator, recipient, card.getString("cardID"), card.getString("name"));
            }

            // execute the transfer
            boolean success = userCardsDatabase.exchangeCards(initiator, recipient, offeredCards);

            if (success) {
                // mark trade as completed
                tradeDatabase.updateTradeStatus(tradeId, "completed");
                return true; 
            } else {
                tradeDatabase.updateTradeStatus(tradeId, "failed");
                TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, "Transaction failed");
                return false; 
            }
        
        } catch (Exception e) {
            tradeDatabase.updateTradeStatus(tradeId, "failed");
            TradeLogger.getInstance().logTradeFailure(tradeId, initiator, recipient, e.getMessage());
            return false; 
        } finally {
            // lock cards
            tradeDatabase.unlockCards(offeredCards);
        }

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
     * handles request to collect cards
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

    public boolean validateDatabaseIntegrity() {
    
        // check for duplicate cards
        Set<String> seenCardIds = new HashSet<>();
        Map<String, List<String>> duplicateOwnership = new HashMap<>();
        boolean hasDuplicates = false; 

        try {
            // identify duplicates ; first pass
            for (String username : userCardsDatabase.getAllUsernames()) {
                JSONArray userCards = userCardsDatabase.getUserCards(username);
            
                for (int i = 0; i < userCards.size(); i++) {
                    JSONObject card = (JSONObject) userCards.get(i);
                    String cardId = card.getString("cardID");

                    if (seenCardIds.contains(cardId)) {
                        hasDuplicates = true; 

                        // tracking ownsership
                        if (!duplicateOwnership.containsKey(cardId)) {
                            duplicateOwnership.put(cardId, new ArrayList<>());
                        
                            // find the first owner
                            for (String otherUser : userCardsDatabase.getAllUsernames()) {
                                if (!otherUser.equals(username)) {
                                    JSONArray otherCards = userCardsDatabase.getUserCards(otherUser);
                                    for (int j = 0; j < otherCards.size(); j++) {
                                        if (cardId.equals(((JSONObject)otherCards.get(j)).getString("cardID"))) {
                                            duplicateOwnership.get(cardId).add(otherUser);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        duplicateOwnership.get(cardId).add(username);
                    } else {
                        seenCardIds.add(cardId);
                    }
                } 
            }

            // report detailed findings if a duplicate exists
            if (hasDuplicates) {
                System.err.println("DATABASE INTEGRITY ERROR: Duplicate cards detected");
                for (String cardId : duplicateOwnership.keySet()) {
                    System.err.println("  Card " + cardId + " appears in multiple collections: " + String.join(", ", duplicateOwnership.get(cardId)));
                }
                return false; 
            }

            int totalCards = userCardsDatabase.countTotalCards();
            System.out.println("Database integrity check passed. Total cards: " + totalCards);
            return true; 

        } catch (Exception e) {
            System.err.println("Error validating database: " + e.getMessage());
            return false; 
        }
    }

}
