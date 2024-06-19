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

public class RecruiterMenuView extends JFrame{
    private Client client;
    private String token;
    private JButton btnLookUp;
    private JButton btnJob;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnLogout;
    private JPanel panelRecruiterMenu;
    private JCheckBox checkBoxSkill;
    private JTextArea textAreaSearch;
    private JComboBox comboBoxSkill;
    private JCheckBox checkBoxExperience;
    private JCheckBox checkBoxE;
    private JCheckBox checkBoxOU;
    private JTextField txtfExperience;
    private JButton btnSearch;
    private JButton btnEraseSearch;
    private JTable tableCandidate;
    private JButton btnChoose;

    public RecruiterMenuView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelRecruiterMenu);
        setTitle("Menu Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350,400);
        setLocationRelativeTo(null);

        for (String skill : Skills.getSkills()) {
            comboBoxSkill.addItem(skill);
        }
        DefaultListModel<String> skillListModel = new DefaultListModel<>();
        // Crie um modelo de tabela com os títulos das colunas desejados
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Nome","Habilidades", "Experiência", "ID"}, 0);

        tableCandidate.setModel(tableModel);
        tableModel.addRow(new Object[]{"Nome","Habilidades", "Experiência", "ID"});
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
                new RecruiterJobMenuView(client,token);dispose();
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
                        new RecruiterLoginView(client); // Volte para a tela de login
                        dispose();
                    } else if ("INVALID_TOKEN".equals(status)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Token inválido. Por favor, tente novamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "O logout falhou. Por favor, tente novamente.");
                    }
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
                searchJson.put("operation", "SEARCH_CANDIDATE");
                searchJson.put("token", token);

                // Envie a solicitação para o servidor e obtenha a resposta
                String searchJsonRequest = searchJson.toJson();
                String searchResponse = client.sendRequestToServer(searchJsonRequest);
                JsonObject searchJsonResponse = Jsoner.deserialize(searchResponse, new JsonObject());

                // Verifique o status da resposta
                String status = (String) searchJsonResponse.get("status");
                if ("SUCCESS".equals(status)) {
                    // Se a operação foi bem-sucedida, mostre uma mensagem de sucesso
                    JOptionPane.showMessageDialog(panelRecruiterMenu, "Busca realizada com sucesso!");
                    // Limpe a tabela de vagas
                    DefaultTableModel tableModel = (DefaultTableModel) tableCandidate.getModel();
                    tableModel.setRowCount(0);
                    // Adicione as vagas retornadas à tabela de vagas
                    JsonObject data = (JsonObject) searchJsonResponse.get("data");
                    JsonArray profileSet = (JsonArray) data.get("profile");
                    tableModel.addRow(new Object[]{"Nome","Habilidades", "Experiência", "ID"});
                    for (Object profileObj : profileSet) {
                        JsonObject profile = (JsonObject) profileObj;
                        tableModel.addRow(new Object[]{profile.get("name").toString(),profile.get("skill").toString(), profile.get("experience").toString(), profile.get("id_user").toString()});
                    }
                    tableModel.fireTableDataChanged();
                } else if ("INVALID_TOKEN".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelRecruiterMenu, "Token inválido. Por favor, tente novamente.");
                } else {
                    // Se houver algum outro erro, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelRecruiterMenu, "A busca falhou. Por favor, tente novamente.");
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
                new RecruiterMenuView(client,token);
                dispose();
            }
        });
        btnChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtenha o índice da linha selecionada na tabela
                int selectedRow = tableCandidate.getSelectedRow();
                if (selectedRow != -1) {
                    String userId = tableCandidate.getValueAt(selectedRow, 3).toString();

                    JsonObject chooseJson = new JsonObject();
                    JsonObject dataJson = new JsonObject();
                    dataJson.put("id_user", userId);
                    chooseJson.put("data", dataJson);
                    chooseJson.put("operation", "CHOOSE_CANDIDATE");
                    chooseJson.put("token", token);

                    // Envie o JSON para o servidor (você precisará implementar essa parte)
                    String chooseJsonRequest = chooseJson.toJson();
                    String chooseResponse = client.sendRequestToServer(chooseJsonRequest);
                    JsonObject chooseJsonResponse = Jsoner.deserialize(chooseResponse, new JsonObject());

                    // Lide com a resposta do servidor (por exemplo, exiba uma mensagem de sucesso ou erro)
                    String chooseStatus = (String) chooseJsonResponse.get("status");
                    if ("SUCCESS".equals(chooseStatus)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Candidato escolhido com sucesso!");
                    } else if ("CANDIDATE_NOT_FOUND".equals(chooseStatus)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Candidato não encontrado. Por favor, tente novamente.");
                    } else if ("INVALID_TOKEN".equals(chooseStatus)) {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Token inválido. Por favor, faça login novamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelRecruiterMenu, "Erro ao escolher o candidato. Por favor, tente novamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(panelRecruiterMenu, "Selecione um candidato na tabela antes de escolher.");
                }
            }
        });
    }
}
