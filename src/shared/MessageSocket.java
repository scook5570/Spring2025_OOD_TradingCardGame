package shared;

import shared.messages.*;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Wraps socket class for use to send and receive JSON Messages
 */
public class MessageSocket extends Socket{
    private Scanner recv;
    private PrintWriter send;

    /**
     * Creates Message Socket from a socket
     * @note Useful for ServerSocket.accept();
     * @throws IOException Throws if IOStreams cannot be established
     */
    public MessageSocket(Socket sock) throws IOException {
        super(); //call parent class(socket)

        //get input streams ready
        try {
            //use provided socket
            this.recv = new Scanner(sock.getInputStream());
            this.send = new PrintWriter(sock.getOutputStream(),true);
        } catch (IOException e) {
            System.err.println("Message socket could not get io streams setup");
            //rethrow consumer of class must deal with this error
            throw e;
        }
    }

    /**
     * Creates Message Socket from an addr and port
     * @param addr String - Hostname(IP address) for socket
     * @param port int - Port number for socket
     * @throws IOException Throws if IOStreams cannot be established
     */
    public MessageSocket(String addr, int port) throws IOException {
        super(addr,port); //call parent class(socket)

        //get input streams ready
        try {
            this.recv = new Scanner(this.getInputStream());
            this.send = new PrintWriter(this.getOutputStream(),true);
        } catch (IOException e) {
            System.err.println("Message socket could not get io streams setup");
            //rethrow consumer of class must deal with this error
            throw e;
        }
    }


    /**
     * Sends a Message on the socket
     * @param msg Message Object to send
     */
    public void sendMessage(Message msg) {
        JsonIO.writeSerializedObject(msg, send);
    }

    /**
     * Receive a Message on the socket
     * @return Message of Object received
     */
    public Message getMessage() {
        //get message from sender
       String serializedMessage = recv.nextLine();

       //read object into JSON
       JSONObject obj = JsonIO.readObject(serializedMessage);

       Message message = new Message(obj);

       switch(message.getType()) {
           case "Login", "Register":
                return new UserCredRequest(obj);
            case "Status":
                return new UserCredResponse(obj);
           default:
               throw new RuntimeException("Message does not fit known type, got type: " + message.getType());
       }
    }
}
