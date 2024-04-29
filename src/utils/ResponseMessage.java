package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

public class ResponseMessage {

    private String operation;
    private String status;
    private String token;

    public ResponseMessage(String operation, String status, String token) {
        this.operation = operation;
        this.status = status;
        this.token = token;
    }

    public String toJsonString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("status", status);
        if (token != null && !token.isEmpty()) {
            JsonObject dataObject = new JsonObject();
            dataObject.put("token", token);
            jsonObject.put("data", dataObject);
        } else {
            jsonObject.put("data", new JsonObject());
        }
        return jsonObject.toJson();
    }
}