package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

public class RequestMessage {

    private String operation;
    private String email;
    private String password;
    private String name;

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

        dataObject.put("name", name);//

        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }
}
