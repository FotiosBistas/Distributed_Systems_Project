

package UserNode;

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
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e) {
            System.out.println("Error in constructor");
            e.printStackTrace();
            TerminatePublisherConnection();
        }
    }

    public void sendFile(MultimediaFile file) {

        try {
            ArrayList<Chunk> chunks = file.getChunks();
            System.out.println("Sending the file name: " + file.getMultimediaFileName());
            localoutputStream.writeUTF(file.getMultimediaFileName());
            localoutputStream.flush();
            System.out.println("Informing broker how many chunks there are: " + file.getChunks().size());
            localoutputStream.writeInt(file.getChunks().size());
            localoutputStream.flush();
            int offset = 0;
            /*System.out.println(i + " Chunk is being sent");
            localoutputStream.writeInt(i);
            localoutputStream.flush();
            System.out.println("Sending its actual length...");
            localoutputStream.writeInt(chunks.get(i).getActual_length());
            localoutputStream.flush();*/
            for (int i = 0; i < chunks.size(); i++) {
                System.out.println(i + " Chunk is being sent");
                localoutputStream.writeInt(i);
                localoutputStream.flush();
                System.out.println("Sending its actual length...");
                localoutputStream.writeInt(chunks.get(i).getActual_length());
                localoutputStream.flush();
                localoutputStream.write(chunks.get(i).getChunk(), 0, chunks.get(i).getActual_length());
                localoutputStream.flush();
            }
            //try to synchronize

        } catch (SocketException socketException) {
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in sending file");
            TerminatePublisherConnection();
        }
    }

    public void notifyBrokersNewMessage() {
        try {
            System.out.println("Notifying broker that there is a new message");
            localoutputStream.writeInt(Messages.NOTIFY.ordinal());
            localoutputStream.flush();
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e) {
            System.out.println("Terminating publisher in notify brokers new message...");
            e.printStackTrace();
            TerminatePublisherConnection();
        }
    }

    public void notifyFailure() {

    }

    /**
     * Receives topic list from the according broker.
     */

    public ArrayList<Topic> receiveTopicList() {
        try {
            ArrayList<Topic> topic_list = new ArrayList<>();
            int message_broker = localinputStream.readInt();
            if (message_broker != Messages.SENDING_TOPIC_LIST.ordinal()) {
                while (true) {
                    System.out.println("Requesting for broker list again");
                    localoutputStream.writeInt(Messages.GET_TOPIC_LIST.ordinal());
                    localoutputStream.flush();
                    message_broker = localinputStream.readInt();
                    if (message_broker == Messages.SENDING_TOPIC_LIST.ordinal()) {
                        System.out.println("Received sending topic list");
                        break;
                    }
                }
            }
            while (true) {
                System.out.println("Receiving topic list");
                //wait till you are sure that broker is sending the broker list
                System.out.println("Received message that the topic list is being sent and now accepting elements");
                System.out.println("Reading topic list size...");
                int size = localinputStream.readInt();
                System.out.println("The size of the list is: " + size);
                System.out.println("Reading topic...");
                Topic temp = (Topic) localinputStream.readObject();
                System.out.println("The topic is: " + temp);
                topic_list.add(temp);
                if (topic_list.size() >= size) {
                    System.out.println("Waiting to receive finished operation message from broker inside the receive topic list method");
                    message_broker = localinputStream.readInt();
                    if (message_broker == Messages.FINISHED_OPERATION.ordinal()) {
                        System.out.println("Received finished operation message from broker inside the receive topic list method");
                        break;
                    }
                }
            }

            return topic_list;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            TerminatePublisherConnection();
            return null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            TerminatePublisherConnection();
            return null;
        }
    }


    public void sendMessage(Messages message_type) {
        try {
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
        }catch(SocketException socketException){
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e){
            System.out.println("Error in send message");
            TerminatePublisherConnection();
        }
    }

    /**
     * Publisher gives the name of the topic it wants to publish data to.
     * After finding the proper broker it establishes a connection and publishes data to it.
     * Then after connecting to the right broker receives messages whether publisher is subscribed to topic and can publish data.
     * After that it notifies the broker that it has a new message and sends it through send file method.
     */
    public void push() {
        System.out.println("Requesting for proper broker from the connection");
        try {
            localoutputStream.writeInt(Messages.SEND_APPROPRIATE_BROKER.ordinal());
            localoutputStream.flush();

            System.out.println("Please give the name of the topic");
            String topic_name = sc.next();
            localoutputStream.writeUTF(topic_name);
            localoutputStream.flush();
            boolean subscribed_user = false;

            System.out.println("Waiting to receive finished operation message that the broker received the topic name");
            if (localinputStream.readInt() == Messages.FINISHED_OPERATION.ordinal()) {
                System.out.println("Broker received the topic name");
            }

            System.out.println("Waiting to receive correct broker message from the broker");
            int broker_message = localinputStream.readInt();
            if(Messages.I_AM_NOT_THE_CORRECT_BROKER.ordinal() == broker_message){
                //waiting for broker to send the index of the correct broker in the broker list
                System.out.println("Receiving the index for the correct broker");
                int index = localinputStream.readInt();
                System.out.println("The index received is: " + index);
                String IP = pub.getBrokerList().get(index).getValue1();
                int port = pub.getBrokerList().get(index).getValue2()[1];
                System.out.println("Received IP: " + IP + " and port: " + port);
                //establish new connection
                NetworkingForPublisher new_connection = new NetworkingForPublisher(new Socket(IP,port),pub,thread_continue);
                Thread t = new Thread(new_connection);
                t.start();
                System.out.println("Received the proper broker and i'm closing the connection with the old broker down");
                TerminatePublisherConnection();
                return;
            }
            FinishedOperation();
            System.out.println("Requesting for topic list from the broker");
            localoutputStream.writeInt(Messages.GET_TOPIC_LIST.ordinal());
            localoutputStream.flush();
            ArrayList<Topic> topics = receiveTopicList();
            System.out.println(topics);
            for (int i = 0; i < topics.size(); i++) {
                topics.get(i).printSubscribers();
                System.out.println("This publisher's name is: " + pub.getName());
                if (topics.get(i).getName().equals(topic_name)) {
                    System.out.println("Found the right topic: " + topics.get(i).getName());
                    if (topics.get(i).isUserSubscribed(pub.getName())) {
                        System.out.println("This publisher is subcribed to the topic");
                        subscribed_user = true;
                        break;
                    }
                }
            }
            if (subscribed_user) {
                notifyBrokersNewMessage();
                System.out.println("Give the name of the file");
                String filename = sc.next();
                thread_continue.notifyThread();
                MultimediaFile new_file = new MultimediaFile(filename, "Fotis");
                sendFile(new_file);
            } else {
                System.out.println("User is not subscribed to topic and can't post there");
                thread_continue.notifyThread();
                TerminatePublisherConnection();
                return;
            }
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            TerminatePublisherConnection();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void FinishedOperation(){
        try {
            localoutputStream.writeInt(Messages.FINISHED_OPERATION.ordinal());
            localoutputStream.flush();
        }catch(SocketException socketException){
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            TerminatePublisherConnection();
        }
    }

    public int waitForBrokerPrompt(){
        try {
            System.out.println("Waiting for Broker node prompt in publisher connection");
            return localinputStream.readInt();
        }catch(SocketException socketException){
            System.out.println("Socket error");
            TerminatePublisherConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            TerminatePublisherConnection();
        }
        return -1;
    }

    @Override
    public void run() {
        System.out.println("New publisher was created");
        push();
        TerminatePublisherConnection();
    }

    public void TerminatePublisherConnection(){
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

