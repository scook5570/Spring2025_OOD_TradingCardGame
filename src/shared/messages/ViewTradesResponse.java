package shared.messages;

import merrimackutil.json.types.JSONArray;

public class ViewTradesResponse extends Message {
    // sends all trades in database that have the user as a participant
    JSONArray trades; // list of trades

    public ViewTradesResponse(JSONArray trades) {
        super("ViewTradesResponse"); // Call to the parent class constructor
        this.trades = trades;
    }

    public ViewTradesResponse(merrimackutil.json.types.JSONObject obj) {
        super(obj);
        if (!super.type.equals("ViewTradesResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }


    public JSONArray getTrades() {
        return trades;
    }

    @Override
    public merrimackutil.json.types.JSONObject toJSONType() {
        merrimackutil.json.types.JSONObject jsonObject = super.toJSONType();
        jsonObject.put("trades", trades);
        return jsonObject;
    }

    @Override
    public void deserialize(merrimackutil.json.types.JSONType jsonType) throws java.io.InvalidObjectException {
        super.deserialize(jsonType);
        merrimackutil.json.types.JSONObject jsonObject = (merrimackutil.json.types.JSONObject) jsonType;
        this.trades = jsonObject.getArray("trades");
    }
}
