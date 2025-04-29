package shared.messages;

import merrimackutil.json.types.JSONObject;

public class ViewTradesRequest extends Message {
    private String username;

    public ViewTradesRequest(String username) {
        super("ViewTradesRequest"); // Call to the parent class constructor
        this.username = username;
    }

    public ViewTradesRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("ViewTradesRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        this.username = obj.getString("username");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("username", username);
        return jsonObject;
    }

    @Override
    public void deserialize(merrimackutil.json.types.JSONType jsonType) throws java.io.InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.username = jsonObject.getString("username");
    }
}
