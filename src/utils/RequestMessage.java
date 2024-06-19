package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

public class RequestMessage {
    private String operation;
    private String email;
    private String password;
    private String name;
    private String industry;
    private String skill;
    private String experience;
    private String description;
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
    public RequestMessage(String operation, String token, String skill, String experience, boolean isSkill) {
        this.operation = operation;
        this.token = token;
        this.skill = skill;
        this.experience = experience;
    }
    public RequestMessage(String operation, String token, String email, String password, String name) {
        this.operation = operation;
        this.token = token;
        this.email = email;
        this.password = password;
        this.name = name;//
    }
    public RequestMessage(String operation, String email, String password, String name, String industry, String description) {
        this.operation = operation;
        this.email = email;
        this.password = password;
        this.name = name;
        this.industry = industry;
        this.description = description;
    }
    public RequestMessage(String operation, String token, String email, String password, String name, String industry, String description) {
        this.operation = operation;
        this.token = token;
        this.email = email;
        this.password = password;
        this.name = name;
        this.industry = industry;
        this.description = description;
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
    public String toJsonStringToken() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("token", token);
        JsonObject dataObject = new JsonObject();
        dataObject.put("email", email);
        dataObject.put("password", password);
        if (name != null) {
            dataObject.put("name", name);
        }
        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }
    public String toJsonStringExtend() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        JsonObject dataObject = new JsonObject();
        dataObject.put("email", email);
        dataObject.put("password", password);
        dataObject.put("industry", industry);
        dataObject.put("description", description);
        if (name != null) {
            dataObject.put("name", name);
        }
        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }
    public String toJsonStringExtendToken() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("token", token);
        JsonObject dataObject = new JsonObject();
        dataObject.put("email", email);
        dataObject.put("password", password);
        dataObject.put("industry", industry);
        dataObject.put("description", description);
        if (name != null) {
            dataObject.put("name", name);
        }
        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }
    public String toJsonStringTokenSkill() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("operation", operation);
        jsonObject.put("token", token);
        JsonObject dataObject = new JsonObject();
        if (skill != null) {
            dataObject.put("skill", skill);
        }
        if (experience != null) {
            dataObject.put("experience", experience);
        }
        dataObject.put("searchable", "NO");
        dataObject.put("available", "NO");
        jsonObject.put("data", dataObject);
        return jsonObject.toJson();
    }

}
