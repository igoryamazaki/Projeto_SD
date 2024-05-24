package server;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import utils.JwtManager;
import utils.MessageSender;
import utils.Skills;
import utils.Validation;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;

public class OperationExecutorCandidate {
    private MessageSender messageSender;
    private JwtManager jwtManager;
    String key = "DISTRIBUIDOS";

    public OperationExecutorCandidate(PrintWriter out) {
        this.messageSender = new MessageSender(out);
        this.jwtManager = new JwtManager(key);
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
/*
                        String jwtToken = Jwts.builder().setHeaderParam("typ", "JWT")
                                .claim("id", userIdString)
                                .claim("role", "CANDIDATE")
                                .signWith(SignatureAlgorithm.HS256, key).compact();
*/
                        String jwtToken = jwtManager.createToken(userIdString, "CANDIDATE");

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
                    //Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                    Jws<Claims> claims = jwtManager.validateToken(token);

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

        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidSignup(data)) {
            messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "INVALID_FIELD", "");
        } else {
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
                        //Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                        Jws<Claims> claims = jwtManager.validateToken(token);
                        String userIdString = (String) claims.getBody().get("id");
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
                        messageSender.sendMessage("UPDATE_ACCOUNT_CANDIDATE", "INVALID_TOKEN", "");
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
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
                    Jws<Claims> claims = jwtManager.validateToken(token);
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
/*
    public void executeIncludeSkill(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        // Verifique se os campos são válidos
        if (!Validation.areFieldsValidIncludeSkill(data)) {
            messageSender.sendMessage("INCLUDE_SKILL", "INVALID_FIELD", "");
        } else {
            String skill = (String) data.get("skill");
            String experience = (String) data.get("experience");

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                // Verifique se o token está no banco de dados
                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    // O token não está no banco de dados, então é inválido
                    messageSender.sendMessage("INCLUDE_SKILL", "INVALID_TOKEN", "");
                    System.out.println("invalid token 1: ");
                } else {
                    try {
                        Jws<Claims> claims = jwtManager.validateToken(token);
                        String userIdString = (String) claims.getBody().get("id");
                        int userId = Integer.parseInt(userIdString);

                        // Verifique se a habilidade já existe
                        PreparedStatement checkSkillStmt = conn.prepareStatement("SELECT * FROM habilidades WHERE habilidade = ?");
                        checkSkillStmt.setString(1, skill);
                        ResultSet rsSkillCheck = checkSkillStmt.executeQuery();

                        if (rsSkillCheck.next()) {
                            // A habilidade já existe
                            messageSender.sendMessage("INCLUDE_SKILL", "SKILL_EXISTS", "");
                        } else {
                            // Verifique se a habilidade está entre as pré-estabelecidas
                            if (!Arrays.asList(Skills.getSkills()).contains(skill)) {
                                // A habilidade não está entre as pré-estabelecidas
                                messageSender.sendMessage("INCLUDE_SKILL", "SKILL_NOT_EXIST", "");
                            } else {
                                // A habilidade é válida e não existe, então prossiga com a adição da habilidade
                                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO habilidades (habilidade, experiencia) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                                insertStmt.setString(1, skill);
                                insertStmt.setString(2, experience);
                                insertStmt.executeUpdate();

                                // Recupere o ID da habilidade que acabou de ser inserida
                                ResultSet rsSkill = insertStmt.getGeneratedKeys();
                                int skillId = -1;
                                if (rsSkill.next()) {
                                    skillId = rsSkill.getInt(1);
                                }

                                // Insira o mapeamento entre o candidato e a habilidade na tabela candidato_habilidades
                                PreparedStatement insertMappingStmt = conn.prepareStatement("INSERT INTO candidato_habilidades (candidato_id, habilidade_id) VALUES (?, ?)");
                                insertMappingStmt.setInt(1, userId);
                                insertMappingStmt.setInt(2, skillId);
                                insertMappingStmt.executeUpdate();

                                messageSender.sendMessage("INCLUDE_SKILL", "SUCCESS", "");
                            }
                        }
                    } catch (Exception e) {
                        messageSender.sendMessage("INCLUDE_SKILL", "INVALID_TOKEN", "");
                        System.out.println("invalid token 2: "+e);
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
*/
public void executeIncludeSkill(JsonObject requestJson) {
    JsonObject data = (JsonObject) requestJson.get("data");
    String token = (String) requestJson.get("token"); // Alteração aqui

    // Verifique se os campos são válidos
    if (!Validation.areFieldsValidIncludeSkill(data)) {
        messageSender.sendMessage("INCLUDE_SKILL", "INVALID_FIELD", "");
    } else {
        String skill = (String) data.get("skill");
        String experience = (String) data.get("experience");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("INCLUDE_SKILL", "INVALID_TOKEN", "");
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // Verifique se a habilidade já existe para o candidato
                    PreparedStatement checkSkillStmt = conn.prepareStatement("SELECT * FROM habilidades h JOIN candidato_habilidades ch ON h.id = ch.habilidade_id WHERE h.habilidade = ? AND ch.candidato_id = ?");
                    checkSkillStmt.setString(1, skill);
                    checkSkillStmt.setInt(2, userId);
                    ResultSet rsSkillCheck = checkSkillStmt.executeQuery();

                    if (rsSkillCheck.next()) {
                        // A habilidade já existe para o candidato
                        messageSender.sendMessage("INCLUDE_SKILL", "SKILL_EXISTS", "");
                    } else {
                        // Verifique se a habilidade está entre as pré-estabelecidas
                        if (!Arrays.asList(Skills.getSkills()).contains(skill)) {
                            // A habilidade não está entre as pré-estabelecidas
                            messageSender.sendMessage("INCLUDE_SKILL", "SKILL_NOT_EXIST", "");
                        } else {
                            // A habilidade é válida e não existe para o candidato, então prossiga com a adição da habilidade
                            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO habilidades (habilidade, experiencia) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                            insertStmt.setString(1, skill);
                            insertStmt.setString(2, experience);
                            insertStmt.executeUpdate();

                            // Recupere o ID da habilidade que acabou de ser inserida
                            ResultSet rsSkill = insertStmt.getGeneratedKeys();
                            int skillId = -1;
                            if (rsSkill.next()) {
                                skillId = rsSkill.getInt(1);
                            }

                            // Insira o mapeamento entre o candidato e a habilidade na tabela candidato_habilidades
                            PreparedStatement insertMappingStmt = conn.prepareStatement("INSERT INTO candidato_habilidades (candidato_id, habilidade_id) VALUES (?, ?)");
                            insertMappingStmt.setInt(1, userId);
                            insertMappingStmt.setInt(2, skillId);
                            insertMappingStmt.executeUpdate();

                            messageSender.sendMessage("INCLUDE_SKILL", "SUCCESS", "");
                        }
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("INCLUDE_SKILL", "INVALID_TOKEN", "");
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}

    /*public void executeLookUpSkill(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String skill = (String) data.get("skill");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_SKILL", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca da habilidade
                    PreparedStatement selectStmt = conn.prepareStatement("SELECT * FROM habilidades WHERE habilidade = ?"); // Alteração aqui
                    selectStmt.setString(1, skill); // Alteração aqui
                    rs = selectStmt.executeQuery();

                    if (!rs.next()) {
                        // A habilidade não está no banco de dados
                        messageSender.sendMessage("LOOKUP_SKILL", "SKILL_NOT_FOUND", new JsonObject());
                        System.out.println("SKILL NOT FOUND !!!");
                    } else {
                        // A habilidade foi encontrada, então retorne os detalhes da habilidade
                        String experience = rs.getString("experiencia");
                        int id = rs.getInt("id"); // Alteração aqui

                        JsonObject responseData = new JsonObject();
                        responseData.put("skill", skill);
                        responseData.put("experience", experience);
                        responseData.put("id", id);

                        messageSender.sendMessage("LOOKUP_SKILL", "SUCCESS", responseData);
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_SKILL", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }*/

    public void executeLookUpSkill(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String skill = (String) data.get("skill");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_SKILL", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca da habilidade
                    PreparedStatement selectStmt = conn.prepareStatement(
                            "SELECT h.experiencia, h.id " +
                                    "FROM habilidades h " +
                                    "JOIN candidato_habilidades ch ON h.id = ch.habilidade_id " +
                                    "WHERE ch.candidato_id = ? AND h.habilidade = ?"
                    );
                    selectStmt.setInt(1, userId);
                    selectStmt.setString(2, skill);
                    rs = selectStmt.executeQuery();

                    if (!rs.next()) {
                        // A habilidade não está no banco de dados
                        messageSender.sendMessage("LOOKUP_SKILL", "SKILL_NOT_FOUND", new JsonObject());
                        System.out.println("SKILL NOT FOUND !!!");
                    } else {
                        // A habilidade foi encontrada, então retorne os detalhes da habilidade
                        String experience = rs.getString("experiencia");
                        int id = rs.getInt("id"); // Alteração aqui

                        JsonObject responseData = new JsonObject();
                        responseData.put("skill", skill);
                        responseData.put("experience", experience);
                        responseData.put("id", id);

                        messageSender.sendMessage("LOOKUP_SKILL", "SUCCESS", responseData);
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_SKILL", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeLookUpSkillSet(JsonObject requestJson) {
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_SKILLSET", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca do conjunto de habilidades
                    PreparedStatement selectStmt = conn.prepareStatement("SELECT habilidades.* FROM habilidades JOIN candidato_habilidades ON habilidades.id = candidato_habilidades.habilidade_id WHERE candidato_habilidades.candidato_id = ?");
                    selectStmt.setInt(1, userId);
                    rs = selectStmt.executeQuery();

                    JsonArray skillset = new JsonArray();
                    while (rs.next()) {
                        // Para cada habilidade, adicione os detalhes da habilidade ao conjunto de habilidades
                        String skill = rs.getString("habilidade");
                        String experience = rs.getString("experiencia");
                        String id = rs.getString("id");

                        JsonObject skillData = new JsonObject();
                        skillData.put("skill", skill);
                        skillData.put("experience", experience);
                        skillData.put("id", id);

                        skillset.add(skillData);
                    }

                    // Retorne o conjunto de habilidades
                    JsonObject responseData = new JsonObject();
                    responseData.put("skillset_size", skillset.size());
                    responseData.put("skillset", skillset);

                    messageSender.sendMessage("LOOKUP_SKILLSET", "SUCCESS", responseData);
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_SKILLSET", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeDeleteSkill(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String skill = (String) data.get("skill");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("DELETE_SKILL", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a exclusão da habilidade
                    PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM habilidades WHERE habilidade = ?"); // Alteração aqui
                    deleteStmt.setString(1, skill);
                    int affectedRows = deleteStmt.executeUpdate();

                    if (affectedRows == 0) {
                        // A habilidade não está no banco de dados
                        messageSender.sendMessage("DELETE_SKILL", "SKILL_NOT_FOUND", new JsonObject());
                        System.out.println("SKILL NOT FOUND !!!");
                    } else {
                        // A habilidade foi excluída, então retorne sucesso
                        messageSender.sendMessage("DELETE_SKILL", "SUCCESS", new JsonObject());
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("DELETE_SKILL", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeUpdateSkill(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String skill = (String) data.get("skill"); // Habilidade atual
        String newSkill = (String) data.get("newSkill"); // Nova habilidade
        String experience = (String) data.get("experience"); // Experiência

        if (!Validation.areFieldsValidIncludeSkill(data)) {
            messageSender.sendMessage("UPDATE_SKILL", "INVALID_FIELD", "");
        } else {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                // Verifique se o token está no banco de dados
                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    // O token não está no banco de dados, então é inválido
                    messageSender.sendMessage("UPDATE_SKILL", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 1: ");
                } else {
                    try {
                        Jws<Claims> claims = jwtManager.validateToken(token);
                        String userIdString = (String) claims.getBody().get("id");
                        int userId = Integer.parseInt(userIdString);

                        // Verifique se a nova habilidade já existe para o candidato
                        PreparedStatement checkNewSkillStmt = conn.prepareStatement("SELECT * FROM habilidades h JOIN candidato_habilidades ch ON h.id = ch.habilidade_id WHERE h.habilidade = ? AND ch.candidato_id = ?");
                        checkNewSkillStmt.setString(1, newSkill);
                        checkNewSkillStmt.setInt(2, userId);
                        ResultSet rsNewSkillCheck = checkNewSkillStmt.executeQuery();

                        if (rsNewSkillCheck.next()) {
                            // A nova habilidade já existe para o candidato
                            messageSender.sendMessage("UPDATE_SKILL", "SKILL_EXISTS", new JsonObject());
                        } else {
                            // A nova habilidade não existe para o candidato, então prossiga com a atualização da habilidade
                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE habilidades SET habilidade = ?, experiencia = ? WHERE habilidade = ? AND id IN (SELECT habilidade_id FROM candidato_habilidades WHERE candidato_id = ?)");
                            updateStmt.setString(1, newSkill); // Nova habilidade
                            updateStmt.setString(2, experience); // Experiência
                            updateStmt.setString(3, skill); // Habilidade atual
                            updateStmt.setInt(4, userId); // ID do candidato
                            int affectedRows = updateStmt.executeUpdate();

                            if (affectedRows == 0) {
                                // A habilidade não está no banco de dados para o candidato
                                messageSender.sendMessage("UPDATE_SKILL", "SKILL_NOT_FOUND", new JsonObject());
                            } else {
                                // A habilidade foi atualizada, então retorne sucesso
                                messageSender.sendMessage("UPDATE_SKILL", "SUCCESS", new JsonObject());
                            }
                        }
                    } catch (Exception e) {
                        messageSender.sendMessage("UPDATE_SKILL", "INVALID_TOKEN", new JsonObject());
                        System.out.println("invalid token 2: " + e);
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void executeSearchJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        JsonArray skillArray = data.get("skill") != null ? (JsonArray) data.get("skill") : null;
        String experience = data.get("experience") != null ? (String) data.get("experience") : null;
        String filter = data.get("filter") != null ? (String) data.get("filter") : null;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                messageSender.sendMessage("SEARCH_JOB", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // Inicializa a consulta SQL base
                    String selectSql = "SELECT * FROM recrutador_vagas rv JOIN vagas v ON rv.vaga_id = v.id";

                    // Verifica qual tipo de busca é e constrói a consulta SQL de acordo
                    /*if (skillArray != null && experience == null) {
                        // Busca apenas por habilidades
                        selectSql += " WHERE v.habilidade IN (" + String.join(",", Collections.nCopies(skillArray.size(), "?")) + ")";
                    } else if (skillArray == null && experience != null) {
                        // Busca apenas por experiência
                        selectSql += " WHERE v.experiencia = ?";
                    } else if (skillArray != null && experience != null) {
                        // Busca combinada de habilidades e experiência com filtro lógico
                        String skillCondition = "v.habilidade IN (" + String.join(",", Collections.nCopies(skillArray.size(), "?")) + ")";
                        String experienceCondition = "v.experiencia = ?";

                        if (filter.equals("AND")) {
                            selectSql += " WHERE (" + skillCondition + ") AND (" + experienceCondition + ")";
                        } else {
                            selectSql += " WHERE (" + skillCondition + ") OR (" + experienceCondition + ")";
                        }
*/
                    if (skillArray != null && experience == null) {
                        // Busca apenas por habilidades
                        //selectSql += " WHERE v.habilidade IN (" + String.join(",", Collections.nCopies(skillArray.size(), "?")) + ")";
                        //selectSql += " AND v.experiencia <= ?";
                        selectSql += " WHERE v.habilidade IN (" + String.join(",", Collections.nCopies(skillArray.size(), "?")) + ")";
                    } else if (skillArray == null && experience != null) {
                        // Busca apenas por experiência
                        selectSql += " WHERE v.experiencia <= ?";
                    } else if (skillArray != null && experience != null) {
                        // Busca combinada de habilidades e experiência com filtro lógico
                        String skillCondition = "v.habilidade IN (" + String.join(",", Collections.nCopies(skillArray.size(), "?")) + ")";
                        String experienceCondition = "v.experiencia <= ?";

                        if (filter.equals("AND")) {
                            selectSql += " WHERE (" + skillCondition + ") AND (" + experienceCondition + ")";
                        } else {
                            selectSql += " WHERE (" + skillCondition + ") OR (" + experienceCondition + ")";
                        }
                    }

                    PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                    int index = 1;

                    // Define os parâmetros da consulta SQL baseado no tipo de busca
                    if (skillArray != null) {
                        for (Object skill : skillArray) {
                            selectStmt.setString(index++, (String) skill);
                        }
                    }
                    if (experience != null) {
                        selectStmt.setString(index++, experience);
                    }

                    rs = selectStmt.executeQuery();
                    JsonArray jobset = new JsonArray();

                    while (rs.next()) {
                        String foundSkill = rs.getString("habilidade");
                        String foundExperience = rs.getString("experiencia");
                        int id = rs.getInt("id");

                        JsonObject job = new JsonObject();
                        job.put("skill", foundSkill);
                        job.put("experience", foundExperience);
                        job.put("id", id);
                        jobset.add(job);
                    }

                    JsonObject responseData = new JsonObject();
                    responseData.put("jobset_size", jobset.size());
                    responseData.put("jobset", jobset);

                    messageSender.sendMessage("SEARCH_JOB", "SUCCESS", responseData);

                } catch (Exception e) {
                    messageSender.sendMessage("SEARCH_JOB", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: " + e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


/*
    public void executeSearchJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        JsonArray skillArray = data.get("skill") != null ? (JsonArray) data.get("skill") : null;
        String experience = data.get("experience") != null ? (String) data.get("experience") : null;
        String filter = data.get("filter") != null ? (String) data.get("filter") : null;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("SEARCH_JOB", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca da vaga
                    String selectSql = "SELECT * FROM recrutador_vagas rv JOIN vagas v ON rv.vaga_id = v.id";
                    boolean hasSkill = skillArray != null && !skillArray.isEmpty();
                    boolean hasExperience = experience != null && !experience.isEmpty();

                    if (hasSkill || hasExperience) {
                        selectSql += " WHERE";
                        if (hasSkill) {
                            selectSql += " v.habilidade IN (";
                            for (int i = 0; i < skillArray.size(); i++) {
                                selectSql += "?";
                                if (i < skillArray.size() - 1) {
                                    selectSql += ",";
                                }
                            }
                            selectSql += ")";
                            if (hasExperience) {
                                selectSql += filter.equals("E") ? " AND" : " OR";
                            }
                        }
                        if (hasExperience) {
                            selectSql += " v.experiencia = ?";
                        }
                    }

                    PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                    int index = 1;
                    if (hasSkill) {
                        for (Object skill : skillArray) {
                            selectStmt.setString(index++, (String) skill);
                        }
                    }
                    if (hasExperience) {
                        selectStmt.setString(index++, experience);
                    }

                    rs = selectStmt.executeQuery();

                    JsonArray jobset = new JsonArray();
                    while (rs.next()) {
                        // A vaga foi encontrada, então adicione os detalhes da vaga ao conjunto de vagas
                        String foundSkill = rs.getString("habilidade");
                        String foundExperience = rs.getString("experiencia");
                        int id = rs.getInt("id");

                        JsonObject job = new JsonObject();
                        job.put("skill", foundSkill);
                        job.put("experience", foundExperience);
                        job.put("id", id);
                        jobset.add(job);
                    }

                    // Crie o objeto de resposta
                    JsonObject responseData = new JsonObject();
                    responseData.put("jobset_size", jobset.size());
                    responseData.put("jobset", jobset);

                    messageSender.sendMessage("SEARCH_JOB", "SUCCESS", responseData);
                } catch (Exception e) {
                    messageSender.sendMessage("SEARCH_JOB", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
*/
}
