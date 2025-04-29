package client;

//import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        // String serverAddress = "localhost";
        // int port = 5000;
        String username = null;
        String password = null;

        try (Scanner scanner = new Scanner(System.in)) {

            ClientConnectionHandler connectionHandler = ClientConnectionHandler.getInstance();
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
                    
                    connectionHandler.login(username, password);

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
                    connectionHandler.getCollection(username);      
                    
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
        System.out.println("Enter the username of the player you'd like to trade with: ");
        String recipient = scanner.nextLine();

        connectionHandler.getCollection(username);
    }

    /**
     * allows users to view trades addressed to them
     * @param scanner
     * @param connectionHandler
     * @param username
     */
    private static void viewPendingTradesFlow(Scanner scanner, ClientConnectionHandler connectionHandler, String username) {
        // set up the callback to handle incoming trade offers
        connectionHandler.waitForTradeOffer(15);

        System.out.println("Waiting for trade notifications...");
        System.out.println("Press enter to return to the main menu");
        scanner.nextLine();

    }
}