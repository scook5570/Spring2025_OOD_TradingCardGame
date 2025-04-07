package server;

public class Server {
    public static void main(String[] args) {
        ServerConnectionHandler handler = new ServerConnectionHandler();
        handler.start(5000); // or get port from args
    }
}
