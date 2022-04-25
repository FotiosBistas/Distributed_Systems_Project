

import java.io.*;
import java.net.*;
import java.util.*;

//broker implements serializable due to the list of brokers
public class Broker{

    private List<Consumer_Handler> consumer_Handlers = new ArrayList<>();
    private List<Publisher_Handler> publisher_Handlers = new ArrayList<>();
    private List<UserNode> registeredUsers = new ArrayList<>();


    private List<Topic> Topics = new ArrayList<>();
    private HashMap<Integer,ArrayList<Topic>> Brokers_Topics = new HashMap<>();
    private HashMap<String, Value> message_queue = new HashMap<>();

    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> id_list = new ArrayList<>();
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket consumer_service;
    private ServerSocket publisher_service;
    private ServerSocket broker_listener_service;
    private Socket connection_to_other_brokers;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;

    private final String ip;
    private final int consumer_port;
    private final int publisher_port;
    private final int broker_port;
    private int id;
    private int number_of_brokers;


    public Broker(String ip,int consumer_port,int publisher_port,int broker_port){
        this.ip = ip;
        this.consumer_port = consumer_port;
        this.publisher_port = publisher_port;
        this.broker_port = broker_port;
        writeBrokertoConfigFile("config.txt");
        this.id = SHA1.hextoInt(SHA1.encrypt(consumer_port + publisher_port + broker_port + ip),(countLines())*100);
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

    public HashMap<String, Value> getMessage_queue() {
        return message_queue;
    }

    public void setMessage_queue(HashMap<String, Value> message_queue) {
        this.message_queue = message_queue;
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

    public ServerSocket getBroker_listener_service() {
        return broker_listener_service;
    }

    public void setBroker_listener_service(ServerSocket broker_listener_service) {
        this.broker_listener_service = broker_listener_service;
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

    public int getBroker_port() {
        return broker_port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * counts lines from the config file to properly hash the new broker and give it the correct id.
     * @return the number of brokers which are the counted lines
     */
    public int countLines(){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
            int lines = 0;
            while(reader.readLine() != null){
                lines++;
            }
            reader.close();
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while reading from config file...");
            shutdownBroker();
        }
        return 0;
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
     * writes the newly created broker in the config file
     * @param filename give the relative file name
     */
    public void writeBrokertoConfigFile(String filename){
        try {
            //File write set true append mode
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename,true));
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            Set<String> temp = new HashSet<>();
            while((line = reader.readLine()) != null){
                temp.add(line);
            }
            if(temp.contains(ip + " " + consumer_port + " " + publisher_port + " " + broker_port)){
                System.out.println("Broker already exists in file");
            }else {
                writer.append(ip + " " + consumer_port + " " + publisher_port + " " + broker_port + "\n");
                writer.close();
                System.out.println("Wrote Ip and port to the file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while writing to config file");
        }

    }

    /**
     * Reads broker list from config file and inserts it into local field broker list.
     * Inserts the according ids of the brokers to the broker list.
     * Also calls the sort operation on the broker list.
     */
    public void readBrokerListFromConfigFile(){
        File file = new File("config.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while((line = br.readLine()) != null) {
                String[] splitted = line.split("\\s+");
                int [] array = new int[3];
                array[0] = Integer.parseInt(splitted[1]);
                array[1] = Integer.parseInt(splitted[2]);
                array[2] = Integer.parseInt(splitted[3]);
                System.out.println("Inserted into array ports: " + array[0] + " for consumer connections," + array[1] + " for publisher connections and " + array[2] + " for broker connections");
                BrokerList.add(new Tuple<String, int[]>(splitted[0], array));
                System.out.println("Broker list size now is: " + BrokerList.size());
                id_list.add(SHA1.hextoInt(SHA1.encrypt(array[0] + array[1] + array[2] + ip),countLines()*100));

            }
            sortBrokerList();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while reading config file");
        }
    }

    /**
     * Starts three separate threads for each possible service.
     * The three types of service are: Consumer service, Publisher service and Broker service
     */
    public void startBroker() {
        try {
            System.out.println("Broker with id: " + this.id + ",listens on port: " + this.consumer_port + " for subscriber services" + " and listens to port: " + this.publisher_port + " for publisher services");
            System.out.println("It also listens to port: " + this.broker_port + " for broker communication");
            System.out.println("IP address: " + this.ip);

            /*listener for receiving messages from other brokers*/
            new Thread(() -> {
                try {
                    broker_listener_service = new ServerSocket(broker_port);
                    System.out.println("Opened thread to receive broker connections");
                    while(!broker_listener_service.isClosed()){
                        Socket broker_connection = broker_listener_service.accept();
                        Broker_Handler broker_handler = new Broker_Handler(broker_connection,Broker.this);
                        Thread t3 = new Thread(broker_handler);
                        t3.start();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Could not open listener service for brokers");
                    shutdownBroker();
                }
            }).start();

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
            /* after starting everything up notify brokers that there is a new broker*/
            notifyBrokersOnChanges(Messages.NEW_BROKER);

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
            if(broker_listener_service != null){
                broker_listener_service.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * adds the subscriber to the subscribed users set in the topic class
     * @param topic give a topic class instance
     * @param consumer give a usernode class instance
     */
    public void addConsumerToTopic(Topic topic, UserNode consumer){
        //this if condition checks whether there's an topic that the new consumer can subscribe to
        if(Topics.contains(topic)){
            topic.addSubscription(consumer);
        }else{ // this is the case where the topic does not exist and the new topic must be inserted in the hash map
            Topics.add(topic);
            topic.addSubscription(consumer);
        }
    }

    /**
     * remove subscriber from the set of subscribers inside the topic class
     * @param topic give a topic class instance
     * @param consumer give a usernode class instance
     */
    public void UnsubscribeFromTopic(Topic topic, UserNode consumer){
        if(Topics.contains(topic)){
            topic.removeSubscription(consumer);
        }
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

    public void tryagain(Messages message_type){
        notifyBrokersOnChanges(message_type);
    }

    /**
     *After a change e.g. new broker is inserted into the list or a topic is inserted into the topic list.
     *Notify all the brokers about the changes, like a broadcast operation.
     */
    public void notifyBrokersOnChanges(Messages message_type){
        if(BrokerList.size() <=1){
            return;
        }
        try {
            for (Tuple<String, int[]> val : BrokerList) {
                boolean same_broker = false;
                if(val.getValue1().equals(this.ip)){
                        if(this.consumer_port == val.getValue2()[0]){
                            same_broker = true;
                        }
                }
                if(same_broker){
                    continue;
                }
                //opens connection to all other brokers and sends them the new data
                System.out.println(val.getValue2()[2]);
                connection_to_other_brokers = new Socket(val.getValue1(), val.getValue2()[2]);
                localoutputStream = new ObjectOutputStream(connection_to_other_brokers.getOutputStream());
                localinputStream = new ObjectInputStream(connection_to_other_brokers.getInputStream());
                if(message_type == Messages.NEW_BROKER){
                    System.out.println("Sending message type: " + Messages.WAITING_FOR_ACK + " with ordinal number " + Messages.WAITING_FOR_ACK.ordinal());
                    localoutputStream.writeInt(Messages.WAITING_FOR_ACK.ordinal());
                    localoutputStream.flush();
                    while(true){
                        System.out.println("Waiting to receive ACK");
                        if(localinputStream.readInt() == Messages.ACK.ordinal()){
                            break;
                        }
                    }
                    Shared_Network_Methods.sendBrokerList(Broker.this);
                    Shared_Network_Methods.sendIdList(Broker.this);
                    shutdownConnection();
                }else if(message_type == Messages.NEW_TOPIC){
                    Shared_Network_Methods.sendTopicList();
                    shutdownConnection();
                }
            }
        }catch (ConnectException e){
            System.out.println("The socket is not open on the other end");
            e.printStackTrace();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryagain(message_type);
        } catch (IOException e){
            System.out.println("Error while trying to notify other brokers");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void FinishedOperation(){
        try {
            localoutputStream.writeInt(Messages.FINISHED_OPERATION.ordinal());
            localoutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            shutdownConnection();
        }
    }



    public static void main(String[] args) {
        if(args.length <= 3) {
            System.out.println("You did not provide an ip address or appropriate port numbers");
        }
        else {
            String ip = args[0];
            int consumer_port = Integer.parseInt(args[1]);
            int service_port = Integer.parseInt(args[2]);
            int broker_port = Integer.parseInt(args[3]);
            Broker broker = new Broker(ip, consumer_port,service_port,broker_port);
            broker.startBroker();
        }
    }

}
