package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterSignUpView extends JFrame{
    private JButton btnSignUp;
    private JButton btnReturn;
    private JTextField txtfUser;
    private JTextField txtfEmail;
    private JPasswordField txtfPassword;
    private JTextField txtfIndustry;
    private JTextField txtfDescription;
    private JPanel panelRecruiterSignUP;
    private Client client;

    public RecruiterSignUpView(Client client) {
        this.client = client;
        setContentPane(panelRecruiterSignUP);
        setTitle("Cadastro recrutador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300,220);
        setLocationRelativeTo(null);
        setVisible(true);
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterLoginView(client);
                dispose();
            }
        });
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtfEmail.getText();
                String password = new String(txtfPassword.getPassword());
                String name = txtfUser.getText();
                String industry = txtfIndustry.getText();
                String description = txtfDescription.getText();

                RequestMessage signupRequest = new RequestMessage("SIGNUP_RECRUITER", email, password, name, industry, description);
                String signupJsonRequest = signupRequest.toJsonStringExtend();

                String signupResponse = client.sendRequestToServer(signupJsonRequest);
                JsonObject signupJson = Jsoner.deserialize(signupResponse, new JsonObject());
                String status = (String) signupJson.get("status");
                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelRecruiterSignUP, "Cadastro realizado com sucesso!");
                    new RecruiterLoginView(client);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(panelRecruiterSignUP, "O cadastro falhou. Por favor, tente novamente.");
                }
            }
        });
    }
}
