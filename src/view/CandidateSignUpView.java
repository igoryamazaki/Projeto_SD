package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateSignUpView extends JFrame{
    private JButton btnSignUp;
    private JButton btnReturn;
    private JTextField txtfUser;
    private JTextField txtfEmail;
    private JPasswordField txtfPassword;
    private JPanel panelCandidateSignUP;
    private Client client;

    public CandidateSignUpView(Client client) {
        this.client = client;
        setContentPane(panelCandidateSignUP);
        setTitle("Cadastro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300,200);
        setLocationRelativeTo(null);
        setVisible(true);
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateLoginView(client);
                dispose();
            }
        });
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtfEmail.getText();
                String password = new String(txtfPassword.getPassword());
                String name = txtfUser.getText();

                RequestMessage signupRequest = new RequestMessage("SIGNUP_CANDIDATE", email, password, name);
                String signupJsonRequest = signupRequest.toJsonString();

                String signupResponse = client.sendRequestToServer(signupJsonRequest);
                JsonObject signupJson = Jsoner.deserialize(signupResponse, new JsonObject());
                String status = (String) signupJson.get("status");
                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateSignUP, "Cadastro realizado com sucesso!");
                    new CandidateLoginView(client);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(panelCandidateSignUP, "O cadastro falhou. Por favor, tente novamente.");
                }
            }
        });
    }
}
