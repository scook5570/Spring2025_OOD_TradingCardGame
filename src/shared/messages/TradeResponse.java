package shared.messages;

import merrimackutil.json.types.JSONObject;

public class TradeResponse extends Message {
    private boolean status;
    private String tradeKey;
    private String cardID;

    public TradeResponse(boolean status, String tradeKey, String cardID) {
        super("TradeResponse");
        this.status = status;
        this.tradeKey = tradeKey;
        this.cardID = cardID;
    }

    public TradeResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public boolean getStatus() {
        return status;
    }

    public String getTradeKey() {
        return tradeKey;
    }

    public String getCardID() {
        return cardID;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("status", status);
        jsonObject.put("tradeKey", tradeKey);
        jsonObject.put("cardID", cardID);
        return jsonObject;
    }
    
}
