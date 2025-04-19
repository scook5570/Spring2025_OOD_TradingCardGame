package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeOfferNotification extends Message {
    
    private String tradeId;
    private String senderUsername;
    private JSONArray offeredCards;

    public TradeOfferNotification(String tradeId, String senderUsername, JSONArray offeredCards) {
        super("TradeOfferNotification");
        this.tradeId = tradeId;
        this.senderUsername = senderUsername; 
        this.offeredCards = offeredCards; 
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

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        tradeId = jsonObject.getString("tradeId");
        senderUsername = jsonObject.getString("senderUsername");
        offeredCards = jsonObject.getArray("offeredCards");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeId", tradeId);
        jsonObject.put("senderUsername", senderUsername);
        jsonObject.put("offeredCards", offeredCards);
        return jsonObject;
    }

}
