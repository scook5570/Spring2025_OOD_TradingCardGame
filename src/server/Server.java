package server;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 
 */
public class Server {
    public static void main(String[] args) {

        UserCredentials userCreds = new UserCredentials(new File("src/server/databases/users.json"));
        UserCardsDatabase userCardsDatabase = new UserCardsDatabase(new File("src/server/databases/usercards.json"));
        TradeDatabase tradeDatabase = new TradeDatabase(new File("src/server/databases/trades.json"));

        ServerConnectionHandler handler = new ServerConnectionHandler();

        // schedule trade cleanup task
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {

            

            try {
                tradeDatabase.cleanupStaleTrades(30000); // 30 seconds
            } catch (Exception e) {
                System.err.println("Error during trade cleanup: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS); // run every 30 seconds
        
        // schedule database integrity check
        scheduler.scheduleAtFixedRate(() -> {
        try {
                boolean isConsistent = handler.validateDatabaseIntegrity();
                if (isConsistent) {
                    System.out.println("Database integrity check: PASSED");
                } else {
                    userCardsDatabase.validateAndRepairDatabaseIntegrity();
                }
            } catch (Exception e) {
                System.err.println("Error durign database integrity check: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS); // run every 30 seconds 

        handler.start(5100, userCreds, userCardsDatabase, tradeDatabase); // or get port from args

        // shutdown hook to clean up resources when server is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            System.out.println("Server shutting down");
        }));
    }
}
