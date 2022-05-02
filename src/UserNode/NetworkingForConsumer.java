
package UserNode;

import Logging.ConsoleColors;
import NetworkUtilities.GeneralUtils;
import NetworkUtilities.UserNodeUtils;
import Tools.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
public class NetworkingForConsumer implements Runnable{

    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private Socket request_socket;
    private UserNode cons;
    boolean exit = false;
    private Scanner sc = new Scanner(System.in);
    private int operation;


    public NetworkingForConsumer(Socket request_socket,UserNode cons,int operation){
        this.request_socket = request_socket;
        this.cons = cons;
        this.operation = operation;
        try {
            localoutputStream = new ObjectOutputStream(request_socket.getOutputStream());
            localinputStream = new ObjectInputStream(request_socket.getInputStream());
        }catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Socket Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }catch (IOException e) {
            System.out.println("\033[0;31m" + "Error while constructing networking for consumer" + "\033[0m");
            shutdownConnection();
            return;
        }
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

    public void startNewConnection(Tuple<String,int[]> new_broker,int operation){
        shutdownConnection();
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        int port = new_broker.getValue2()[0];
        System.out.println("New broker port: " + port);
        NetworkingForConsumer new_connection = null;
        try {
            new_connection = new NetworkingForConsumer(new Socket(IP,port),cons,operation);
            shutdownConnection();
        } catch (ConnectException connectException){
            System.out.println(ConsoleColors.RED + "Could not connect to the new broker" + ConsoleColors.RESET);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println(ConsoleColors.RED + "IO error while trying to connect to the new broker" + ConsoleColors.RESET);
        }
        Thread t = new Thread(new_connection);
        t.start();
    }

    @Override
    public void run() {
        Integer index;
        switch (operation){
            case 0:
                if(UserNodeUtils.getBrokerList(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(UserNodeUtils.receiveBrokerList(localinputStream,localoutputStream,request_socket,cons) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case 1:
                if(UserNodeUtils.getIDList(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(UserNodeUtils.receiveIDList(localinputStream,localoutputStream,request_socket,cons) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case 2:
                if(UserNodeUtils.sendNickname(localoutputStream,cons) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
                break;
            case 3:
                if((index = UserNodeUtils.register(localinputStream,localoutputStream,request_socket,sc,cons)) == null){
                    shutdownConnection();
                    break;
                }else if(index == -1){
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                }else{
                    Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                    startNewConnection(brk,3);
                    break;
                }

            case 4:
                if((index = UserNodeUtils.unsubscribe(localinputStream,localoutputStream,request_socket,sc,this.cons)) == null){
                    shutdownConnection();
                    return;
                }else if(index == -1){
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                }else{
                    Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                    startNewConnection(brk,4);
                    break;
                }

            case 5:
                if(UserNodeUtils.showConversationData(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case 6:
                push();
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            default:
                System.out.println("Invalid Request... Try again");
                shutdownConnection();
        }
        shutdownConnection();
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
