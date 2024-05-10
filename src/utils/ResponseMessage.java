package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

public class ResponseMessage {

    private String operation;
    private String status;
    private String token;
    private String email;
    private String password;
    private String name;
    private JsonObject data;

    public ResponseMessage(String operation, String status, String token) {
        this.operation = operation;
        this.status = status;
        this.token = token;
    }

    public ResponseMessage(String operation, String status, JsonObject data) {
        this.operation = operation;
        this.status = status;
        this.data = data;
    }

    public String toJsonString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("status", status);
        if (data != null) {
            jsonObject.put("data", data);
        } else if (token != null && !token.isEmpty()) {
            JsonObject dataObject = new JsonObject();
            dataObject.put("token", token);
            jsonObject.put("data", dataObject);
        } else {
            jsonObject.put("data", new JsonObject());
        }
        return jsonObject.toJson();
    }
}


