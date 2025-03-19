package    chatapp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;


public class  Client{

    private  Socket clientSocket ;
    private BufferedReader bufferedReader ;
    private  BufferedWriter bufferedWriter;
    static  String username ;

    public Client(Socket clientSocket ,String username){
        try{
      this.username = username ;
      this.clientSocket=clientSocket;
       this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

       this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        }catch(IOException e ) {
                closeEverything(clientSocket,bufferedReader,bufferedWriter);
        }
      }

      public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

         Scanner sc = new Scanner(System.in);
         while(clientSocket.isConnected()){
            String messageToSend=sc.nextLine();
            bufferedWriter.write(username+" : "+messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();

         }
        } catch (Exception e) {
            e.getStackTrace();

        }

      }
      public void ListenForMessage() {
          new Thread(new Runnable() {
            @Override
            public void run(){
                String messageFromGroupChat;
                while(clientSocket.isConnected()){
                    try{
                        messageFromGroupChat=bufferedReader.readLine();
                        System.out.println(messageFromGroupChat);


                    }catch(IOException e){
                        closeEverything(clientSocket, bufferedReader, bufferedWriter);
                        break;

                    }
                    
                }
   }
 }).start();

      }
      public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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


public static void main(String[] args) throws  Exception {
    System.out.println("Enter your Username : ");
    Scanner scanner = new Scanner(System.in);
     String username=scanner.nextLine();
    Socket clientSocket=new Socket("localhost",1234);
    Client client = new Client(clientSocket, username);
    client.ListenForMessage();
    client.sendMessage();


}


} 

    
    


