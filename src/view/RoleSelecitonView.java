package view;

import client.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoleSelecitonView extends JFrame {
    private Client client;
    private JPanel panelRole;
    private JButton btnCandidate;
    private JButton btnRecruiter;

    public RoleSelecitonView(Client client) {
        this.client = client;
        setContentPane(panelRole);
        setTitle("Entrar como");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(250,100);
        setLocationRelativeTo(null);
        setVisible(true);
        btnCandidate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CandidateLoginView(client);
                dispose();
            }
        });
        btnRecruiter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecruiterLoginView(client);
                dispose();
            }
        });
    }
}
