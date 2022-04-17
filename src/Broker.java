import java.io.*;
import java.net.*;
import java.util.*;

//broker implements serializable due to the list of brokers
public class Broker implements Serializable{

    private List<UserNode> ConnectedUsers = new ArrayList<UserNode>();
    private List<Thread> Connections = new ArrayList<Thread>();

    private List<Topic> list_of_topics = new ArrayList<Topic>();
    private List<Tuple<String,Byte>> message_queue = new ArrayList<Tuple<String,Byte>>();

    private List<Broker> BrokerList = new ArrayList<Broker>();
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket server;
    private Socket connection;


    private String ip;
    private int port;


    private final int  maxBrokers = 3;
    private int id = 0;

    public Broker(String ip,int port){
        this.ip = ip;
        this.port = port;
        this.id = SHA1.hextoInt(SHA1.encrypt(String.valueOf(port) + ip),maxBrokers);
    }

    public void startBroker() {
        try {
            server = new ServerSocket(port);
            while(true){
                connection = server.accept();
                Thread action = new ActionsForBroker(connection);
                Connections.add(action);
                action.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ActionsForBroker extends Thread {

        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket connected_socket;

        public ActionsForBroker(Socket connection){
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connected_socket = connection;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            HandleRequest();
        }


        public void HandleRequest() {
            try {
                out.writeUTF("Server established connection with client: " + connected_socket.getInetAddress().getHostAddress());
                out.flush();
                String message;
                while((message = (in.readUTF())) != null) {
                    if (message.equals("GetBrokerList")) {
                        out.writeObject(BrokerList);
                        out.flush();
                    } else if (message.equals("Register")) {
                        //TODO subscribe function
                        String topic_name = in.readUTF();
                        Consumer new_cons = (Consumer) in.readObject();
                        addConsumerToTopic(list_of_topics.get(list_of_topics.indexOf(topic_name)),new_cons);
                        //someone can subscribe and unsubscribe 
                        out.writeUTF("Send list size");
                        out.flush();
                        while(in.read() == 0){}
                        int list_size = in.readInt();
                        //TODO call pull method
                    } else if (message.equals("Push")){
                        //TODO call pull method
                        String topic = in.readUTF();
                    } else if (message.equals("Pull")) {

                    } else if (message.equals("Unsubscribe")) {

                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /* below methods accept the topic name from the input streams and find the corresponding object */
    public void addConsumerToTopic(Topic topic, Consumer consumer){
        //this if condition checks whether there's an topic that the new consumer can subscribe to
        if(list_of_topics.contains(topic)){
            topic.addSubscription(consumer);
        }else{ // this is the case where the topic does not exist and the new topic must be inserted in the hash map
            list_of_topics.add(topic);
            topic.addSubscription(consumer);
        }
    }

    public void UnsubscribeFromTopic(Topic topic, Consumer consumer){
        if(list_of_topics.contains(topic)){
            topic.removeSubscription(consumer);
        }
    }

    public Tuple<Set<Consumer>,Byte> pull(Topic topic,int consumer_list_size){
        //pull function is called when the consumer registers for a first time to a topic
        //and when there is a new message available from the publisher
        //if the list that the consumers hold is smaller than the list of messages it needs to receive new messages
        //because the consumers hold messages only for the topics that they are interested we need the message q
        if(consumer_list_size<message_queue.size()){
            Tuple<String,Byte> chunk = message_queue.get(consumer_list_size);
            Set<Consumer> Set_of_subscribers = topic.getSubscribedUsers();
            //take the subscribers send them the chuck
            Tuple<Set<Consumer>,Byte> new_tuple = new Tuple<Set<Consumer>,Byte>(Set_of_subscribers, chunk.getValue2());
            return new_tuple;
        }
        return null;
    }

    public void notifyBrokersOnChanges(){

    }

    public void notifyPublisher(Topic topic){

    }

    public void pull(Topic topic){

    }

    public List<Broker> getBrokerList() { return BrokerList; }

    public List<Tuple<Topic, Byte>> getMessage_queue(){
        return message_queue;
    }

    public int getId() {
        return id;
    }

}