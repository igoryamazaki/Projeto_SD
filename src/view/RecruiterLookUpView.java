package view;

import client.Client;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import utils.RequestMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RecruiterLookUpView extends JFrame{
    private Client client;
    private String token;
    private JButton btnReturn;
    private JLabel labelName;
    private JLabel labelEmail;
    private JLabel labelIndustry;
    private JLabel labelDescription;
    private JPanel panelRecruiterLookUp;
    private JLabel image;

    public RecruiterLookUpView(Client client, String token) {
        this.client = client;
        this.token = token;
        RequestMessage lookupRequest = new RequestMessage("LOOKUP_ACCOUNT_RECRUITER", token);
        String lookupJsonRequest = lookupRequest.toJsonStringWithToken();

        String response = client.sendRequestToServer(lookupJsonRequest);
        JsonObject responseJson = Jsoner.deserialize(response, new JsonObject());
        String status = (String) responseJson.get("status");
        if ("SUCCESS".equals(status)) {
            JsonObject dataLookUp = (JsonObject) responseJson.get("data");
            String email = (String) dataLookUp.get("email");
            String password = (String) dataLookUp.get("password");
            String name = (String) dataLookUp.get("name");
            String industry = (String) dataLookUp.get("industry");
            String description = (String) dataLookUp.get("description");

            labelName.setText(name);
            labelEmail.setText(email);
            labelIndustry.setText(industry);
            labelDescription.setText(description);
        } else if ("INVALID_TOKEN".equals(status)) {
            JOptionPane.showMessageDialog(panelRecruiterLookUp, "Token inválido. Por favor, tente novamente.");
        } else {
            JOptionPane.showMessageDialog(panelRecruiterLookUp, "Ocorreu um erro. Por favor, tente novamente.");
        }
        setContentPane(panelRecruiterLookUp);
        setTitle("Perfil");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,180);
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
                new RecruiterMenuView(client,token);
                dispose();
            }
        });
    }
}
