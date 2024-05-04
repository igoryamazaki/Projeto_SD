package view;

import client.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionView extends JFrame{
    private JTextField txtfIP;
    private JTextField txtfPort;
    private JButton btnConnect;
    private JPanel panelConnection;
    private Client client;

    public ConnectionView(Client client) {
        this.client = client;
        setContentPane(panelConnection);
        setTitle("Conexão com Servidor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(250,150);
        setLocationRelativeTo(null);
        setVisible(true);


        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverIP = txtfIP.getText();
                int serverPort = Integer.parseInt(txtfPort.getText());
                client.setIP(serverIP);
                client.setPort(serverPort);
                //client.setConnect(true);
                if (client.isConnect()) {
                    // Abra a nova tela aqui. Supondo que MainView seja sua nova tela.
                     new RoleSelecitonView(client);
                    // Feche a tela de conexão
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(panelConnection, "Não foi possível conectar ao servidor. Por favor, tente novamente.");
                }

            }

        });
    }
}
