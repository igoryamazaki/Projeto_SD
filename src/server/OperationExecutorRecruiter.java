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

public class OperationExecutorRecruiter {
    private MessageSender messageSender;
    private JwtManager jwtManager;
    String key = "DISTRIBUIDOS";

    public OperationExecutorRecruiter(PrintWriter out) {
        this.messageSender = new MessageSender(out);
        this.jwtManager = new JwtManager(key);
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
/*
                        String jwtToken = Jwts.builder().setHeaderParam("typ", "JWT")
                                .claim("id", userIdString)
                                .claim("role", "RECRUITER")
                                .signWith(SignatureAlgorithm.HS256, key).compact();
*/
                        String jwtToken = jwtManager.createToken(userIdString, "RECRUITER");
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
                    //Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                    Jws<Claims> claims = jwtManager.validateToken(token);

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
                        //Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                        Jws<Claims> claims = jwtManager.validateToken(token);
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
                    //Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                    Jws<Claims> claims = jwtManager.validateToken(token);
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
   /* public void executeIncludeJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token"); // Alteração aqui
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
                messageSender.sendMessage("INCLUDE_JOB", "INVALID_TOKEN", "");
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // Verifique se a vaga já existe para o recrutador
                    PreparedStatement checkJobStmt = conn.prepareStatement("SELECT * FROM vagas v JOIN recrutador_vagas rv ON v.id = rv.vaga_id WHERE v.habilidade = ? AND rv.recrutador_id = ?");
                    checkJobStmt.setString(1, skill);
                    checkJobStmt.setInt(2, userId);
                    ResultSet rsJobCheck = checkJobStmt.executeQuery();

                    if (rsJobCheck.next()) {
                        // A vaga já existe para o recrutador
                        messageSender.sendMessage("INCLUDE_JOB", "JOB_EXISTS", "");
                    } else {
                        // A vaga não existe para o recrutador, então prossiga com a adição da vaga
                        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO vagas (habilidade, experiencia) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS); // Alteração aqui
                        insertStmt.setString(1, skill);
                        insertStmt.setString(2, experience);
                        insertStmt.executeUpdate();

                        // Recupere o ID da vaga que acabou de ser inserida
                        ResultSet rsJob = insertStmt.getGeneratedKeys();
                        int jobId = -1;
                        if (rsJob.next()) {
                            jobId = rsJob.getInt(1);
                        }

                        // Insira o mapeamento entre o recrutador e a vaga na tabela recrutador_vagas
                        PreparedStatement insertMappingStmt = conn.prepareStatement("INSERT INTO recrutador_vagas (recrutador_id, vaga_id) VALUES (?, ?)");
                        insertMappingStmt.setInt(1, userId);
                        insertMappingStmt.setInt(2, jobId);
                        insertMappingStmt.executeUpdate();

                        messageSender.sendMessage("INCLUDE_JOB", "SUCCESS", "");
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("INCLUDE_JOB", "INVALID_TOKEN", "");
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }*/
   public void executeIncludeJob(JsonObject requestJson) {
       JsonObject data = (JsonObject) requestJson.get("data");
       String token = (String) requestJson.get("token");
       String skill = (String) data.get("skill");
       String experience = (String) data.get("experience");

       // Verifique se a habilidade é uma das pré-definidas
       if (!Arrays.asList(Skills.getSkills()).contains(skill)) {
           messageSender.sendMessage("INCLUDE_JOB", "SKILL_NOT_EXIST", "");
       }else {

           try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
               String sql = "SELECT * FROM active_tokens WHERE token = ?";
               PreparedStatement pstmt = conn.prepareStatement(sql);
               pstmt.setString(1, token);
               ResultSet rs = pstmt.executeQuery();

               if (!rs.next()) {
                   messageSender.sendMessage("INCLUDE_JOB", "INVALID_TOKEN", "");
               } else {
                   try {
                       Jws<Claims> claims = jwtManager.validateToken(token);
                       String userIdString = (String) claims.getBody().get("id");
                       int userId = Integer.parseInt(userIdString);

                       // A vaga não existe para o recrutador, então prossiga com a adição da vaga
                       PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO vagas (habilidade, experiencia, disponivel, divulgavel) VALUES (?, ?, 0, 0)", Statement.RETURN_GENERATED_KEYS);
                       insertStmt.setString(1, skill);
                       insertStmt.setString(2, experience);
                       insertStmt.executeUpdate();

                       // Recupere o ID da vaga que acabou de ser inserida
                       ResultSet rsJob = insertStmt.getGeneratedKeys();
                       int jobId = -1;
                       if (rsJob.next()) {
                           jobId = rsJob.getInt(1);
                       }

                       // Insira o mapeamento entre o recrutador e a vaga na tabela recrutador_vagas
                       PreparedStatement insertMappingStmt = conn.prepareStatement("INSERT INTO recrutador_vagas (recrutador_id, vaga_id) VALUES (?, ?)");
                       insertMappingStmt.setInt(1, userId);
                       insertMappingStmt.setInt(2, jobId);
                       insertMappingStmt.executeUpdate();

                       messageSender.sendMessage("INCLUDE_JOB", "SUCCESS", "");

                   } catch (Exception e) {
                       messageSender.sendMessage("INCLUDE_JOB", "INVALID_TOKEN", "");
                       System.out.println("invalid token 2: " + e);
                   }
               }
           } catch (SQLException e) {
               System.err.println(e.getMessage());
           }
       }
   }

    public void executeLookUpJobSet(JsonObject requestJson) {
        String token = (String) requestJson.get("token"); // Alteração aqui

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_JOBSET", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca do conjunto de vagas
                    PreparedStatement selectStmt = conn.prepareStatement("SELECT vagas.* FROM vagas JOIN recrutador_vagas ON vagas.id = recrutador_vagas.vaga_id WHERE recrutador_vagas.recrutador_id = ?");
                    selectStmt.setInt(1, userId);
                    rs = selectStmt.executeQuery();

                    JsonArray jobset = new JsonArray();
                    while (rs.next()) {
                        // Para cada vaga, adicione os detalhes da vaga ao conjunto de vagas
                        String skill = rs.getString("habilidade");
                        String experience = rs.getString("experiencia");
                        String id = rs.getString("id");

                        int searchable = rs.getInt("divulgavel"); // Valor 0 ou 1
                        int available = rs.getInt("disponivel"); // Valor 0 ou 1

                        // Transforme os valores em "NO" ou "YES"
                        String searchableText = (searchable == 1) ? "YES" : "NO";
                        String availableText = (available == 1) ? "YES" : "NO";

                        JsonObject jobData = new JsonObject();
                        jobData.put("skill", skill);
                        jobData.put("experience", experience);
                        jobData.put("id", id);
                        jobData.put("searchable", searchableText);
                        jobData.put("available", availableText);

                        jobset.add(jobData);
                    }

                    // Retorne o conjunto de vagas
                    JsonObject responseData = new JsonObject();
                    responseData.put("jobset_size", jobset.size());
                    responseData.put("jobset", jobset);

                    messageSender.sendMessage("LOOKUP_JOBSET", "SUCCESS", responseData);
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_JOBSET", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void executeLookUpJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token"); // Alteração aqui
        String jobId = (String) data.get("id"); // Habilidade da vaga

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("LOOKUP_JOB", "INVALID_TOKEN", new JsonObject());
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // O token é válido, então prossiga com a busca da vaga
                    PreparedStatement selectStmt = conn.prepareStatement("SELECT * FROM vagas WHERE id = ?");
                    selectStmt.setString(1, jobId);
                    rs = selectStmt.executeQuery();

                    if (!rs.next()) {
                        // A vaga não está no banco de dados
                        messageSender.sendMessage("LOOKUP_JOB", "JOB_NOT_FOUND", new JsonObject());
                    } else {
                        // A vaga foi encontrada, então retorne os detalhes da vaga
                        String skill = rs.getString("habilidade");
                        String experience = rs.getString("experiencia");
                        int id = rs.getInt("id");

                        int searchable = rs.getInt("divulgavel"); // Valor 0 ou 1
                        int available = rs.getInt("disponivel"); // Valor 0 ou 1

                        // Transforme os valores em "NO" ou "YES"
                        String searchableText = (searchable == 1) ? "YES" : "NO";
                        String availableText = (available == 1) ? "YES" : "NO";

                        JsonObject responseData = new JsonObject();
                        responseData.put("skill", skill);
                        responseData.put("experience", experience);
                        responseData.put("id", id);
                        responseData.put("searchable", searchableText);
                        responseData.put("available", availableText);

                        messageSender.sendMessage("LOOKUP_JOB", "SUCCESS", responseData);
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("LOOKUP_JOB", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeDeleteJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String habilidade = (String) data.get("skill");
        String idJob = (String) data.get("id");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token está no banco de dados
            String sql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // O token não está no banco de dados, então é inválido
                messageSender.sendMessage("DELETE_JOB", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM vagas WHERE id = ?"); // Alteração aqui
                    deleteStmt.setString(1, idJob);
                    int affectedRows = deleteStmt.executeUpdate();
                    if (affectedRows == 0) {
                        messageSender.sendMessage("DELETE_JOB", "JOB_NOT_FOUND", new JsonObject());
                    } else {
                        // O trabalho foi excluído, então retorne sucesso
                        messageSender.sendMessage("DELETE_JOB", "SUCCESS", new JsonObject());
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("DELETE_JOB", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void executeUpdateJob(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        String id = (String) data.get("id"); // ID da vaga que será atualizada
        String newSkill = (String) data.get("skill"); // Nova habilidade
        String experience = (String) data.get("experience"); // Experiência

        if (!Validation.areFieldsValidIncludeJob(data)) {
            messageSender.sendMessage("UPDATE_JOB", "INVALID_FIELD", "");
        } else {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, token);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    messageSender.sendMessage("UPDATE_JOB", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 1: ");
                } else {
                    try {
                        Jws<Claims> claims = jwtManager.validateToken(token);
                        String userIdString = (String) claims.getBody().get("id");
                        int userId = Integer.parseInt(userIdString);

                        PreparedStatement checkNewJobStmt = conn.prepareStatement("SELECT * FROM vagas v JOIN recrutador_vagas rv ON v.id = rv.vaga_id WHERE v.id = ? AND rv.recrutador_id = ?");
                        checkNewJobStmt.setString(1, id);
                        checkNewJobStmt.setInt(2, userId);
                        ResultSet rsNewJobCheck = checkNewJobStmt.executeQuery();

                        if (!rsNewJobCheck.next()) {
                            messageSender.sendMessage("UPDATE_JOB", "JOB_NOT_FOUND", new JsonObject());
                        } else {
                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE vagas SET habilidade = ?, experiencia = ? WHERE id = ?");
                            updateStmt.setString(1, newSkill);
                            updateStmt.setString(2, experience);
                            updateStmt.setString(3, id);
                            int affectedRows = updateStmt.executeUpdate();

                            if (affectedRows == 0) {
                                messageSender.sendMessage("UPDATE_JOB", "JOB_NOT_FOUND", new JsonObject());
                            } else {
                                messageSender.sendMessage("UPDATE_JOB", "SUCCESS", new JsonObject());
                            }
                        }
                    } catch (Exception e) {
                        messageSender.sendMessage("UPDATE_JOB", "INVALID_TOKEN", new JsonObject());
                        System.out.println("invalid token 2: " + e);
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    public void setJobAvailable(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String jobId = (String) data.get("id");
        String availableValue = (String) data.get("available");
        int availableInt = availableValue.equalsIgnoreCase("YES") ? 1 : 0;
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique o token e recupere o ID do recrutador
            String tokenSql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement tokenPstmt = conn.prepareStatement(tokenSql);
            tokenPstmt.setString(1, token);
            ResultSet tokenRs = tokenPstmt.executeQuery();

            if (!tokenRs.next()) {
                // Token inválido
                messageSender.sendMessage("SET_JOB_AVAILABLE", "INVALID_TOKEN", "");

            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);
                    //int recrutadorId = tokenRs.getInt("recrutador_id");

                    // Verifique se a vaga pertence ao recrutador
                    String vagaSql = "SELECT * FROM recrutador_vagas WHERE recrutador_id = ? AND vaga_id = ?";
                    PreparedStatement vagaPstmt = conn.prepareStatement(vagaSql);
                    vagaPstmt.setInt(1, userId);
                    vagaPstmt.setString(2, jobId);
                    ResultSet vagaRs = vagaPstmt.executeQuery();

                    if (!vagaRs.next()) {
                        // Vaga não encontrada para o recrutador
                        messageSender.sendMessage("SET_JOB_AVAILABLE", "JOB_NOT_FOUND", "");

                    } else {
                        // Atualize o status da vaga
                        String updateSql = "UPDATE vagas SET disponivel = ? WHERE id = ?";
                        PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setInt(1, availableInt);
                        updatePstmt.setString(2, jobId);
                        updatePstmt.executeUpdate();

                        messageSender.sendMessage("SET_JOB_AVAILABLE", "SUCCESS", "");
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("SET_JOB_AVAILABLE", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: "+e);
                }
            }
        } catch (SQLException e) {
            messageSender.sendMessage("SET_JOB_AVAILABLE", "INVALID_TOKEN", new JsonObject());
        }
    }
    public void setJobSearchable(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String jobId = (String) data.get("id");
        String searchableValue = (String) data.get("searchable");
        int searchableInt = searchableValue.equalsIgnoreCase("YES") ? 1 : 0;
        String token = (String) requestJson.get("token");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            String tokenSql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement tokenPstmt = conn.prepareStatement(tokenSql);
            tokenPstmt.setString(1, token);
            ResultSet tokenRs = tokenPstmt.executeQuery();

            if (!tokenRs.next()) {
                // Token inválido
                messageSender.sendMessage("SET_JOB_SEARCHABLE", "INVALID_TOKEN", "");

            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);
                   // int recrutadorId = tokenRs.getInt("recrutador_id");

                    // Verifique se a vaga pertence ao recrutador
                    String vagaSql = "SELECT * FROM recrutador_vagas WHERE recrutador_id = ? AND vaga_id = ?";
                    PreparedStatement vagaPstmt = conn.prepareStatement(vagaSql);
                    vagaPstmt.setInt(1, userId);
                    vagaPstmt.setString(2, jobId);
                    ResultSet vagaRs = vagaPstmt.executeQuery();

                    if (!vagaRs.next()) {
                        // Vaga não encontrada para o recrutador
                        messageSender.sendMessage("SET_JOB_SEARCHABLE", "JOB_NOT_FOUND", "");

                    } else {

                        // Atualize o status da vaga
                        String updateSql = "UPDATE vagas SET divulgavel = ? WHERE id = ?";
                        PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                        updatePstmt.setInt(1, searchableInt);
                        updatePstmt.setString(2, jobId);
                        updatePstmt.executeUpdate();

                        messageSender.sendMessage("SET_JOB_SEARCHABLE", "SUCCESS", "");
                    }
                }catch (Exception e) {
                    messageSender.sendMessage("SET_JOB_SEARCHABLE", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: " + e);
                }
            }
        } catch (SQLException e) {
            messageSender.sendMessage("SET_JOB_SEARCHABLE", "INVALID_TOKEN", new JsonObject());
        }
    }
    public void executeSearchCandidate(JsonObject requestJson) {
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
                messageSender.sendMessage("SEARCH_CANDIDATE", "INVALID_TOKEN", new JsonObject());
                System.out.println("invalid token 1: ");
            } else {
                try {
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String userIdString = (String) claims.getBody().get("id");
                    int userId = Integer.parseInt(userIdString);

                    // Inicializa a consulta SQL base
                    //String selectSql = "SELECT * FROM candidato_habilidades rv JOIN habilidades v ON rv.habilidade_id = v.id";
                    String selectSql = "SELECT rv.*, v.habilidade, v.experiencia, c.nome FROM candidato_habilidades rv " +
                            "JOIN habilidades v ON rv.habilidade_id = v.id " +
                            "JOIN candidatos c ON rv.candidato_id = c.id";
                    // Verifica qual tipo de busca é e constrói a consulta SQL de acordo
                    if (skillArray != null && experience == null) {
                        // Busca apenas por habilidades
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
                    JsonArray profile = new JsonArray();

                    while (rs.next()) {
                        String foundSkill = rs.getString("habilidade");
                        String foundExperience = rs.getString("experiencia");
                        int id = rs.getInt("habilidade_id");
                        int candidateId = rs.getInt("candidato_id");
                        String foundName = rs.getString("nome");

                        JsonObject job = new JsonObject();
                        job.put("skill", foundSkill);
                        job.put("experience", foundExperience);
                        job.put("id", id);
                        job.put("id_user", candidateId);
                        job.put("name", foundName);
                        profile.add(job);
                    }

                    JsonObject responseData = new JsonObject();
                    responseData.put("profile_size", profile.size());
                    responseData.put("profile", profile);

                    messageSender.sendMessage("SEARCH_CANDIDATE", "SUCCESS", responseData);

                } catch (Exception e) {
                    messageSender.sendMessage("SEARCH_CANDIDATE", "INVALID_TOKEN", new JsonObject());
                    System.out.println("invalid token 2: " + e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    /*public void executeChooseCandidate(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        int userId = Integer.parseInt(data.get("id_user").toString());

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token é válido
            String tokenSql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement tokenPstmt = conn.prepareStatement(tokenSql);
            tokenPstmt.setString(1, token);
            ResultSet tokenRs = tokenPstmt.executeQuery();

            if (!tokenRs.next()) {
                messageSender.sendMessage("CHOOSE_CANDIDATE", "INVALID_TOKEN", "");
                System.out.println("Token inválido.");
            } else {
                try {
                    // Recupere o ID do recrutador a partir do token
                    Jws<Claims> claims = jwtManager.validateToken(token);
                    String recruiterIdString = (String) claims.getBody().get("id");
                    int recruiterId = Integer.parseInt(recruiterIdString);

                    // Verifique se o candidato existe e tem habilidades
                    String candidateSkillSql = "SELECT habilidade_id FROM candidato_habilidades WHERE candidato_id = ?";
                    PreparedStatement candidateSkillPstmt = conn.prepareStatement(candidateSkillSql);
                    candidateSkillPstmt.setInt(1, userId);
                    ResultSet candidateSkillRs = candidateSkillPstmt.executeQuery();

                    if (candidateSkillRs.next()) {
                        int candidateSkillId = candidateSkillRs.getInt("habilidade_id");

                        // Salve o mapeamento entre recrutador, candidato e vaga
                        String insertMappingSql = "INSERT INTO candidato_vagas (recrutador_id, candidato_id) VALUES (?, ?)";
                        PreparedStatement insertMappingStmt = conn.prepareStatement(insertMappingSql);
                        insertMappingStmt.setInt(1, recruiterId);
                        insertMappingStmt.setInt(2, userId);
                        insertMappingStmt.executeUpdate();

                        messageSender.sendMessage("CHOOSE_CANDIDATE", "SUCCESS", "");
                    } else {
                        messageSender.sendMessage("CHOOSE_CANDIDATE", "CANDIDATE_NOT_FOUND", "");
                    }
                } catch (Exception e) {
                    messageSender.sendMessage("CHOOSE_CANDIDATE", "INVALID_TOKEN", new JsonObject());
                    System.out.println("Erro ao validar o token: " + e);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }*/
    public void executeChooseCandidate(JsonObject requestJson) {
        JsonObject data = (JsonObject) requestJson.get("data");
        String token = (String) requestJson.get("token");
        int userId = Integer.parseInt(data.get("id_user").toString());

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
            // Verifique se o token é válido
            String tokenSql = "SELECT * FROM active_tokens WHERE token = ?";
            PreparedStatement tokenPstmt = conn.prepareStatement(tokenSql);
            tokenPstmt.setString(1, token);
            ResultSet tokenRs = tokenPstmt.executeQuery();

            if (!tokenRs.next()) {
                messageSender.sendMessage("CHOOSE_CANDIDATE", "INVALID_TOKEN", "");
                System.out.println("Token inválido.");
            } else {
                // Verifique se o candidato existe
                String candidateExistsSql = "SELECT COUNT(*) FROM candidatos WHERE id = ?";
                PreparedStatement candidateExistsPstmt = conn.prepareStatement(candidateExistsSql);
                candidateExistsPstmt.setInt(1, userId);
                ResultSet candidateExistsRs = candidateExistsPstmt.executeQuery();

                if (candidateExistsRs.next() && candidateExistsRs.getInt(1) > 0) {
                    try {
                        // Recupere o ID do recrutador a partir do token
                        Jws<Claims> claims = jwtManager.validateToken(token);
                        String recruiterIdString = (String) claims.getBody().get("id");
                        int recruiterId = Integer.parseInt(recruiterIdString);

                        String mappingExistsSql = "SELECT COUNT(*) FROM candidato_vagas WHERE recrutador_id = ? AND candidato_id = ?";
                        PreparedStatement mappingExistsPstmt = conn.prepareStatement(mappingExistsSql);
                        mappingExistsPstmt.setInt(1, recruiterId);
                        mappingExistsPstmt.setInt(2, userId);
                        ResultSet mappingExistsRs = mappingExistsPstmt.executeQuery();

                        if (mappingExistsRs.next() && mappingExistsRs.getInt(1) > 0) {
                            messageSender.sendMessage("CHOOSE_CANDIDATE", "CANDIDATE_NOT_FOUND", "");
                            System.out.println("Candidato já escolhido anteriormente.");
                        } else {
                            // Salve o mapeamento entre recrutador e candidato
                            String insertMappingSql = "INSERT INTO candidato_vagas (recrutador_id, candidato_id) VALUES (?, ?)";
                            PreparedStatement insertMappingStmt = conn.prepareStatement(insertMappingSql);
                            insertMappingStmt.setInt(1, recruiterId);
                            insertMappingStmt.setInt(2, userId);
                            insertMappingStmt.executeUpdate();

                            messageSender.sendMessage("CHOOSE_CANDIDATE", "SUCCESS", "");
                        }
                    }catch (Exception e) {
                        System.err.println(e.getMessage());
                        messageSender.sendMessage("CHOOSE_CANDIDATE", "CANDIDATE_NOT_FOUND", "");
                        System.out.println("candidate not found 1.");
                    }
                    } else {
                    messageSender.sendMessage("CHOOSE_CANDIDATE", "CANDIDATE_NOT_FOUND", "");
                    System.out.println("candidate not found 2.");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


}
