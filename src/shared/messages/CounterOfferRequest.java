package shared.messages;

import java.io.InvalidObjectException; 

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject; 
import merrimackutil.json.types.JSONType; 

public class CounterOfferRequest extends Message {

    private String tradeId;
    private String username; 
    private JSONArray offeredCards; 
    
    public CounterOfferRequest(String tradeId, String username, JSONArray offeredCards) {
        super("CounterOfferRequest");
        this.tradeId = tradeId;
        this.username = username;
        this.offeredCards = offeredCards;
    }

    public CounterOfferRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("CounterOfferRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    // getters 
    public String getTradeId() {
        return this.tradeId; 
    }

    public String getUsername() {
        return this.username; 
    }

    public JSONArray getOfferedCards() {
        return this.offeredCards; 
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType; 
        this.tradeId = jsonObject.getString("tradeId");
        this.username = jsonObject.getString("username");
        this.offeredCards = jsonObject.getArray("offeredCards");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeId", this.tradeId);
        jsonObject.put("username", this.username);
        jsonObject.put("offeredCards", offeredCards);
        return jsonObject;
    }
}
