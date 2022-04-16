import java.io.*;
import java.net.*;
import java.util.*;

//broker implements serializable due to the list of brokers
public class Broker implements Serializable{


    private List<Consumer> registeredUsers = new ArrayList<Consumer>();
    private List<Publisher> registeredPublishers = new ArrayList<Publisher>();


    private Queue<Tuple<Topic,Byte>> message_queue = new LinkedList<Tuple<Topic,Byte>>();
    private Queue<String> history = new LinkedList<String>();

    private List<Broker> BrokerList = new ArrayList<Broker>();
    private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



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

                out.writeUTF("Server established connection with client: " + socket.getInetAddress().getHostAddress());
                out.flush();
                String request = in.readUTF();
                if (request.equals("GetBrokerList")) {

                    out.writeObject(BrokerList);
                    out.flush();
                } else if (request.equals("Register")) {

                } else if (request.equals("Push")) {
                    String topic = in.readUTF();
                } else if (request.equals("Pull")) {

                } else if (request.equals("Unsubscribe")) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addConsumerToTopic(String topic_name, Consumer consumer){
        //this if condition checks whether there's an topic that the new consumer can subscribe to
        if(subscribedUsersToTopic.containsKey(topic_name)){
            Set<Consumer> set = subscribedUsersToTopic.get(topic_name);
            set.add(consumer);
            subscribedUsersToTopic.put(topic_name,set);
        }else{ // this is the case where the topic does not exist and the new topic must be inserted in the hash map
            Set<Consumer> set = new HashSet<Consumer>();
            set.add(consumer);
            subscribedUsersToTopic.put(topic_name,set);
        }
    }

    public void UnsubscribeFromTopic(String topic_name, Consumer consumer){
        if(subscribedUsersToTopic.containsKey(topic_name)){
            Set<Consumer> set = subscribedUsersToTopic.get(topic_name);
            set.remove(consumer);
            subscribedUsersToTopic.put(topic_name,set);
        }
    }

    public Set<Consumer> sendMessagesToConsumers(){
        //TODO check what happens with synchronization
        //TODO check how this will happen constantly
        while(!message_queue.isEmpty()){
            Tuple<Topic,Byte> chunk = message_queue.remove();
            String topic_name = chunk.getValue1().getName();
            Set<Consumer> Set_of_subscribers = subscribedUsersToTopic.get(topic_name);
            //take the subscribers send them the chuck
            return Set_of_subscribers;
        }
        return null;
    }

    public void notifyBrokersOnChanges(){

    }

    public void notifyPublisher(String topic){

    }

    public void pull(Topic topic){

    }

    public List<Broker> getBrokerList() { return BrokerList; }

    public Queue<Tuple<Topic, Byte>> getMessage_queue(){
        return message_queue;
    }

    public int getId() {
        return id;
    }

}