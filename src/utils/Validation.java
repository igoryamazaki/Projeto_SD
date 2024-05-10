package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Arrays;
import java.util.List;

public class Validation {
    public static boolean isValidOperation(String operation) {
        // Adicione todas as operações válidas a esta lista
        List<String> validOperations = Arrays.asList("LOGIN_CANDIDATE", "SIGNUP_CANDIDATE", "LOOKUP_ACCOUNT_CANDIDATE", "LOGOUT_CANDIDATE", "UPDATE_ACCOUNT_CANDIDATE", "DELETE_ACCOUNT_CANDIDATE");
        return validOperations.contains(operation);
    }
    public static boolean areFieldsValidSignup(JsonObject data) {
        // Verifique se todos os campos necessários estão presentes e não são nulos
        if (data.get("email") == null || data.get("password") == null || data.get("name") == null) {
            return false;
        }
        // Verifique se os campos não estão vazios
        if (data.get("email").toString().isEmpty() || data.get("password").toString().isEmpty() || data.get("name").toString().isEmpty()) {
            return false;
        }
        return true;
    }
    public static boolean areFieldsValidLogin(JsonObject data) {
        // Verifique se todos os campos necessários estão presentes e não são nulos
        if (data.get("email") == null || data.get("password") == null) {
            return false;
        }
        // Verifique se os campos não estão vazios
        if (data.get("email").toString().isEmpty() || data.get("password").toString().isEmpty()) {
            return false;
        }
        return true;
    }
}
