package server;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import db.DatabaseInitializer;
import utils.MessageSender;
import utils.Validation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private Socket clientSocket;
    private BufferedWriter fileWriter;
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

                MessageSender messageSender = new MessageSender(out);
                OperationExecutorCandidate operationExecutor = new OperationExecutorCandidate(out);

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

                    if (!Validation.isValidOperation(operation)) {
                        messageSender.sendMessage(operation, "INVALID_OPERATION", "");
                    } else {
                        switch (operation) {
                            case "LOGIN_CANDIDATE":
                                operationExecutor.executeLoginCandidate(requestJson);
                                break;
                            case "SIGNUP_CANDIDATE":
                                operationExecutor.executeSignupCandidate(requestJson);
                                break;
                            case "LOOKUP_ACCOUNT_CANDIDATE":
                                operationExecutor.executeLookupCandidate(requestJson);
                                break;
                            case "LOGOUT_CANDIDATE":
                                operationExecutor.executeLogoutCandidate(requestJson);
                                break;
                            case "UPDATE_ACCOUNT_CANDIDATE":
                                operationExecutor.executeUpdateCandidate(requestJson);
                                break;
                            case "DELETE_ACCOUNT_CANDIDATE":
                                operationExecutor.executeDeleteCandidate(requestJson);
                                break;
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
