package shared.messages;

import java.io.InvalidObjectException;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

// i probably couldve simplified this by using multiple types but idc
public class TradeRequest extends Message {
    // should have sender user id, recipient user id, and card id
    private String requesterID;
    private String recipientID;
    private String offerCardID;

    public TradeRequest(String requesterID, String recipientID, String offerCardID) {
        super("TradeRequest"); // Call to the parent class constructor
        this.requesterID = requesterID;
        this.recipientID = recipientID;
        this.offerCardID = offerCardID;
    }

    public TradeRequest(JSONObject obj) {
        super(obj);
        if (!super.type.equals("TradeRequest")) {
            throw new IllegalArgumentException("Bad type: " + super.type);
        }
        this.requesterID = obj.getString("requesterID");
        this.recipientID = obj.getString("recipientID");
        this.offerCardID = obj.getString("offerCardID");
    }

    public String getRequesterID() {
        return requesterID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public String getOfferCardID() {
        return offerCardID;
    }

    @Override
    public JSONObject toJSONType() {
        JSONObject jsonObject = super.toJSONType();
        jsonObject.put("requesterID", requesterID);
        jsonObject.put("recipientID", recipientID);
        jsonObject.put("offerCardID", offerCardID);
        return jsonObject;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);
        JSONObject jsonObject = (JSONObject) jsonType;
        this.requesterID = jsonObject.getString("requesterID");
        this.recipientID = jsonObject.getString("recipientID");
        this.offerCardID = jsonObject.getString("offerCardID");
    }
    
}
