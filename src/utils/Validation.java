package utils;

import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.Arrays;
import java.util.List;

public class Validation {
    public static boolean isValidOperation(String operation) {
        List<String> validOperations = Arrays.asList(
                "LOGIN_CANDIDATE",
                "SIGNUP_CANDIDATE",
                "LOOKUP_ACCOUNT_CANDIDATE",
                "LOGOUT_CANDIDATE",
                "UPDATE_ACCOUNT_CANDIDATE",
                "DELETE_ACCOUNT_CANDIDATE",
                "LOGIN_RECRUITER",
                "SIGNUP_RECRUITER",
                "LOOKUP_ACCOUNT_RECRUITER",
                "LOGOUT_RECRUITER",
                "UPDATE_ACCOUNT_RECRUITER",
                "DELETE_ACCOUNT_RECRUITER",
                "INCLUDE_SKILL",
                "LOOKUP_SKILL",
                "LOOKUP_SKILLSET",
                "DELETE_SKILL",
                "UPDATE_SKILL",
                "INCLUDE_JOB",
                "LOOKUP_JOB",
                "LOOKUP_JOBSET",
                "DELETE_JOB",
                "UPDATE_JOB",
                "SEARCH_JOB",
                "SET_JOB_AVAILABLE",
                "SET_JOB_SEARCHABLE",
                "SEARCH_CANDIDATE",
                "GET_COMPANY",
                "CHOOSE_CANDIDATE"
                );
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
    public static boolean areFieldsValidIncludeSkill(JsonObject data) {
        // Verifique se todos os campos necessários estão presentes e não são nulos
        if (data.get("skill") == null || data.get("experience") == null) {
            return false;
        }
        // Verifique se os campos não estão vazios
        if (data.get("skill").toString().isEmpty() || data.get("experience").toString().isEmpty()) {
            return false;
        }
        return true;
    }
    public static boolean areFieldsValidIncludeJob(JsonObject data) {
        // Verifique se todos os campos necessários estão presentes e não são nulos
        if (data.get("skill") == null || data.get("experience") == null /*|| data.get("searchable") == null*/) {
            return false;
        }
        // Verifique se os campos não estão vazios
        if (data.get("skill").toString().isEmpty() || data.get("experience").toString().isEmpty()) {
            return false;
        }
        // Verifique se o campo "searchable" é um booleano
       /* if (!(data.get("searchable") instanceof Boolean)) {
            return false;
        }*/
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
