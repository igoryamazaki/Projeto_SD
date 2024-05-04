package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateMenuView extends JFrame {
    private Client client;
    private String token;
    private JTable table1;
    private JButton btnLookUp;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnSkill;
    private JPanel panelCandidateMenu;
    private JButton btnLogout;

    public CandidateMenuView(Client client,String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelCandidateMenu);
        setTitle("Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350,250);
        setLocationRelativeTo(null);
        setVisible(true);

        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        model.addColumn("Cargo");
        model.addColumn("Descrição");
        model.addRow(new Object[]{"Desenvolvedor Full Stack", "JavaScript, React, Node.js "});
        model.addRow(new Object[]{"Analista de Dados", "SQL, Python, R, Excel"});
        btnLookUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateLookUpView(client,token);dispose();
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateUpdateView(client,token);dispose();
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(null, "Você realmente deseja excluir sua conta?", "Aviso", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    JsonObject deleteRequest = new JsonObject();
                    deleteRequest.put("operation", "DELETE_ACCOUNT_CANDIDATE");
                    deleteRequest.put("token", token);

                    String deleteJsonRequest = deleteRequest.toJson();
                    String deleteResponse = client.sendRequestToServer(deleteJsonRequest);
                    JsonObject deleteJson = Jsoner.deserialize(deleteResponse, new JsonObject());
                    String status = (String) deleteJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        JOptionPane.showMessageDialog(panelCandidateMenu, "Conta excluída com sucesso!");
                        new RoleSelecitonView(client);
                        dispose();
                    } else if ("INVALID_TOKEN".equals(status)) {
                        JOptionPane.showMessageDialog(panelCandidateMenu, "Token inválido. Por favor, tente novamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelCandidateMenu, "A exclusão falhou. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JsonObject logoutRequest = new JsonObject();
                logoutRequest.put("operation", "LOGOUT_CANDIDATE");
                logoutRequest.put("token", token);

                String logoutJsonRequest = logoutRequest.toJson();
                String logoutResponse = client.sendRequestToServer(logoutJsonRequest);
                JsonObject logoutJson = Jsoner.deserialize(logoutResponse, new JsonObject());
                String status = (String) logoutJson.get("status");
                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateMenu, "Logout realizado com sucesso!");
                    new CandidateLoginView(client); // Volte para a tela de login
                    dispose();
                } else if ("INVALID_TOKEN".equals(status)) {
                    JOptionPane.showMessageDialog(panelCandidateMenu, "Token inválido. Por favor, tente novamente.");
                } else {
                    JOptionPane.showMessageDialog(panelCandidateMenu, "O logout falhou. Por favor, tente novamente.");
                }
            }
        });
    }

}
