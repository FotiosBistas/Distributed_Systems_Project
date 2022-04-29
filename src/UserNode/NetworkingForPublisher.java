

package UserNode;

import NetworkUtilities.UserNodeUtils;
import Tools.Messages;
import Tools.MultimediaFile;
import Tools.Chunk;
import Tools.Topic;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NetworkingForPublisher implements Runnable {

    private Socket connection;
    private UserNode pub;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private BufferedOutputStream bufferedOutputStream;
    private BufferedInputStream bufferedInputStream;
    private boolean exit = false;
    private NetworkingForConsumer thread_continue;
    //idea here is that the user node will open a connection with the broker it wants to communicate and keep it for while
    //also its corresponding streams must be stored somewhere or not
    //private List<Tuple<ObjectInputStream,ObjectOutputStream>> streams = new ArrayList<>();
    //publisher doesn't need a while loop

    private Scanner sc = new Scanner(System.in);

    public NetworkingForPublisher(Socket connection, UserNode pub, NetworkingForConsumer thread_continue) {
        this.connection = connection;
        this.pub = pub;
        this.thread_continue = thread_continue;
        //connections.put(,connection);
        try {
            localoutputStream = new ObjectOutputStream(connection.getOutputStream());
            localinputStream = new ObjectInputStream(connection.getInputStream());
            bufferedInputStream = new BufferedInputStream(connection.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
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



    @Override
    public void run() {
        System.out.println("New publisher was created");
        UserNodeUtils.push(localinputStream,localoutputStream,connection,sc,pub,thread_continue);
        shutdownConnection();
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

