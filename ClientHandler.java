

/******************************************************* *********************************************/
/* THE CLIENTHANDLER class allows us to handle each client separately, in terms of the thread he ll be using */
/******************************************************************************************************* */
package chatapp;

 
import java.io.*;
import  java.net.*;
import  java.util.*;


public class ClientHandler implements Runnable{

  private String clientUsername;
  private Socket clientSocket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;

  public  static ArrayList<ClientHandler>clients=new ArrayList<>();


  public ClientHandler(Socket clientSocket){
    try {
     this.clientSocket=clientSocket;

      this.bufferedWriter =new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
      this.bufferedReader =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

     this.clientUsername = bufferedReader.readLine();

     clients.add(this);

     broadCastMessage("[Server] "+clientUsername+" has joinded the chat ");
     
         } catch (Exception e) {
          closeEverything(clientSocket, bufferedReader, bufferedWriter);
         }
       }

     
    @Override
public  void run(){
 String messageFromClient;
    //while(!clientSocket.isClosed()){
    while(clientSocket.isConnected()){
        try {
            messageFromClient=bufferedReader.readLine();
            broadCastMessage(messageFromClient);
        } catch (IOException e) {
            closeEverything(clientSocket,bufferedReader,bufferedWriter);
                        break;
          }                 
                 }
 }
         
            private void broadCastMessage(String messageToSend) {
                try{
                for (ClientHandler clientHandler:clients){
                    if(!clientHandler.clientUsername.equals(clientUsername)){
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();

                    }

                
            }} catch(IOException e ){
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }

}

   public void removeClientHandler(){
      clients.remove(this);
      broadCastMessage("[Server] "+clientUsername+"has left the chat");

   }


   private void closeEverything(Socket clientSocket, BufferedReader bufferedReader,BufferedWriter bufferedWriter){
      removeClientHandler();
 
      try{
      if(bufferedWriter!=null){
       bufferedWriter.close();
      }
      if(bufferedReader!=null){
        bufferedReader.close();
      }

      if(clientSocket !=null){
        clientSocket.close();
      }
   }   catch(IOException e){ 
    e.printStackTrace();

   } 
}


}
