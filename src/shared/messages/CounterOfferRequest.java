package shared.messages;

import java.io.InvalidObjectException; 

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject; 
import merrimackutil.json.types.JSONType; 

public class CounterOfferRequest extends Message {

    private String originalTradeId;
    private String senderUsername; 
    private String recipientUsername;
    private JSONArray offeredCards; 
    
    public CounterOfferRequest(String originalTradeId, String senderUsername, String recipientUsername, JSONArray offeredCards) {
        super("CounterOfferRequst");
        this.originalTradeId = originalTradeId;
        this.senderUsername = senderUsername; 
        this.recipientUsername = recipientUsername;
        this.offeredCards = offeredCards;
    }

    public CounterOfferRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("CounterOfferRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    // getters 
    public String getOriginalTradeId() {
        return this.originalTradeId; 
    }

    public String getSenderUsername() {
        return this.senderUsername; 
    }

    public String getRecipientUsername() {
        return this.recipientUsername; 
    }

    public JSONArray getOfferedCards() {
        return this.offeredCards; 
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType; 
        this.originalTradeId = jsonObject.getString("originalTradeId");
        this.senderUsername = jsonObject.getString("senderUsername");
        this.recipientUsername = jsonObject.getString("recipientUsername");
        this.offeredCards = jsonObject.getArray("offeredCards");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("originalTradeId", this.originalTradeId);
        jsonObject.put("senderUsername", this.senderUsername);
        jsonObject.put("recipientUsername", this.recipientUsername);
        jsonObject.put("offeredCards", offeredCards);
        return jsonObject;
    }
}
