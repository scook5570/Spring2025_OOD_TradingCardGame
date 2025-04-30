package shared.messages;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class UserListResponse extends Message {
    private JSONArray users; // Array of usernames

    public UserListResponse(JSONArray users) {
        super("UserListResponse"); // Call to the parent class constructor
        this.users = users;
    }

    public UserListResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("UserListResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        
        this.users = obj.getArray("users"); // Deserialize the users array from the JSON object
    }

    public JSONArray getUsers() {
        return users;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        // Serialize the users array to the JSON object
        jsonObject.put("users", users);
        return jsonObject;
    }

    @Override
    public void deserialize(JSONType jsonType) throws java.io.InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        // Deserialize the users array from the JSON object
        this.users = jsonObject.getArray("users");
    }
}
