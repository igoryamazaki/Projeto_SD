package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CandidateLookUpView extends JFrame{
    private Client client;
    private String token;
    private JButton btnReturn;
    private JLabel image;
    private JPanel panelLookUp;
    private JLabel labelName;
    private JLabel labelEmail;

    public CandidateLookUpView(Client client, String token) {
        this.client = client;
        this.token = token;

        RequestMessage lookupRequest = new RequestMessage("LOOKUP_ACCOUNT_CANDIDATE", token);
        String lookupJsonRequest = lookupRequest.toJsonStringWithToken();

        String response = client.sendRequestToServer(lookupJsonRequest);
        JsonObject responseJson = Jsoner.deserialize(response, new JsonObject());
        String status = (String) responseJson.get("status");
        if ("SUCCESS".equals(status)) {
            JsonObject dataLookUp = (JsonObject) responseJson.get("data");
            String email = (String) dataLookUp.get("email");
            String password = (String) dataLookUp.get("password");
            String name = (String) dataLookUp.get("name");

            labelName.setText(name);
            labelEmail.setText(email);
        } else if ("INVALID_TOKEN".equals(status)) {
            JOptionPane.showMessageDialog(panelLookUp, "Token inválido. Por favor, tente novamente.");
        } else {
            JOptionPane.showMessageDialog(panelLookUp, "Ocorreu um erro. Por favor, tente novamente.");
        }

        setContentPane(panelLookUp);
        setTitle("Perfil");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300,180);
        setLocationRelativeTo(null);
        ImageIcon imageIcon = (ImageIcon) image.getIcon();
        Image img = imageIcon.getImage();
        Image scaledImage = img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        image.setIcon(scaledIcon);
        setVisible(true);
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateMenuView(client,token);
                dispose();
            }
        });
    }
}
