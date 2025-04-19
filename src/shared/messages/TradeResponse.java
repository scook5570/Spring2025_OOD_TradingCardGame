package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONObject; 
import merrimackutil.json.types.JSONType;

public class TradeResponse extends Message {
    
    private String tradeId; 
    private boolean accepted; 
    private String recipientUsername; 

    public TradeResponse(String tradeId, boolean accepted, String recipientUsername) {
        super("TradeResponse");
        this.tradeId = tradeId;
        this.accepted = accepted;
        this.recipientUsername = recipientUsername;
    }

    public TradeResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String getTradeId() {
        return this.tradeId; 
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public String getRecipientUsername() {
        return this.recipientUsername;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.tradeId = jsonObject.getString("tradeId");
        this.accepted = jsonObject.getBoolean("accepted");
        this.recipientUsername = jsonObject.getString("recipientUsername");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeId", this.tradeId);
        jsonObject.put("accepted", this.accepted);
        jsonObject.put("recipientUsername", this.recipientUsername);
        return jsonObject;
    }
    
}
