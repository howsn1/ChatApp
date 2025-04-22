package src.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.client.Client;

public class chatInterface extends JFrame {
    private String username;
    private Client client;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton sendFileButton;
    private  JList<String> userList;
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
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // User list panel
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellWidth(150);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        // Message input area with both Send and Send File buttons
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.TRUETYPE_FONT, 14));
        
        // Create buttons panel to hold both buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // Send File button
        sendFileButton = new JButton("Send File");
        sendFileButton.setPreferredSize(new Dimension(100, 30));
        sendFileButton.addActionListener(e -> openFileChooser());
        
        // Send button
        sendButton = new JButton("Message");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(e -> sendMessage());
        
        // Add buttons to the panel
        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(sendButton);
        
        // Add components to input panel
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(userScrollPane, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Add to frame
        setContentPane(mainPanel);
        
        // Add action listener for message field
        messageField.addActionListener(e -> sendMessage());
        
        // Add some welcome text
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeStamp = formatter.format(new Date());
        chatArea.append("[" + timeStamp + "] Welcome to the chat, " + username + "!\n");
        chatArea.append("[" + timeStamp + "] Type your message and press Enter or click Send.\n\n");
        
        // Add yourself to user list for demo
        userListModel.addElement(username + " (You)");
    }
    
    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this); // Use 'this' as the parent component
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Create a progress dialog
            JDialog progressDialog = new JDialog(this, "Sending File", true);
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setIndeterminate(true); // Using indeterminate mode for simplicity
            
            JLabel statusLabel = new JLabel("Sending " + selectedFile.getName() + "...");
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(statusLabel, BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);
            
            progressDialog.getContentPane().add(panel);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            
            // Send file in a background thread
            new Thread(() -> {
                try {
                    client.sendFile(selectedFile);
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        
                        // Add a message to the chat area
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        String timeStamp = formatter.format(new Date());
                        chatArea.append("[" + timeStamp + "] You sent file: " + selectedFile.getName() + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this, 
                                "Error sending file: " + ex.getMessage(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
            
            // Show dialog
            progressDialog.setVisible(true);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            
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
    
            // Handle file notifications
            if (message.startsWith("SERVER:") && message.contains(" sent a file: ")) {
                String[] parts = message.split(" sent a file: ");
                
                if (parts.length == 2) {
                    String sender = parts[0].replace("SERVER: ", "");
                    String fileName = parts[1];
    
                    chatArea.append("[" + timeStamp + "] " + sender + " sent a file: " + fileName + "\n");
    
                    int option = JOptionPane.showConfirmDialog(
                        this,
                        sender + " sent a file: " + fileName + "\nWould you like to download it?",
                        "File Received",
                        JOptionPane.YES_NO_OPTION
                    );
    
                    if (option == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(this, 
                            "File download feature not yet implemented.", 
                            "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
                    }
    
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    return;
                }
            }
    
            // Handle SERVER messages about user join/leave
            if (message.startsWith("SERVER:")) {
                String[] parts = message.split(" ", 3);
                if (parts.length >= 3) {
                    String user = parts[1];
                    String action = parts[2];
    
                    if (action.contains("joined")) {
                        if (!userListModel.contains(user)) {
                            userListModel.addElement(user);
                        }
                    } else if (action.contains("left")) {
                        userListModel.removeElement(user);
                    }
                }
    
                // Also print server message in chat
                chatArea.append("[" + timeStamp + "] " + message + "\n");
            } else {
                // ✅ HERE is where you show normal messages
                chatArea.append("[" + timeStamp + "] " + message + "\n");
            }
    
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    

            
            // Add normal message with timestamp
            
            // Auto-scroll to bottom
    }