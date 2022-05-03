

package UserNode;


import Logging.ConsoleColors;
import NetworkUtilities.UserNodeUtils;
import Tools.MultimediaFile;
import Tools.Text_Message;
import Tools.Tuple;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class NetworkingForPublisher implements Runnable {

    private Socket connection;
    private UserNode pub;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private boolean exit = false;
    private String topic_name;
    private int operation;
    private String contents_file_name;
    //idea here is that the user node will open a connection with the broker it wants to communicate and keep it for while
    //also its corresponding streams must be stored somewhere or not
    //private List<Tuple<ObjectInputStream,ObjectOutputStream>> streams = new ArrayList<>();
    //publisher doesn't need a while loop


    public NetworkingForPublisher(Socket connection, UserNode pub, String topic_name, int operation, String contents_file_name) {
        this.connection = connection;
        this.pub = pub;
        this.topic_name = topic_name;
        this.operation = operation;
        this.contents_file_name = contents_file_name;
        //connections.put(,connection);
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

    public void startNewConnection(Tuple<String,int[]> new_broker,int operation){
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
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub,0,contents_file_name)) == null) {
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
                if ((index = UserNodeUtils.push(localinputStream, localoutputStream, connection, topic_name, pub,1,contents_file_name)) == null) {
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
        }
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    public void shutdownConnection(){
        System.out.println("Terminating publisher: " + pub.getName());
        exit = true;
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

