package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;
import utils.Skills;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterNewJobView extends JFrame{
    private Client client;
    private String token;
    private JPanel panelNewJob;
    private JComboBox comboBoxSkill;
    private JTextField txtfExperience;
    private JButton btnReturn;
    private JButton btnNewJob;

    public RecruiterNewJobView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelNewJob);
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
                new RecruiterJobMenuView(client,token);
                dispose();
            }
        });
        btnNewJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String skill = (String) comboBoxSkill.getSelectedItem();
                String experience = txtfExperience.getText();

                // Use o novo construtor e passe true para o parâmetro isSkill
                RequestMessage requestMessage = new RequestMessage("INCLUDE_JOB", token, skill, experience, true);

                // Use o novo método toJsonStringTokenSkill para gerar o JSON
                String includeSkillJsonRequest = requestMessage.toJsonStringTokenSkill();
                String includeSkillResponse = client.sendRequestToServer(includeSkillJsonRequest);
                JsonObject includeSkillJson = Jsoner.deserialize(includeSkillResponse, new JsonObject());
                String status = (String) includeSkillJson.get("status");

                if ("SUCCESS".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewJob, "Vaga adicionada com sucesso!");
                    new RecruiterNewJobView(client,token);
                    dispose();
                } else if ("INVALID_TOKEN".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewJob, "Token inválido. Por favor, tente novamente.");
                } else if ("SKILL_EXISTS".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewJob, "A habilidade já existe. Por favor, escolha outra habilidade.");
                } else if ("SKILL_NOT_EXIST".equals(status)) {
                    JOptionPane.showMessageDialog(panelNewJob, "A habilidade não existe entre as habilidades pré-estabelecidas. Por favor, escolha uma habilidade válida.");
                } else {
                    JOptionPane.showMessageDialog(panelNewJob, "A adição da habilidade falhou. Por favor, tente novamente.");
                }
            }
        });
    }
}
