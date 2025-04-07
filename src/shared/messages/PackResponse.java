package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class PackResponse extends Message {
    private String[] cards;

    public PackResponse(String[] cards) {
        super("PackResponse");
        this.cards = cards;
    }

    public PackResponse(JSONObject obj) {
        super(obj);
        if (!super.type.equals("PackResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public String[] getCards() {
        return cards;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        cards = jsonObject.getArray("cards").stream()
                .map(Object::toString)
                .toArray(String[]::new); // yeah this is scuffed
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("cards", cards);
        return jsonObject;
    }
}
