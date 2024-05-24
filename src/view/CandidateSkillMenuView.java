package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CandidateSkillMenuView extends JFrame{
    private Client client;
    private String token;
    private JsonArray skillset;
    private JButton btnIncludeSkill;
    private JButton btnUpdateSkill;
    private JButton btnDeleteSkill;
    private JButton btnReturn;
    private JList listSkill;
    private JPanel panelSkillMenu;

    public CandidateSkillMenuView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelSkillMenu);
        setTitle("Habilidades");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,200);
        setLocationRelativeTo(null);

        setVisible(true);


        // Crie o objeto JSON para a operação LOOKUP_SKILLSET
        JsonObject lookupSkillsetRequest = new JsonObject();
        lookupSkillsetRequest.put("operation", "LOOKUP_SKILLSET");
        lookupSkillsetRequest.put("token", token); // Alteração aqui

        JsonObject data = new JsonObject();
        lookupSkillsetRequest.put("data", data);
        // Envie a solicitação para o servidor e obtenha a resposta
        String lookupSkillsetJsonRequest = lookupSkillsetRequest.toJson();
        String lookupSkillsetResponse = client.sendRequestToServer(lookupSkillsetJsonRequest);
        JsonObject lookupSkillsetJson = Jsoner.deserialize(lookupSkillsetResponse, new JsonObject());

        // Verifique o status da resposta
        String status = (String) lookupSkillsetJson.get("status");

        if ("SUCCESS".equals(status)) {
            // Se a operação foi bem-sucedida, atualize a lista de habilidades
            JsonObject responseData = (JsonObject) lookupSkillsetJson.get("data");
            //JsonArray skillset = (JsonArray) responseData.get("skillset");
            skillset = (JsonArray) responseData.get("skillset");
            // Converta o JsonArray para um array de Strings para usar na JList
            String[] skillsetArray = new String[skillset.size()];
            for (int i = 0; i < skillset.size(); i++) {
                JsonObject skillData = (JsonObject) skillset.get(i);
                String skill = (String) skillData.get("skill");
                skillsetArray[i] = (i + 1) + "- " + skill;
            }

            // Atualize a JList com o novo conjunto de habilidades
            listSkill.setListData(skillsetArray);
        } else if ("INVALID_TOKEN".equals(status)) {
            // Se o token for inválido, mostre uma mensagem de erro
            JOptionPane.showMessageDialog(panelSkillMenu, "Token inválido. Por favor, tente novamente.");
        } else {
            // Se houver algum outro erro, mostre uma mensagem de erro
            JOptionPane.showMessageDialog(panelSkillMenu, "A busca do conjunto de habilidades falhou. Por favor, tente novamente.");
        }
        btnIncludeSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateNewSkillView(client,token);
                dispose();
            }
        });
        btnUpdateSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listSkill.getSelectedIndex();

                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelSkillMenu, "Por favor, selecione uma habilidade.");
                } else {
                    // Obtenha a habilidade associada ao item selecionado
                    JsonObject skillData = (JsonObject) skillset.get(index);
                    String skillUpdate = (String) skillData.get("skill");

                    // Passe a habilidade selecionada para a tela CandidateUpdateSkillView
                    new CandidateUpdateSkillView(client, token, skillUpdate); // Alteração aqui
                    dispose();
                }
            }
        });
        btnDeleteSkill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listSkill.getSelectedIndex();

                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelSkillMenu, "Por favor, selecione uma habilidade.");
                } else {
                    // Obtenha a habilidade associada ao item selecionado
                    JsonObject skillData = (JsonObject) skillset.get(index);
                    String skill = (String) skillData.get("skill"); // Alteração aqui

                    // Crie o objeto JSON para a operação DELETE_SKILL
                    JsonObject deleteSkillRequest = new JsonObject();
                    deleteSkillRequest.put("operation", "DELETE_SKILL");
                    deleteSkillRequest.put("token", token); // Alteração aqui
                    JsonObject data = new JsonObject();
                    data.put("skill", skill); // Alteração aqui
                    deleteSkillRequest.put("data", data);

                    // Envie a solicitação para o servidor e obtenha a resposta
                    String deleteSkillJsonRequest = deleteSkillRequest.toJson();
                    String deleteSkillResponse = client.sendRequestToServer(deleteSkillJsonRequest);
                    JsonObject deleteSkillJson = Jsoner.deserialize(deleteSkillResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) deleteSkillJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Se a operação foi bem-sucedida, remova a habilidade da lista e atualize a JList
                        skillset.remove(index);
                        String[] skillsetArray = new String[skillset.size()];
                        for (int i = 0; i < skillset.size(); i++) {
                            JsonObject skillDataDelete = (JsonObject) skillset.get(i);
                            String skillName = (String) skillDataDelete.get("skill");
                            skillsetArray[i] = (i + 1) + "- " + skillName;
                        }
                        listSkill.setListData(skillsetArray);
                        JOptionPane.showMessageDialog(panelSkillMenu, "Habilidade excluída com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelSkillMenu, "Token inválido. Por favor, tente novamente.");
                    } else if ("SKILL_NOT_FOUND".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelSkillMenu, "A habilidade não existe. Por favor, tente novamente.");
                    }else {
                        // Se houver algum outro erro, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelSkillMenu, "A exclusão da habilidade falhou. Por favor, tente novamente.");
                    }
                }
            }
        });

        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateMenuView(client,token);
                dispose();
            }
        });
        listSkill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Duplo clique
                    int index = listSkill.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        // Obtenha a habilidade associada ao item clicado
                        JsonObject skillData = (JsonObject) skillset.get(index);
                        String skill = (String) skillData.get("skill"); // Alteração aqui

                        // Crie o objeto JSON para a operação LOOKUP_SKILL
                        JsonObject lookupSkillRequest = new JsonObject();
                        lookupSkillRequest.put("operation", "LOOKUP_SKILL");
                        lookupSkillRequest.put("token", token); // Alteração aqui
                        JsonObject data = new JsonObject();
                        data.put("skill", skill); // Alteração aqui
                        lookupSkillRequest.put("data", data);

                        // Envie a solicitação para o servidor e obtenha a resposta
                        String lookupSkillJsonRequest = lookupSkillRequest.toJson();
                        String lookupSkillResponse = client.sendRequestToServer(lookupSkillJsonRequest);
                        JsonObject lookupSkillJson = Jsoner.deserialize(lookupSkillResponse, new JsonObject());

                        // Verifique o status da resposta
                        String status = (String) lookupSkillJson.get("status");
                        if ("SUCCESS".equals(status)) {
                            // Se a operação foi bem-sucedida, mostre os detalhes da habilidade em um pop-up
                            JsonObject responseData = (JsonObject) lookupSkillJson.get("data");
                            String experience = (String) responseData.get("experience");

                            JOptionPane.showMessageDialog(panelSkillMenu, "Habilidade: " + skill + "\nExperiência: " + experience);
                        } else if ("INVALID_TOKEN".equals(status)) {
                            // Se o token for inválido, mostre uma mensagem de erro
                            JOptionPane.showMessageDialog(panelSkillMenu, "Token inválido. Por favor, tente novamente.");
                        } else {
                            // Se houver algum outro erro, mostre uma mensagem de erro
                            JOptionPane.showMessageDialog(panelSkillMenu, "A busca da habilidade falhou. Por favor, tente novamente.");
                        }
                    }
                }
            }
        });
    }
}
