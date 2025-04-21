package shared.messages;

public class LogOutRequest extends Message {

    String username;

    /**
     * Creates a new LogOutRequest message from a JSONObject. Has type of "LogOutRequest".
     * @param obj JSONObject to deserialize
     */
    public LogOutRequest(String username) {
        super("LogOutRequest");
        this.username = username;
    }
}
