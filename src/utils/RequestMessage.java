package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

public class RequestMessage {

    private String operation;
    private String email;
    private String password;
    private String name;
    private String token;
    public RequestMessage(String operation, String token) {
        this.operation = operation;
        this.token =token;
    }
    public RequestMessage(String operation, String email, String password) {
        this.operation = operation;
        this.email = email;
        this.password = password;
    }

    public RequestMessage(String operation, String email, String password, String name) {
        this.operation = operation;
        this.email = email;
        this.password = password;
        this.name = name;//
    }

    public String toJsonString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        JsonObject dataObject = new JsonObject();
        dataObject.put("email", email);
        dataObject.put("password", password);
        if (name != null) {
            dataObject.put("name", name);
        }
        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }
    public String toJsonStringWithToken() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("token", token);
        jsonObject.put("data", new JsonObject());
        return jsonObject.toJson();
    }
}
