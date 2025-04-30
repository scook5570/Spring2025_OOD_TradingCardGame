package shared.messages;

public class UserListRequest extends Message {
    // sends all users in database
    public UserListRequest() {
        super("UserListRequest"); // Call to the parent class constructor
    }

    public UserListRequest(merrimackutil.json.types.JSONObject obj) {
        super(obj);
        if (!super.type.equals("UserListRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    @Override
    public merrimackutil.json.types.JSONObject toJSONType() {
        return super.toJSONType();
    }

    @Override
    public void deserialize(merrimackutil.json.types.JSONType jsonType) throws java.io.InvalidObjectException {
        super.deserialize(jsonType);
    }
    
}
