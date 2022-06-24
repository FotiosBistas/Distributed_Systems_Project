

package UserNode;


import Logging.ConsoleColors;
import NetworkUtilities.UserNodeUtils;
import Tools.Tuple;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class NetworkingForPublisher implements Runnable {

    private final Socket connection;
    private final UserNode pub;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private final String topic_name;
    private final int operation;
    private final String contents_file_name;


    public NetworkingForPublisher(Socket connection, UserNode pub, String topic_name, int operation, String contents_file_name) {
        this.connection = connection;
        this.pub = pub;
        this.topic_name = topic_name;
        this.operation = operation;
        this.contents_file_name = contents_file_name;
        try {
            localoutputStream = new ObjectOutputStream(connection.getOutputStream());
            localinputStream = new ObjectInputStream(connection.getInputStream());
        } catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Error while constructing networking for publisher" + "\033[0m");
            shutdownConnection();
            return;
        }catch (IOException e) {
            System.out.println("\033[0;31m" + "Error while constructing networking for publisher" + "\033[0m");
            shutdownConnection();
            return;
        }
    }

    /**
     * If the current broker that the user node is connected to is not the right broker for the topic a new connection is opened to serve the corresponding request
     * for the specific topic.
     * @param new_broker Accepts a tuple type that is the new broker's ip and port(port[1] because it is for publisher connections). The tuple broker is found in the broker list that the user received.
     * @param operation Accepts the operation that the new connection must serve.
     */
    private void startNewConnection(Tuple<String,int[]> new_broker,int operation){
        String IP = new_broker.getValue1();
        System.out.println("New connection IP: " + IP);
        int port = new_broker.getValue2()[1];
        System.out.println("New broker port: " + port);
        NetworkingForPublisher new_connection = null;
        try {
            new_connection = new NetworkingForPublisher(new Socket(IP,port),pub,topic_name,operation,contents_file_name);
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
        System.out.println("New publisher was created");
        Integer index;
        switch (operation) {
            case 0:
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, 0, contents_file_name)) == null) {
                    System.out.println(ConsoleColors.RED + "Error while trying to push file" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else if (index == -1) {
                    System.out.println(ConsoleColors.PURPLE + "Finished the push file operation" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else {
                    Tuple<String, int[]> brk = pub.getBrokerList().get(index);
                    startNewConnection(brk, 0);
                }
                break;
            case 1:
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, 1, contents_file_name)) == null) {
                    System.out.println(ConsoleColors.RED + "Error while trying to push message" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else if (index == -1) {
                    System.out.println(ConsoleColors.PURPLE + "Finished the push message operation" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else {
                    Tuple<String, int[]> brk = pub.getBrokerList().get(index);
                    startNewConnection(brk, 1);
                }
                break;

            case 2:
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub, 2, contents_file_name)) == null) {
                    System.out.println(ConsoleColors.RED + "Error while trying to push message" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else if (index == -1) {
                    System.out.println(ConsoleColors.PURPLE + "Finished the push message operation" + ConsoleColors.RESET);
                    shutdownConnection();
                    return;
                } else {
                    Tuple<String, int[]> brk = pub.getBrokerList().get(index);
                    startNewConnection(brk, 2);
                }
                break;
        }
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    private void shutdownConnection(){
        System.out.println("Terminating publisher: " + pub.getName());
        try {
            if(connection != null){
                connection.close();
            }

            if(localoutputStream != null){
                localoutputStream.close();
            }
            if(localinputStream != null){
                localinputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

