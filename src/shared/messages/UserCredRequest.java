package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class UserCredRequest extends Message {

    private String username;
    private String password;

    /**
     * Creates a new UserCredRequest message. Acceptable types are "Login" and "Register".
     * @param type
     * @param username
     * @param password
     */
    public UserCredRequest(String type, String username, String password) {
        super(type);
        if (!type.equals("Login") && !type.equals("Register")) {
            throw new IllegalArgumentException("Bad type: " + type);
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a new UserCredRequest message from a JSONObject. Acceptable types are "Login" and "Register".
     * @param obj JSONObject to deserialize
     */
    public UserCredRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("Login") && !super.type.equals("Register")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        username = jsonObject.getString("username");
        password = jsonObject.getString("password");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        return jsonObject;
    }
    
}
