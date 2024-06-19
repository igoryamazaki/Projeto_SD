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
import java.util.Objects;

public class RecruiterJobMenuView extends JFrame{
    private Client client;
    private String token;
    private JsonArray jobset;
    private JButton btnIncludeJob;
    private JButton btnUpdateJob;
    private JButton btnDeleteJob;
    private JButton btnReturn;
    private JList listJobs;
    private JPanel panelMenuJob;
    private JButton btnNoAvailable;
    private JButton btnYesAvailable;
    private JButton btnYesSearchable;
    private JButton btnNoSearchable;

    public RecruiterJobMenuView(Client client, String token) {
        this.client = client;
        this.token = token;
        setContentPane(panelMenuJob);
        setTitle("Menu de Vagas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,310);
        setLocationRelativeTo(null);

        setVisible(true);
        // Crie o objeto JSON para a operação LOOKUP_JOBSET
        JsonObject lookupJobsetRequest = new JsonObject();
        lookupJobsetRequest.put("operation", "LOOKUP_JOBSET");
        lookupJobsetRequest.put("token", token); // Alteração aqui

        // Envie a solicitação para o servidor e obtenha a resposta
        String lookupJobsetJsonRequest = lookupJobsetRequest.toJson();
        String lookupJobsetResponse = client.sendRequestToServer(lookupJobsetJsonRequest);
        JsonObject lookupJobsetJson = Jsoner.deserialize(lookupJobsetResponse, new JsonObject());

        // Verifique o status da resposta
        String status = (String) lookupJobsetJson.get("status");
        if ("SUCCESS".equals(status)) {
            // Se a operação foi bem-sucedida, atualize a lista de habilidades
            JsonObject responseData = (JsonObject) lookupJobsetJson.get("data");
            //JsonArray skillset = (JsonArray) responseData.get("skillset");
            jobset = (JsonArray) responseData.get("jobset");
            // Converta o JsonArray para um array de Strings para usar na JList
            String[] skillsetArray = new String[jobset.size()];
            for (int i = 0; i < jobset.size(); i++) {
                JsonObject skillData = (JsonObject) jobset.get(i);
                String skill = (String) skillData.get("skill");
                skillsetArray[i] = (i + 1) + "- " + skill;
            }

            // Atualize a JList com o novo conjunto de habilidades
            listJobs.setListData(skillsetArray);
        } else if ("INVALID_TOKEN".equals(status)) {
            // Se o token for inválido, mostre uma mensagem de erro
            JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
        } else {
            // Se houver algum outro erro, mostre uma mensagem de erro
            JOptionPane.showMessageDialog(panelMenuJob, "A busca do conjunto de habilidades falhou. Por favor, tente novamente.");
        }
        btnIncludeJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterNewJobView(client,token);
                dispose();
            }
        });
        btnUpdateJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();

                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione uma habilidade.");
                } else {
                    // Obtenha a habilidade associada ao item selecionado
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String jobId = (String) jobData.get("id");

                    // Passe a habilidade selecionada para a tela CandidateUpdateSkillView
                    new RecruiterUpdateJobView(client, token, jobId); // Alteração aqui
                    dispose();
                }
            }
        });
        btnDeleteJob.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();

                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione um trabalho.");
                } else {
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String idJob = (String) jobData.get("id");

                    // Crie o objeto JSON para a operação DELETE_JOB
                    JsonObject deleteJobRequest = new JsonObject();
                    deleteJobRequest.put("operation", "DELETE_JOB");
                    deleteJobRequest.put("token", token);
                    JsonObject data = new JsonObject();
                    data.put("id", idJob);
                    deleteJobRequest.put("data", data);
                    // Envie a solicitação para o servidor e obtenha a resposta
                    String deleteJobJsonRequest = deleteJobRequest.toJson();
                    String deleteJobResponse = client.sendRequestToServer(deleteJobJsonRequest);
                    JsonObject deleteJobJson = Jsoner.deserialize(deleteJobResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) deleteJobJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Se a operação foi bem-sucedida, remova o trabalho da lista e atualize a JList
                        jobset.remove(index);
                        String[] jobsetArray = new String[jobset.size()];
                        for (int i = 0; i < jobset.size(); i++) {
                            JsonObject jobDataDelete = (JsonObject) jobset.get(i);
                            String jobName = (String) jobDataDelete.get("skill");
                            jobsetArray[i] = (i + 1) + "- " + jobName;
                        }
                        listJobs.setListData(jobsetArray);
                        JOptionPane.showMessageDialog(panelMenuJob, "Trabalho excluído com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                    } else if ("JOB_NOT_FOUND".equals(status)) {
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                    }else {
                        // Se houver algum outro erro, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "A exclusão do trabalho falhou. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterMenuView(client,token);
                dispose();
            }
        });
        listJobs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Duplo clique
                    int index = listJobs.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        // Obtenha a habilidade associada ao item clicado
                        //JsonObject skillData = (JsonObject) jobset.get(index);
                        //String skill = (String) skillData.get("skill"); // Alteração aqui
                        JsonObject jobData = (JsonObject) jobset.get(index);
                        String jobId = (String) jobData.get("id");

                        // Crie o objeto JSON para a operação LOOKUP_SKILL
                        JsonObject lookupSkillRequest = new JsonObject();
                        lookupSkillRequest.put("operation", "LOOKUP_JOB");
                        lookupSkillRequest.put("token", token); // Alteração aqui
                        JsonObject data = new JsonObject();
                        data.put("id", jobId); // Alteração aqui
                        lookupSkillRequest.put("data", data);

                        // Envie a solicitação para o servidor e obtenha a resposta
                        String lookupSkillJsonRequest = lookupSkillRequest.toJson();
                        String lookupSkillResponse = client.sendRequestToServer(lookupSkillJsonRequest);
                        JsonObject lookupSkillJson = Jsoner.deserialize(lookupSkillResponse, new JsonObject());
                        // Verifique o status da resposta
                        String status = (String) lookupSkillJson.get("status");
                        if ("SUCCESS".equals(status)) {
                            JsonObject responseData = (JsonObject) lookupSkillJson.get("data");
                            String skill = (String) responseData.get("skill");
                            String experience = (String) responseData.get("experience");
                            String searchable = (String) responseData.get("searchable");
                            String available = (String) responseData.get("available");

                            String searchableText = (Objects.equals(searchable, "YES")) ? "SIM" : "NÃO";
                            String availableText = (Objects.equals(available, "YES")) ? "SIM" : "NÃO";

                            JOptionPane.showMessageDialog(panelMenuJob, "ID: " + jobId + "\nHabilidade: " + skill + "\nExperiência: " + experience + "\nDisponível: " + availableText + "\nDivulgável: " + searchableText ); } else if ("INVALID_TOKEN".equals(status)) {
                            // Se o token for inválido, mostre uma mensagem de erro
                            JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                        } else if ("JOB_NOT_FOUND".equals(status)) {
                            JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                        } else {
                            // Se houver algum outro erro, mostre uma mensagem de erro
                            JOptionPane.showMessageDialog(panelMenuJob, "A busca da habilidade falhou. Por favor, tente novamente.");
                        }
                    }
                }
            }
        });
        btnYesAvailable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();
                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione uma habilidade.");
                } else {
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String jobId = (String) jobData.get("id");

                    // Crie o objeto JSON para a operação SET_JOB_AVAILABLE com "YES"
                    JsonObject setJobAvailableRequest = new JsonObject();
                    setJobAvailableRequest.put("operation", "SET_JOB_AVAILABLE");
                    setJobAvailableRequest.put("token", token);
                    JsonObject data = new JsonObject();
                    data.put("id", jobId);
                    data.put("available", "YES");
                    setJobAvailableRequest.put("data", data);

                    // Envie a solicitação para o servidor e obtenha a resposta
                    String setJobAvailableJsonRequest = setJobAvailableRequest.toJson();
                    String setJobAvailableResponse = client.sendRequestToServer(setJobAvailableJsonRequest);
                    JsonObject setJobAvailableJson = Jsoner.deserialize(setJobAvailableResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) setJobAvailableJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Exiba a mensagem de sucesso
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga definida como disponível com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                    } else if ("JOB_NOT_FOUND".equals(status)) {
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                    } else {
                        // Se houver algum outro erro, mostre uma mensagem de erro genérica
                        JOptionPane.showMessageDialog(panelMenuJob, "Ocorreu um erro. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnNoAvailable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();
                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione uma habilidade.");
                } else {
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String jobId = (String) jobData.get("id");

                    // Crie o objeto JSON para a operação SET_JOB_AVAILABLE com "YES"
                    JsonObject setJobAvailableRequest = new JsonObject();
                    setJobAvailableRequest.put("operation", "SET_JOB_AVAILABLE");
                    setJobAvailableRequest.put("token", token);
                    JsonObject data = new JsonObject();
                    data.put("id", jobId);
                    data.put("available", "NO");
                    setJobAvailableRequest.put("data", data);

                    // Envie a solicitação para o servidor e obtenha a resposta
                    String setJobAvailableJsonRequest = setJobAvailableRequest.toJson();
                    String setJobAvailableResponse = client.sendRequestToServer(setJobAvailableJsonRequest);
                    JsonObject setJobAvailableJson = Jsoner.deserialize(setJobAvailableResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) setJobAvailableJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Exiba a mensagem de sucesso
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga definida como indisponível com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                    } else if ("JOB_NOT_FOUND".equals(status)) {
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                    } else {
                        // Se houver algum outro erro, mostre uma mensagem de erro genérica
                        JOptionPane.showMessageDialog(panelMenuJob, "Ocorreu um erro. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnYesSearchable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();
                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione uma habilidade.");
                } else {
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String jobId = (String) jobData.get("id");

                    // Crie o objeto JSON para a operação SET_JOB_AVAILABLE com "YES"
                    JsonObject setJobAvailableRequest = new JsonObject();
                    setJobAvailableRequest.put("operation", "SET_JOB_SEARCHABLE");
                    setJobAvailableRequest.put("token", token);
                    JsonObject data = new JsonObject();
                    data.put("id", jobId);
                    data.put("searchable", "YES");
                    setJobAvailableRequest.put("data", data);

                    // Envie a solicitação para o servidor e obtenha a resposta
                    String setJobAvailableJsonRequest = setJobAvailableRequest.toJson();
                    String setJobAvailableResponse = client.sendRequestToServer(setJobAvailableJsonRequest);
                    JsonObject setJobAvailableJson = Jsoner.deserialize(setJobAvailableResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) setJobAvailableJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Exiba a mensagem de sucesso
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga definida como disponível com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                    } else if ("JOB_NOT_FOUND".equals(status)) {
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                    } else {
                        // Se houver algum outro erro, mostre uma mensagem de erro genérica
                        JOptionPane.showMessageDialog(panelMenuJob, "Ocorreu um erro. Por favor, tente novamente.");
                    }
                }
            }
        });
        btnNoSearchable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = listJobs.getSelectedIndex();
                if (index == -1) {
                    // Nenhum item está selecionado na lista
                    JOptionPane.showMessageDialog(panelMenuJob, "Por favor, selecione uma habilidade.");
                } else {
                    JsonObject jobData = (JsonObject) jobset.get(index);
                    String jobId = (String) jobData.get("id");

                    // Crie o objeto JSON para a operação SET_JOB_AVAILABLE com "YES"
                    JsonObject setJobAvailableRequest = new JsonObject();
                    setJobAvailableRequest.put("operation", "SET_JOB_SEARCHABLE");
                    setJobAvailableRequest.put("token", token);
                    JsonObject data = new JsonObject();
                    data.put("id", jobId);
                    data.put("searchable", "NO");
                    setJobAvailableRequest.put("data", data);

                    // Envie a solicitação para o servidor e obtenha a resposta
                    String setJobAvailableJsonRequest = setJobAvailableRequest.toJson();
                    String setJobAvailableResponse = client.sendRequestToServer(setJobAvailableJsonRequest);
                    JsonObject setJobAvailableJson = Jsoner.deserialize(setJobAvailableResponse, new JsonObject());

                    // Verifique o status da resposta
                    String status = (String) setJobAvailableJson.get("status");
                    if ("SUCCESS".equals(status)) {
                        // Exiba a mensagem de sucesso
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga definida como indisponível com sucesso!");
                    } else if ("INVALID_TOKEN".equals(status)) {
                        // Se o token for inválido, mostre uma mensagem de erro
                        JOptionPane.showMessageDialog(panelMenuJob, "Token inválido. Por favor, tente novamente.");
                    } else if ("JOB_NOT_FOUND".equals(status)) {
                        JOptionPane.showMessageDialog(panelMenuJob, "Vaga não encontrada. Por favor, tente novamente.");
                    } else {
                        // Se houver algum outro erro, mostre uma mensagem de erro genérica
                        JOptionPane.showMessageDialog(panelMenuJob, "Ocorreu um erro. Por favor, tente novamente.");
                    }
                }
            }
        });
    }
}
