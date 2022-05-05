
package Broker;

import Logging.ConsoleColors;
import Tools.Story;
import Tools.Topic;
import Tools.Tuple;
import Tools.Value;
import UserNode.UserNode;
import SHA1.SHA1;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

//broker implements serializable due to the list of brokers
public class  Broker{

    private List<Consumer_Handler> consumer_Handlers = new ArrayList<>();
    private List<Publisher_Handler> publisher_Handlers = new ArrayList<>();
    private List<UserNode> registeredUsers = new ArrayList<>();


    private List<Topic> Topics = new ArrayList<>();
    private HashMap<Integer,ArrayList<Topic>> Brokers_Topics = new HashMap<>();

    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> id_list = new ArrayList<>();
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket consumer_service;
    private ServerSocket publisher_service;
    private Socket connection_to_other_brokers;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;

    private final String ip;
    private final int consumer_port;
    private final int publisher_port;
    private Integer id;



    public Broker(String ip,int consumer_port , int publisher_port){
        this.ip = ip;
        this.consumer_port = consumer_port;
        this.publisher_port = publisher_port;
        if((this.id = SHA1.hextoInt(SHA1.encrypt(consumer_port + publisher_port + ip),300)) == null){
            System.out.println("\033[0;31m" + "Error while constructing broker" + "\033[0m");
            return;
        }
        readBrokerListFromConfigFile();
    }

    public List<Consumer_Handler> getConsumer_Handlers() {
        return consumer_Handlers;
    }

    public void setConsumer_Handlers(List<Consumer_Handler> consumer_Handlers) {
        this.consumer_Handlers = consumer_Handlers;
    }

    public List<Publisher_Handler> getPublisher_Handlers() {
        return publisher_Handlers;
    }

    public void setPublisher_Handlers(List<Publisher_Handler> publisher_Handlers) {
        this.publisher_Handlers = publisher_Handlers;
    }

    public List<UserNode> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(List<UserNode> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public List<Topic> getTopics() {
        return Topics;
    }

    public void setTopics(List<Topic> topics) {
        Topics = topics;
    }

    public HashMap<Integer, ArrayList<Topic>> getBrokers_Topics() {
        return Brokers_Topics;
    }

    public void setBrokers_Topics(HashMap<Integer, ArrayList<Topic>> brokers_Topics) {
        Brokers_Topics = brokers_Topics;
    }

    public List<Tuple<String, int[]>> getBrokerList() {
        return BrokerList;
    }

    public void setBrokerList(List<Tuple<String, int[]>> brokerList) {
        BrokerList = brokerList;
    }

    public List<Integer> getId_list() {
        return id_list;
    }

    public void setId_list(List<Integer> id_list) {
        this.id_list = id_list;
    }

    public ServerSocket getConsumer_service() {
        return consumer_service;
    }

    public void setConsumer_service(ServerSocket consumer_service) {
        this.consumer_service = consumer_service;
    }

    public ServerSocket getPublisher_service() {
        return publisher_service;
    }

    public void setPublisher_service(ServerSocket publisher_service) {
        this.publisher_service = publisher_service;
    }

    public Socket getConnection_to_other_brokers() {
        return connection_to_other_brokers;
    }

    public void setConnection_to_other_brokers(Socket connection_to_other_brokers) {
        this.connection_to_other_brokers = connection_to_other_brokers;
    }

    public ObjectOutputStream getLocaloutputStream() {
        return localoutputStream;
    }

    public void setLocaloutputStream(ObjectOutputStream localoutputStream) {
        this.localoutputStream = localoutputStream;
    }

    public ObjectInputStream getLocalinputStream() {
        return localinputStream;
    }

    public void setLocalinputStream(ObjectInputStream localinputStream) {
        this.localinputStream = localinputStream;
    }

    public String getIp() {
        return ip;
    }

    public int getConsumer_port() {
        return consumer_port;
    }

    public int getPublisher_port() {
        return publisher_port;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void addToMessageQueue(Value val,String topic_name){
        if(val instanceof Story){
            System.out.println(ConsoleColors.PURPLE + "Trying to insert story: " + val + "into the message list of the topic: " + topic_name);
            Topic temp = null;
            for (Topic topic : Topics) {
                if (topic.getName().equals(topic_name)) {
                    temp = topic;
                    System.out.println(ConsoleColors.BLUE + "Found the topic" + ConsoleColors.RESET);
                    break;
                }
            }
            if (temp == null) {
                System.out.println(ConsoleColors.RED + "No topic with the name: " + topic_name + " was found" + ConsoleColors.RESET);
                return;
            }
            temp.addToStoryQueue((Story)val);
            System.out.println(temp.getStory_queue());
        }else {
            System.out.println(ConsoleColors.PURPLE + "Trying to insert value: " + val + "into the message list of the topic: " + topic_name);
            Topic temp = null;
            for (Topic topic : Topics) {
                if (topic.getName().equals(topic_name)) {
                    temp = topic;
                    System.out.println(ConsoleColors.BLUE + "Found the topic" + ConsoleColors.RESET);
                    break;
                }
            }
            if (temp == null) {
                System.out.println(ConsoleColors.RED + "No topic with the name: " + topic_name + " was found" + ConsoleColors.RESET);
                return;
            }
            temp.addToMessageQueue(val);
            System.out.println(temp.getMessage_queue());
        }
    }

    /**
     * Sorts the broker list using bubblesort.
     * The mechanic behind this is an index list that changes according to the ids of the brokers.
     */
    public void sortBrokerList(){
        int[] indexes = new int[id_list.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        int n = id_list.size();
        int temp;
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
        /* spread the keys evenly to the brokers */
        List<Tuple<String,int[]>> temp_list = new ArrayList<>();
        for (int i = 0; i < indexes.length; i++) {
            temp_list.add(BrokerList.get(indexes[i]));
            if(i == 0){
                id_list.set(i,0);
            }else {
                id_list.set(i, i * 100);
            }
        }
        BrokerList = temp_list;
        System.out.println(BrokerList);
    }


    /**
     * Reads broker list from config file and inserts it into local field broker list.
     * Inserts the according ids of the brokers to the broker list.
     * Also calls the sort operation on the broker list.
     */
    public void readBrokerListFromConfigFile(){
        File file = new File("C:\\Users\\fotis\\IdeaProjects\\DSproject\\src\\Broker\\config.txt");
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while((line = br.readLine()) != null) {
                String[] splitted = line.split("\\s+");
                System.out.println(Arrays.toString(splitted));
                int [] array = new int[2];
                array[0] = Integer.parseInt(splitted[1]);
                array[1] = Integer.parseInt(splitted[2]);
                System.out.println("Inserted into array ports: " + array[0] + " for consumer connections," + array[1] + " for publisher connections");
                BrokerList.add(new Tuple<String, int[]>(splitted[0], array));
                System.out.println("Broker list size now is: " + BrokerList.size());
                id_list.add(SHA1.hextoInt(SHA1.encrypt(array[0] + array[1] + ip),300));
                System.out.println("Running in while loop");
            }
            sortBrokerList();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while reading config file");
        }
    }

    //public void CreateTopics(){
    //    Topic topic1 = new Topic("Distributed_Systems");
    //    Topic topic2 = new Topic("Operating_Systems");
    //    Topic topic3 = new Topic("Programming with python");
    //    Topic topic4 = new Topic("C++");
    //    int index;
    //    index = hashTopic(topic1.getName());
        
    //}

    /**
     * Starts three separate threads for each possible service.
     * The three types of service are: Consumer service, Publisher service and Broker service
     */
    public void startBroker() {
        try {
            System.out.println("Broker with id: " + this.id + ",listens on port: " + this.consumer_port + " for subscriber services" + " and listens to port: " + this.publisher_port + " for publisher services");
            System.out.println("IP address: " + this.ip);
            Topic topic1 = new Topic("DSystems");
            Topic topic2 = new Topic("OSystems");
            Topic topic3 = new Topic("Programmingpython");
            Topic topic4 = new Topic("C++");
            Topics.add(topic1);
            Topics.add(topic2);
            Topics.add(topic3);
            Topics.add(topic4);
            /* separate thread for receiving consumer connections */
            new Thread(() -> {
                try {
                    consumer_service = new ServerSocket(consumer_port);
                    System.out.println("Opened thread to service consumer connections");
                    /* accepts all consumer connections on the predestined port*/
                    while (!consumer_service.isClosed()) {
                        Socket consumer_connection = consumer_service.accept();
                        Consumer_Handler consumer_handler = new Consumer_Handler(consumer_connection,Broker.this);
                        Thread t1 = new Thread(consumer_handler);
                        consumer_Handlers.add(consumer_handler);
                        t1.start();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                    System.out.println("Error in consumer service thread");
                    shutdownBroker();
                }
            }).start();


            /* separate thread for receiving publisher connections*/
            new Thread(() -> {
                try {
                    publisher_service = new ServerSocket(publisher_port);
                    /*accepts all publisher connection on the predestined port*/
                    System.out.println("Opened thread to receive publisher connections");
                    while (!publisher_service.isClosed()) {
                        Socket publisher_connection = publisher_service.accept();
                        Publisher_Handler publisher_handler = new Publisher_Handler(publisher_connection,Broker.this);
                        Thread t2 = new Thread(publisher_handler);
                        publisher_Handlers.add(publisher_handler);
                        t2.start();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                    System.out.println("Error in publisher service thread");
                    shutdownBroker();
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not open broker");
            shutdownBroker();
        }

    }

    /**
     * shutdown broker and close all the corresponding services
     */
    public void shutdownBroker(){
        System.out.println("Shutting down broker with id: " + this.id);
        try {
            if(consumer_service != null) {
                consumer_service.close();
            }
            if(publisher_service != null){
                publisher_service.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * adds the subscriber to the subscribed users set in the topic class
     * @param topic give a topic class instance
     * @param consumer give a string that is the name of the consumer
     */
    public  void addConsumerToTopic(Topic topic, String consumer){
        //this if condition checks whether there's an topic that the new consumer can subscribe to
        if (Topics.contains(topic)) {
            System.out.println(ConsoleColors.PURPLE + "Topic is in topic list and now subscribing consumer: " + consumer + ConsoleColors.RESET);
            topic.addSubscription(consumer);
        } else { // this is the case where the topic does not exist and the new topic must be inserted in the hash map
            Topics.add(topic);
            topic.addSubscription(consumer);
        }
        System.out.println("Subscribed users: ");
        System.out.println(Topics.get(Topics.indexOf(topic)).getSubscribedUsers());
    }

    /**
     * remove subscriber from the set of subscribers inside the topic class
     * @param topic give a topic class instance
     * @param consumer give a usernode class instance
     */
    public void UnsubscribeFromTopic(Topic topic, String consumer){
        if(Topics.contains(topic)){
            System.out.println(ConsoleColors.PURPLE + "Topic is in topic list and now unsubscribing consumer: " + consumer + ConsoleColors.RESET);
            topic.removeSubscription(consumer);
        }
        System.out.println("Subscribed users: ");
        System.out.println(Topics.get(Topics.indexOf(topic)).getSubscribedUsers());
    }


    /**
     * Shutdowns socket connection in broker that tried to communicate with other brokers.
     * Also shutdowns the corresponding streams.
     */
    public void shutdownConnection(){
        System.out.println("Shutting down communication socket between brokers");
        try {
            if (localinputStream != null) {
                localinputStream.close();
            }
            if(localoutputStream != null){
                localoutputStream.close();
            }
            if(connection_to_other_brokers != null){
                connection_to_other_brokers.close();
            }
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Error in shutting down connection");
        }
    }

    /**
     * Hashes the topic with its name and returns the broker that will serve the request.
     * @param topic first parameter is the topic name
     * @return returns a tuple instance of string, int[]
     */
    public Integer hashTopic(String topic){
        // hash the topic and choose the correct broker
        Integer identifier;
        if((identifier = SHA1.hextoInt(SHA1.encrypt(topic),BrokerList.size()*100)) == null){
            System.out.println("\033[0;31m" + "Error while hashing topic: " + topic +  "\033[0m");
            return null;
        }
        System.out.println("The identifier for the topic: " + topic + " is: " + identifier);
        Integer index = null;
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0 ; i < id_list.size() ; i++){

            if(i == 0){
                if (id_list.get(i) < identifier && identifier < id_list.get(i) + 100) {
                    index = i;
                    break;
                }
            }else {
                if (id_list.get(i) + 1 < identifier && identifier < id_list.get(i) + 100) {
                    index = i;
                    break;
                }
            }
        }
        if(index == null){
            index = id_list.size() - 1;
        }
        System.out.println("The correct Broker is: " + Arrays.toString(BrokerList.get(index).getValue2()) + " and id: " + id_list.get(index));
        return index;
    }

    public static void main(String[] args) {
        if(args.length <= 2) {
            System.out.println("You did not provide an ip address or appropriate port numbers");
        }
        else {
            String ip = args[0];
            int consumer_port = Integer.parseInt(args[1]);
            int service_port = Integer.parseInt(args[2]);
            Broker broker = new Broker(ip, consumer_port,service_port);
            broker.startBroker();
        }
    }

}
