package server;

import com.github.cliftonlabs.json_simple.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import utils.MessageSender;
import utils.Validation;

import java.io.PrintWriter;
import java.sql.*;

public class OperationExecutorRecruiter {
    private MessageSender messageSender;
    String key = "DISTRIBUIDOS";

    public OperationExecutorRecruiter(PrintWriter out) {
        this.messageSender = new MessageSender(out);
    }

    public void executeLoginRecruiter(JsonObject requestJson){
        JsonObject data = (JsonObject) requestJson.get("data");

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidLogin(data)) {
            messageSender.sendMessage("LOGIN_RECRUITER", "INVALID_FIELD", new JsonObject());
        } else {
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT * FROM recrutador WHERE email = '" + email + "'";
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    String storedPassword = rs.getString("senha");
                    if (storedPassword.equals(password)) {
                        int userId = rs.getInt("id");
                        String userIdString = String.valueOf(userId);

                        String jwtToken = Jwts.builder().setHeaderParam("typ", "JWT")
                                .claim("id", userIdString)
                                .claim("role", "RECRUITER")
                                .signWith(SignatureAlgorithm.HS256, key).compact();

                        // adicionando o token no bd
                        String insertSql = "INSERT INTO active_tokens (user_id, token) VALUES (?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(insertSql);
                        pstmt.setInt(1, userId);
                        pstmt.setString(2, jwtToken);
                        pstmt.executeUpdate();

                        JsonObject jwtData = new JsonObject();
                        jwtData.put("token", jwtToken);
                        messageSender.sendMessage("LOGIN_RECRUITER", "SUCCESS", jwtData);
                    } else {
                        messageSender.sendMessage("LOGIN_RECRUITER", "INVALID_LOGIN", "");
                    }
                } else {
                    messageSender.sendMessage("LOGIN_RECRUITER", "INVALID_LOGIN", "");
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeSignupRecruiter(JsonObject requestJson){
        JsonObject data = (JsonObject) requestJson.get("data");

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidSignup(data)) {
            messageSender.sendMessage("SIGNUP_RECRUITER", "INVALID_FIELD", "");
        } else {
            String email = (String) data.get("email");
            String password = (String) data.get("password");
            String name = (String) data.get("name");
            String industry = (String) data.get("industry");
            String description = (String) data.get("description");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT * FROM recrutador WHERE email = '" + email + "'";
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    messageSender.sendMessage("SIGNUP_RECRUITER", "USER_EXISTS", "");
                } else {
                    sql = "INSERT INTO recrutador (nome, email, senha, industria, descricao) VALUES ('" + name + "', '" + email + "', '" + password + "','" + industry + "', '" + description +"')";
                    stmt.executeUpdate(sql);

                    messageSender.sendMessage("SIGNUP_RECRUITER", "SUCCESS", "");
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeLookupRecruiter(JsonObject requestJson){
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
            } else {
                try {
                    Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);

                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    Statement stmt = conn.createStatement(); // Crie o Statement aqui
                    sql = "SELECT * FROM recrutador WHERE id = " + userId;
                    rs = stmt.executeQuery(sql);

                    if (rs.next()) {
                        String storedEmail = rs.getString("email");
                        String storedPassword = rs.getString("senha");
                        String storedName = rs.getString("nome");
                        String storedIndustry = rs.getString("industria");
                        String storedDescription = rs.getString("descricao");

                        JsonObject responseData = new JsonObject();
                        responseData.put("email", storedEmail);
                        responseData.put("password", storedPassword);
                        responseData.put("name", storedName);
                        responseData.put("industry", storedIndustry);
                        responseData.put("description", storedDescription);


                        messageSender.sendMessage("LOOKUP_ACCOUNT_RECRUITER", "SUCCESS", responseData);
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeUpdateRecruiter(JsonObject requestJson){
        String token = (String) requestJson.get("token");
        JsonObject data = (JsonObject) requestJson.get("data");

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidSignup(data)) {
            messageSender.sendMessage("UPDATE_ACCOUNT_RECRUITER", "INVALID_FIELD", "");
        } else {
            String email = (String) data.get("email");
            String password = (String) data.get("password");
            String name = (String) data.get("name");
            String industry = (String) data.get("industry");
            String description = (String) data.get("description");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                // Verifique se o token está no banco de dados
                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    // O token não está no banco de dados, então é inválido
                    messageSender.sendMessage("UPDATE_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
                } else {
                    try {
                        Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                        String userIdString = (String) claims.getBody().get("id");
                        int userId = Integer.parseInt(userIdString);

                        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recrutador WHERE email = ? AND id != ?");
                        stmt.setString(1, email);
                        stmt.setInt(2, userId);
                        rs = stmt.executeQuery();

                        if (rs.next()) {
                            // O email já está sendo usado por outro usuário
                            messageSender.sendMessage("UPDATE_ACCOUNT_RECRUITER", "INVALID_EMAIL", "");
                        } else {
                            // O email não está sendo usado, então prossiga com a atualização
                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE recrutador SET email = ?, senha = ?, nome = ?, industria = ?, descricao = ? WHERE id = ?");
                            updateStmt.setString(1, email);
                            updateStmt.setString(2, password);
                            updateStmt.setString(3, name);
                            updateStmt.setString(4, industry);
                            updateStmt.setString(5, description);
                            updateStmt.setInt(6, userId);
                            updateStmt.executeUpdate();

                            messageSender.sendMessage("UPDATE_ACCOUNT_RECRUITER", "SUCCESS", "");
                        }
                    } catch (Exception e) {
                        messageSender.sendMessage("UPDATE_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeLogoutRecruiter(JsonObject requestJson){
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOGOUT_RECRUITER", "INVALID_TOKEN", "");
            } else {
                // Remova o token do banco de dados
                sql = "DELETE FROM active_tokens WHERE token = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                pstmt.executeUpdate();

                messageSender.sendMessage("LOGOUT_RECRUITER", "SUCCESS", "");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeDeleteRecruiter(JsonObject requestJson){
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("DELETE_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
            } else {
                try {
                    // Validação do token usando o claim
                    Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // Remova o token do banco de dados
                    sql = "DELETE FROM active_tokens WHERE token = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, token);
                    pstmt.executeUpdate();

                    // Remova o usuário da tabela de candidatos
                    sql = "DELETE FROM recrutador WHERE id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();

                    messageSender.sendMessage("DELETE_ACCOUNT_RECRUITER", "SUCCESS", "");
                } catch (Exception e) {
                    messageSender.sendMessage("DELETE_ACCOUNT_RECRUITER", "INVALID_TOKEN", "");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
