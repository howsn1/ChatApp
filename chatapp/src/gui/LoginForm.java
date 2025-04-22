package src.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import src.client.Client;
import src.db.Authenticate;
import src.gui.chatInterface;


public class LoginForm extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    public LoginForm() {
        setTitle("Chat Application - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(buttonPanel, gbc);
        
        add(panel);
        

        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> openRegisterForm());
        
        // Enter key for login
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        });
    }
    
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password cannot be empty", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // First verify credentials with your database
            boolean isAuthenticated = Authenticate.validateLogin(username, password);
            
            if (isAuthenticated) {
                connectToServer(username, password);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Login error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void connectToServer(String username, String password) {
        try {
            Socket socket = new Socket("localhost", 1234);
            Client client = new Client(socket, username);
            
            // Authenticate with server
            boolean serverAuthenticated = client.sendLoginMessage(password);
            
            if (serverAuthenticated) {
                // Open chat interface and pass the client
                openChatInterface(client, username);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Server authentication failed", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
                client.closeEverything();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Could not connect to server: " + e.getMessage(), 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openChatInterface(Client client, String username) {
        // Close the login form
        this.dispose();
        client.listenForMessages(message -> {
            System.out.println("📨 " + message); // DEBUG: print received message
        });

        // Create and show the chat interface
        EventQueue.invokeLater(() -> {
            try {
                chatInterface chatWindow = new chatInterface(client, username);
                chatWindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void openRegisterForm() {
        // Open register form
        EventQueue.invokeLater(() -> {
            try {
                RegisterForm registerWindow = new RegisterForm();
                registerWindow.setVisible(true);
                this.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
}