package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;
import utils.Skills;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateMenuView extends JFrame {
    private Client client;
    private String token;
    private JTable tableJob;
    private JButton btnLookUp;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnSkill;
    private JPanel panelCandidateMenu;
    private JButton btnLogout;
    private JCheckBox checkBoxSkill;
    private JCheckBox checkBoxExperience;
    private JComboBox<String> comboBoxSkill;
    private JTextField txtfExperience;
    private JCheckBox checkBoxE;
    private JCheckBox checkBoxOU;
    private JButton btnSearch;
    private JTextArea textAreaSearch;
    private JButton btnEraseSearch;
    private JsonArray skillset;

    public CandidateMenuView(Client client,String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelCandidateMenu);
        setTitle("Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350,400);
        setLocationRelativeTo(null);

        for (String skill : Skills.getSkills()) {
            comboBoxSkill.addItem(skill);
        }
        DefaultListModel<String> skillListModel = new DefaultListModel<>();
        // Crie um modelo de tabela com os títulos das colunas desejados
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Habilidades", "Experiência", "ID", "Disponível"}, 0);

        tableJob.setModel(tableModel);
        tableModel.addRow(new Object[]{"Habilidades", "Experiencia", "ID", "Disponível"});
        setVisible(true);
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
                    RequestMessage deleteRequest = new RequestMessage("DELETE_ACCOUNT_CANDIDATE", token);

                    String deleteJsonRequest = deleteRequest.toJsonStringWithToken();

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
                RequestMessage logoutRequest = new RequestMessage("LOGOUT_CANDIDATE", token);

                String logoutJsonRequest = logoutRequest.toJsonStringWithToken();
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
        btnSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateSkillMenuView(client,token);
                dispose();
            }
        });

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Crie um objeto JsonObject para armazenar os dados da pesquisa
                JsonObject searchJson = new JsonObject();
                JsonObject dataJson = new JsonObject();

                // Adicione as habilidades selecionadas ao objeto Json se a checkBoxSkill estiver selecionada
                if (checkBoxSkill.isSelected()) {
                    JsonArray skillArray = new JsonArray();
                    for (int i = 0; i < skillListModel.size(); i++) {
                        skillArray.add(skillListModel.get(i));
                    }
                    dataJson.put("skill", skillArray);
                }

                // Adicione a experiência ao objeto Json se a checkBoxExperience estiver selecionada
                if (checkBoxExperience.isSelected()) {
                    String experience = txtfExperience.getText();
                    dataJson.put("experience", experience);
                }

                // Adicione o filtro ao objeto Json apenas se ambas as checkBoxes de habilidade e experiência estiverem selecionadas
                if (checkBoxSkill.isSelected() && checkBoxExperience.isSelected()) {
                    if (checkBoxE.isSelected()) {
                        dataJson.put("filter", "AND");
                       // checkBoxOU.setSelected(false);
                    } else if (checkBoxOU.isSelected()) {
                        dataJson.put("filter", "OR");
                        //checkBoxE.setSelected(false);
                    }
                }

                // Adicione o objeto dataJson ao objeto searchJson
                searchJson.put("data", dataJson);
                searchJson.put("operation", "SEARCH_JOB");
                searchJson.put("token", token);

                // Envie a solicitação para o servidor e obtenha a resposta
                String searchJsonRequest = searchJson.toJson();
                String searchResponse = client.sendRequestToServer(searchJsonRequest);
                JsonObject searchJsonResponse = Jsoner.deserialize(searchResponse, new JsonObject());

                // Verifique o status da resposta
                String status = (String) searchJsonResponse.get("status");
                if ("SUCCESS".equals(status)) {
                    // Se a operação foi bem-sucedida, mostre uma mensagem de sucesso
                    JOptionPane.showMessageDialog(panelCandidateMenu, "Busca realizada com sucesso!");
                    // Limpe a tabela de vagas
                    DefaultTableModel tableModel = (DefaultTableModel) tableJob.getModel();
                    tableModel.setRowCount(0);
                    // Adicione as vagas retornadas à tabela de vagas
                    JsonObject data = (JsonObject) searchJsonResponse.get("data");
                    JsonArray jobset = (JsonArray) data.get("jobset");
                    tableModel.addRow(new Object[]{"Habilidades", "Experiência", "ID", "Disponível"});
                    for (Object jobObj : jobset) {
                        JsonObject job = (JsonObject) jobObj;
                        tableModel.addRow(new Object[]{job.get("skill").toString(), job.get("experience").toString(), job.get("id").toString(),job.get("available")});
                    }
                    tableModel.fireTableDataChanged();
                } else if ("INVALID_TOKEN".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelCandidateMenu, "Token inválido. Por favor, tente novamente.");
                } else {
                    // Se houver algum outro erro, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelCandidateMenu, "A busca falhou. Por favor, tente novamente.");
                }
            }
        });


        comboBoxSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSkill = (String) comboBoxSkill.getSelectedItem();
                if (!skillListModel.contains(selectedSkill)) {
                    skillListModel.addElement(selectedSkill);
                    if (textAreaSearch.getText().isEmpty()) {
                        textAreaSearch.setText(selectedSkill);
                    } else {
                        textAreaSearch.setText(textAreaSearch.getText() + ", " + selectedSkill);
                    }
                }
            }
        });
        checkBoxE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkBoxOU.setSelected(false);
            }
        });
        checkBoxOU.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkBoxE.setSelected(false);
            }
        });
        btnEraseSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateMenuView(client,token);
                dispose();
            }
        });
    }
}
