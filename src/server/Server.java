package server;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import db.DatabaseInitializer;
import utils.MessageSender;
import utils.Validation;
import view.ConnectedClientsView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

                MessageSender messageSender = new MessageSender(out);
                OperationExecutorCandidate candidate = new OperationExecutorCandidate(out);
                OperationExecutorRecruiter recruiter = new OperationExecutorRecruiter(out);

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
                                candidate.executeLoginCandidate(requestJson);
                                break;
                            case "SIGNUP_CANDIDATE":
                                candidate.executeSignupCandidate(requestJson);
                                break;
                            case "LOOKUP_ACCOUNT_CANDIDATE":
                                candidate.executeLookupCandidate(requestJson);
                                break;
                            case "LOGOUT_CANDIDATE":
                                candidate.executeLogoutCandidate(requestJson);
                                break;
                            case "UPDATE_ACCOUNT_CANDIDATE":
                                candidate.executeUpdateCandidate(requestJson);
                                break;
                            case "DELETE_ACCOUNT_CANDIDATE":
                                candidate.executeDeleteCandidate(requestJson);
                                break;
                            case "INCLUDE_SKILL":
                                candidate.executeIncludeSkill(requestJson);
                                break;
                            case "LOOKUP_SKILL":
                                candidate.executeLookUpSkill(requestJson);
                                break;
                            case "LOOKUP_SKILLSET":
                                candidate.executeLookUpSkillSet(requestJson);
                                break;
                            case "DELETE_SKILL":
                                candidate.executeDeleteSkill(requestJson);
                                break;
                            case "UPDATE_SKILL":
                                candidate.executeUpdateSkill(requestJson);
                                break;
                            case "SEARCH_JOB":
                                candidate.executeSearchJob(requestJson);
                                break;
                            case "GET_COMPANY":
                                candidate.executeGetCompany(requestJson);
                                break;
                            case "LOGIN_RECRUITER":
                                recruiter.executeLoginRecruiter(requestJson);
                                break;
                            case "SIGNUP_RECRUITER":
                                recruiter.executeSignupRecruiter(requestJson);
                                break;
                            case "LOOKUP_ACCOUNT_RECRUITER":
                                recruiter.executeLookupRecruiter(requestJson);
                                break;
                            case "LOGOUT_RECRUITER":
                                recruiter.executeLogoutRecruiter(requestJson);
                                break;
                            case "UPDATE_ACCOUNT_RECRUITER":
                                recruiter.executeUpdateRecruiter(requestJson);
                                break;
                            case "DELETE_ACCOUNT_RECRUITER":
                                recruiter.executeDeleteRecruiter(requestJson);
                                break;
                            case "INCLUDE_JOB":
                                recruiter.executeIncludeJob(requestJson);
                                break;
                            case "LOOKUP_JOB":
                                recruiter.executeLookUpJob(requestJson);
                                break;
                            case "LOOKUP_JOBSET":
                                recruiter.executeLookUpJobSet(requestJson);
                                break;
                            case "DELETE_JOB":
                                recruiter.executeDeleteJob(requestJson);
                                break;
                            case "UPDATE_JOB":
                                recruiter.executeUpdateJob(requestJson);
                                break;
                            case "SET_JOB_AVAILABLE":
                                recruiter.setJobAvailable(requestJson);
                                break;
                            case "SET_JOB_SEARCHABLE":
                                recruiter.setJobSearchable(requestJson);
                                break;
                            case "SEARCH_CANDIDATE":
                                recruiter.executeSearchCandidate(requestJson);
                                break;
                            case "CHOOSE_CANDIDATE":
                                recruiter.executeChooseCandidate(requestJson);
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
                ConnectedClientsView connectedClientsScreen = new ConnectedClientsView(); ////
                boolean running = true;
                while (running) {
                    System.out.println("Aguardando conexão...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket);
                    // Crie uma nova instância da classe Server para cada cliente e inicie a thread
                    Server server = new Server(clientSocket, fileWriter);
                    connectedClientsScreen.updateClientInfo(clientSocket);
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
