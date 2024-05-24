package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.Skills;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterUpdateJobView extends JFrame{
    private Client client;
    private String token;
    private String skillUpdate;
    private JPanel panelUpdateJob;
    private JButton btnReturn;
    private JButton btnUpdateJob;
    private JComboBox<String> comboBoxSkill;
    private JTextField txtfExperience;

    public RecruiterUpdateJobView(Client client, String token, String jobId) {
        this.client = client;
        this.token = token;
        this.skillUpdate = skillUpdate;
        setContentPane(panelUpdateJob);
        setTitle("Adicionar Habilidade");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,160);
        setLocationRelativeTo(null);
        for (String skill : Skills.getSkills()) {
            comboBoxSkill.addItem(skill);
        }
        setVisible(true);
        btnUpdateJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newSkill = (String) comboBoxSkill.getSelectedItem();
                String experience = txtfExperience.getText();

                // Crie o objeto JSON para a operação UPDATE_JOB
                JsonObject updateJobRequest = new JsonObject();
                updateJobRequest.put("operation", "UPDATE_JOB");
                updateJobRequest.put("token", token);
                JsonObject data = new JsonObject();
                data.put("id", jobId);
                data.put("skill", newSkill);
                data.put("experience", experience);
                updateJobRequest.put("data", data);

                // Envie a solicitação para o servidor e obtenha a resposta
                String updateJobJsonRequest = updateJobRequest.toJson();
                String updateJobResponse = client.sendRequestToServer(updateJobJsonRequest);
                JsonObject updateJobJson = Jsoner.deserialize(updateJobResponse, new JsonObject());

                // Verifique o status da resposta
                String status = (String) updateJobJson.get("status");
                if ("SUCCESS".equals(status)) {
                    // Se a operação foi bem-sucedida, mostre uma mensagem de sucesso
                    JOptionPane.showMessageDialog(panelUpdateJob, "Vaga atualizada com sucesso!");
                } else if ("INVALID_TOKEN".equals(status)) {
                    // Se o token for inválido, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateJob, "Token inválido. Por favor, tente novamente.");
                } else if ("JOB_NOT_FOUND".equals(status)) {
                    JOptionPane.showMessageDialog(panelUpdateJob, "Vaga não encontrada. Por favor, tente novamente.");
                }else {
                    // Se houver algum outro erro, mostre uma mensagem de erro
                    JOptionPane.showMessageDialog(panelUpdateJob, "A atualização da vaga falhou. Por favor, tente novamente.");
                }
            }
        });
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterJobMenuView(client,token);
                dispose();
            }
        });
    }
}
