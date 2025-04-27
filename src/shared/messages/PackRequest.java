package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class PackRequest extends Message{
    private int cardCount;
    private String packName;
    private String username;

    public PackRequest(String username, String packName, int cardCount) {
        super("PackRequest");
        this.username = username;
        this.packName = packName;
        this.cardCount = cardCount;
    }

    public PackRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("PackRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPackName() {
        return packName;
    }

    public int getCardCount() {
        return cardCount;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        username = jsonObject.getString("username");
        packName = jsonObject.getString("packName");
        cardCount = jsonObject.getInt("cardCount");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("username", username);
        jsonObject.put("packName", packName);
        jsonObject.put("cardCount", cardCount);
        return jsonObject;
    }
}
