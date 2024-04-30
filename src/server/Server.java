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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private Socket clientSocket;
    private BufferedWriter fileWriter;
    private static List<String> tokenBlacklist = new ArrayList<>();
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

                    if ("LOGIN_CANDIDATE".equals(operation)) {
                        JsonObject data = (JsonObject) requestJson.get("data");
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
                                    String key = "DISTRIBUIDOS";

                                    String jwtToken = Jwts.builder()
                                            .claim("id", userId)
                                            .claim("role", "CANDIDATE")
                                            .signWith(SignatureAlgorithm.HS256, key)
                                            .compact();

                                    ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", jwtToken);
                                    out.println(successResponse.toJsonString());
                                } else {
                                    ResponseMessage invalidPasswordResponse = new ResponseMessage(operation, "INVALID_PASSWORD", "");
                                    out.println(invalidPasswordResponse.toJsonString());
                                }
                            } else {
                                ResponseMessage userNotFoundResponse = new ResponseMessage(operation, "USER_NOT_FOUND", "");
                                out.println(userNotFoundResponse.toJsonString());
                            }
                        } catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }
                    } if ("SIGNUP_CANDIDATE".equals(operation)) {
                        JsonObject data = (JsonObject) requestJson.get("data");
                        String email = (String) data.get("email");
                        String password = (String) data.get("password");
                        String name = (String) data.get("name");

                        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                             Statement stmt = conn.createStatement()) {

                            String sql = "SELECT * FROM candidatos WHERE email = '" + email + "'";
                            ResultSet rs = stmt.executeQuery(sql);

                            if (rs.next()) {
                                ResponseMessage userExistsResponse = new ResponseMessage(operation, "USER_EXISTS", "");
                                out.println(userExistsResponse.toJsonString());
                            } else {
                                sql = "INSERT INTO candidatos (nome, email, senha) VALUES ('" + name + "', '" + email + "', '" + password + "')";
                                stmt.executeUpdate(sql);

                                ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", "");
                                out.println(successResponse.toJsonString());
                            }
                        } catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }

                    } if ("LOOKUP_ACCOUNT_CANDIDATE".equals(operation)) {
                        String token = (String) requestJson.get("token");

                        //System.out.println("test 1\n");
                        if (tokenBlacklist.contains(token)) {
                            // O token foi invalidado, então retorne um erro
                            JsonObject responseData = new JsonObject();
                            ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                            out.println(errorResponse.toJsonString());
                        }else{
                            try {
                                Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                                int userId = (int) claims.getBody().get("id");
                                System.out.println("\nuser id:");
                                System.out.println(userId);
                                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                                     Statement stmt = conn.createStatement()) {
                                    //System.out.println("test 3\n");
                                    String sql = "SELECT * FROM candidatos WHERE id = " + userId;
                                    ResultSet rs = stmt.executeQuery(sql);

                                    if (rs.next()) {
                                        String storedEmail = rs.getString("email");
                                        String storedPassword = rs.getString("senha");
                                        String storedName = rs.getString("nome");


                                        System.out.println(storedEmail);
                                        System.out.println(storedPassword);
                                        System.out.println(storedName);

                                        JsonObject responseData = new JsonObject();
                                        responseData.put("email", storedEmail);
                                        responseData.put("password", storedPassword);
                                        responseData.put("name", storedName);

                                        ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                        out.println(successResponse.toJsonString());
                                    }
                                } catch (SQLException e) {
                                    System.err.println(e.getMessage());
                                }
                            } catch (Exception e) {
                                ResponseMessage invalidTokenResponse = new ResponseMessage(operation, "INVALID_TOKEN", "");
                                out.println(invalidTokenResponse.toJsonString());
                            }
                        }

                    }if ("LOGOUT_CANDIDATE".equals(operation)) {
                        String token = (String) requestJson.get("token");

                        // Adicione o token à lista de bloqueio
                        tokenBlacklist.add(token);

                        JsonObject responseData = new JsonObject();
                        ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                        out.println(successResponse.toJsonString());
                    }if ("UPDATE_ACCOUNT_CANDIDATE".equals(operation)) {
                        String token = (String) requestJson.get("token");
                        JsonObject data = (JsonObject) requestJson.get("data");
                        String email = (String) data.get("email");
                        String password = (String) data.get("password");
                        String name = (String) data.get("name");

                        // Verifique se o token está na lista de bloqueio
                        if (tokenBlacklist.contains(token)) {
                            // O token foi invalidado, então retorne um erro
                            JsonObject responseData = new JsonObject();
                            ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                            out.println(errorResponse.toJsonString());
                        } else {
                            // O token é válido, então prossiga normalmente
                            try {
                                Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                                int userId = (int) claims.getBody().get("id");

                                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                                     PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidatos WHERE email = ? AND id != ?")) {

                                    stmt.setString(1, email);
                                    stmt.setInt(2, userId);
                                    ResultSet rs = stmt.executeQuery();

                                    if (rs.next()) {
                                        // O email já está sendo usado por outro usuário
                                        JsonObject responseData = new JsonObject();
                                        ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_EMAIL", responseData);
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
                                        out.println(successResponse.toJsonString());
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Erro ao validar o token: " + e.getMessage());
                                JsonObject responseData = new JsonObject();
                                ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                out.println(errorResponse.toJsonString());
                            }
                        }
                    }if ("DELETE_ACCOUNT_CANDIDATE".equals(operation)) {
                        String token = (String) requestJson.get("token");

                        // Verifique se o token está na lista de bloqueio
                        if (tokenBlacklist.contains(token)) {
                            // O token foi invalidado, então retorne um erro
                            JsonObject responseData = new JsonObject();
                            ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                            out.println(errorResponse.toJsonString());
                        } else {
                            // O token é válido, então prossiga normalmente
                            try {
                                Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
                                int userId = (int) claims.getBody().get("id");

                                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
                                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM candidatos WHERE id = ?")) {

                                    stmt.setInt(1, userId);
                                    stmt.executeUpdate();

                                    JsonObject responseData = new JsonObject();
                                    ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", responseData);
                                    out.println(successResponse.toJsonString());
                                }
                            } catch (Exception e) {
                                System.err.println("Erro ao validar o token: " + e.getMessage());
                                JsonObject responseData = new JsonObject();
                                ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
                                out.println(errorResponse.toJsonString());
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        int serverPort = 0;
        boolean validPort = false;

        while (!validPort) {
            try {
                System.out.println("Digite o número da porta para iniciar o servidor:");
                String portInput = reader.readLine();
                serverPort = Integer.parseInt(portInput);

                if (serverPort > 20000 && serverPort < 25000) {
                    validPort = true;
                } else {
                    System.out.println("Por favor, insira uma porta vállida (20000 - 25000).");
                }
            } catch (NumberFormatException | IOException e) {
                System.out.println(e.getMessage());
            }
        }

        BufferedWriter fileWriter = null;

        try {
            fileWriter = new BufferedWriter(new FileWriter("server_log.txt", true));

            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Servidor iniciado na porta " + serverPort);

                while (true) {
                    System.out.println("Aguardando conexão...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket);

                    new Server(clientSocket, fileWriter);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}

/*//para validar token
 if (tokenBlacklist.contains(token)) {
    // O token foi invalidado, então retorne um erro
    JsonObject responseData = new JsonObject();
    ResponseMessage errorResponse = new ResponseMessage(operation, "INVALID_TOKEN", responseData);
    out.println(errorResponse.toJsonString());
} else {
    // O token é válido, então prossiga normalmente
    // Aqui você colocaria o código para processar a solicitação do usuário
}
* */