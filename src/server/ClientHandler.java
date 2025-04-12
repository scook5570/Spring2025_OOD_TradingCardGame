package server;

import java.io.IOException;
import java.net.Socket;

import merrimackutil.json.types.JSONArray;
import shared.MessageSocket;
import shared.messages.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MessageSocket msgSocket;
    private ServerConnectionHandler server;
    private String username;

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

    @Override
    public void run() {
        Message recvMsg;
        // Receive the message from the client
        recvMsg = msgSocket.getMessage();

        if (recvMsg instanceof UserCredRequest) {
            UserCredRequest userCredRequest = (UserCredRequest) recvMsg;
            switch (recvMsg.getType()) {
                case "Login":
                    // these methods must be implemented in the ServerConnectionHandler class
                    // to handle the different client connections
                    if (server.handleLogin(userCredRequest)) {
                        this.username = userCredRequest.getUsername();
                        server.addClient(this.username, this);
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
            // Handle CollectionResponse if needed
        } else {
            System.err.println("Unknown message type: " + recvMsg.getType());
        }
    }

    // Method to send a message to the client
    public void sendMessage(Message message) {
        msgSocket.sendMessage(message);
    }
}
