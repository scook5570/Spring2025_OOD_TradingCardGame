package server;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import shared.messages.*;

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

    public void handlePackRequest(PackRequest packRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePackRequest'");
    }

    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }
}
