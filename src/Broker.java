

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

//broker implements serializable due to the list of brokers
public class Broker{

    // private List<UserNode> ConnectedUsers = new ArrayList<UserNode>();
    private List<Thread> Connections = new ArrayList<>();

    private List<Topic> list_of_topics = new ArrayList<Topic>();
    private List<Tuple<String,Byte>> message_queue = new ArrayList<Tuple<String,Byte>>();

    private List<Tuple<String,Integer>> BrokerList = new ArrayList<Tuple<String,Integer>>();
    private List<Integer> id_list = new ArrayList<>();
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket server;
    private Socket connection;


    private String ip;
    private int port;


    private int id;



    public Broker(String ip,int port){
        this.ip = ip;
        this.port = port;
        this.id = SHA1.hextoInt(SHA1.encrypt(String.valueOf(port) + ip),300);
        writeBrokertoConfigFile("config.txt");
    }

    public void writeBrokertoConfigFile(String filename){
        try {
            //File write set true append mode
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename,true));
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            Set<String> temp = new HashSet<String>();
            while((line = reader.readLine()) != null){
                temp.add(line);
            }
            if(temp.contains(ip + " " + port)){
                System.out.println("Broker already exists in file");
            }else {
                line = ip + " " + port + "\n";
                writer.append(line);
                writer.close();
                System.out.println("Wrote Ip and port to the file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sortBrokerList(){
        int[] indexes = new int[id_list.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        int n = id_list.size();
        int temp = 0;
        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if(id_list.get(j-1) > id_list.get(j)){
                    //swap elements
                    temp = id_list.get(j-1);
                    id_list.set(j-1,id_list.get(j));
                    id_list.set(j,temp);
                    //while we do that change the index position
                    temp = indexes[j-1];
                    indexes[j-1] = indexes[j];
                    indexes[j] = temp;
                }
            }
        }
        List<Tuple<String,Integer>> temp_list = new ArrayList<>();
        for (int i = 0; i < indexes.length; i++) {
            temp_list.add(BrokerList.get(indexes[i]));
            if(i == 0){
                id_list.set(i,0);
            }else {
                id_list.set(i, i * 100 - 1);
            }
        }
        BrokerList = temp_list;
    }

    public void readBrokerListFromConfigFile(){
        File file = new File("config.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                String[] splitted = line.split("\\s+");
                BrokerList.add(new Tuple<String, Integer>(splitted[0], Integer.valueOf(splitted[1])));
                id_list.add(Integer.valueOf(splitted[2]));
            }
            sortBrokerList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startBroker() {
        try {
            server = new ServerSocket(port);
            System.out.println("Broker with id: " + this.id + ",listens on port: " + this.port);
            System.out.println("IP address: " + this.ip);
            while(!server.isClosed()){
                connection = server.accept();
                Thread action = new ActionsForBroker(connection);
                Connections.add(action);
                action.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not open broker");
            shutdownBroker();
        }

    }


    private class ActionsForBroker extends Thread {

        private ObjectInputStream is;
        private ObjectOutputStream ous;
        private Socket connected_socket;


        public ActionsForBroker(Socket connection){
            try {
                ous = new ObjectOutputStream(connection.getOutputStream());
                is = new ObjectInputStream(connection.getInputStream());
                connected_socket = connection;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not connect");
                shutdownConnection();
            }
        }

        public void ServerUnsubscribeRequest(){
            try {
                System.out.println("Serving unsubscribe request for client: " + connected_socket.getInetAddress().getHostName());
                String topic_name = is.readUTF();
                Consumer new_cons = (Consumer) is.readObject();
                System.out.println("Topic name: " + topic_name);
                System.out.println("Unsubscribing user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " from topic: " + topic_name);
                UnsubscribeFromTopic(list_of_topics.get(list_of_topics.indexOf(topic_name)), new_cons);
            }catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in unsubscribe");
                shutdownConnection();
            }
        }

        public void ServeRegisterRequest(){
            //TODO subscribe function
            try {
                System.out.println("Serving register request for client: " + connection.getInetAddress().getHostName());
                String topic_name = is.readUTF();
                Consumer new_cons = (Consumer) is.readObject();
                System.out.println("Topic name: " + topic_name);
                System.out.println("Registering user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " to topic: " + topic_name);
                addConsumerToTopic(list_of_topics.get(list_of_topics.indexOf(topic_name)), new_cons);
                //someone can subscribe and unsubscribe
                ous.writeUTF("Send list size");
                ous.flush();
                int list_size = is.readInt();
            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
                System.out.println("Shutting down connection in register...");
                shutdownConnection();
            }
        }

        public int waitForUserNodePrompt(){
            try {
                System.out.println("Waiting for user node prompt");
                return is.readInt();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in wait for user node...");
                shutdownConnection();
            }
            return -1;
        }


        public void FinishedOperation(){
            try {
                ous.writeInt(Messages.FINISHED_OPERATION.ordinal());
                ous.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in finished operation...");
                shutdownConnection();
            }
        }


        public void sendBrokerList(){
            try {
                System.out.println("Sending broker list");
                ous.writeInt(Messages.SENDING_BROKER_LIST.ordinal());
                ous.flush();
                for (Tuple<String,Integer> val: BrokerList) {
                    System.out.println("Sending list size: " + BrokerList.size());
                    ous.writeUTF(String.valueOf(BrokerList.size()));
                    ous.flush();
                    System.out.println("Sending broker's IP: " + val.getValue1());
                    ous.writeUTF(val.getValue1());
                    ous.flush();
                    System.out.println("Sending broker's port: " + val.getValue2());
                    ous.writeUTF(String.valueOf(val.getValue2()));
                    ous.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in send broker list...");
                shutdownConnection();
            }
        }

        public void removeConnection(){
            Connections.remove(this);
        }

        public void shutdownConnection(){
            System.out.println("Shutted connection: " + connected_socket.getInetAddress());
            removeConnection();
            try{
                if(ous != null){
                    ous.close();
                }
                if(is != null){
                    is.close();
                }
                if(connected_socket != null){
                    connected_socket.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            HandleRequest();
        }


        public void HandleRequest() {
            System.out.println("Server established connection with client: " + connected_socket.getInetAddress().getHostAddress());
            int message;
            message = waitForUserNodePrompt();
            System.out.println("Received message from client: " + message);
            while (connected_socket.isConnected()) {
                if (message == Messages.FINISHED_OPERATION.ordinal()) {
                    System.out.println("Finished operation waiting for next input");
                    message = waitForUserNodePrompt();
                } else if (message == Messages.GET_BROKER_LIST.ordinal()) {
                    readBrokerListFromConfigFile();
                    sendBrokerList();
                    FinishedOperation();
                    System.out.println("Finished sending brokers");
                    message = waitForUserNodePrompt();
                } else if (message == Messages.REGISTER.ordinal()) {
                    ServeRegisterRequest();
                    //TODO call pull method
                    message = waitForUserNodePrompt();
                } else if (message == Messages.PUSH.ordinal()) {
                    //TODO call pull method
                    message = waitForUserNodePrompt();
                } else if (message == Messages.PULL.ordinal()) {
                    message = waitForUserNodePrompt();
                } else if (message == Messages.UNSUBSCRIBE.ordinal()) {
                    ServerUnsubscribeRequest();
                    FinishedOperation();
                    message = waitForUserNodePrompt();
                }
            }

        }
        public Socket getSocket(){
            return connected_socket;
        }

    }

    public void shutdownBroker(){
        System.out.println("Shutting down broker with id: " + this.id);
        try {
            if(server != null) {
                server.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            return new Tuple<Set<Consumer>,Byte>(Set_of_subscribers, chunk.getValue2());
        }
        return null;
    }

    public void notifyBrokersOnChanges(){

    }

    public void notifyPublisher(Topic topic){

    }

    public void pull(Topic topic){

    }

    //public List<Broker> getBrokerList() { return BrokerList; }

    public List<Tuple<String, Byte>> getMessage_queue(){
        return message_queue;
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args) {
        if(args[0] == null) {
            System.out.println("You did not provide an ip address");
        }else if(args[1] == null){
            System.out.println("You did not provide a port number");
        }
        else {
            String ip = args[0];
            int port = Integer.valueOf(args[1]);
            Broker broker = new Broker(ip, port);
            broker.startBroker();
        }
    }

}
