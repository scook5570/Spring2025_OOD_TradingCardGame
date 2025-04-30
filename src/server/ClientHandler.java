package server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap; // description in ServerConnectionHandler

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.MessageSocket;
import shared.messages.*;

/**
 * Handles client operations 
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private MessageSocket msgSocket;
    private ServerConnectionHandler server;
    private String username;
    private ConcurrentHashMap<String, Long> pendingTradeResponses = new ConcurrentHashMap<>();

    /**
     * constructor 
     * @param socket
     * @param server
     */
    public ClientHandler(Socket socket, ServerConnectionHandler server) {
        this.socket = socket;
        this.server = server;
        try {
            this.msgSocket = new MessageSocket(socket);
        } catch (IOException e) {
            System.err.println("Could not create MessageSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

   /**
    * runs the client side with better trade handling 
    */
    @Override
    public void run() {
        try {

            while (!socket.isClosed()) {

                Message recvMsg = msgSocket.getMessage();
                if (recvMsg instanceof AvailableUsersRequest) {
                    try {
                        AvailableUsersRequest request = (AvailableUsersRequest) recvMsg;
                        JSONArray availableUsers = server.handleAvailableUsersRequest(request);
                        AvailableUsersResponse response = new AvailableUsersResponse(availableUsers);
                        sendMessage(response);
                    } catch (Exception e) {
                        System.err.println("Error handling available users request: " + e.getMessage());
                    }
                } else if (recvMsg instanceof TradeInitiateRequest) {
                    try {
                        TradeInitiateRequest tradeRequest = (TradeInitiateRequest) recvMsg;
                        String tradeId = server.handleTradeInitiation(tradeRequest);

                        if (tradeId != null) {
                            pendingTradeResponses.put(tradeId, System.currentTimeMillis());
                            System.out.println("Trade initiated successfully, waiting for response...");
                        } else {
                            TradeResponse errorResponse = new TradeResponse("error", false, tradeRequest.getUsername());
                            sendMessage(errorResponse);
                        }
                    } catch (Exception e) {
                        System.err.println("error handling trade request: " + e.getMessage());
                    }
                } else if (recvMsg instanceof TradeResponse) {
                    try {
                        TradeResponse tradeResponse = (TradeResponse) recvMsg;
                        boolean success = server.handleTradeResponse(tradeResponse);

                        if (!success) {
                            System.err.println("Failed to process trade response for trade ID: " + tradeResponse.getTradeId());
                        }

                        // remove from pending responses
                        pendingTradeResponses.remove(tradeResponse.getTradeId());
                    } catch (Exception e) {
                        System.err.println("Error handling trade response: " + e.getMessage());
                    }
                } else if (recvMsg instanceof UserCredRequest) {
                    UserCredRequest userCredRequest = (UserCredRequest) recvMsg;
                    switch (recvMsg.getType()) {
                        case "Login":
                            // these methods must be implemented in the ServerConnectionHandler class
                            // to handle the different client connections
                            if (server.handleLogin(userCredRequest)) {
                                this.username = userCredRequest.getUsername();
                                server.addClient(this.username, this);
                                System.out.println("DEBUG: Registered client for " + this.username);
                                UserCredResponse response = new UserCredResponse(true);
                                sendMessage(response);
                            } else {
                                UserCredResponse response = new UserCredResponse(false);
                                sendMessage(response);
                            }
                            break;
                        case "Register":
                            if(server.handleRegistration(userCredRequest)) {
                                this.username = userCredRequest.getUsername();
                                server.addClient(this.username, this);
                                UserCredResponse response = new UserCredResponse(true);
                                sendMessage(response);
                            } else {
                                UserCredResponse response = new UserCredResponse(false);
                                sendMessage(response);
                            }
                            break;
                        default:
                            System.err.println("Unknown message type: " + recvMsg.getType());
                    }
                } else if (recvMsg instanceof PackRequest) {
                    PackRequest packRequest = (PackRequest) recvMsg;
                    switch (recvMsg.getType()) {
                        case "PackRequest":
                            JSONArray cards = server.handlePackRequest(packRequest);
                            PackResponse packResponse = new PackResponse(cards);
                            sendMessage(packResponse);
                            break;
                        default:
                            System.err.println("Unknown message type: " + recvMsg.getType());
                    }
                } else if (recvMsg instanceof CollectionRequest) {
                    CollectionRequest collectionRequest = (CollectionRequest) recvMsg;
                    switch (recvMsg.getType()) {
                        case "CollectionRequest":
                            JSONArray collection = server.handleCollectionRequest(collectionRequest);
                            CollectionResponse collectionResponse = new CollectionResponse(collection);
                            sendMessage(collectionResponse);
                            break;
                        default:
                            System.err.println("Unknown message type: " + recvMsg.getType());
                    }
                } else if (recvMsg instanceof CollectionResponse) {
                    System.out.println("ClientHandler.java, line 86: Collection Response handling not yet specified");
                } else if (recvMsg instanceof TradeInitiateRequest) {
                    TradeInitiateRequest tradeRequest = (TradeInitiateRequest) recvMsg;
                    String tradeId = server.handleTradeInitiation(tradeRequest);
                    if (tradeId != null) {
                        System.out.println("Trade initiated successfully, trade ID: " + tradeId + " waiting for response");
                    }
                } else if (recvMsg instanceof TradeResponse) {
                    TradeResponse tradeResponse = (TradeResponse) recvMsg;
                    server.handleTradeResponse(tradeResponse);
                } else if (recvMsg instanceof CounterOfferRequest) {
                    CounterOfferRequest counterOffer = (CounterOfferRequest) recvMsg;
                    boolean success = server.handleCounterOffer(counterOffer);

                    if (!success) {
                        System.err.println("Counter offer failed: " + counterOffer.getTradeId());
                    }
                } else if (recvMsg instanceof TradeConfirmationRequest) {
                    TradeConfirmationRequest confirmation = (TradeConfirmationRequest) recvMsg;
                    boolean success = server.handleTradeConfirmation(confirmation);

                    if (!success) {
                        System.err.println("Confirmation failed: " + confirmation.getTradeId());
                    }
                } else {
                    System.err.println("Unknown message type: " + recvMsg.getType());
                }
            }
        } catch (Exception e) {
            System.err.println("Connection closed: " + e.getMessage());
        } finally {
            
            if (this.username != null) { // cleanup code for when the connection ends
                server.removeClient(this.username);

                // cancel any pending trades when the usr disconnects (lmk if you want to add functionality to be able to complete trades even if the user is offline)
                for (String tradeId : pendingTradeResponses.keySet()) {
                    try {
                        JSONObject trade = server.tradeDatabase.getTrade(tradeId);
                        if (trade != null && "pending".equals(trade.getString("status"))) {
                            server.tradeDatabase.updateTradeStatus(tradeId, "cencelled");
                        }

                        // notify the other party 
                        String otherUsername = this.username.equals(trade.getString("initiator")) ? trade.getString("recipient") : trade.getString("initiator");
                        ClientHandler otherHandler = server.clients.get(otherUsername);

                        if (otherHandler != null) {
                            TradeResponse cancelResponse = new TradeResponse(tradeId, false, this.username);
                            otherHandler.sendMessage(cancelResponse);
                        }
                    } catch (Exception e) {
                        System.err.println("error cancelling trade on disconnect: " + e.getMessage());
                    }
                }
            }
        }
    }

    // method to send a message to the client
    public void sendMessage(Message message) {
        msgSocket.sendMessage(message);
    }
}
