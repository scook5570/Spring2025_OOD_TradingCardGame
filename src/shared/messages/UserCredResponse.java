package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class UserCredResponse extends Message {
    private boolean status;

    /**
     * Creates a new UserCredResponse message from a JSONObject. Has type of "Status".
     * @param success
     */
    public UserCredResponse(boolean status) {
        super("Status");
        this.status = status;
    }

    /**
     * Creates a new UserCredResponse message from a JSONObject. Has type of "Status".
     * @param obj JSONObject to deserialize
     */
    public UserCredResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("Status")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        this.status = obj.getBoolean("status");
    }

    public boolean isSuccess() {
        return status;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        status = jsonObject.getBoolean("status");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("status", status);
        return jsonObject;
    }
}
