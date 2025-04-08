package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class PackResponse extends Message {
    private JSONArray cards;

    public PackResponse(JSONArray cards) {
        super("PackResponse");
        this.cards = cards;
    }

    public PackResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("PackResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public JSONArray getCards() {
        return cards;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        cards = jsonObject.getArray("cards");
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("cards", cards);
        return jsonObject;
    }
}
