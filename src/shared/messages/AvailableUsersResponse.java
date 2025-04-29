package shared.messages;

import java.io.InvalidObjectException;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class AvailableUsersResponse extends Message {

    private JSONArray users;

    public AvailableUsersResponse(JSONArray users) {
        super("AvailableUsersRepsonse");
        this.users = users;
    }

    public AvailableUsersResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("AvailableUsersResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public JSONArray getUsers() {
        return this.users;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.users = jsonObject.getArray("users");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("users", this.users);
        return jsonObject;
    }
}
