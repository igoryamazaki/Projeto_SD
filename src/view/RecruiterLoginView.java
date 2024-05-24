package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterLoginView extends JFrame{
    private Client client;
    private JTextField txtfEmail;
    private JPasswordField txtfPassword;
    private JButton btnLogin;
    private JButton btnSignUp;
    private JPanel panelCandidateLogin;
    private String token;

    public RecruiterLoginView(Client client) {
        this.client = client;
        setContentPane(panelCandidateLogin);
        setTitle("Login Recrutador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(290,180);
        setLocationRelativeTo(null);
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Menu");

        JMenuItem menuItem = new JMenuItem("Voltar");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RoleSelecitonView(client);
                dispose();
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setVisible(true);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtfEmail.getText();
                String password = new String(txtfPassword.getPassword());

                RequestMessage loginRequest = new RequestMessage("LOGIN_RECRUITER", email, password);
                String loginJsonRequest = loginRequest.toJsonString();

                String loginResponse = client.sendRequestToServer(loginJsonRequest);
                JsonObject loginJson = Jsoner.deserialize(loginResponse, new JsonObject());
                String status = (String) loginJson.get("status");
                JsonObject data = (JsonObject) loginJson.get("data");
                if (data != null) {
                    token = (String) data.get("token");
                }
                if ("SUCCESS".equals(status)) {
                    // Se o login for bem-sucedido, vá para a próxima tela
                    new RecruiterMenuView(client,token);
                    dispose();
                } else if ("INVALID_LOGIN".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateLogin, "Login inválido. Por favor, tente novamente.");
                }  else {
                    JOptionPane.showMessageDialog(panelCandidateLogin, "Ocorreu um erro. Por favor, tente novamente.");
                }
            }
        });
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterSignUpView(client);
                dispose();
            }
        });
    }

}
