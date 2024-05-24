package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;
import utils.Skills;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateNewSkillView extends JFrame{
    private Client client;
    private String token;
    private JTextField txtfSkill;
    private JTextField txtfExperience;
    private JButton btnReturn;
    private JButton btnNewSkill;
    private JPanel panelNewSkill;
    private JComboBox<String> comboBoxSkill;

    public CandidateNewSkillView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelNewSkill);
        setTitle("Adicionar Habilidade");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,160);
        setLocationRelativeTo(null);
        for (String skill : Skills.getSkills()) {
            comboBoxSkill.addItem(skill);
        }

        setVisible(true);
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateSkillMenuView(client,token);
                dispose();
            }
        });
        btnNewSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String skill = (String) comboBoxSkill.getSelectedItem();
                String experience = txtfExperience.getText();

                // Use o novo construtor e passe true para o parâmetro isSkill
                RequestMessage requestMessage = new RequestMessage("INCLUDE_SKILL", token, skill, experience, true);

                // Use o novo método toJsonStringTokenSkill para gerar o JSON
                String includeSkillJsonRequest = requestMessage.toJsonStringTokenSkill();
                String includeSkillResponse = client.sendRequestToServer(includeSkillJsonRequest);
                JsonObject includeSkillJson = Jsoner.deserialize(includeSkillResponse, new JsonObject());
                String status = (String) includeSkillJson.get("status");

                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewSkill, "Habilidade adicionada com sucesso!");
                    new CandidateNewSkillView(client,token);
                    dispose();
                } else if ("INVALID_TOKEN".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewSkill, "Token inválido. Por favor, tente novamente.");
                } else if ("SKILL_EXISTS".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewSkill, "A habilidade já existe. Por favor, escolha outra habilidade.");
                } else if ("SKILL_NOT_EXIST".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewSkill, "A habilidade não existe entre as habilidades pré-estabelecidas. Por favor, escolha uma habilidade válida.");
                } else {
                    JOptionPane.showMessageDialog(panelNewSkill, "A adição da habilidade falhou. Por favor, tente novamente.");
                }
            }
        });
    }
}
