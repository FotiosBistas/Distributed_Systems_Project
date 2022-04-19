

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
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket server;
    private Socket connection;


    private String ip;
    private int port;


    private int id;



    public Broker(String ip,int port){
        this.ip = ip;
        this.port = port;
        writeBrokertoConfigFile("config.txt");
        this.id = SHA1.hextoInt(SHA1.encrypt(String.valueOf(port) + ip),300);
    }

    public void writeBrokertoConfigFile(String filename){
        try {
            //File write set true append mode
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename,true));
            writer.append(ip + " " + port + "\n");
            writer.close();
            System.out.println("Wrote Ip and port to the file");
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
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not connect");
                shutdownConnection();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Could not connect");
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
            try {
                System.out.println("Server established connection with client: " + connected_socket.getInetAddress().getHostAddress());
                int message;
                message = is.readInt();
                System.out.println("Received message from client: " + message);
                while(connected_socket.isConnected()) {
                    if(message == Messages.FINISHED_OPERATION.ordinal()){
                        System.out.println("Finished operation waiting for next input");
                        message = is.readInt();
                    } else if (message == Messages.GET_BROKER_LIST.ordinal()) {
                        System.out.println("Sending broker list");
                        File file = new File("config.txt");
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while((line = br.readLine()) != null){
                            String[] splitted = line.split("\\s+");
                            for (int i = 0; i < splitted.length - 1; i++) {
                                BrokerList.add(new Tuple<String,Integer>(splitted[0],Integer.valueOf(splitted[1])));
                            }
                        }
                        ous.writeInt(Messages.SENDING_BROKER_LIST.ordinal());
                        ous.flush();
                        for (int i = 0; i < BrokerList.size(); i++) {
                            System.out.println("Sending list size: " + BrokerList.size());
                            ous.writeUTF(String.valueOf(BrokerList.size()));
                            ous.flush();
                            System.out.println("Sending broker's IP: " + BrokerList.get(i).getValue1());
                            ous.writeUTF(BrokerList.get(i).getValue1());
                            ous.flush();
                            System.out.println("Sending broker's port: " + BrokerList.get(i).getValue2());
                            ous.writeUTF(String.valueOf(BrokerList.get(i).getValue2()));
                            ous.flush();
                        }
                        ous.writeInt(Messages.FINISHED_OPERATION.ordinal());
                        ous.flush();
                        message = is.readInt();
                        System.out.println("Finished sending brokers");
                    } else if (message == Messages.REGISTER.ordinal()) {
                        //TODO subscribe function
                        System.out.println("Serving register request for client: " + connection.getInetAddress().getHostName());
                        String topic_name = is.readUTF();
                        Consumer new_cons = (Consumer) is.readObject();
                        System.out.println("Topic name: " + topic_name);
                        System.out.println("Registering user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " to topic: " + topic_name);
                        //addConsumerToTopic(list_of_topics.get(list_of_topics.indexOf(topic_name)),new_cons);
                        //someone can subscribe and unsubscribe
                        ous.writeUTF("Send list size");
                        ous.flush();
                        int list_size = is.readInt();
                        //TODO call pull method
                        message = is.readInt();
                    } else if (message == Messages.PUSH.ordinal()){
                        //TODO call pull method
                        String topic = is.readUTF();
                        message = is.readInt();
                    } else if (message == Messages.PULL.ordinal()) {
                        message = is.readInt();
                    } else if (message == Messages.UNSUBSCRIBE.ordinal()) {
                        message = is.readInt();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection...");
                shutdownConnection();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Shutting down connection...");
                shutdownConnection();
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
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
