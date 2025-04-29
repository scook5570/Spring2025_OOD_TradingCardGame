package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class AvailableUserRequest extends Message {
    
    private String username;

    public AvailableUserRequest(String username) {
        super("AvailableUsersRequest");
        this.username = username; 
    }

    public AvailableUserRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("AvailableUsersRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        username = jsonObject.getString("username");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("username", username);
        return jsonObject;
    }

}
