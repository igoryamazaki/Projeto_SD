package view;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectedClientsView extends JFrame {
    private JTextArea clientInfoTextArea;

    public ConnectedClientsView() {
        setTitle("Clientes Conectados");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        clientInfoTextArea = new JTextArea();
        clientInfoTextArea.setEditable(false);
        add(new JScrollPane(clientInfoTextArea), BorderLayout.CENTER);

        setVisible(true);
    }

    public void updateClientInfo(Socket clientSocket) {
        InetAddress clientAddress = clientSocket.getInetAddress();
        int clientPort = clientSocket.getPort();
        String clientInfo = "IP: " + clientAddress.getHostAddress() + " | Porta: " + clientPort;
        clientInfoTextArea.append(clientInfo + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConnectedClientsView());
    }
}

