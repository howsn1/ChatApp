package src.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyledDocument;
import src.client.Client;

public class chatInterface extends JFrame {
    private String username;
    private Client client;
    private JTextPane chatArea;
    private StyledDocument doc;
    private JTextField messageField;
    private JButton sendButton;
    private JButton sendFileButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public chatInterface(Client client, String username) {
        this.username = username;
        this.client = client;

        initComponents();

        client.listenForMessages(message -> {
            appendMessage(message);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.closeEverything();
                dispose();
            }
        });
    }

    private void initComponents() {
        setTitle("ChatGroup - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        doc = chatArea.getStyledDocument();
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellWidth(150);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.TRUETYPE_FONT, 14));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        sendFileButton = new JButton("Send File");
        sendFileButton.setPreferredSize(new Dimension(100, 30));
        sendFileButton.addActionListener(e -> openFileChooser());

        sendButton = new JButton("Message");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(e -> sendMessage());

        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(sendButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonsPanel, BorderLayout.EAST);

        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(userScrollPane, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        messageField.addActionListener(e -> sendMessage());

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeStamp = formatter.format(new Date());
        try {
            doc.insertString(doc.getLength(), "[" + timeStamp + "] Welcome to the chat, " + username + "!\n", null);
            doc.insertString(doc.getLength(), "[" + timeStamp + "] Type your message and press Enter or click Send.\n\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        userListModel.addElement(username + " (You)");
        client.setUserListListener(users -> {
            SwingUtilities.invokeLater(() -> {
                userListModel.clear();
                for (String user : users) {
                    userListModel.addElement(user);
                }
            });
        });
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            JDialog progressDialog = new JDialog(this, "Sending File", true);
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setIndeterminate(true);

            JLabel statusLabel = new JLabel("Sending " + selectedFile.getName() + "...");
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(statusLabel, BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);

            progressDialog.getContentPane().add(panel);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);

            new Thread(() -> {
                try {
                    client.sendFile(selectedFile);
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        String timeStamp = formatter.format(new Date());
                        try {
                            doc.insertString(doc.getLength(), "[" + timeStamp + "] You sent file: " + selectedFile.getName() + "\n", null);
                            if (isImageFile(selectedFile.getName())) {
                                insertImage(selectedFile);
                            } else {
                                insertFileLink(selectedFile);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this, "Error sending file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            progressDialog.setVisible(true);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String timeStamp = formatter.format(new Date());
            try {
                doc.insertString(doc.getLength(), "[" + timeStamp + "] You: " + message + "\n", null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            messageField.setText("");
        }
        messageField.requestFocus();
    }

    private void downloadFile(String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int option = fileChooser.showSaveDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File saveLocation = fileChooser.getSelectedFile();
            client.requestFileDownload(fileName, saveLocation);

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            if (isImageFile(fileName)) {
                                insertImage(saveLocation);
                            } else {
                                insertFileLink(saveLocation);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String timeStamp = formatter.format(new Date());

            try {
                if (message.startsWith("SERVER:") && message.contains(" sent a file: ")) {
                    String[] parts = message.split(" sent a file: ");

                    if (parts.length == 2) {
                        String sender = parts[0].replace("SERVER: ", "");
                        String fileName = parts[1];

                        doc.insertString(doc.getLength(), "[" + timeStamp + "] " + sender + " sent a file: " + fileName + "\n", null);

                        int option = JOptionPane.showConfirmDialog(this, sender + " sent a file: " + fileName + "\nWould you like to download it?", "File Received", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            downloadFile(fileName);
                        }
                    }
                } else if (message.startsWith("SERVER:")) {
                    doc.insertString(doc.getLength(), "[" + timeStamp + "] " + message + "\n", null);
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
                } else {
                    doc.insertString(doc.getLength(), "[" + timeStamp + "] " + message + "\n", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif");
    }

    private void insertImage(File file) {
        try {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
            chatArea.setCaretPosition(doc.getLength());
            chatArea.insertIcon(icon);
            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertFileLink(File file) {
        try {
            JLabel fileLabel = new JLabel("<html><a href=''>" + file.getName() + "</a></html>");
            fileLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fileLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(chatArea, "Can't open file: " + ex.getMessage());
                    }
                }
            });
            chatArea.setCaretPosition(doc.getLength());
            chatArea.insertComponent(fileLabel);
            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
