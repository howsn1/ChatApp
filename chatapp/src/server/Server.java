



package src.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import src.db.Authenticate;
import src.media.FileHelper;
import src.media.FileUtils;

public class Server {
    private final ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                ClientHandler clientHandler = new ClientHandler(socket, clients);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            Server server = new Server(serverSocket);
            System.out.println("Server started on port 1234");
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientUsername;
        private static ArrayList<ClientHandler> clients;
        private boolean isLoggedIn = false  ;

        public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) {
            try {
                this.socket = socket;
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.clients = clients;
            } catch (IOException e) {
                closeEverything();
            }
        }

        @Override
        public void run() {
            String messageFromClient;

            try {
                while (socket.isConnected() && (messageFromClient = bufferedReader.readLine()) != null) {
                    if (!isLoggedIn && messageFromClient.startsWith("LOGIN")) {
                        handleLogin(messageFromClient);
                    } else if (isLoggedIn) {
                        broadcastMessage(messageFromClient);
                    if(messageFromClient.startsWith("FILE|")){
                        receiveFile(messageFromClient);
                    }
                         else {
                      broadcastMessage(messageFromClient);

                        }
                     
                       // bufferedWriter.write("You must login first!");
                    
                     }
                     bufferedWriter.newLine();
                     bufferedWriter.flush();
                    }
                
            } catch (IOException e) {
                closeEverything();
            }
        }
       
        
        private void handleLogin(String loginMessage) {
            try {
                // Format: LOGIN username password
                String[] parts = loginMessage.split(" ", 3);
                if (parts.length < 3) {
                    bufferedWriter.write("Invalid login format!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    return;
                }

                String username = parts[1];
                String password = parts[2];

                // Authenticate against database
                boolean authenticated = Authenticate.validateLogin(username, password);

                if (authenticated) {
                    this.clientUsername = username;
                    this.isLoggedIn = true;
                    bufferedWriter.write("Login successful!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    
                    // Notify everyone that user joined
                    broadcastMessage("SERVER: " + clientUsername + " has joined the chat!");
                } else {
                    bufferedWriter.write("Login failed!");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything();
            }
        }

        public void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                try {
                    if (client != this && client.isLoggedIn) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything();
                }
            }
        }


  private void receiveFile(String header) {
    try {
        // Parse metadata
        String[] parts = header.split("\\|");
        if (parts.length < 3) {
            bufferedWriter.write("Invalid file format received.");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            return;
        }
        
        String fileName = parts[1];
        long fileSize = Long.parseLong(parts[2]);

        System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

        // Create a new file to save
        File file = new File("received_" + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        
        byte[] buffer = new byte[4096];  // Buffer size for reading file
        long bytesReadTotal = 0;

        // Use the socket's InputStream (raw bytes)
        InputStream inputStream = socket.getInputStream();

        // Read file data from input stream and write it to the file output stream
        while (bytesReadTotal < fileSize) {
            int bytesToRead = (int) Math.min(buffer.length, fileSize - bytesReadTotal);
            int bytesRead = inputStream.read(buffer, 0, bytesToRead);

            if (bytesRead == -1) break;  // Break if we reach end of stream unexpectedly

            fos.write(buffer, 0, bytesRead);
            bytesReadTotal += bytesRead;
        }

        fos.close();
        System.out.println("✅ File received: " + fileName);

        // Optionally, broadcast to others that a file was received
        broadcastMessage("SERVER: " + clientUsername + " sent a file: " + fileName);

    } catch (IOException e) {
        System.out.println("❌ Failed to receive file.");
        e.printStackTrace();
    }
}
 



        public void removeClientHandler() {
            clients.remove(this);
            if (clientUsername != null) {
                broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
            }
        }

        public void closeEverything() {
            removeClientHandler();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
