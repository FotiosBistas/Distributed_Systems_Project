
package Broker;

import Logging.ConsoleColors;
import Tools.*;
import UserNode.UserNode;
import SHA1.SHA1;


import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class  Broker{

    private final List<Consumer_Handler> consumer_Handlers = new ArrayList<>();
    private final List<Publisher_Handler> publisher_Handlers = new ArrayList<>();

    AliveBroker multicast_alive_message = null;

    //this list is matching in indexes with the Broker list
    private final Boolean[] alive_brokers = new Boolean[3];
    private final String[] LastTimeAlive = new String[3];

    private final List<Topic> Topics = new ArrayList<>();
    private final HashMap<Integer,ArrayList<Topic>> Topics_From_Other_Brokers = new HashMap<>();

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private final List<Integer> id_list = new ArrayList<>();

    private ServerSocket consumer_service;
    private ServerSocket publisher_service;
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

    public Boolean[] getAlive_brokers() {
        return alive_brokers;
    }

    public String[] getLastTimeAlive() {
        return LastTimeAlive;
    }

    public List<Consumer_Handler> getConsumer_Handlers() {
        return consumer_Handlers;
    }
    public List<Publisher_Handler> getPublisher_Handlers() {
        return publisher_Handlers;
    }

    public List<Topic> getTopics() {
        return Topics;
    }

    public List<Tuple<String, int[]>> getBrokerList() {
        return BrokerList;
    }

    public List<Integer> getId_list() {
        return id_list;
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


    /**
     * Receives a value class type and inserts it to the corresponding queue present in the topic.
     * @param val Accepts the value class type (story,multimediafile,text_message).
     * @param topic_name Accepts the topic.
     */
    public void addToMessageQueue(Value val,String topic_name) {
        if (val instanceof Story) {
            System.out.println(ConsoleColors.PURPLE + "Trying to insert story: " + val + " into the message list of the topic: " + topic_name);
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
            temp.addToStoryQueue((Story) val);
        } else if (val instanceof MultimediaFile) {
            System.out.println(ConsoleColors.PURPLE + "Trying to insert value: " + val + "into the file list of the topic: " + topic_name);
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
            temp.addToFileQueue((MultimediaFile) val);

        } else if (val instanceof Text_Message) {
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
            temp.addToMessageQueue((Text_Message) val);
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
                //change the local id to the new one for better distribution of topics
                //this is used in the inter broker communications class
                int id = id_list.get(i);
                id_list.set(i,0);
                if(id == this.id){
                    this.id = id_list.get(i);
                }
            }else {
                //change the local id to the new one for better distribution of topics
                //this is used in the inter broker communications class
                int id = id_list.get(i);
                id_list.set(i,i*100);
                if(id == this.id){
                    this.id = id_list.get(i);
                }
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


    /**
     * Starts two separate threads for each possible service.
     * The two types of service are: Consumer service, Publisher service
     */
    public void startBroker() {
        try {
            System.out.println("Broker with id: " + this.id + ",listens on port: " + this.consumer_port + " for subscriber services" + " and listens to port: " + this.publisher_port + " for publisher services");
            System.out.println("IP address: " + this.ip);
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

            //start the object to initiate broker communications.
            this.multicast_alive_message = new AliveBroker(this);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not open broker");
            shutdownBroker();
        }

    }

    /**
     * Shutdown broker and close all the corresponding services
     */
    private void shutdownBroker(){
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
     * Creates topic if the topic that was received by the consumer is not present in the broker's topic list.
     * This is done when subscribe is called by the user node.
     * @param topic_name The name of the topic.
     * @param consumer The consumer that tried to subscribe to the topic.
     */
    public void createTopic(String topic_name,String consumer){
       Topic new_topic = new Topic(topic_name);
       executor.scheduleAtFixedRate(new_topic::checkExpiredStories,0,20, TimeUnit.SECONDS);
       Topics.add(new_topic);
       System.out.println("Created new topic: " + topic_name);
       addConsumerToTopic(new_topic,consumer);
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
     * Inserts a new topic that was created in another broker so in case the broker dies this broker(or some other) can serve the requests for the topic.
     * @param id Accepts the id of the broker that sent the topic.
     * @param topic Accepts the topic from the broker.
     */
    public void addNewTopicReceivedFromOtherBroker(int id,Topic topic){
        if(Topics_From_Other_Brokers.containsKey(id)){
            ArrayList<Topic> topic_list_of_broker = Topics_From_Other_Brokers.get(id);
            for (int i = 0; i < topic_list_of_broker.size(); i++) {
                if(topic_list_of_broker.get(i).getName().equals(topic.getName())) {
                    //if topic is in the topic list change the local topic to another topic
                    int index = topic_list_of_broker.indexOf(topic);
                    System.out.println("index is: " + index);
                    System.out.println("old topic: " + topic_list_of_broker.get(i));
                    topic_list_of_broker.add(index,topic);
                    System.out.println("old topic: " + topic_list_of_broker.get(i));
                    return;
                }
            }
            //if topic doesn't exist in the broker array list
            System.out.println("Doesn't exist");
            Topics_From_Other_Brokers.get(id).add(topic);
        }else{
            System.out.println("Lone beast case");
            Topics_From_Other_Brokers.put(id,new ArrayList<>());
            Topics_From_Other_Brokers.get(id).add(topic);
        }
    }

    /**
     * Hashes the topic with its name and returns the broker that will serve the request.
     * @param topic first parameter is the topic name
     * @return returns the index of the broker in the broker list. Returns null if an error occurs.
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

    public void addValueTypeFromOtherBroker(Value value,int id,String topic_name) {
        if(value instanceof Story){

        }else if(value instanceof MultimediaFile){

        }else if(value instanceof Text_Message){

        }
    }
}
