package client;

import java.net.Socket;
import java.util.Scanner;

import shared.MessageSocket;
import shared.messages.Message;
import shared.messages.UserCredRequest;
import shared.messages.UserCredResponse;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000;

        try (Scanner scanner = new Scanner(System.in)) {
            // Connect to the server
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Connected to server at " + serverAddress + ":" + port);

            MessageSocket messageSocket = new MessageSocket(socket);
            System.out.println("MessageSocket created");

            // CLI for user input
            System.out.println("Choose an option:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            String requestType;
            if (choice == 1) {
                requestType = "Register";
            } else if (choice == 2) {
                requestType = "Login";
            } else {
                System.out.println("Invalid choice. Exiting...");
                messageSocket.close();
                socket.close();
                return;
            }

            // Prompt for username and password
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Create and send the UserCredRequest
            UserCredRequest userCredRequest = new UserCredRequest(requestType, username, password);
            System.out.println("Sending " + requestType + " request...");
            messageSocket.sendMessage(userCredRequest);

            // Receive and process the response
            Message response = messageSocket.getMessage();
            if (response instanceof UserCredResponse) {
                UserCredResponse userCredResponse = (UserCredResponse) response;
                if (userCredResponse.isSuccess()) {
                    System.out.println(requestType + " successful!");
                } else {
                    System.out.println(requestType + " failed...");
                }
            } else {
                System.err.println("Unexpected response type: " + response.getType());
            }

            // Close the connection
            messageSocket.close();
            socket.close();
            System.out.println("Socket closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
