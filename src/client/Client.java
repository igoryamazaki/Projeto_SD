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
