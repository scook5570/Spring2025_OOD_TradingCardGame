package client;

import java.net.Socket;
import java.util.Scanner;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.MessageSocket;
import shared.messages.*;

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
                System.out.println("3. Send trade request");
                System.out.println("4. Respond to a trade");
                System.out.println("5. Log out");
                int homeChoice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                if (homeChoice == 1) {
                    System.out.println("Opening a pack...");
                    MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
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
                    messageSocket.close();
                } else if (homeChoice == 3) {
                    System.out.println("Retrieving list of users...");
                    MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
                    UserListRequest userListRequest = new UserListRequest();
                    messageSocket.sendMessage(userListRequest);

                    Message response = messageSocket.getMessage();
                    messageSocket.close();
                    if (response instanceof UserListResponse) {
                        UserListResponse userListResponse = (UserListResponse) response;
                        JSONArray users = userListResponse.getUsers();
                        System.out.println("Available users:");
                        for (int i = 0; i < users.size(); i++) {
                            String user = users.getString(i);
                            if (!user.equals(username)) { // Exclude the current user's username
                                System.out.println((i + 1) + ". " + user);
                            }
                        }

                        System.out.print("Select a user to send a trade request to (enter number): ");
                        int userChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume the newline character
                        if (userChoice < 1 || userChoice > users.size()) {
                            System.out.println("Invalid choice.");
                        } else {
                            String recipient = users.getString(userChoice - 1);
                            System.out.println("Retrieving your collection to select a card...");
                            MessageSocket messageSocket2 = new MessageSocket(new Socket(serverAddress, port));
                            CollectionRequest collectionRequest = new CollectionRequest(username);
                            messageSocket2.sendMessage(collectionRequest);

                            Message collectionResponse = messageSocket2.getMessage();
                            messageSocket2.close();
                            if (collectionResponse instanceof CollectionResponse) {
                                CollectionResponse collectionResp = (CollectionResponse) collectionResponse;
                                JSONArray cards = collectionResp.getCollection();
                                System.out.println("Your collection contains the following cards:");
                                for (int i = 0; i < cards.size(); i++) {
                                    JSONObject card = (JSONObject) cards.get(i);
                                    System.out.println((i + 1) + ". Card ID: " + card.getString("cardID") +
                                            " | Name: " + card.getString("name") +
                                            " | Rarity: " + card.getInt("rarity"));
                                }

                                System.out.print("Select a card to offer (enter number): ");
                                int cardChoice = scanner.nextInt();
                                scanner.nextLine(); // Consume the newline character

                                if (cardChoice < 1 || cardChoice > cards.size()) {
                                    System.out.println("Invalid choice.");
                                } else {
                                    JSONObject selectedCard = (JSONObject) cards.get(cardChoice - 1);
                                    String offerCardID = selectedCard.getString("cardID");

                                    TradeRequest tradeRequest = new TradeRequest(username, recipient, offerCardID);
                                    MessageSocket messageSocket3 = new MessageSocket(new Socket(serverAddress, port));
                                    messageSocket3.sendMessage(tradeRequest);
                                    System.out.println("Trade request sent to " + recipient + ".");

                                    Message tradeResponse = messageSocket3.getMessage();
                                    if (tradeResponse instanceof ServerTradeStatus) {
                                        ServerTradeStatus tradeStatus = (ServerTradeStatus) tradeResponse;
                                        System.out.println("Trade request status: " + tradeStatus.getMessage());
                                    } else {
                                        System.err.println("Unexpected response type: " + tradeResponse.getType());
                                    }
                                }
                            } else {
                                System.err.println("Unexpected response type: " + collectionResponse.getType());
                            }
                            messageSocket2.close();
                        }
                    } else {
                        System.err.println("Unexpected response type: " + response.getType());
                    }
                    messageSocket.close();
                } else if (homeChoice == 4) {
                    System.out.println("Retrieving active trades...");
                    MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
                    ViewTradesRequest viewTradesRequest = new ViewTradesRequest(username);
                    messageSocket.sendMessage(viewTradesRequest);

                    Message response = messageSocket.getMessage();
                    messageSocket.close();
                    if (response instanceof ViewTradesResponse) {
                        ViewTradesResponse viewTradesResponse = (ViewTradesResponse) response;
                        JSONArray trades = viewTradesResponse.getTrades();
                        System.out.println("Active trades:");

                        System.out.println("Trade Requests (You are the recipient):");
                        for (int i = 0; i < trades.size(); i++) {
                            JSONArray trade = trades.getArray(i);
                            if (trade == null || trade.size() < 4)
                                continue;

                            String type = trade.getString(0); // "request" or "response"
                            String requesterID = trade.getString(1);
                            String recipientID = trade.getString(2);
                            String offerCardID = trade.getString(3);

                            if ("request".equals(type) && username.equals(recipientID)) {
                                System.out.println(
                                        (i + 1) + ". From: " + requesterID + " | Offered Card: " + offerCardID);
                            }
                        }

                        System.out.println("\nTrade Responses (You are the original requester):");
                        for (int i = 0; i < trades.size(); i++) {
                            JSONArray trade = trades.getArray(i);
                            if (trade == null || trade.size() < 5)
                                continue; // Ensure at least 5 elements for response

                            String type = trade.getString(0); // "request" or "response"
                            String requesterID = trade.getString(1);
                            String recipientID = trade.getString(2);
                            String offerCardID = trade.getString(3);
                            String responseCardID = trade.getString(4); // Response card, could be null

                            if ("response".equals(type) && username.equals(requesterID)) {
                                System.out.println((i + 1) + ". From: " + recipientID + " | Offered Card: "
                                        + offerCardID + " | Response Card: "
                                        + (responseCardID == null ? "None" : responseCardID));
                            }
                        }

                        System.out.print("Select a trade to interact with (enter number): ");
                        int tradeChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        if (tradeChoice < 1 || tradeChoice > trades.size()) {
                            System.out.println("Invalid choice.");
                        } else {
                            JSONArray selectedTrade = trades.getArray(tradeChoice - 1);

                            String tradeType = selectedTrade.getString(0);
                            String requesterID = selectedTrade.getString(1);
                            String recipientID = selectedTrade.getString(2);
                            String offerCardID = selectedTrade.getString(3);
                            String responseCardID = selectedTrade.size() > 4 && selectedTrade.get(4) != null
                                    ? selectedTrade.getString(4)
                                    : null;

                            // Reconstruct the tradeKey if not provided in data
                            String tradeKey = requesterID + recipientID;

                            if (tradeType.equals("request") && recipientID.equals(username)) {
                                // You're the recipient, so you can respond to this trade
                                System.out.println("Retrieving your collection to select a card...");
                                MessageSocket messageSocket3 = new MessageSocket(new Socket(serverAddress, port));
                                CollectionRequest collectionRequest = new CollectionRequest(username);
                                messageSocket3.sendMessage(collectionRequest);

                                Message collectionResponse = messageSocket3.getMessage();
                                messageSocket3.close();
                                if (collectionResponse instanceof CollectionResponse) {
                                    CollectionResponse collectionResp = (CollectionResponse) collectionResponse;
                                    JSONArray cards = collectionResp.getCollection();
                                    System.out.println("Your collection contains the following cards:");
                                    for (int i = 0; i < cards.size(); i++) {
                                        JSONObject card = (JSONObject) cards.get(i);
                                        System.out.println((i + 1) + ". Card ID: " + card.getString("cardID") +
                                                " | Name: " + card.getString("name") +
                                                " | Rarity: " + card.getInt("rarity"));
                                    }

                                    System.out.print("Select a card to offer in response (enter number): ");
                                    int cardChoice = scanner.nextInt();
                                    scanner.nextLine(); // Consume the newline character

                                    if (cardChoice < 1 || cardChoice > cards.size()) {
                                        System.out.println("Invalid choice.");
                                    } else {
                                        JSONObject selectedCard = (JSONObject) cards.get(cardChoice - 1);
                                        String responseInputCardID = selectedCard.getString("cardID");

                                        TradeResponse tradeResponse = new TradeResponse(true, tradeKey, responseInputCardID);
                                        MessageSocket messageSocket4 = new MessageSocket(new Socket(serverAddress, port));
                                        messageSocket4.sendMessage(tradeResponse);

                                        Message tradeStatusResponse = messageSocket4.getMessage();
                                        if (tradeStatusResponse instanceof ServerTradeStatus) {
                                            ServerTradeStatus tradeStatus = (ServerTradeStatus) tradeStatusResponse;
                                            System.out.println("Trade response status: " + tradeStatus.getMessage());
                                        } else {
                                            System.err.println("Unexpected response type: " + tradeStatusResponse.getType());
                                        }
                                    }
                                } else {
                                    System.err.println("Unexpected response type: " + collectionResponse.getType());
                                }
                                messageSocket3.close();

                            } else if (tradeType.equals("response") && requesterID.equals(username)) {
                                // You're the original requester confirming a response
                                System.out.print("Do you want to confirm this trade? (yes/no): ");
                                String confirmation = scanner.nextLine();

                                if (confirmation.equalsIgnoreCase("yes")) {
                                    TradeConfirmation tradeConfirmation = new TradeConfirmation(tradeKey, true);
                                    MessageSocket messageSocket3 = new MessageSocket(new Socket(serverAddress, port));
                                    messageSocket3.sendMessage(tradeConfirmation);

                                    System.out.println("Trade confirmed successfully.");
                                } else {
                                    System.out.println("Trade confirmation canceled.");
                                }

                            } else {
                                System.out.println("You cannot interact with this trade.");
                            }
                        }
                    } else {
                        System.err.println("Unexpected response type: " + response.getType());
                    }

                    messageSocket.close();
                } else if (homeChoice == 5) {
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