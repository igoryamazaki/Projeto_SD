package client;

import view.ConnectionView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    static String serverIP;
    static int serverPort;
    static boolean connected = false;
    private static Socket socket;

    public void setIP(String serverIP) {
        this.serverIP = serverIP;
    }
    public void setPort(int serverPort) {
        this.serverPort = serverPort;
    }
    public void setConnect(boolean connected) {
        this.connected = connected;
    }
    public boolean getConnect() {
        return this.connected;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String token = null;

        Client client = new Client();
        ConnectionView connectionView = new ConnectionView(client);

        /*
        System.out.println("Digite o endereço IP do servidor:");
        String serverIP = reader.readLine();

        System.out.println("Digite a porta do servidor:");
        int serverPort = Integer.parseInt(reader.readLine());
*/
        while (true) {/*

            System.out.println("=== Portal de vagas ===");
            System.out.println("1. Cadastro");
            System.out.println("2. Login");
            System.out.println("3. Visualizar Dados");
            System.out.println("4. Atualização de dados");
            System.out.println("5. Excluir conta");
            System.out.println("6. Logout");
            System.out.println("Escolha uma opção: ");
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

                    client.sendRequestToServer(signupJsonRequest);
                    break;
                case 2:
                    System.out.println("Digite o endereço de email para login:");
                    String loginEmail = reader.readLine();

                    System.out.println("Digite a senha para login:");
                    String loginPassword = reader.readLine();

                    RequestMessage loginRequest = new RequestMessage("LOGIN_CANDIDATE", loginEmail, loginPassword);
                    String loginJsonRequest = loginRequest.toJsonString();

                    String loginResponse = client.sendRequestToServer(loginJsonRequest);
                    JsonObject loginJson = Jsoner.deserialize(loginResponse, new JsonObject());
                    JsonObject data = (JsonObject) loginJson.get("data");
                    if (data != null) {
                        token = (String) data.get("token");
                    }
                    break;
                case 3:
                    RequestMessage lookupRequest = new RequestMessage("LOOKUP_ACCOUNT_CANDIDATE", token);
                    String lookupJsonRequest = lookupRequest.toJsonStringWithToken();

                    String response = client.sendRequestToServer(lookupJsonRequest);

                    JsonObject responseJson = Jsoner.deserialize(response, new JsonObject());
                    JsonObject dataLookUp = (JsonObject) responseJson.get("data");
                    if (dataLookUp != null) {
                        String email = (String) dataLookUp.get("email");
                        String password = (String) dataLookUp.get("password");
                        String name = (String) dataLookUp.get("name");

                        System.out.println("Email: " + email);
                        System.out.println("Password: " + password);
                        System.out.println("Name: " + name);
                    }
                    break;
                case 4:
                    System.out.println("Digite o novo endereço de email:");
                    String updateEmail = reader.readLine();

                    System.out.println("Digite a nova senha:");
                    String updatePassword = reader.readLine();

                    System.out.println("Digite o novo nome:");
                    String updateName = reader.readLine();

                    JsonObject updateData = new JsonObject();
                    updateData.put("email", updateEmail);
                    updateData.put("password", updatePassword);
                    updateData.put("name", updateName);

                    JsonObject updateRequest = new JsonObject();
                    updateRequest.put("operation", "UPDATE_ACCOUNT_CANDIDATE");
                    updateRequest.put("token", token); // O token atual do usuário
                    updateRequest.put("data", updateData);
                    String updateJsonRequest = updateRequest.toJson();

                    client.sendRequestToServer(updateJsonRequest);
                    break;
                case 5:
                    JsonObject deleteRequest = new JsonObject();
                    deleteRequest.put("operation", "DELETE_ACCOUNT_CANDIDATE");
                    deleteRequest.put("token", token); // O token atual do usuário
                    String deleteJsonRequest = deleteRequest.toJson();

                    client.sendRequestToServer(deleteJsonRequest);
                    break;
                case 6:
                    JsonObject logoutRequest = new JsonObject();
                    logoutRequest.put("operation", "LOGOUT_CANDIDATE");
                    logoutRequest.put("token", token); // O token atual do usuário
                    String logoutJsonRequest = logoutRequest.toJson();

                    client.sendRequestToServer(logoutJsonRequest);
            }
            */
        }
    }

    public boolean isConnect() {
        if (this.socket == null || this.socket.isClosed()) {
            try {
                this.socket = new Socket(serverIP, serverPort);
                this.connected = true;
            } catch (IOException e) {
                System.err.println(e.getMessage());
                this.connected = false;
            }
        }
        return this.connected;
    }

    public String sendRequestToServer(String jsonRequest) {
        String response = null;
        try {
            if (socket == null || socket.isClosed()) {
                this.isConnect();
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("[Sending]: " + jsonRequest);
            out.println(jsonRequest);

            response = in.readLine();
            System.out.println("[Receiving]: " + response);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return response;
    }

}
