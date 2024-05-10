package server;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import db.DatabaseInitializer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import utils.ResponseMessage;
import utils.Validation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Server extends Thread {
    private Socket clientSocket;
    private BufferedWriter fileWriter;
    // private static List<String> tokenBlacklist = new ArrayList<>();
    String key = "DISTRIBUIDOS";

    public Server(Socket clientSoc, BufferedWriter writer) {
        clientSocket = clientSoc;
        fileWriter = writer;
        start();
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            boolean running = true;
            while (running) {
                String jsonMessage = in.readLine();
                System.out.println("[Receiving]: " + jsonMessage);
                if (jsonMessage != null && jsonMessage.equalsIgnoreCase("sair")) {
                    running = false;
                    continue;
                }
                if (jsonMessage != null) {
                    fileWriter.write(jsonMessage.length());
                    fileWriter.newLine();
                    fileWriter.flush();
                    JsonObject requestJson = (JsonObject) Jsoner.deserialize(jsonMessage);
                    String operation = (String) requestJson.get("operation");

                    JsonObject dataValidation = (JsonObject) requestJson.get("data");
                    if (!Validation.isValidOperation(operation)) {
                        ResponseMessage invalidOperationResponse = new ResponseMessage(operation, "INVALID_OPERATION", new JsonObject());
                        System.out.println("[Sending]: " + invalidOperationResponse.toJsonString());
                        out.println(invalidOperationResponse.toJsonString());
                        continue;
                    } else {
                        if ("LOGIN_CANDIDATE".equals(operation)) {
                            JsonObject data = (JsonObject) requestJson.get("data");

                            // Verifique se os campos são válidos
                            if (!Validation.areFieldsValidLogin(data)) {
                                ResponseMessage invalidFieldResponse = new ResponseMessage(operation, "INVALID_FIELD", new JsonObject());
                                System.out.println("[Sending]: " + invalidFieldResponse.toJsonString());
                                out.println(invalidFieldResponse.toJsonString());
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

                                            ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", jwtToken);
                                            System.out.println("[Sending]: " + successResponse.toJsonString());
                                            out.println(successResponse.toJsonString());
                                        } else {
                                            ResponseMessage invalidPasswordResponse = new ResponseMessage(operation, "INVALID_LOGIN", "");
                                            System.out.println("[Sending]: " + invalidPasswordResponse.toJsonString());
                                            out.println(invalidPasswordResponse.toJsonString());
                                        }
                                    } else {
                                        ResponseMessage userNotFoundResponse = new ResponseMessage(operation, "INVALID_LOGIN", "");
                                        System.out.println("[Sending]: " + userNotFoundResponse.toJsonString());
                                        out.println(userNotFoundResponse.toJsonString());
                                    }
                                } catch (SQLException e) {
                                    System.err.println(e.getMessage());
                                }
                            }
                        }
                        if ("SIGNUP_CANDIDATE".equals(operation)) {
                            JsonObject data = (JsonObject) requestJson.get("data");

                            // Verifique se os campos são válidos
                            if (!Validation.areFieldsValidSignup(data)) {
                                ResponseMessage invalidFieldResponse = new ResponseMessage(operation, "INVALID_FIELD", new JsonObject());
                                System.out.println("[Sending]: " + invalidFieldResponse.toJsonString());
                                out.println(invalidFieldResponse.toJsonString());
                            } else {
                                String email = (String) data.get("email");
                                String password = (String) data.get("password");
                                String name = (String) data.get("name");

                                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                                     Statement stmt = conn.createStatement()) {

                                    String sql = "SELECT * FROM candidatos WHERE email = '" + email + "'";
                                    ResultSet rs = stmt.executeQuery(sql);

                                    if (rs.next()) {
                                        ResponseMessage userExistsResponse = new ResponseMessage(operation, "USER_EXISTS", "");
                                        System.out.println("[Sending]: " + userExistsResponse.toJsonString());
                                        out.println(userExistsResponse.toJsonString());
                                    } else {
                                        sql = "INSERT INTO candidatos (nome, email, senha) VALUES ('" + name + "', '" + email + "', '" + password + "')";
                                        stmt.executeUpdate(sql);

                                        ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", "");
                                        System.out.println("[Sending]: " + successResponse.toJsonString());
                                        out.println(successResponse.toJsonString());
                                    }
                                } catch (SQLException e) {
                                    System.err.println(e.getMessage());
                                }
                            }

                        }
                        if ("LOOKUP_ACCOUNT_CANDIDATE".equals(operation)) {
                            String token = (String) requestJson.get("token");

                            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                                // Verifique se o token está no banco de dados
                                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                                PreparedStatement pstmt = conn.prepareStatement(sql);
                                pstmt.setString(1, token);
                                ResultSet rs = pstmt.executeQuery();

                                if (!rs.next()) {
                                    // O token não está no banco de dados, então é inválido
                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                    System.out.println("[Sending]: " + errorResponse.toJsonString());
                                    out.println(errorResponse.toJsonString());
                                } else {
                                    try {
                                        Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);

                                        String userIdString = (String) claims.getBody().get("id");
                                        //int userId = (int) claims.getBody().get("id");
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

                                            ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                            System.out.println("[Sending]: " + successResponse.toJsonString());
                                            out.println(successResponse.toJsonString());
                                        }
                                    } catch (Exception e) {
                                        ResponseMessage invalidTokenResponse = new ResponseMessage(operation, "INVALID_TOKEN", "");
                                        System.out.println("[Sending]: " + invalidTokenResponse.toJsonString());
                                        out.println(invalidTokenResponse.toJsonString());
                                    }
                                }
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            }

                        }
                        if ("LOGOUT_CANDIDATE".equals(operation)) {
                            String token = (String) requestJson.get("token");

                            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                                // Verifique se o token está no banco de dados
                                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                                PreparedStatement pstmt = conn.prepareStatement(sql);
                                pstmt.setString(1, token);
                                ResultSet rs = pstmt.executeQuery();

                                if (!rs.next()) {
                                    // O token não está no banco de dados, então é inválido
                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                    System.out.println("[Sending]: " + errorResponse.toJsonString());
                                    out.println(errorResponse.toJsonString());
                                } else {
                                    // Remova o token do banco de dados
                                    sql = "DELETE FROM active_tokens WHERE token = ?";
                                    pstmt = conn.prepareStatement(sql);
                                    pstmt.setString(1, token);
                                    pstmt.executeUpdate();

                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                    System.out.println("[Sending]: " + successResponse.toJsonString());
                                    out.println(successResponse.toJsonString());
                                }
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                        if ("UPDATE_ACCOUNT_CANDIDATE".equals(operation)) {
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
                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                    System.out.println("[Sending]: " + errorResponse.toJsonString());
                                    out.println(errorResponse.toJsonString());
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
                                            JsonObject responseData = new JsonObject();
                                            ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_EMAIL", responseData);
                                            System.out.println("[Sending]: " + errorResponse.toJsonString());
                                            out.println(errorResponse.toJsonString());
                                        } else {
                                            // O email não está sendo usado, então prossiga com a atualização
                                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE candidatos SET email = ?, senha = ?, nome = ? WHERE id = ?");
                                            updateStmt.setString(1, email);
                                            updateStmt.setString(2, password);
                                            updateStmt.setString(3, name);
                                            updateStmt.setInt(4, userId);
                                            updateStmt.executeUpdate();

                                            JsonObject responseData = new JsonObject();
                                            ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                            System.out.println("[Sending]: " + successResponse.toJsonString());
                                            out.println(successResponse.toJsonString());
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Erro ao validar o token: " + e.getMessage());
                                        JsonObject responseData = new JsonObject();
                                        ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                        System.out.println("[Sending]: " + errorResponse.toJsonString());
                                        out.println(errorResponse.toJsonString());
                                    }
                                }
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                        if ("DELETE_ACCOUNT_CANDIDATE".equals(operation)) {
                            String token = (String) requestJson.get("token");

                            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db")) {
                                // Verifique se o token está no banco de dados
                                String sql = "SELECT * FROM active_tokens WHERE token = ?";
                                PreparedStatement pstmt = conn.prepareStatement(sql);
                                pstmt.setString(1, token);
                                ResultSet rs = pstmt.executeQuery();

                                if (!rs.next()) {
                                    // O token não está no banco de dados, então é inválido
                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                    System.out.println("[Sending]: " + errorResponse.toJsonString());
                                    out.println(errorResponse.toJsonString());
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

                                        JsonObject responseData = new JsonObject();
                                        ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                        System.out.println("[Sending]: " + successResponse.toJsonString());
                                        out.println(successResponse.toJsonString());
                                    } catch (Exception e) {
                                        ResponseMessage invalidTokenResponse = new ResponseMessage(operation, "INVALID_TOKEN", "");
                                        System.out.println("[Sending]: " + invalidTokenResponse.toJsonString());
                                        out.println(invalidTokenResponse.toJsonString());
                                    }
                                }
                            } catch (SQLException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (IOException | JsonException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        int serverPort = 21234;

        BufferedWriter fileWriter = null;

        try {
            fileWriter = new BufferedWriter(new FileWriter("server_log.txt", true));

            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Servidor iniciado na porta " + serverPort);

                boolean running = true;
                while (running) {
                    System.out.println("Aguardando conexão...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket);

                    // Crie uma nova instância da classe Server para cada cliente e inicie a thread
                    Server server = new Server(clientSocket, fileWriter);
                    if (!server.isAlive()) {
                        server.start();
                    }

                    // Verifique se o cliente se desconectou
                    if (clientSocket.isClosed()) {
                        System.out.println("Cliente desconectado: " + clientSocket);
                        running = false; // Saia do loop
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
