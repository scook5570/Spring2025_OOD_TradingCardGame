package client;

import java.io.IOException;

import java.net.Socket;

import java.util.concurrent.CompletableFuture;
/** CompletableFuture
 * - allows tasks to be completed in the background without holding up the code
 * - can execute multiple tasks in sequence in the background 
 */
import java.util.concurrent.ScheduledExecutorService;
/** ScheduledExecutorService 
 * - provides the ability to schedule certain tasks (i.e "do this in 5 seconds")
 * - can make tasks execute periodically 
 */
import java.util.concurrent.Executors; 
/** Executors 
 * provides the ability to manage tasks given to it by utlizing a set amount of threads  
 */
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.regex.Pattern;

import org.w3c.dom.css.Counter;

import java.util.function.Consumer; 

import client.EventBus.EventType;

import shared.MessageSocket;
import shared.messages.*;

import merrimackutil.json.types.JSONArray;

/**
 * Handles the client's connection to the sever using mltithreading and the Observer pattern
 * Implements a single persisten tconection that handles requests and responses asynchronously 
 */
public class ClientConnectionHandler {
    
    //Singleton Pattern for client connection
    private static ClientConnectionHandler instance; 

    private String serverAddress;
    private int serverPort; 
    private String username;
    private MessageSocket messageSocket; 
    private boolean connected = false; 

    //Thread management 
    private Thread receiverThread;
    private ScheduledExecutorService executorService; 
    
    //Regular expressrion for validating username and password 
    private static final Pattern VALID_CREDENTIALS = Pattern.compile("[a-zA-Z0-9_]{3,16}$");

    /**
     * Private constructor for singleton pattern
     */
    private ClientConnectionHandler() {
        this.serverAddress = "localhost";
        this.serverPort = 5100; // CHANGE IF NEEDED, MY MAC's 5000 PORT IS OCCUPIED
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * Get the singleton instance 
     * @return ^ 
     */
    public static synchronized ClientConnectionHandler getInstance() {
        if (instance == null) {
            instance = new ClientConnectionHandler();
        }
        return instance;
    }

    /**
     * Configure server connection settings
     * @param serverAddress
     * @param ServerPort
     */
    public void configure(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort; 
    }

    /**
     * Connect to the server
     * @return true if connected, false otherwise
     */
    public boolean connect() {

        System.out.println("Connecting...");

        if (this.connected) {
            System.out.println("already connected✅");
            return true; 
        }

        try {
            Socket socket = new Socket(this.serverAddress, this.serverPort);
            this.messageSocket = new MessageSocket(socket);
            this.connected = true; 
            System.out.println("starting receiver thread...");

            // Start receiver thread to listen to server for messages 
            startReceiverThread();
            System.out.println("connected✅");
            return true; 
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false; 
        }

    }

    /**
     * Start the thread that listens for messages from the server
     */
    public void startReceiverThread() {
        System.out.println("Starting thread...");
        this.receiverThread = new Thread(() -> {
            try {
                while (this.connected) {
                    Message response = messageSocket.getMessage();
                    handleResponse(response);
                }
            } catch (Exception e) {
                System.err.println("Connection to server lost: " + e.getMessage());
                disconnect();
            }
        });
        receiverThread.setDaemon(true);
        receiverThread.start();
        System.out.println("Thread started✅");

    }

    /**
     * Handle responses from the server
     * @param response
     */
    private void handleResponse(Message response) {
        System.out.println("Handling response: " + response.getType());

        switch (response.getType()) {
            case "Status":
                EventBus.getInstance().publish(EventType.LOGIN_RESPONSE, response);
                break;
            case "PackResponse":
                EventBus.getInstance().publish(EventType.PACK_RESPONSE, response);
                break;
            case "CollectionResponse":
                EventBus.getInstance().publish(EventType.COLLECTION_RESPONSE, response);
                break;
            case "TradeOfferNotification":
                EventBus.getInstance().publish(EventType.TRADE_OFFER, response);
                break;
            case "TradeResponse":
                EventBus.getInstance().publish(EventType.TRADE_RESPONSE, response);
                break;
            case "CounterOfferResponse":
                EventBus.getInstance().publish(EventType.COUNTER_OFFER, response);
            default:
            System.err.println("Unkown response type: " + response.getType());
        }
    }

    /**
     * Disconnect from the server
     */
    public void disconnect() {

        System.out.println("disconnecting...");
        if (!isConnected()) {
            System.out.println("Already disconnected");
            return;
        }

        try {
            // Send the logout request if there's a username
            if (this.username != null) {
                sendMessage(new LogOutRequest(this.username)); 
                System.out.println(this.username + " logged out");
            }

            this.messageSocket.close();
            this.connected = false; 
            System.out.println("socket closed, connection set to false");

            if (this.receiverThread != null) {
                this.receiverThread.interrupt();
                System.out.println("receiver thread interrupted...");
            }

            System.out.println("disconnected✅"); // Debugging helper: delete if it's annoying 

        } catch (IOException e) {
            System.err.println("Error diconnecting: " + e.getMessage());
        }
    }

    /**
     * Send a message to the server
     * @param message
     */
    public void sendMessage(Message message) {
        System.out.println("Checking connection...");
        if (!this.connected) {
            System.out.println("Not connected, trying to connect...");
            if (!connect()) {
                return; 
            }
        }

        System.out.println("Sending message...");
        this.executorService.submit(() -> {
            this.messageSocket.sendMessage(message);
            System.out.println("Message sent✅");
        });
    }

    /**
     * Validate username and password format using regex 
     * @param username
     * @param password
     * @return true if valid, false otherwise
     */
    public boolean validateCredentials(String username, String password) {
        if (VALID_CREDENTIALS.matcher(username).matches() && 
            VALID_CREDENTIALS.matcher(password).matches()) {
            System.out.println("Credentials valid✅");
            return true; 
        }
        System.out.println("Invalid CredentialsCredentials❌");
        return false; 
    }

    /**
     * Login to the server
     * @param username
     * @param password
     * @param callback
     */
    public CompletableFuture<Boolean>  login(String username, String password) {
        System.out.println("Logging in...");
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // setup one time event listener
        Consumer<UserCredResponse> listener = response -> {
            boolean success = response.isSuccess();
            if (success) {
                this.username = username;
                System.out.println("Login Successful");
            } else {
                System.out.println("Login failed");
            }
            future.complete(success);
        };

        EventBus.getInstance().subscribe(EventType.LOGIN_RESPONSE, listener);

        // send login request
        UserCredRequest request = new UserCredRequest("Login", username, password);
        sendMessage(request);

        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.LOGIN_RESPONSE, listener));
    }

    /**
     * Register a new user
     * @param username
     * @param password
     */
    public CompletableFuture<Boolean> register(String username, String password) {
        System.out.println("Registering...");
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Consumer<UserCredResponse> listener = response -> {
            boolean success = response.isSuccess();
            if (success) {
                this.username = username;
                System.out.println("Registration successful");
            } else {
                System.out.println("Registration failed");
            }
            future.complete(success);
        };

        EventBus.getInstance().subscribe(EventType.LOGIN_RESPONSE, listener);

        UserCredRequest request = new UserCredRequest("Register", username, password);
        sendMessage(request);

        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.LOGIN_RESPONSE, listener));
    }

    /**
     * Request to open a card pack
     * @param username
     * @param packName
     * @param cardCount
     */
    public CompletableFuture<JSONArray> openPack(String username, String packName, int cardCount) {
       System.out.println("Opening pack...");
       CompletableFuture<JSONArray> future = new CompletableFuture<>();

       Consumer<PackResponse> listener = response -> {
        JSONArray cards = response.getCards();
        System.out.println("Pack opened with " + cards.size() + " cards");
        future.complete(cards);
       };

       PackRequest request = new PackRequest(username, packName, cardCount);
       sendMessage(request);

       return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.PACK_RESPONSE, listener));
    }

    /**
     * Request a user's card collection
     * @param username
     */
    public CompletableFuture<JSONArray> getCollection(String username) {
        System.out.println("Getting collection...");
        CompletableFuture<JSONArray> future = new CompletableFuture<>();

        Consumer<CollectionResponse> listener = response -> {
            JSONArray collection = response.getCollection();
            System.out.println("Collection received with " + collection.size() + " cards");
            future.complete(collection);
        };

        EventBus.getInstance().subscribe(EventType.COLLECTION_RESPONSE, listener);

        CollectionRequest request = new CollectionRequest(username);
        sendMessage(request);

        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.COLLECTION_RESPONSE, listener));

    }

    /**
     * sends a trade intitiation message 
     * @param recipient
     * @param offeredCards
     */
    public CompletableFuture<String> initiateTrade(String recipient, JSONArray offeredCards) {
        System.out.println("Initiating trade with " + recipient);

        if (this.username == null) {
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("Not logged in"));
            return failedFuture;
        }

        TradeInitiateRequest request = new TradeInitiateRequest(this.username, recipient, offeredCards);
        sendMessage(request);

        System.out.println("Trade request sent");
        return CompletableFuture.completedFuture("Trade initiated");
    }

    /**
     * 
     * @param timeoutSeconds
     * @return
     */
    public CompletableFuture<TradeOfferNotification> waitForTradeOffer(long timeoutSeconds) {
        System.out.println("Waiting for trade offers...");
        CompletableFuture<TradeOfferNotification> future = new CompletableFuture<>();

        Consumer<TradeOfferNotification> listener = offer -> {
            System.out.println("Received trade offer from: " + offer.getSenderUsername());
            future.complete(offer);
        };

        EventBus.getInstance().subscribe(EventType.TRADE_OFFER, listener);

        // schedule timeout
        executorService.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("No trade offer reveived"));
                EventBus.getInstance().unsubscribe(EventType.TRADE_OFFER, listener);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return future.whenComplete((result, ex) -> {
            if (result != null) {
                EventBus.getInstance().unsubscribe(EventType.TRADE_OFFER, listener);
            }
        });
    }

    /**
     * sends a trade response message 
     * @param tradeId
     * @param accept
     */
    public CompletableFuture<Boolean> respondToTrade(String tradeId, boolean accept) {
        System.out.println("Responding to trade " + tradeId + ": " + (accept ? "accept" : "reject"));
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Consumer<TradeResponse> listener = response -> {
            boolean success = response.isAccepted() == accept;
            System.out.println("Trade response processed: " + success);
            future.complete(success);
        };

        EventBus.getInstance().subscribe(EventType.TRADE_RESPONSE, listener);

        TradeResponse response = new TradeResponse(tradeId, accept, this.username);
        sendMessage(response);

        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.TRADE_RESPONSE, listener));
    }

    /**
     * handl counteroffers from the server
     * @param originalTradeId
     * @param recipient
     * @param offeredCards
     * @return
     */
    public CompletableFuture<Boolean> sendCounterOffer(String tradeId, JSONArray offeredCards) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CounterOfferRequest request = new CounterOfferRequest(tradeId, this.username, offeredCards);
        sendMessage(request);

        // set up listener for trade response
        Consumer<TradeResponse> listener = response -> {
            if (response.getTradeId().equals(tradeId)) {
                future.complete(true);
            }
        };

        EventBus.getInstance().subscribe(EventType.TRADE_RESPONSE, listener);
        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.TRADE_RESPONSE, listener));

    }

    /**
     * send final confirmation for a trade
     * @param tradeId
     * @param confirmed
     * @return
     */
    public CompletableFuture<Boolean> confirmTrade(String tradeId, boolean confirmed) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        TradeConfirmationRequest request = new TradeConfirmationRequest(tradeId, this.username, confirmed);
        sendMessage(request);

        // set up listenere for trade response
        Consumer<TradeResponse> listener = response -> {
            if (response.getTradeId().equals(tradeId)) {
                future.complete(response.isAccepted() == confirmed);
            }
        };

        EventBus.getInstance().subscribe(EventType.TRADE_RESPONSE, listener);

        return future.whenComplete((result, ex) -> EventBus.getInstance().unsubscribe(EventType.TRADE_RESPONSE, listener));

    }

    /**
     * wait for incoming counteroffers
     * @param timeoutSeconds
     * @return
     */
    public CompletableFuture<CounterOfferRequest> waitForCounterOffer(long timeoutSeconds) {
        System.out.println("Waiting for counteroffers...");
        CompletableFuture<CounterOfferRequest> future = new CompletableFuture<>();

        Consumer<CounterOfferRequest> listener = offer -> {
            System.out.println("Received counteroffer for trade: " + offer.getOriginalTradeId()); // <- getTradeId???
            future.complete(offer);
        };

        EventBus.getInstance().subscribe(EventType.COUNTER_OFFER, listener);

        executorService.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("No counteroffer received"));
                EventBus.getInstance().unsubscribe(EventType.COUNTER_OFFER, listener);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return future.whenComplete((result, ex) -> {
            if (result != null) {
                EventBus.getInstance().unsubscribe(EventType.COUNTER_OFFER, listener);
            }
        });
    }

    /**
     * Set the current username
     * @param username
     */
    public void setUsername(String username) {
        this.username = username; 
    }

    /**
     * Get the current username
     * @return
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Check if connected to the server
     * @return true if so, false otherwise
     */
    public boolean isConnected() {
        System.out.println("is connected: " + this.connected);
        return this.connected; 
    }

    /**
     * Clean up resources when done
     */
    public void shutdown() {
        disconnect();
        System.out.println("Shutting executor down");
        this.executorService.shutdown();

    }
}
