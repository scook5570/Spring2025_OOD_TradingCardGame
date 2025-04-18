package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeInitiateRequest extends Message {

    private String senderUsername; 
    private String recipientUsername;
    private JSONArray offeredCards; 

    public  TradeInitiateRequest(String senderUsername, String recipientUsername, JSONArray offeredCards) {
        super("TradeInitiateRequest");
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.offeredCards = offeredCards; 
    }

    public TradeInitiateRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeInitiateRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    // Getters 
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
        senderUsername = jsonObject.getString("senderUsername");
        recipientUsername = jsonObject.getString("recipientUsername");
        offeredCards = jsonObject.getArray("offeredCards");
    }

    @Override 
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("senderUsername", senderUsername);
        jsonObject.put("recipientUsername", recipientUsername);
        jsonObject.put("offeredCards", offeredCards);
        return jsonObject;
    }

}
