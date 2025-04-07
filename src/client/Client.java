package client;

import java.net.Socket;

import shared.MessageSocket;
import shared.messages.Message;
import shared.messages.UserCredRequest;
import shared.messages.UserCredResponse;

public class Client {
    public static void main(String[] args) {
        String serString = "localhost";
        int port = 5000;

        try {
            Socket socket = new Socket(serString, port);
            System.out.println("Connected to server at " + serString + ":" + port);

            MessageSocket messageSocket = new MessageSocket(socket);
            System.out.println("MessageSocket created");

            UserCredRequest userCredRequest = new UserCredRequest("Login", "username", "password");
            System.out.println("Sending UserCredRequest: " + userCredRequest.toJSONType().toString());

            messageSocket.sendMessage(userCredRequest);
            System.out.println("UserCredRequest sent");

            Message response = messageSocket.getMessage();

            System.out.println("Received response: " + response.toJSONType().toString());

            if (!(response instanceof UserCredResponse)) {
                System.err.println("Error: Expected UserCredResponse, but got " + response.getType());
            } else {
                UserCredResponse userCredResponse = (UserCredResponse) response;
                if (userCredResponse.isSuccess()) {
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Login failed...");
                }
            }

            messageSocket.close();
            socket.close();
            System.out.println("Socket closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
