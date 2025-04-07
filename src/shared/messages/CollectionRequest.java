package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class CollectionRequest extends Message{

    private String username;

    public CollectionRequest(String username) {
        super("CollectionRequest");
        this.username = username;
    }

    public CollectionRequest(JSONObject JSONMessage) {
        super(JSONMessage);
        if (!super.type.equals("CollectionRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String getUsername() {
        return username;
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
