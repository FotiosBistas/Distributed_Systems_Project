
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
    //idea here is that the user node will open a connection with the broker it wants to communicate and keep it for while
    //also its corresponding streams must be stored somewhere or not
    //private List<Tuple<ObjectInputStream,ObjectOutputStream>> streams = new ArrayList<>();
    //publisher doesn't need a while loop

    private Scanner sc = new Scanner(System.in);

    public NetworkingForPublisher(Socket connection,UserNode pub){
        this.connection = connection;
        this.pub = pub;
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
        ArrayList<byte[]> chunks = file.getChunks();
        try {
            os.writeUTF(file.getMultimediaFileName());
            os.flush();
            for (int i = 0; i < chunks.size(); i++) {
                os.write(chunks.get(i));
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        // find the proper broker for the topic
        //Tuple<String,int[]> brk = pub.hashTopic(topic_name);
        //find the position of the broker inside the array list in order to get its id
        //can't use index of here
        //int index = -1;
        //for (int i = 0; i < pub.getBrokerList().size(); i++) {
        //    if(pub.getBrokerList().get(i).getValue1().equals(brk.getValue1()) && pub.getBrokerList().get(i).getValue2()[1] == brk.getValue2()[1] && pub.getBrokerList().get(i).getValue2()[0] == brk.getValue2()[0]){
        //        index = i;
        //        break;
        //    }
        //}
        // start a new connection with the broker
        //we can't use contains here
        //TODO check if i can use comparable for the tuple class
        //Socket connection = new Socket(brk.getValue1(),brk.getValue2()[1]);
        //call send file on a existing multimedia file object or a create one
        //also notify the proper broker that you have a new message
        notifyBrokersNewMessage();
        System.out.println("Give the name of the file");
        String filename = sc.next();
        MultimediaFile new_file = new MultimediaFile(filename,"Fotis");
        sendFile(new_file);
    }

    @Override
    public void run() { push();}

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

