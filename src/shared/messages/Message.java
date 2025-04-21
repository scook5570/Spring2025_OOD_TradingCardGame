package shared.messages;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

/**
 * Super class of messages used in program
 */
public class Message implements JSONSerializable {
    //type of message
    protected String type;

    /**
     * Constructor creates a new message object from JSONobject by deserializing it
     * @param JSONMessage JSONObject representing the message
     */
    public Message(JSONObject JSONMessage) {
        try {
            deserialize(JSONMessage);
            //todo update try catch
        } catch (InvalidObjectException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Constructor creates a new message from parameters
     */
    public Message(String type) {
        this.type = type;
    }

    /**
     * Get message type
     * @return String of message type
     */
    public String getType() {
        return type;
    }

    /**
     * Deserializes Message from JSONObject
     * @param jsonType JSONObject to deserialize
     * @throws InvalidObjectException throws if jsonType is not JSONObject or if there is no type
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject))
            throw new InvalidObjectException("jsonType must be a JSONObject");
        JSONObject messageJSON = (JSONObject) jsonType;

        messageJSON.checkValidity(new String[] {"type"});
        type = messageJSON.getString("type");
    }

    /**
     * Create JSON representation of this class
     * @return JSONObject representation of class
     */
    @Override
    public JSONObject toJSONType() {
        JSONObject messageJSON = new JSONObject();
        messageJSON.put("type",type);
        return messageJSON;
    }
}
