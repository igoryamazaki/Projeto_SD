package client;

import utils.RequestMessage;

import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Digite o endereço IP do servidor:");
        String serverIP = reader.readLine();

        System.out.println("Digite a porta do servidor:");
        int serverPort = Integer.parseInt(reader.readLine());

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Cadastro");
            System.out.println("2. Login");
            System.out.println("3. Atualização de dados");
            System.out.println("4. Excluir conta");
            System.out.println("5. Sair");

            int option = Integer.parseInt(reader.readLine());

            switch (option) {
                case 1:
                    System.out.println("Digite o endereço de email para cadastro:");
                    String signupEmail = reader.readLine();

                    System.out.println("Digite a senha para cadastro:");
                    String signupPassword = reader.readLine();

                    System.out.println("Digite o nome para cadastro:");
                    String signupName = reader.readLine();

                    RequestMessage signupRequest = new RequestMessage("SIGNUP_CANDIDATE", signupEmail, signupPassword, signupName);
                    String signupJsonRequest = signupRequest.toJsonString();

                    sendRequestToServer(serverIP, serverPort, signupJsonRequest);
                    break;
                case 2:
                    System.out.println("Digite o endereço de email para login:");
                    String loginEmail = reader.readLine();

                    System.out.println("Digite a senha para login:");
                    String loginPassword = reader.readLine();

                    RequestMessage loginRequest = new RequestMessage("LOGIN_CANDIDATE", loginEmail, loginPassword);
                    String loginJsonRequest = loginRequest.toJsonString();

                    sendRequestToServer(serverIP, serverPort, loginJsonRequest);
                    break;
                case 3:
                    // Implemente a lógica de atualização de dados aqui
                    break;
                case 4:
                    // Implemente a lógica de exclusão de conta aqui
                    break;
                case 5:
                    System.exit(0);
            }
        }
    }

    private static void sendRequestToServer(String serverIP, int serverPort, String jsonRequest) {
        try (Socket socket = new Socket(serverIP, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(jsonRequest);

            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
