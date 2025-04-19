package server;

import java.io.File;

public class Server {
    public static void main(String[] args) {

        UserCredentials userCreds = new UserCredentials(new File("src/server/databases/users.json"));
        UserCardsDatabase userCardsDatabase = new UserCardsDatabase(new File("src/server/databases/usercards.json"));
        TradeDatabase tradeDatabase = new TradeDatabase(new File("src/server/databases/trades.json"));

        ServerConnectionHandler handler = new ServerConnectionHandler();
        handler.start(5000, userCreds, userCardsDatabase, tradeDatabase); // or get port from args
    }
}
