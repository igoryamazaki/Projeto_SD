package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.Skills;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateUpdateSkillView extends JFrame{
    private Client client;
    private String token;
    private String skillUpdate;
    private JTextField txtfExperience;
    private JButton btnUpdateSkill;
    private JPanel panelUpdateSkillCandidate;
    private JComboBox<String> comboBoxSkill;
    private JButton btnReturn;

    public CandidateUpdateSkillView(Client client, String token, String skillUpdate) {
        this.client = client;
        this.token = token;
        this.skillUpdate = skillUpdate;
        setContentPane(panelUpdateSkillCandidate);
        setTitle("Adicionar Habilidade");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,160);
        setLocationRelativeTo(null);
        for (String skill : Skills.getSkills()) {
            comboBoxSkill.addItem(skill);
        }
        setVisible(true);
        btnUpdateSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newSkill = (String) comboBoxSkill.getSelectedItem();
                String experience = txtfExperience.getText();

                // Crie o objeto JSON para a operação UPDATE_SKILL
                JsonObject updateSkillRequest = new JsonObject();
                updateSkillRequest.put("operation", "UPDATE_SKILL");
                updateSkillRequest.put("token", token);
                JsonObject data = new JsonObject();
                data.put("skill", skillUpdate);
                data.put("experience", experience);
                data.put("newSkill", newSkill);
                updateSkillRequest.put("data", data);

                // Envie a solicitação para o servidor e obtenha a resposta
                String updateSkillJsonRequest = updateSkillRequest.toJson();
                String updateSkillResponse = client.sendRequestToServer(updateSkillJsonRequest);
                JsonObject updateSkillJson = Jsoner.deserialize(updateSkillResponse, new JsonObject());

                // Verifique o status da resposta
                String status = (String) updateSkillJson.get("status");
                if ("SUCCESS".equals(status)) {
                    // Se a operação foi bem-sucedida, mostre uma mensagem de sucesso
                    JOptionPane.showMessageDialog(panelUpdateSkillCandidate, "Habilidade atualizada com sucesso!");
                } else if ("INVALID_TOKEN".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateSkillCandidate, "Token inválido. Por favor, tente novamente.");
                } else if ("SKILL_NOT_FOUND".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateSkillCandidate, "Habilidade não encontrada. Por favor, tente novamente.");
                }else if ("SKILL_EXISTS".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateSkillCandidate, "A habilidade já existe. Por favor, tente novamente.");
                }else {
                    // Se houver algum outro erro, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateSkillCandidate, "A atualização da habilidade falhou. Por favor, tente novamente.");
                }
            }
        });
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateSkillMenuView(client,token);
                dispose();
            }
        });
    }
}
