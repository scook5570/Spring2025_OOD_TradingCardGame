package client;

import java.io.IOException;

import java.net.Socket;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 

import java.util.regex.Pattern;

import java.util.function.Consumer; 

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
    private ExecutorService executorService; 

    // Callbacks for different message types 
    private Consumer<UserCredResponse> loginCallback; 
    private Consumer<PackResponse> packCallback;
    private Consumer<CollectionResponse> collectionCallback;
    private Consumer<TradeOfferNotification> tradeOfferCallback;
    private Consumer<TradeResponse> tradeResponseCallback;
    
    //Regular expressrion for validating username and password 
    private static final Pattern VALID_CREDENTIALS = Pattern.compile("[a-zA-Z0-9_]{3,16}$");

    /**
     * Private constructor for singleton pattern
     */
    private ClientConnectionHandler() {
        this.serverAddress = "localhost";
        this.serverPort = 5100; // CHANGE IF NEEDED, MY MAC's 5000 PORT IS OCCUPIED
        this.executorService = Executors.newCachedThreadPool();
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
        System.out.println("Handling response...");
        if (response instanceof UserCredResponse) {
            if (this.loginCallback != null) {
                System.out.println("Handling User Cred Request...");
                loginCallback.accept((UserCredResponse) response);
                System.out.println("Request Handled✅");
            }  
        } else if (response instanceof PackResponse) {
            if (this.packCallback != null) {
                System.out.println("Handling Pack Request...");
                packCallback.accept((PackResponse) response);
                System.out.println("Request Handled✅");
            }
        } else if (response instanceof CollectionResponse) {
            if (this.collectionCallback != null) {
                System.out.println("Handling Collection Request...");
                collectionCallback.accept((CollectionResponse) response);
                System.out.println("Request Handled✅");
            }
        } else if (response instanceof TradeOfferNotification) {
            if (this.tradeOfferCallback != null) {
                System.out.println("Handling Trade Offer Notification...");
                tradeOfferCallback.accept((TradeOfferNotification) response);
                System.out.println("Notification Handled✅");
            }
        } else if (response instanceof TradeResponse) {
            if (this.tradeResponseCallback != null) {
                System.out.println("Handling Trade Response...");
                tradeResponseCallback.accept((TradeResponse) response);
                System.out.println("Response Handled✅");
            }
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
    public void login(String username, String password, Consumer<UserCredResponse> callback) {
        System.out.println("Logging in...");
        this.loginCallback = callback; 
        UserCredRequest request = new UserCredRequest("Login", username, password);
        sendMessage(request);
        System.out.println("Login successful✅");
    }

    /**
     * Register a new user
     * @param username
     * @param password
     * @param callback
     */
    public void register(String username, String password, Consumer<UserCredResponse> callback) {
        System.out.println("Registering...");
        this.loginCallback = callback; 
        UserCredRequest request = new UserCredRequest("Register", username, password);
        sendMessage(request);
        System.out.println("Registering successful✅");
    }

    /**
     * Request to open a card pack
     * @param username
     * @param packName
     * @param cardCount
     * @param callback
     */
    public void openPack(String username, String packName, int cardCount, Consumer<PackResponse> callback) {
        System.out.println("Opening pack...");
        this.packCallback = callback; 
        PackRequest request = new PackRequest(username, packName, cardCount);
        sendMessage(request);
        System.out.println("Pack opened✅");
    }

    /**
     * Request a user's card collection
     * @param username
     * @param callback
     */
    public void getCollection(String username, Consumer<CollectionResponse> callback) {
        System.out.println("Getting collection...");
        this.collectionCallback = callback; 
        CollectionRequest request = new CollectionRequest(username); 
        sendMessage(request);
        System.out.println("Collection received✅");
    }

    /**
     * sends a trade intitiation message 
     * @param recipient
     * @param offeredCards
     * @param callback
     */
    public void initiateTrade(String recipient, JSONArray offeredCards, Consumer<String> callback) {
        System.out.println("Initiating trade...");

        if (!this.connected) {
            System.out.println("Not connected, trying to connect...");
            if (!connect()) {
                return;
            }
        }

        TradeInitiateRequest request = new TradeInitiateRequest(this.username, recipient, offeredCards);
        sendMessage(request);
        System.out.println("Trade request sent");
    }

    /**
     * sends a trade response message 
     * @param tradeId
     * @param accept
     * @param callback
     */
    public void respondToTrade(String tradeId, boolean accept, Consumer<TradeResponse> callback) {
        System.out.println("Responding to trade");

        if (!this.connected) {
            System.out.println("Not connected, trying to connect...");
            if (!connect()) {
                return;
            }
        }

        this.tradeResponseCallback = callback;
        TradeResponse response = new TradeResponse(tradeId, accept, this.username);
        sendMessage(response);
        System.out.println("Trade response sent");
    }

    public void setTradeOfferCallback(Consumer<TradeOfferNotification> callback) {
        this.tradeOfferCallback = callback;
    }

    public void setTradeResponseCallback(Consumer<TradeResponse> callback) {
        this.tradeResponseCallback = callback;
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
