
package UserNode;
import NetworkUtilities.GeneralUtils;
import NetworkUtilities.UserNodeUtils;
import Tools.Messages;
import Tools.Tuple;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
public class NetworkingForConsumer implements Runnable{

    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private Socket request_socket;
    private UserNode cons;
    boolean exit = false;
    private Scanner sc = new Scanner(System.in);


    public NetworkingForConsumer(Socket request_socket,UserNode cons){
        this.request_socket = request_socket;
        this.cons = cons;
        try {
            localoutputStream = new ObjectOutputStream(request_socket.getOutputStream());
            localinputStream = new ObjectInputStream(request_socket.getInputStream());
        }catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }catch (IOException e) {
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
        if(UserNodeUtils.getBrokerList(localoutputStream) == null){
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
        if(UserNodeUtils.sendNickname(localoutputStream,cons) == null){
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
        if(GeneralUtils.FinishedOperation(localoutputStream) == null){
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
        System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
    }



    public synchronized void notifyThread(){
        System.out.println("Waking up networking for consumer");
        notifyAll();
    }

    public synchronized void push(){
        System.out.println("Pushing operation has started");
        try {
            NetworkingForPublisher publish = new NetworkingForPublisher(new Socket("192.168.1.5", 1235), cons,this);
            Thread t = new Thread(publish);
            t.start();
            //waits until input is given by the publisher and servers the push request in the background
            wait();
        }catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }catch(IOException | InterruptedException e){
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
    }

    public void pull(String topic){

    }

    @Override
    public void run() {
        while(!exit) {
            System.out.println("1.Register to topic");
            System.out.println("2.Unsubscribe from topic");
            System.out.println("3.Show conversation data");
            System.out.println("4.Push");
            System.out.println("0.Exit");
            System.out.println("Enter an int from the above options");
            int userinput = sc.nextInt();
            switch (userinput){
                case 1:
                    Integer index;
                    if((index = UserNodeUtils.register(localinputStream,localoutputStream,request_socket,sc,cons)) == null){
                        shutdownConnection();
                        break;
                    }
                    /*Tuple<String,int[]> brk = cons.getBrokerList().get(index);
                    String IP = brk.getValue1();
                    int port = brk.getValue2()[0];
                    NetworkingForConsumer new_connection = new NetworkingForConsumer(new Socket(IP,port),cons);
                    Thread t = new Thread(new_connection);
                    t.start();
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null{
                        shutdownConnection();
                        break;
                    };*/
                    break;
                case 2:
                    if(UserNodeUtils.unsubscribe(localinputStream,localoutputStream,request_socket,sc,this.cons) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case 3:
                    if(UserNodeUtils.showConversationData(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case 4:
                    push();
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case 0:
                    System.out.println("Terminating connection with server in run");
                    shutdownConnection();
                    break;
                default:
                    System.out.println("Invalid Request... Try again");
            }
        }
    }



    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    public void shutdownConnection(){
        System.out.println("Ending client: " + cons.getName());
        exit = true;
        try{
            if(localinputStream != null) {
                System.out.println("Input stream was not null so we had to close it");
                localinputStream.close();
            }
            if(localoutputStream != null) {
                System.out.println("Output stream was not null so we had to close it");
                localoutputStream.close();
            }
            if(request_socket != null) {
                System.out.println("Socket was not closed so we had to close it");
                request_socket.close();
            }
        }catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Socket error" +  "\033[0m");
            shutdownConnection();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
