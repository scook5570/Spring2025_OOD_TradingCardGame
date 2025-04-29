package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeConfirmationRequest extends Message {
    
    private String tradeId;
    private String username;
    private boolean confirmed;

    // constructors 
    public TradeConfirmationRequest(String tradeId, String username, boolean confirmed) {
        super("TradeConfirmationRequest");
        this.tradeId = tradeId; 
        this.username = username;
        this.confirmed = confirmed;
    }

    public TradeConfirmationRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeConfigurationRequest")) {
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

    public boolean getConfirmed() {
        return this.confirmed;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.tradeId = jsonObject.getString("tradeId");
        this.username = jsonObject.getString("username");
        this.confirmed = jsonObject.getBoolean("confirmed");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeId", this.tradeId);
        jsonObject.put("username", this.username);
        jsonObject.put("confirmed", this.confirmed);
        return jsonObject;
    }

}
