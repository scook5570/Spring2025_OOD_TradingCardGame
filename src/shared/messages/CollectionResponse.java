package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class CollectionResponse extends Message {
    JSONArray collection;

    public CollectionResponse(JSONObject JSONMessage) {
        super(JSONMessage);
        if (!super.type.equals("CollectionResponse")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
    }

    public CollectionResponse(JSONArray collection) {
        super("CollectionResponse");
        this.collection = collection;
    }

    public JSONArray getCollection() {
        return collection;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("collection", collection);
        return jsonObject;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        collection = jsonObject.getArray("collection");
    }
    
}
