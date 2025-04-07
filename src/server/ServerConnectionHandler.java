package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import shared.messages.*;

public class ServerConnectionHandler {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public void start(int port) {
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
        return true;
    }

    public void handleRegistration(UserCredRequest userCredRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleRegistration'");
    }

    public void handlePackRequest(PackRequest packRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handlePackRequest'");
    }

    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
        UserCredResponse response = new UserCredResponse(true);
        clientHandler.sendMessage(response);
        System.out.println("Client " + username + " added to the server.");
    }

    public void removeClient(String username) {
        clients.remove(username);
    }
}
