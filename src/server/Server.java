package server;

import java.io.File;

public class Server {
    public static void main(String[] args) {

        UserCredentials userCreds = new UserCredentials(new File("src/server/databases/users.json"));
        
        ServerConnectionHandler handler = new ServerConnectionHandler();
        handler.start(5000, userCreds); // or get port from args
    }
}
