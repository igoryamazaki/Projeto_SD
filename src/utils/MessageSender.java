package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.io.PrintWriter;

public class MessageSender {
    private PrintWriter out;

    public MessageSender(PrintWriter out) {
        this.out = out;
    }

    public void sendMessage(String operation, String status, Object data) {
        ResponseMessage responseMessage;
        if (data instanceof JsonObject) {
            responseMessage = new ResponseMessage(operation, status, (JsonObject) data);
        } else if (data instanceof String) {
            responseMessage = new ResponseMessage(operation, status, (String) data);
        } else {
            responseMessage = new ResponseMessage(operation, status, new JsonObject());
        }
        send(responseMessage);
    }

    public void send(ResponseMessage responseMessage) {
        String message = responseMessage.toJsonString();
        System.out.println("[Sending]: " + message);
        out.println(message);
    }
}
