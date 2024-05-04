package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateUpdateView extends JFrame{
    private Client client;
    private String token;
    private JButton btnUpdate;
    private JTextField txtfUser;
    private JTextField txtfEmail;
    private JPasswordField txtfPassword;
    private JButton btnReturn;
    private JPanel panelCandidateUpdate;

    public CandidateUpdateView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelCandidateUpdate);
        setTitle("Entrar como");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300,200);
        setLocationRelativeTo(null);
        setVisible(true);
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtfEmail.getText();
                String password = new String(txtfPassword.getPassword());
                String name = txtfUser.getText();

                JsonObject updateData = new JsonObject();
                updateData.put("email", email);
                updateData.put("password", password);
                updateData.put("name", name);

                JsonObject updateRequest = new JsonObject();
                updateRequest.put("operation", "UPDATE_ACCOUNT_CANDIDATE");
                updateRequest.put("token", token);
                updateRequest.put("data", updateData);

                String updateJsonRequest = updateRequest.toJson();
                String updateResponse = client.sendRequestToServer(updateJsonRequest);
                JsonObject updateJson = Jsoner.deserialize(updateResponse, new JsonObject());
                String status = (String) updateJson.get("status");
                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateUpdate, "Atualização realizada com sucesso!");
                    new CandidateMenuView(client, token);
                    dispose();
                } else if ("INVALID_TOKEN".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateUpdate, "Token inválido. Por favor, tente novamente.");
                } else if ("INVALID_EMAIL".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateUpdate, "Email inválido. Por favor, tente novamente com um email diferente.");
                } else {
                    JOptionPane.showMessageDialog(panelCandidateUpdate, "A atualização falhou. Por favor, tente novamente.");
                }

            }

        });
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateMenuView(client,token);
                dispose();
            }
        });
    }
}
