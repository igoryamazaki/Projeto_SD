package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterMenuView extends JFrame{
    private Client client;
    private String token;
    private JButton btnLookUp;
    private JButton btnJob;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnLogout;
    private JPanel panelRecruiterMenu;

    public RecruiterMenuView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelRecruiterMenu);
        setTitle("Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350,170);
        setLocationRelativeTo(null);
        setVisible(true);
        btnLookUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterLookUpView(client,token);dispose();
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterUpdateView(client,token);dispose();
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(null, "Você realmente deseja excluir sua conta?", "Aviso", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    RequestMessage deleteRequest = new RequestMessage("DELETE_ACCOUNT_RECRUITER", token);

                    String deleteJsonRequest = deleteRequest.toJsonStringWithToken();

                    String deleteResponse = client.sendRequestToServer(deleteJsonRequest);
                    JsonObject deleteJson = Jsoner.deserialize(deleteResponse, new JsonObject());
                    String status = (String) deleteJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Conta excluída com sucesso!");
                        new RoleSelecitonView(client);
                        dispose();
                    } else if ("INVALID_TOKEN".equals(status)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Token inválido. Por favor, tente novamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "A exclusão falhou. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    RequestMessage logoutRequest = new RequestMessage("LOGOUT_RECRUITER", token);

                    String logoutJsonRequest = logoutRequest.toJsonStringWithToken();
                    String logoutResponse = client.sendRequestToServer(logoutJsonRequest);
                    JsonObject logoutJson = Jsoner.deserialize(logoutResponse, new JsonObject());
                    String status = (String) logoutJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Logout realizado com sucesso!");
                        new CandidateLoginView(client); // Volte para a tela de login
                        dispose();
                    } else if ("INVALID_TOKEN".equals(status)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Token inválido. Por favor, tente novamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "O logout falhou. Por favor, tente novamente.");
                    }
            }
        });
    }
}
