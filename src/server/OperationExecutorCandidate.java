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

public class OperationExecutorCandidate {
    private MessageSender messageSender;
    String key = "DISTRIBUIDOS";

    public OperationExecutorCandidate(PrintWriter out) {
        this.messageSender = new MessageSender(out);
    }

    public void executeLoginCandidate(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidLogin(data)) {
            messageSender.sendMessage("LOGIN_CANDIDATE", "INVALID_FIELD", new JsonObject());
        } else {
            String email = (String) data.get("email");
            String password = (String) data.get("password");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT * FROM candidatos WHERE email = '" + email + "'";
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    String storedPassword = rs.getString("senha");
                    if (storedPassword.equals(password)) {
                        int userId = rs.getInt("id");
                        String userIdString = String.valueOf(userId);

                        String jwtToken = Jwts.builder().setHeaderParam("typ", "JWT")
                                .claim("id", userIdString)
                                .claim("role", "CANDIDATE")
                                .signWith(SignatureAlgorithm.HS256, key).compact();

                        // adicionando o token no bd
                        String insertSql = "INSERT INTO active_tokens (user_id, token) VALUES (?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(insertSql);
                        pstmt.setInt(1, userId);
                        pstmt.setString(2, jwtToken);
                        pstmt.executeUpdate();

                        JsonObject jwtData = new JsonObject();
                        jwtData.put("token", jwtToken);
                        messageSender.sendMessage("LOGIN_CANDIDATE", "SUCCESS", jwtData);
                    } else {
                        messageSender.sendMessage("LOGIN_CANDIDATE", "INVALID_LOGIN", "");
                    }
                } else {
                    messageSender.sendMessage("LOGIN_CANDIDATE", "INVALID_LOGIN", "");
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeSignupCandidate(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidSignup(data)) {
            messageSender.sendMessage("SIGNUP_CANDIDATE", "INVALID_FIELD", "");
        } else {
            String email = (String) data.get("email");
            String password = (String) data.get("password");
            String name = (String) data.get("name");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                 Statement stmt = conn.createStatement()) {

                String sql = "SELECT * FROM candidatos WHERE email = '" + email + "'";
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {
                    messageSender.sendMessage("SIGNUP_CANDIDATE", "USER_EXISTS", "");
                } else {
                    sql = "INSERT INTO candidatos (nome, email, senha) VALUES ('" + name + "', '" + email + "', '" + password + "')";
                    stmt.executeUpdate(sql);

                    messageSender.sendMessage("SIGNUP_CANDIDATE", "SUCCESS", "");
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeLookupCandidate(JsonObject requestJson) {
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
            } else {
                try {
                    Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);

                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    Statement stmt = conn.createStatement(); // Crie o Statement aqui
                    sql = "SELECT * FROM candidatos WHERE id = " + userId;
                    rs = stmt.executeQuery(sql);

                    if (rs.next()) {
                        String storedEmail = rs.getString("email");
                        String storedPassword = rs.getString("senha");
                        String storedName = rs.getString("nome");

                        JsonObject responseData = new JsonObject();
                        responseData.put("email", storedEmail);
                        responseData.put("password", storedPassword);
                        responseData.put("name", storedName);

                        messageSender.sendMessage("LOOKUP_ACCOUNT_CANDIDATE", "SUCCESS", responseData);
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeUpdateCandidate(JsonObject requestJson) {
        String token = (String) requestJson.get("token");
        JsonObject data = (JsonObject) requestJson.get("data");
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        String name = (String) data.get("name");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
            } else {
                try {
                    Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                    String userIdString = (String) claims.getBody().get("id");
                    //int userId = (int) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidatos WHERE email = ? AND id != ?");
                    stmt.setString(1, email);
                    stmt.setInt(2, userId);
                    rs = stmt.executeQuery();

                    if (rs.next()) {
                        // O email já está sendo usado por outro usuário
                        messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "INVALID_EMAIL", "");
                    } else {
                        // O email não está sendo usado, então prossiga com a atualização
                        PreparedStatement updateStmt = conn.prepareStatement("UPDATE candidatos SET email = ?, senha = ?, nome = ? WHERE id = ?");
                        updateStmt.setString(1, email);
                        updateStmt.setString(2, password);
                        updateStmt.setString(3, name);
                        updateStmt.setInt(4, userId);
                        updateStmt.executeUpdate();

                        messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "SUCCESS", "");
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");;
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeLogoutCandidate(JsonObject requestJson){
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOGOUT_CANDIDATE", "INVALID_TOKEN", "");
            } else {
                // Remova o token do banco de dados
                sql = "DELETE FROM active_tokens WHERE token = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                pstmt.executeUpdate();

                messageSender.sendMessage("LOGOUT_CANDIDATE", "SUCCESS", "");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeDeleteCandidate(JsonObject requestJson) {
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("DELETE_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
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
                    sql = "DELETE FROM candidatos WHERE id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();

                    messageSender.sendMessage("DELETE_ACCOUNT_CANDIDATE", "SUCCESS", "");
                } catch (Exception e) {
                    messageSender.sendMessage("DELETE_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
