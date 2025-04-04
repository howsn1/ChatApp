package src.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyleConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

import src.Client.Client;

public class chatInterface extends JFrame {
    private String username;
    private Client client;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    public chatInterface(Client client, String username) {
        this.username = username;
        this.client = client;
        
        // Set up the UI components
        initComponents();
        
        // Set up message listener
        client.listenForMessages(message -> {
            appendMessage(message);
        });
        
        // Add window listener to handle closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.closeEverything();
                dispose();
            }
        });
    }
    
    private void initComponents() {
        // Set up your chat window components
        setTitle("Chat - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
       // StyleConstants.setBackground(, new Color(0, 132, 255)); // Blue

        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // User list panel
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellWidth(150);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        // Message input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(userScrollPane, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Add to frame
        setContentPane(mainPanel);
        
        // Add action listeners
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        // Add some welcome text
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeStamp = formatter.format(new Date());
        chatArea.append("[" + timeStamp + "] Welcome to the chat, " + username + "!\n");
        chatArea.append("[" + timeStamp + "] Type your message and press Enter or click Send.\n\n");
        
        // Add yourself to user list for demo
        userListModel.addElement(username + " (You)");
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            
            // Add your own message to the chat area
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String timeStamp = formatter.format(new Date());
            chatArea.append("[" + timeStamp + "] You: " + message + "\n");
            
            messageField.setText("");
        }
        messageField.requestFocus();
    }
    
    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String timeStamp = formatter.format(new Date());
            
            // Add timestamp to message
            chatArea.append("[" + timeStamp + "] " + message + "\n");
            
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            
            // If it's a server message about a user joining/leaving
            if (message.startsWith("SERVER:")) {
                String[] parts = message.split(" ", 3);
                if (parts.length >= 3) {
                    String user = parts[1];
                    String action = parts[2];
                    
                    if (action.contains("joined")) {
                        // Add user to list
                        if (!userListModel.contains(user)) {
                            userListModel.addElement(user);
                        }
                    } else if (action.contains("left")) {
                        // Remove user from list
                        userListModel.removeElement(user);
                    }
                }
            }
        });
    }
}