package server;

import java.io.File;

public class Server {
    public static void main(String[] args) {

        UserCredentials userCreds = new UserCredentials(new File("src/server/databases/users.json"));
        UserCardsDatabase userCardsDatabase = new UserCardsDatabase(new File("src/server/databases/usercards.json"));
        TradeRequestDatabase tradeRequestDatabase = new TradeRequestDatabase(new File("src/server/databases/traderequests.json"));
        
        ServerConnectionHandler handler = new ServerConnectionHandler();
        handler.start(5000, userCreds, userCardsDatabase, tradeRequestDatabase); // or get port from args
    }
}
