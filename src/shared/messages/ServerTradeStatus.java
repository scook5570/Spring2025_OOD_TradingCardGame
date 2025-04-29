package shared.messages;

import merrimackutil.json.types.JSONObject;

public class ServerTradeStatus extends Message {
    // just has a boolean status
    private boolean status;
    private String message; // message to be sent to the client


    public ServerTradeStatus(boolean status, String message) {
        super("ServerTradeStatus"); // Explicitly call a valid constructor of the Message class
        this.status = status;
        this.message = message;
    }

    public ServerTradeStatus(JSONObject arg0) {
        super(arg0);
        if (!super.type.equals("ServerTradeStatus")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        this.status = arg0.getBoolean("status");
        this.message = arg0.getString("message");
    }

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("status", status);
        jsonObject.put("message", message);
        return jsonObject;
    }

    @Override
    public void deserialize(merrimackutil.json.types.JSONType arg0) throws java.io.InvalidObjectException {
        super.deserialize(arg0);
        JSONObject jsonObject = (JSONObject) arg0;
        this.status = jsonObject.getBoolean("status");
        this.message = jsonObject.getString("message");
    }
}
