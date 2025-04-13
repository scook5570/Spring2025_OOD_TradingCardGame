package client;

import java.net.Socket;
import java.util.Scanner;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.MessageSocket;
import shared.messages.CollectionRequest;
import shared.messages.CollectionResponse;
import shared.messages.Message;
import shared.messages.PackRequest;
import shared.messages.PackResponse;
import shared.messages.UserCredRequest;
import shared.messages.UserCredResponse;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000;
        String username = null;
        String password = null;

        try (Scanner scanner = new Scanner(System.in)) {
            boolean loggedIn = false;

            while (!loggedIn) {
                // Connect to the server
                MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
                System.out.println("Connected to server at " + serverAddress + ":" + port);

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
                    continue;
                }

                // Prompt for username and password
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password: ");
                password = scanner.nextLine();

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
                        if (requestType.equals("Login")) {
                            loggedIn = true;
                        }
                    } else {
                        System.out.println(requestType + " failed...");
                    }
                } else {
                    System.err.println("Unexpected response type: " + response.getType());
                }

                // Close the connection
                messageSocket.close();
                System.out.println("Socket closed");
            }

            // Home page loop
            boolean running = true;
            while (running) {
                System.out.println("Home Page:");
                System.out.println("1. Open a pack");
                System.out.println("2. Get collection");
                System.out.println("3. Log out");
                int homeChoice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                if (homeChoice == 1) {
                    System.out.println("Opening a pack...");
                    MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
                    System.out.println("Connected to server at " + serverAddress + ":" + port);
                    PackRequest packRequest = new PackRequest(username, "PackNamePlaceholder", 5);
                    messageSocket.sendMessage(packRequest);

                    Message response = messageSocket.getMessage();
                    if (response instanceof PackResponse) {
                        PackResponse packResponse = (PackResponse) response;
                        JSONArray cards = packResponse.getCards();
                        System.out.println("You opened a pack with the following cards:");
                        for (int i = 0; i < cards.size(); i++) {
                            JSONObject card = (JSONObject) cards.get(i);
                            System.out.println("Card ID: " + card.getString("cardID"));
                            System.out.println("Name: " + card.getString("name"));
                            System.out.println("Rarity: " + card.getInt("rarity"));
                            System.out.println("Image Link: " + card.getString("imageLink"));
                        }
                    } else {
                        System.err.println("Unexpected response type: " + response.getType());
                    }

                    messageSocket.close();
                } else if (homeChoice == 2) {
                    System.out.println("Retrieving collection...");
                    MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
                    System.out.println("Connected to server at " + serverAddress + ":" + port);
                    CollectionRequest collectionRequest = new CollectionRequest(username);
                    messageSocket.sendMessage(collectionRequest);

                    Message response = messageSocket.getMessage();
                    if (response instanceof CollectionResponse) {
                        CollectionResponse collectionResponse = (CollectionResponse) response;
                        JSONArray cards = collectionResponse.getCollection();
                        System.out.println("Your collection contains the following cards:");
                        for (int i = 0; i < cards.size(); i++) {
                            JSONObject card = (JSONObject) cards.get(i);
                            System.out.println("Card ID: " + card.getString("cardID"));
                            System.out.println("Name: " + card.getString("name"));
                            System.out.println("Rarity: " + card.getInt("rarity"));
                            System.out.println("Image Link: " + card.getString("imageLink"));
                        }
                    } else {
                        System.err.println("Unexpected response type: " + response.getType());
                    }
                } else if (homeChoice == 3) {
                    System.out.println("Logging out...");
                    running = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
