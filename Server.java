package chatapp;

import java.io.*;
import java.net.*;

 

public class Server{


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
