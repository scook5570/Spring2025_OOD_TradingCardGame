package client;

//import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import shared.messages.TradeOfferNotification;

public class Client {
    public static void main(String[] args) {
        // String serverAddress = "localhost";
        // int port = 5000;
        String username = null;
        String password = null;

        try (Scanner scanner = new Scanner(System.in)) {

            ClientConnectionHandler connectionHandler = ClientConnectionHandler.getInstance();
            connectionHandler.startConnectionMnitor(); // start connection monitor
            BooleanWrapper loggedIn = new BooleanWrapper(); //for use in the lamba expression
            loggedIn.value = false;

            while (!loggedIn.value) {

                // CLI for user input
                System.out.println("Choose an option:");
                System.out.println("1. Register");
                System.out.println("2. Login");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                if (choice != 1 && choice != 2) {
                    System.out.println("Invalid choice. Exiting...");
                    continue; 
                }

                // Prompt for username and password
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password: ");
                password = scanner.nextLine();

                //Validate username/password
                if (!connectionHandler.validateCredentials(username, password)) {
                    System.out.println("Invalid username or password format (3-16 alphanumeric characters)");
                    return;
                }

                //String requestType;
                if (choice == 1) {
                    System.out.println("Sending Register request...");
                    connectionHandler.connect(); // make sure connection is there

                    connectionHandler.register(username, password);
                } else if (choice == 2) {
                    //requestType = "Login";
                    System.out.println("Sending login request...");
                    connectionHandler.connect();
                    
                    //connectionHandler.login(username, password);
                    try {
                        boolean success = connectionHandler.login(username, password).get(10, TimeUnit.SECONDS);
                        if (success) {
                            loggedIn.value = true;
                            System.out.println("Login Successful - proceeding to home page");
                        } else {
                            System.out.println("Login failed, Please try again");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during login: " + e.getMessage());
                    }

                } else {
                    System.out.println("Invalid choice. Exiting...");
                    connectionHandler.disconnect();
                    continue;
                }
                
                // waiting for response (callbacks are asynchronous)
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } 
            }

            // Home page loop
            boolean running = true;
            while (running && loggedIn.value) {
                System.out.println("Home Page:");
                System.out.println("1. Open a pack");
                System.out.println("2. Get collection");
                System.out.println("3. Initiate a trade");
                System.out.println("4. View pending trades");
                System.out.println("5. Log out");
                System.out.println("Select an option: ");

                int homeChoice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                if (homeChoice == 1) {
                    System.out.println("Opening a pack...");
                    connectionHandler.openPack(username, "StandardPack", 5);        
                    try { //waiting for the response to be processed
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (homeChoice == 2) {
                    System.out.println("Retrieving collection...");
                    connectionHandler.getCollection(username).thenAccept(cards -> {
                        System.out.println("\nYour collection contains the following cards:");
                        displayCards(cards);
                    }).exceptionally(ex -> {
                        System.err.println("Error retrieving collection: " + ex.getMessage());
                        return null;
                    });      
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (homeChoice == 3) {
                    initiateTradeFlow(scanner, connectionHandler, username);
                } else if (homeChoice == 4) {
                    viewPendingTradesFlow(scanner, connectionHandler, username);
                } else if (homeChoice == 5) {
                    System.out.println("Logging out...");
                    connectionHandler.disconnect();
                    running = false;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }

            connectionHandler.shutdown();
            System.out.println("Thank you for playing");

        } catch (Exception e) {
            System.err.println("An error occured: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * handles the intitiation of a trading process
     * @param scanner
     * @param connectionHandler
     * @param username
     */
    private static void initiateTradeFlow(Scanner scanner, ClientConnectionHandler connectionHandler, String username) {
        try {
            // step 1 : get available users to trade with
            System.out.println("Fetching available users...");
            JSONArray availableUsers = connectionHandler.getAvailableTradeUsers().get(10, TimeUnit.SECONDS);

            if (availableUsers == null || availableUsers.size() == 0) {
                System.out.println("No users available for trading");
                return;
            }

            // step 2 : display availabe users
            System.out.println("Available users to trade with:");
            for (int i = 0; i < availableUsers.size(); i++) {
                JSONObject user = (JSONObject) availableUsers.get(i);
                System.out.println((i+1) + "." + user.getString("username"));
            }

            // step 3 : select recipient 
            System.out.println("Enter the number of the user you want to trade with (or 0 to cancel): ");
            int userChoice = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            if (userChoice <= 0 || userChoice > availableUsers.size()) {
                System.out.println("Trade canceled.");
                return;
            }

            JSONObject userObj = (JSONObject) availableUsers.get(userChoice - 1);
            String recipient = userObj.getString("username");
            System.out.println("Selected user: " + recipient);

            // step 4 : get user's cards
            System.out.println("Fetching your card collection");
            JSONArray userCards = connectionHandler.getCollection(username).get(10, TimeUnit.SECONDS);
        
            if (userCards == null || userCards.size() == 0) {
                System.out.println("You don't have any cards to trade");
                return;
            }

            // step 5 : display user's cards
            System.out.println("Your cards:");
            for (int i = 0; i < userCards.size(); i++) {
                JSONObject card = (JSONObject) userCards.get(i);
                System.out.println((i + 1) + "." + card.getString("name") + " (Rarity " + card.getInt("rarity") + ", ID: " + card.getString("cardID") + ")");
            }

            // step 6 : select cards to offer
            System.out.println("Enter numbers of the cards you want to offer (comma-separated, or 0 to cancel): ");
            String cardChoices = scanner.nextLine();

            if (cardChoices.equals("0")) {
                System.out.println("Trade canceled");
                return;
            }

            String[] choices = cardChoices.split(",");
            JSONArray selectedCards = new JSONArray();

            for (String choice : choices) {
                try {
                    int cardIndex = Integer.parseInt(choice.trim()) - 1;
                    if (cardIndex >= 0 && cardIndex < userCards.size()) {
                        selectedCards.add(userCards.get(cardIndex));
                    }
                } catch (NumberFormatException e) {
                    // skip invalid numbers
                }
            }

            if (selectedCards.size() == 0) {
                System.out.println("No valid cards selected. Trade canceled");
                return;
            }

            System.out.println("You're offering " + selectedCards.size() + " cards to " + recipient + ". Confirm? (y/n)");
            String confirm = scanner.nextLine();

            if (!confirm.toLowerCase().startsWith("y")) {
                System.out.println("Trade cancelled");
                return;
            }

            // step 7 : initiate the trade
            System.out.println("Initiating trade...");
            String result = connectionHandler.initiateTrade(recipient, selectedCards).get(10, TimeUnit.SECONDS);

            System.out.println("Trade initiated: " + result);
            System.out.println("Waiting for " + recipient + " to respond");
        } catch (Exception e) {
            System.err.println("Error initiatin trade: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * allows users to view trades addressed to them
     * @param scanner
     * @param connectionHandler
     * @param username
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private static void viewPendingTradesFlow(Scanner scanner, ClientConnectionHandler connectionHandler, String username) throws InterruptedException, ExecutionException, TimeoutException {
        try
            {System.out.println("Waiting for trade notifications...");

            // wait for trade notifications with timeout 
            TradeOfferNotification offer = connectionHandler.waitForTradeOffer(30).get(10, TimeUnit.SECONDS);
            
            if (offer == null) {
                System.out.println("No trade offers received");
                return;
            }

            System.out.println("Trade offer received from: " + offer.getUsername());
            System.out.println("Cards offered:");
            displayCards(offer.getOfferedCards());

            String tradeStage = offer.getTradeStage();

            if (tradeStage.equals("initial")) {
                // initial offer - recipient needs to counter-offer
                System.out.println("do you want to respond to this trade? (y/n): ");
                String response = scanner.nextLine();

                if (!response.equalsIgnoreCase("y")) {
                    connectionHandler.respondToTrade(offer.getTradeID(), false).get(10, TimeUnit.SECONDS);
                    System.out.println("Trade rejected.");
                    return;
                }

                // get recpient's cards for counter-offer
                JSONArray userCards = connectionHandler.getCollection(username).get(10, TimeUnit.SECONDS);

                if (userCards == null || userCards.size() == 0) {
                    System.out.println("You don't have any cards to offer in return");
                    return; 
                }

                // display user's cards
                System.out.println("Your cards:");
                for (int i = 0; i < userCards.size(); i++) {
                    JSONObject card = (JSONObject) userCards.get(i);
                    System.out.println((i + 1) + ". " + card.getString("name")+ " (Rarity: " + card.getInt("rarity") + ")");
                }

                // select cards for counter offer
                System.out.println("Enter the numbers of the cards you want to offer in return (comma-separated): ");
                String cardChoices = scanner.nextLine();

                String[] choices = cardChoices.split(",");
                JSONArray selectedCards = new JSONArray();

                for (String choice : choices) {
                    int cardIndex = Integer.parseInt(choice.trim()) - 1;
                    if (cardIndex >= 0 && cardIndex <userCards.size()) {
                        selectedCards.add(userCards.get(cardIndex));
                    }
                }

                if (selectedCards.size() == 0) {
                    System.out.println("No valid cards selected. Trade canceled");
                    return;
                }

                // send counter offer
                boolean success = connectionHandler.sendCounterOffer(offer.getTradeID(), selectedCards).get(10, TimeUnit.SECONDS);

                if (success) {
                    System.out.println("Counter-offer sent. Waiting for initiator to confirm");
                } else {
                    System.out.println("Failed to send counter-offer");
                }
            } else if (tradeStage.equals("counterOffer")) {
                // counter-offer received - initiator needs to confirm
                System.out.println("The recipient has counter-offered with these cards:");
                displayCards(offer.getOfferedCards());

                System.out.println("Do you want to accept this counter-offer? (y/n)");
                String response = scanner.nextLine();

                boolean confirmed = response.equalsIgnoreCase("y");

                boolean success = connectionHandler.confirmTrade(offer.getTradeID(), confirmed).get(10, TimeUnit.SECONDS);

                if (success) {
                    System.out.println("Trade " + (confirmed ? "completed" : "rejected") + " successfully");
                
                    if (confirmed) {
                        System.out.println("The cards have been exchanged!");
                    }
                } else {
                    System.out.println("Failed to process traded confirmation");
                }
            }

            System.out.println("Press enter to return to the main menu");
            scanner.nextLine();
        } catch (Exception e) {
            System.err.println("Error initiating trade: " + e.getMessage());
        } 
    }

    private static void displayCards(JSONArray cards) {
        if (cards == null || cards.size() == 0) {
            System.out.println("No cards found");
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            JSONObject card = (JSONObject) cards.get(i);
            System.out.println("----------------------------------");
            System.out.println("CardID: " + card.getString("cardID"));
            System.out.println("Name: " + card.getString("name"));
            System.out.println("Rarity: " + card.getInt("rarity"));
            System.out.println("Image Link: " + card.getString("imageLink"));
        }
        System.out.println("----------------------------------");
    }
}