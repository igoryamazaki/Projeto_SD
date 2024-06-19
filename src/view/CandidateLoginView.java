package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateLoginView extends JFrame{
    private Client client;
    private JTextField txtfEmail;
    private JButton entrarButton;
    private JPasswordField txtfPassword;
    private JButton btnSignUp;
    private JPanel panelCandidateLogin;
    private String token;

    public CandidateLoginView(Client client) {
        this.client = client;
        setContentPane(panelCandidateLogin);
        setTitle("Entrar como");
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
        entrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtfEmail.getText();
                String password = new String(txtfPassword.getPassword());

                RequestMessage loginRequest = new RequestMessage("LOGIN_CANDIDATE", email, password);
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
                    new CandidateMenuView(client,token);
                    JsonObject request = new JsonObject();
                    request.put("operation", "GET_COMPANY");
                    request.put("token", token);
                    request.put("data", new JsonObject());
                    String jsonRequest = request.toJson();
                    String response = client.sendRequestToServer(jsonRequest);
                    JsonObject deleteJobJson = Jsoner.deserialize(response, new JsonObject());

                    dispose();
                } else if ("INVALID_LOGIN".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateLogin, "Login inválido. Por favor, tente novamente.");
                } else if ("USER_NOT_FOUND".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateLogin, "Usuário não encontrado. Por favor, verifique suas credenciais e tente novamente.");
                } else {
                    JOptionPane.showMessageDialog(panelCandidateLogin, "Ocorreu um erro. Por favor, tente novamente.");
                }
            }
        });
        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateSignUpView(client);
                dispose();
            }
        });
    }
}
