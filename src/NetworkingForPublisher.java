
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NetworkingForPublisher implements Runnable {

    private Socket connection;
    private UserNode pub;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean exit = false;
    private NetworkingForConsumer thread_continue;
    //idea here is that the user node will open a connection with the broker it wants to communicate and keep it for while
    //also its corresponding streams must be stored somewhere or not
    //private List<Tuple<ObjectInputStream,ObjectOutputStream>> streams = new ArrayList<>();
    //publisher doesn't need a while loop

    private Scanner sc = new Scanner(System.in);

    public NetworkingForPublisher(Socket connection,UserNode pub,NetworkingForConsumer thread_continue){
        this.connection = connection;
        this.pub = pub;
        this.thread_continue = thread_continue;
        //connections.put(,connection);
        try {
            os = new ObjectOutputStream(connection.getOutputStream());
            is = new ObjectInputStream(connection.getInputStream());
        }catch(IOException e){
            System.out.println("Error in constructor");
            e.printStackTrace();
            TerminatePublisherConnection();
        }
    }

    public void sendFile(MultimediaFile file) {

        try {
            ArrayList<Chunk> chunks = file.getChunks();
            System.out.println("Sending the file name: " + file.getMultimediaFileName());
            os.writeUTF(file.getMultimediaFileName());
            os.flush();
            System.out.println("Informing broker how many chunks there are: " + file.getChunks().size());
            os.writeInt(file.getChunks().size());
            os.flush();
            int offset = 0;
            for (int i = 0; i < chunks.size(); i++) {
                System.out.println(i + " Chunk is being sent");
                System.out.println("Sending its actual length...");
                os.writeInt(chunks.get(i).getActual_length());
                os.flush();
                os.write(chunks.get(i).getChunk(),0,chunks.get(i).getActual_length());
                os.flush();
                while(true){
                    if(Messages.RECEIVED_CHUNK.ordinal() == is.readInt()){
                        System.out.println("Acknowledgement that chunked was received");
                        System.out.println("Sending ack for ack message");
                        os.writeInt(Messages.RECEIVED_ACK.ordinal());
                        os.flush();
                        break;
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Error in sending file");
            TerminatePublisherConnection();
        }
    }

    public void notifyBrokersNewMessage(){
        try{
            System.out.println("Notifying broker that there is a new message");
            os.writeInt(Messages.NOTIFY.ordinal());
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating publisher in notify brokers new message...");
            e.printStackTrace();
            TerminatePublisherConnection();
        }
    }

    public void notifyFailure(){

    }

    public void getTopicList(){

    }

    public void push(){
        System.out.println("Please give the name of the topic");
        String topic_name = sc.next();
        //TODO find the proper broker for the topic by direct broker communication
        //TODO first check into your own topic list
        //Tuple<String,int[]> brk = pub.hashTopic(topic_name);
        ArrayList<Topic> topics = new ArrayList<>();//TODO method for getting topics;
        boolean subscribed_user = false;
        for (int i = 0; i < topics.size(); i++) {
            if(topics.get(i).getName().equals(topic_name)){
                if(topics.get(i).isUserSubscribed(pub.getName())){
                    subscribed_user = true;
                    break;
                };
            }
        }
        if(subscribed_user) {
            notifyBrokersNewMessage();
            System.out.println("Give the name of the file");
            String filename = sc.next();
            thread_continue.notifyThread();
            MultimediaFile new_file = new MultimediaFile(filename, "Fotis");
            sendFile(new_file);
        }else{
            System.out.println("User is not subscribed to topic and can't post there");
        }
    }

    public void FinishedOperation(){
        try {
            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            TerminatePublisherConnection();
        }
    }

    public int waitForBrokerPrompt(){
        try {
            System.out.println("Waiting for Broker node prompt in publisher connection");
            return is.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            TerminatePublisherConnection();
        }
        return -1;
    }

    @Override
    public void run() {
        push();
        FinishedOperation();
        int messageFromBroker = waitForBrokerPrompt();
        while(true) {
            if(messageFromBroker == Messages.FINISHED_OPERATION.ordinal()) {
                System.out.println("Received finished operation in publisher run");
                TerminatePublisherConnection();
                break;
            }
        }
    }

    public void TerminatePublisherConnection(){
        System.out.println("Terminating publisher: " + pub.getName());
        exit = true;
        try {
            if(connection != null){
                connection.close();
            }

            if(os != null){
                os.close();
            }
            if(is != null){
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

