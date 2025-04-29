package server;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.HashMap;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONType;

public class TradeRequestDatabase implements JSONSerializable {
    HashMap<String, JSONArray> tradeRequests; // sender+recipientid (that way we only have one request to specified user
                                              // at a time heh) -> trade info
    // holds a database of trade requests
    // each has a type either "request" or "response"
    // each has a requesterID and a recipientID
    // each has an offerCardID and a responseCardID (null if type is "request")

    private File file;

    public TradeRequestDatabase(File file) {
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
        }
    }

    /**
     * Add a trade request to the database
     * @param requesterID
     * @param recipientID
     * @param offerCardID
     * @param responseCardID (null if type is "request")
     * @throws InvalidObjectException
     *
     */
    public void addTradeRequest(String requesterID, String recipientID, String offerCardID, String responseCardID)
            throws InvalidObjectException {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        String key = requesterID + recipientID;
        if (tradeRequests.containsKey(key)) {
            throw new InvalidObjectException("Trade request already exists in the database");
            // replace with a modal pop up jelly belly :3
        }
        JSONArray tradeInfo = new JSONArray();
        tradeInfo.add("request"); // type
        tradeInfo.add(requesterID); // requesterID
        tradeInfo.add(recipientID); // recipientID
        tradeInfo.add(offerCardID); // offerCardID
        tradeInfo.add(responseCardID); // responseCardID (null if type is "request")
        tradeRequests.put(key, tradeInfo);

        save();
    }

    /**
     * Get trade requests where user is the recipient
     */
    public JSONArray getTradeRequests(String username) {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        JSONArray requests = new JSONArray();
        for (String key : tradeRequests.keySet()) {
            if (key.contains(username)) { // ahhh i love the contains function :3
                JSONArray tradeInfo = tradeRequests.get(key);
                if ("request".equals(tradeInfo.get(0)) && username.equals(tradeInfo.get(2))) { // Ensure the trade is
                                                                                               // set to "request" and
                                                                                               // user is recipient
                    requests.add(tradeInfo);
                }
            }
        }
        return requests;
    }

    /**
     * Get trade responses
     */
    public JSONArray getTradeResponses(String username) {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        JSONArray responses = new JSONArray();
        for (String key : tradeRequests.keySet()) {
            if (key.contains(username)) { // ahhh i love the contains function :3
                JSONArray tradeInfo = tradeRequests.get(key);
                if ("response".equals(tradeInfo.get(0)) && username.equals(tradeInfo.get(1))) { // Ensure the trade is
                                                                                                // set to "response" and
                                                                                                // user is requester
                    responses.add(tradeInfo);
                }
            }
        }
        return responses;
    }

    /**
     * Get trade request by key
     */
    public JSONArray getTradeRequest(String tradeKey) throws InvalidObjectException {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        String key = tradeKey;
        if (!tradeRequests.containsKey(key)) {
            throw new InvalidObjectException("Trade request does not exist in the database");
        }
        return tradeRequests.get(key);
    }

    /**
     * Remove a trade request from the database
     * 
     * @param username
     * @throws InvalidObjectException
     */
    public void removeTradeRequest(String tradeKey) throws InvalidObjectException {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        String key = tradeKey;
        if (!tradeRequests.containsKey(key)) {
            throw new InvalidObjectException("Trade request does not exist in the database");
        }
        tradeRequests.remove(key);
    }

    /**
     * Update trade request with response
     */
    public void updateTradeRequest(String tradeKey, String responseCardID)
            throws InvalidObjectException {
        if (tradeRequests == null) {
            tradeRequests = new HashMap<>();
        }
        String key = tradeKey;
        if (!tradeRequests.containsKey(key)) {
            throw new InvalidObjectException("Trade request does not exist in the database");
        }
        JSONArray tradeInfo = tradeRequests.get(key);
        tradeInfo.set(0, "response"); // type
        tradeInfo.set(4, responseCardID); // responseCardID

        save();
    }

    @Override
    public void deserialize(JSONType arg0) throws InvalidObjectException {
        if (arg0 instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) arg0;
            tradeRequests = new HashMap<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray tradeRequest = (JSONArray) jsonArray.get(i);
                String key = tradeRequest.getString(1) + tradeRequest.getString(2); // requesterID + recipientID
                tradeRequests.put(key, tradeRequest);
            }
        } else {
            throw new InvalidObjectException("Invalid JSON type for TradeRequestDatabase");
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONArray jsonArray = new JSONArray();
        if (tradeRequests == null || tradeRequests.isEmpty()) {
            return jsonArray;
        }

        for (String key : tradeRequests.keySet()) {
            JSONArray tradeInfo = tradeRequests.get(key);
            JSONArray tradeRequest = new JSONArray();
            tradeRequest.add(key); // key
            tradeRequest.add(tradeInfo.get(0)); // type
            tradeRequest.add(tradeInfo.get(1)); // requesterID
            tradeRequest.add(tradeInfo.get(2)); // recipientID
            tradeRequest.add(tradeInfo.get(3)); // offerCardID
            tradeRequest.add(tradeInfo.get(4)); // responseCardID
            jsonArray.add(tradeRequest);
        }
        return jsonArray;
    }

    public void save() {
        try {
            JsonIO.writeFormattedObject(this, file);
        } catch (Exception e) {
            System.err.println("Error writing users file: " + e.getMessage());
        }
    }

}
