package server;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import db.DatabaseInitializer;
import utils.ResponseMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Server extends Thread {
    private Socket clientSocket;
    private BufferedWriter fileWriter;

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
                                    String token = "example_token";
                                    ResponseMessage successResponse = new ResponseMessage(operation, "SUCCESS", token);
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
                    } else if ("SIGNUP_CANDIDATE".equals(operation)) {
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
            } /*{
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            }*/ catch (IOException e) {
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
