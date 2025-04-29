package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeOfferNotification extends Message {
    
    private String tradeId;
    private String senderUsername;
    private JSONArray offeredCards;
    private String tradeStage; // added to track flow stage

    public TradeOfferNotification(String tradeId, String senderUsername, JSONArray offeredCards, String tradeStage) {
        super("TradeOfferNotification");
        this.tradeId = tradeId;
        this.senderUsername = senderUsername; 
        this.offeredCards = offeredCards; 
        this.tradeStage = tradeStage; 
    }

    public TradeOfferNotification(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeOfferNotification")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String getTradeID() {
        return this.tradeId;
    }

    public String getSenderUsername() {
        return this.senderUsername;
    }

    public JSONArray getOfferedCards() {
        return this.offeredCards;
    }

    public String getTradeStage() {
        return this.tradeStage; 
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.tradeId = jsonObject.getString("tradeId");
        this.senderUsername = jsonObject.getString("senderUsername");
        this.offeredCards = jsonObject.getArray("offeredCards");
        this.tradeStage = jsonObject.getString("tradeStage");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeId", this.tradeId);
        jsonObject.put("senderUsername", this.senderUsername);
        jsonObject.put("offeredCards", this.offeredCards);
        jsonObject.put("tradeStage", this.tradeStage); 
        return jsonObject;
    }

}
