package server;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

// creates activeusers.json file in the server directory
// to store the active users in the server -- and username/clientID pairs and socket info
public class ActiveUsers implements JSONSerializable {
    private HashMap<String, Socket> activeUsers;
    File file;

    public ActiveUsers(File file) {
        this.file = file;
        if (!file.exists() || file.length() == 0) {
            try {
                file.getParentFile().mkdir(); // Ensure directory exists
                if (!file.createNewFile()) {
                    System.err.println("Could not create activeusers.json file");
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                System.err.println("Error creating active users file: " + e.getMessage());
            }
            activeUsers = new HashMap<>();
            return;
        }

        try {
            // Read and deserialize JSON data
            deserialize(JsonIO.readArray(this.file));
        } catch (Exception e) {
            System.err.println("Error reading active users file: " + e.getMessage());
        }
    }

    /**
     * Adds a user to the database
     * 
     * @param username
     * @param socket
     */
    public void addUser(String username, Socket socket) {
        if (activeUsers == null) {
            activeUsers = new HashMap<>();
        }
        activeUsers.put(username, socket);
    }

    /**
     * Removes a user from the database
     * 
     * @Note - not thread safe so pray...
     * @param username
     */
    public void removeUser(String username) {
        if (activeUsers == null) {
            return;
        }
        activeUsers.remove(username);
    }

    /**
     * Checks if a user exists in the database
     * 
     * @param username
     */
    public boolean checkUser(String username) {
        if (activeUsers == null) {
            return false;
        }
        return activeUsers.containsKey(username);
    }

    /**
     * Returns the socket for a given username
     * 
     * @param username
     * @return
     */
    public Socket getSocket(String username) {
        if (activeUsers == null) {
            return null;
        }
        return activeUsers.get(username);
    }

    /**
     * Set the socket for a given username
     */
    public void setSocket(String username, Socket socket) {
        removeUser(username);
        if (activeUsers == null) {
            return;
        }
        activeUsers.put(username, socket);
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONArray)) {
            throw new InvalidObjectException("Object received is not valid");
        }
        JSONArray obj = (JSONArray) jsonType;
        activeUsers = new HashMap<>();
        for (int i = 0; i < obj.size(); i++) {
            JSONObject jObj = (JSONObject) obj.get(i);
            String username = jObj.getString("user");
            // lord help me this is scuffed
            String[] socketInfo = jObj.getString("socket").split(":");
            if (socketInfo.length != 2) {
                throw new InvalidObjectException("Invalid socket information format");
            }
            String host = socketInfo[0];
            int port = Integer.parseInt(socketInfo[1]);
            Socket socket = null;
            try {
                socket = new Socket(host, port);
            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
            } catch (IOException e) {
                System.err.println("Error creating socket: " + e.getMessage());
            }

            activeUsers.put(username, socket); 
        }
    }

    @Override
    public JSONType toJSONType() {
        // write hash map to json array
        JSONArray obj = new JSONArray();
        for (String key : activeUsers.keySet()) {
            JSONObject jObj = new JSONObject();
            jObj.put("user", key);
            jObj.put("socket", activeUsers.get(key).toString()); // Assuming Socket has a meaningful toString() method
            obj.add(jObj);
        }
        return obj;
    }

}
