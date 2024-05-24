package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterUpdateView extends JFrame{
    private Client client;
    private String token;
    private JButton btnUpdate;
    private JButton btnReturn;
    private JTextField txtfUser;
    private JTextField txtfEmail;
    private JPasswordField txtfIndustry;
    private JPasswordField txtfDescription;
    private JPasswordField txtfPassword;
    private JPanel panelRecruiterUpdate;

    public RecruiterUpdateView(Client client, String token) {
        setContentPane(panelRecruiterUpdate);
        setTitle("Atualizar Dados");
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
                String industry = txtfIndustry.getText();
                String description = txtfDescription.getText();

                RequestMessage requestMessage = new RequestMessage("UPDATE_ACCOUNT_RECRUITER", token, email, password, name, industry, description);

                String updateJsonRequest = requestMessage.toJsonStringExtendToken();
                String updateResponse = client.sendRequestToServer(updateJsonRequest);
                JsonObject updateJson = Jsoner.deserialize(updateResponse, new JsonObject());
                String status = (String) updateJson.get("status");

                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelRecruiterUpdate, "Atualização realizada com sucesso!");
                    new RecruiterMenuView(client, token);
                    dispose();
                } else if ("INVALID_TOKEN".equals(status)) {
                    JOptionPane.showMessageDialog(panelRecruiterUpdate, "Token inválido. Por favor, tente novamente.");
                } else if ("INVALID_EMAIL".equals(status)) {
                    JOptionPane.showMessageDialog(panelRecruiterUpdate, "Email inválido. Por favor, tente novamente com um email diferente.");
                } else {
                    JOptionPane.showMessageDialog(panelRecruiterUpdate, "A atualização falhou. Por favor, tente novamente.");
                }
            }
        });
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterMenuView(client,token);
                dispose();
            }
        });
    }
}
