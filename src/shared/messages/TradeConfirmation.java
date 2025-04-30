package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class TradeConfirmation extends Message {
    private String tradeKey;
    private boolean status;

    public TradeConfirmation(String tradeKey, boolean status) {
        super("TradeConfirmation"); // Call to the parent class constructor
        this.tradeKey = tradeKey;
        this.status = status;
    }

    public TradeConfirmation(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeConfirmation")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        this.tradeKey = obj.getString("tradeKey");
        this.status = obj.getBoolean("status");
    }

    public String getTradeKey() {
        return tradeKey;
    }

    public boolean getStatus() {
        return status;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("tradeKey", tradeKey);
        jsonObject.put("status", status);
        return jsonObject;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.tradeKey = jsonObject.getString("tradeKey");
        this.status = jsonObject.getBoolean("status");
    }
}
