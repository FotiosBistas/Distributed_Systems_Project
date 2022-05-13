
package UserNode;

import Logging.ConsoleColors;
import NetworkUtilities.GeneralUtils;
import NetworkUtilities.UserNodeUtils;
import Tools.Messages;
import Tools.Tuple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
public class NetworkingForConsumer implements Runnable{

    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private Socket request_socket;
    private UserNode cons;
    boolean exit = false;
    private int operation;
    private String topic_name = null;


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

    public NetworkingForConsumer(Socket request_socket,UserNode cons,int operation,String topic_name){
        this.request_socket = request_socket;
        this.cons = cons;
        this.operation = operation;
        this.topic_name = topic_name;
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


    /**
     * If the current broker that the user node is connected to is not the right broker for the topic a new connection is opened to serve the corresponding request
     * for the specific topic.
     * @param new_broker Accepts a tuple type that is the new broker's ip and port(port[0] because it is for consumer connections). The tuple broker is found in the broker list that the user received.
     * @param operation Accepts the operation that the new connection must serve.
     */
    private void startNewConnection(Tuple<String,int[]> new_broker,int operation){
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        //port for connecting to broker for consumer traffic
        int port = new_broker.getValue2()[0];
        System.out.println("New broker port: " + port);
        NetworkingForConsumer new_connection = null;
        try {
            if(topic_name == null) {
                new_connection = new NetworkingForConsumer(new Socket(IP, port), cons, operation);
            }else{
                new_connection = new NetworkingForConsumer(new Socket(IP,port),cons,operation,topic_name);
            }
            shutdownConnection();
        } catch (ConnectException connectException){
            System.out.println(ConsoleColors.RED + "Could not connect to the new broker" + ConsoleColors.RESET);
            shutdownConnection();
        } catch (IOException ioException) {
            System.out.println(ConsoleColors.RED + "IO error while trying to connect to the new broker" + ConsoleColors.RESET);
            shutdownConnection();
        }
        Thread t = new Thread(new_connection);
        t.start();
    }

    @Override
    public void run() {
        Integer index;
        Integer success;
        switch (operation){
            case 1:
                if((index = UserNodeUtils.register(localinputStream,localoutputStream,request_socket,topic_name,cons)) == null){
                    shutdownConnection();
                    break;
                }else if(index == -1){
                    success = GeneralUtils.waitForNodePrompt(localinputStream,request_socket);
                    if(success == null){
                        return;
                    }else if(success == Messages.NO_SUCH_TOPIC.ordinal()){
                        System.out.println(ConsoleColors.RED + "Broker couldn't subscribe you to the topic so it will create a new one" + ConsoleColors.RESET);
                        cons.addNewSubscription(topic_name);
                        if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                            shutdownConnection();
                            break;
                        }
                        return;
                    }else if(success == Messages.FINISHED_OPERATION.ordinal()) {
                        cons.addNewSubscription(topic_name);
                        if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                            shutdownConnection();
                            break;
                        }
                    }
                    break;
                }else{
                    Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                    startNewConnection(brk,1);
                    break;
                }

            case 2:
                if((index = UserNodeUtils.unsubscribe(localinputStream,localoutputStream,request_socket,topic_name,this.cons)) == null){
                    shutdownConnection();
                    return;
                }else if(index == -1){
                    success = GeneralUtils.waitForNodePrompt(localinputStream,request_socket);
                    if(success == null){
                        return;
                    }else if(success == Messages.NO_SUCH_TOPIC.ordinal()){
                        System.out.println(ConsoleColors.RED + "Broker couldn't unsubscribe you from the topic" + ConsoleColors.RESET);
                        return;
                    }else if(success == Messages.FINISHED_OPERATION.ordinal()) {
                        cons.removeSubscription(topic_name);
                        if (GeneralUtils.FinishedOperation(localoutputStream) == null) {
                            shutdownConnection();
                            break;
                        }
                    }
                    break;
                }else{
                    Tuple<String, int[]> brk = cons.getBrokerList().get(index);
                    startNewConnection(brk,2);
                    break;
                }
            case 3:
                if((index = UserNodeUtils.receiveConversationData(localoutputStream,localinputStream,request_socket,topic_name)) == null){
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
                    startNewConnection(brk,3);
                    break;
                }
            case 4:
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
            case 5:
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
            case 6:
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
            default:
                System.out.println("Invalid Request... Try again");
                shutdownConnection();
        }
        shutdownConnection();
    }



    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    private void shutdownConnection(){
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
