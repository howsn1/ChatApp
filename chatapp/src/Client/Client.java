package src.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.awt.EventQueue;

import src.gui.LoginForm;
import src.gui.chatInterface;

public class Client {
    
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    
    public Client(Socket clientSocket, String username) {
        try {
            this.username = username;
            this.clientSocket = clientSocket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
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
    
    public void listenForMessages(MessageListener listener) {
        new Thread(() -> {
            String messageFromGroupChat;
            
            try {
                while ((messageFromGroupChat = bufferedReader.readLine()) != null) {
                    final String finalMessage = messageFromGroupChat;
                    listener.onMessageReceived(finalMessage);
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
}