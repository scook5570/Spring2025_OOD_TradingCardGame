package server;

import java.io.File;
import java.io.InvalidObjectException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

// creates a json database to store username and password pairs
public class UserCredentials implements JSONSerializable {
    private HashMap<String, String> credentials;
    private File file;

    /**
     * 
     * @param file
     */
    public UserCredentials(File file) {
        this.file = file;
        if (!file.exists() || file.length() == 0) {
            try {
                file.getParentFile().mkdir(); // Ensure directory exists
                if (!file.createNewFile()) {
                    System.err.println("Could not create users.json file");
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                System.err.println("Error creating users file: " + e.getMessage());
            }
            credentials = new HashMap<>();
            return;
        }

        try {
            // Read and deserialize JSON data
            deserialize(JsonIO.readArray(this.file));
        } catch (Exception e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
    }

    /**
     * Adds a user to the database
     * @param username
     * @param password
     */
    public void addUser(String username, String password) {
        if (credentials == null) {
            credentials = new HashMap<>();
        }
        credentials.put(username, password);
        save(); // Save the credentials to the file
    }

    /**
     * returns a set of all usernames in the credentials database
     * @return
     */
    public Set<String> getAllUsernames() {
        if (credentials == null) {
            return new HashSet<>();
        }
        return new HashSet<>(credentials.keySet());
    }

    /**
     * Removes a user from the database
     * @Note - not thread safe so pray...
     * @param username
     */
    public void removeUser(String username) {
        if (credentials == null) {
            return;
        }
        credentials.remove(username);
        save(); // Save the credentials to the file
    }

    /**
     * Checks if a user exists in the database
     * @param username
     * @return
     */
    public boolean checkUser(String username) {
        if (credentials == null) {
            return false;
        }
        return credentials.containsKey(username);
    }

    /**
     * Checks if a password is correct for a given user
     * @param username
     * @param password
     * @return
     */
    public boolean checkPassword(String username, String password) {
        if (credentials == null) {
            return false;
        }
        return credentials.get(username).equals(password);
    }

    /**
     * 
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONArray)) {
            throw new InvalidObjectException("Object received is not valid");
        }
        JSONArray obj = (JSONArray) jsonType;
        credentials = new HashMap<>();
        for (int i = 0; i < obj.size(); i++) {
            JSONObject jObj = (JSONObject) obj.get(i);
            jObj.checkValidity(new String[] { "user", "password" });
            credentials.put(jObj.getString("user"), jObj.getString("password"));
        }
    }

    /**
     * 
     */
    @Override
    public JSONType toJSONType() {
        // write the credentials hash map to a json array
        JSONArray jsonArray = new JSONArray();
        if (credentials == null || credentials.isEmpty()) {
            return jsonArray;
        }

        for (String user : credentials.keySet()) {
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("user", user);
            userMap.put("password", credentials.get(user));
            jsonArray.add(new JSONObject(userMap));
        }

        return jsonArray;
    }

    /**
     * Writes the credentials to the file
     */
    public void save() {
        try {
            JsonIO.writeFormattedObject(this, file);
        } catch (Exception e) {
            System.err.println("Error writing users file: " + e.getMessage());
        }
    }
}
