package server;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.UUID;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeDatabase implements JSONSerializable {
    
    private HashMap<String, JSONObject> pendingTrades;
    private File file;

    public TradeDatabase(File file) {
        this.file = file;
        if (!this.file.exists() || this.file.length() == 0) {
            try {
                this.file.getParentFile().mkdir();
                if (!this.file.createNewFile()) {
                    System.err.println("TARRIF ERROR : Could not create trades.json file");
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                System.err.println("TARRIF ERROR : Error creating trades file: " + e.getMessage());
            }
            pendingTrades = new HashMap<>();
            return;
        }

        try {
            deserialize(JsonIO.readArray(this.file));
        } catch (Exception e) {
            System.err.println("Error reading trades file: " + e.getMessage());
            pendingTrades = new HashMap<>();
        }
    }

    public String createTrade(String initiator, String recipient, JSONArray offeredCards) {

        // If we're being nit-picky, we should iterate throigh ID's an make sure thje ID generated isn't a duplicate 
        String tradeId = UUID.randomUUID().toString();

        JSONObject trade = new JSONObject();
        trade.put("tradeId", tradeId);
        trade.put("initiator", initiator);
        trade.put("recipient", recipient);
        trade.put("offeredCards", offeredCards);
        trade.put("status", "pending");
        trade.put("timestamp", System.currentTimeMillis());

        pendingTrades.put(tradeId, trade);
        save();
        return tradeId;
    }

    public JSONObject getTrade(String tradeId) {
        return pendingTrades.get(tradeId);
    }

    public void updateTradeStatus(String tradeId, String status) {
        JSONObject trade = pendingTrades.get(tradeId);
        if (trade != null) {
            trade.put("status", status);
            save();
        }
    }

    public void removeTrade(String tradeId) {
        pendingTrades.remove(tradeId);
        save();
    }

    public JSONArray getTradesForUser(String username) {
        JSONArray userTrades = new JSONArray();
        for (JSONObject trade : pendingTrades.values()) {
            if (trade.getString("initiator").equals(username) || trade.getString("recipient").equals(username)) {
                userTrades.add(trade);
            }
        }
        return userTrades;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (jsonType instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonType;
            pendingTrades = new HashMap<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject trade = (JSONObject) jsonArray.get(i);
                pendingTrades.put(trade.getString("tradeId"), trade);
            }
        } else {
            throw new InvalidObjectException("Invalid JSON type for TradeDatabase");
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONArray jsonArray = new JSONArray();
        for (JSONObject trade : pendingTrades.values()) {
            jsonArray.add(trade);
        }
        return jsonArray;
    }

    public void save() {
        try {
            JsonIO.writeFormattedObject(this, file);
        } catch (Exception e) {
            System.err.println("Error writing trades file: " + e.getMessage());
        }
    }

}
