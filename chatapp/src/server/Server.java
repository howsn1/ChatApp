

/* 
 public class  Server{
  private ServerSocket ss ; 
  public Server(ServerSocket ss){
    this.ss=ss;
    System.out.println("[Server] IS ON");
  }

 /*  public void startServer(){
    
    try{
    Socket clientSocket = new Socket() ;

    while(clientSocket.isConnected()){
      clientSocket=ss.accept();
      System.out.println("Welcome to the Chat !");
      ClientHandler clientHandler = new ClientHandler(clientSocket);
 
      

      Thread thread = new Thread(clientHandler);
      thread.start();
    }
  }  catch(IOException e){
      e.getStackTrace(); }
   }*/



   /* 
   public void startServer() {
    try {
      
        while(!ss.isClosed()) {
            Socket clientSocket = ss.accept(); // This blocks until a client connects
            System.out.println("New client connected!");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    } catch(IOException e) {
        e.printStackTrace(); // Use printStackTrace() instead of getStackTrace()
    }
}


 public void closeServerSocket(){
  try{
    if(ss!=null){
      ss.close();
    }
 }catch(IOException e ){
     e.printStackTrace();
 }
}
 public static void main(String[] args) throws Exception {
  

  ServerSocket ss = new ServerSocket(1234);
  Server server = new Server(ss);
  server.startServer();
  
  
}
  }
}

*/


package src.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.db.Authenticate;

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
        private ArrayList<ClientHandler> clients;
        private boolean isLoggedIn = false;

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
                    } else {
                        bufferedWriter.write("You must login first!");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
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