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

    public void start(int port, UserCredentials userCreds, UserCardsDatabase userCardsDatabase) {
        this.userCreds = userCreds;
        this.userCardsDatabase = userCardsDatabase;

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
}
