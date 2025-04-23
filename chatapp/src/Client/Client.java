package src.client;

import java.awt.EventQueue;
import java.io.*;
import java.net.Socket;
import src.gui.LoginForm;
import java.util.List;
public class Client {

    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private OutputStream os;
    
    private java.util.List<String> connectedUsers = new java.util.ArrayList<>();

List <String> getConnectedUsers() {
    return new java.util.ArrayList<>(connectedUsers);
}
public interface UserListListener {
    void onUserListUpdated(java.util.List<String> users);
}
private UserListListener userListListener;

public void setUserListListener(UserListListener listener) {
    this.userListListener = listener;
}

    public Client(Socket clientSocket, String username) {
        try {
            this.username = username;
            this.clientSocket = clientSocket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.os=clientSocket.getOutputStream();

        } catch (IOException e) {
            closeEverything();
        }
    }
    
    public boolean sendLoginMessage(String password) {
        try {
            String loginMessage = "LOGIN " + username + " " + password;
            bufferedWriter.write(loginMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            String response = bufferedReader.readLine();
            if (response.equals("Login successful!")) {
                System.out.println("Logged in successfully!");
                return true;
            } else {
                System.out.println("Login failed!");
                return false;
            }
        } catch (IOException e) {
            closeEverything();
            return false;
        }
    }
    
    public void sendMessage(String message) {
        try {
            bufferedWriter.write(username + ": " + message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }
    



    public void listenForMessages(MessageListener messageListener) {
        new Thread(() -> {
            String messageFromServer;
            try {
                while ((messageFromServer = bufferedReader.readLine()) != null) {
                    if (messageFromServer.startsWith("USERLIST|")) {
                        // This is a user list update
                        String[] parts = messageFromServer.split("\\|");
                        if (parts.length > 1) {
                            String[] users = parts[1].split(",");
                            connectedUsers.clear();
                            for (String user : users) {
                                if (!user.isEmpty()) {
                                    connectedUsers.add(user);
                                }
                            }
                            
                            // Notify listener about updated user list
                            if (userListListener != null) {
                                userListListener.onUserListUpdated(getConnectedUsers());
                            }
                        }
                    } else {
                        // Regular message
                        final String finalMessage = messageFromServer;
                        messageListener.onMessageReceived(finalMessage);
                    }
                }
            } catch (IOException e) {
                closeEverything();
            }
        }).start();
    }
    public void closeEverything() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestFileDownload(String fileName, File destination) {
        try {
            // Envoyer une commande spéciale au serveur
            bufferedWriter.write("DOWNLOAD|" + fileName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
    
            // Réception du fichier
            InputStream inputStream = clientSocket.getInputStream();
            FileOutputStream fos = new FileOutputStream(destination);
    
            byte[] buffer = new byte[4096];
            int bytesRead;
    
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                // Arrêter après avoir reçu la taille complète du fichier si tu l'enregistres
            }
    
            fos.close();
            System.out.println("File downloaded to " + destination.getAbsolutePath());
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendFile(File file) {
        try {
            if (!file.exists()) {
                System.out.println("File does not exist.");
                return;
            }
            
            // Send file header with metadata
            String header = "FILE|" + file.getName() + "|" + file.length();
            bufferedWriter.write(header);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            // Small delay to ensure header is processed
            Thread.sleep(100);
            
            // Send the file data
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            // Get direct access to output stream for binary data
            OutputStream outputStream = clientSocket.getOutputStream();
            
            // Read file and send bytes
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
            fis.close();
             
            System.out.println("File sent: " + file.getName());
            
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending file: " + e.getMessage());
            e.printStackTrace();
        }
    }
  
    // Interface for message callbacks
    public interface MessageListener {
        void onMessageReceived(String message);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginForm loginWindow = new LoginForm();
                loginWindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
// Implementation de code de transfert des fichiers 




}
