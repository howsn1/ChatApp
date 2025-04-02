import db.Authenticate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton backButton;

    public RegisterForm() {
        setTitle("Chat App - Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel headerLabel = new JLabel("Create an Account");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(headerLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(emailField, gbc);

        registerButton = new JButton("Register");
        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        backButton = new JButton("Back to Login");
        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(backButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterForm.this, "All fields are required.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean isRegistered = Authenticate.registerUser(username, password, email);

                if (isRegistered) {
                    JOptionPane.showMessageDialog(RegisterForm.this, "Registration Successful! Please login.");
                    dispose(); // Close Register Form
                } else {
                    JOptionPane.showMessageDialog(RegisterForm.this, " Registration failed! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close register form
            }
        });

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterForm().setVisible(true));
    }
}
