

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;

//broker implements serializable due to the list of brokers
public class Broker{

    private List<Consumer_Connection> Consumer_Connections = new ArrayList<>();
    private List<Publisher_Connection> Publisher_Connections = new ArrayList<>();

    private List<Topic> Topics = new ArrayList<>();
    private HashMap<Integer,ArrayList<Topic>> Brokers_Topics = new HashMap<>();
    private List<Tuple<String,MultimediaFile>> message_queue = new ArrayList<Tuple<String,MultimediaFile>>();

    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> id_list = new ArrayList<>();
    //private Map<String, Set<Consumer>> subscribedUsersToTopic = new HashMap<String,Set<Consumer>>();



    private ServerSocket consumer_service;
    private ServerSocket publisher_service;


    private String ip;
    private int consumer_port;
    private int publisher_port;
    private int id;



    public Broker(String ip,int consumer_port,int publisher_port){
        this.ip = ip;
        this.consumer_port = consumer_port;
        this.publisher_port = publisher_port;
        this.id = SHA1.hextoInt(SHA1.encrypt(String.valueOf(consumer_port) + String.valueOf(publisher_port) + ip),countLines()*100);
        writeBrokertoConfigFile("config.txt");
    }

    /*this method counts the brokers*/
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
        System.out.println(BrokerList);
        //TODO
        /* this would be probably better to be sent only to the user nodes and not happen internally inside the brokers*/
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
            if(temp.contains(ip + " " + consumer_port + " " + publisher_port)){
                System.out.println("Broker already exists in file");
            }else {
                writer.append(ip + " " + consumer_port + " " + publisher_port + "\n");
                writer.close();
                System.out.println("Wrote Ip and port to the file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readBrokerListFromConfigFile(){
        File file = new File("config.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while((line = br.readLine()) != null) {
                String[] splitted = line.split("\\s+");
                int [] array = new int[2];
                array[0] = Integer.valueOf(splitted[1]);
                array[1] = Integer.valueOf(splitted[2]);
                System.out.println("Inserted into array ports: " + array[0] + " and " + array[1]);
                BrokerList.add(new Tuple<String, int[]>(splitted[0], array));
                id_list.add(SHA1.hextoInt(SHA1.encrypt(String.valueOf(array[0]) + String.valueOf(array[1]) + ip),countLines()*100));

            }
            sortBrokerList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startBroker() {
        try {
            //consumer_service = new ServerSocket(consumer_port);
            //publisher_service = new ServerSocket(publisher_port);
            System.out.println("Broker with id: " + this.id + ",listens on port: " + this.consumer_port + " for subscriber services" + " and listens to port: " + this.publisher_port + " for publisher services");
            System.out.println("IP address: " + this.ip);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        consumer_service = new ServerSocket(consumer_port);
                        System.out.println("Opened thread to service consumer connections");
                        /* accepts all consumer connections on the predestined port*/
                        while (!consumer_service.isClosed()) {
                            Socket consumer_connection = consumer_service.accept();
                            Consumer_Connection consumer_handler = new Consumer_Connection(consumer_connection);
                            Thread t1 = new Thread(consumer_handler);
                            Consumer_Connections.add(consumer_handler);
                            t1.start();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                        System.out.println("Error in consumer service thread");
                        shutdownBroker();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        publisher_service = new ServerSocket(publisher_port);
                        /*accepts all publisher connection on the predestined port*/
                        System.out.println("Opened thread to receive publisher connections");
                        while (!publisher_service.isClosed()) {
                            Socket publisher_connection = publisher_service.accept();
                            Publisher_Connection publisher_handler = new Publisher_Connection(publisher_connection);
                            Thread t2 = new Thread(publisher_handler);
                            Publisher_Connections.add(publisher_handler);
                            t2.start();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                        System.out.println("Error in publisher service thread");
                        shutdownBroker();
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not open broker");
            shutdownBroker();
        }

    }


    private class Publisher_Connection implements Runnable{
        private Socket publisher_connection;
        private ObjectInputStream is;
        private ObjectOutputStream os;

        Publisher_Connection(Socket publisher_connection){
            this.publisher_connection = publisher_connection;
            try {
                is = new ObjectInputStream(publisher_connection.getInputStream());
                os = new ObjectOutputStream(publisher_connection.getOutputStream());
            } catch (IOException e) {
                System.out.println("Error in constructor shutting down connection...");
                e.printStackTrace();
                shutdownConnection();
            }
        }

        public void receiveFile(){

            try {
                int bytes = 0;
                System.out.println("Receiving file...");
                String file_name = is.readUTF();
                String new_file = file_name.substring(file_name.lastIndexOf("\\")+1);
                System.out.println("Received file: " + new_file);
                int number_of_chunks = is.readInt();
                System.out.println("You will receive: " + number_of_chunks + " chunks");
                String path_for_broker = new String("C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\");
                FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
                System.out.println("Receiving file...");
                byte[] buffer = new byte[512*1024];
                int offset = 0;
                int repetition = 0;
                ArrayList<byte[]> chunks = new ArrayList<>();
                //System.out.println("In while loop for receiving file");
                while(true) {
                    if(number_of_chunks == chunks.size()){
                        System.out.println("Finished receiving chunks: " + chunks.size());
                        fileOutputStream.close();
                        break;
                    }
                    int actual_size = is.readInt();
                    System.out.println("Actual size of the incoming chunk is: " + actual_size);
                    is.readFully(buffer,0,actual_size);
                    byte[] temp = buffer.clone();
                    fileOutputStream.write(temp,0,actual_size);
                    fileOutputStream.flush();
                    chunks.add(temp);
                    System.out.println("Sending received chunk ack");
                    os.writeInt(Messages.RECEIVED_CHUNK.ordinal());
                    os.flush();
                    while(true){
                        if(Messages.RECEIVED_ACK.ordinal() == is.readInt()){
                            System.out.println("Client received ack");
                            break;
                        }
                    }
                    System.out.println("Chunks size now is: " + chunks.size());
                }
                System.out.println("Finished receiving file");

            } catch (IOException e) {
                System.out.println("Shutting down in receive file...");
                e.printStackTrace();
                shutdownConnection();
            }
        }

        public int waitForUserNodePrompt(){
            try {
                System.out.println("Waiting for user node prompt in publisher connection");
                return is.readInt();
            } catch (IOException e) {
                System.out.println("Shutting down connection in wait for user node...");
                shutdownConnection();
            }
            return -1;
        }


        public void FinishedOperation(){
            try {
                os.writeInt(Messages.FINISHED_OPERATION.ordinal());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in finished operation...");
                shutdownConnection();
            }
        }

        @Override
        public void run() {
            System.out.println("Established connection with publisher: " + publisher_connection.getInetAddress());
            int message;
            message = waitForUserNodePrompt();
            System.out.println("Received message from publisher: " + message);
            while (publisher_connection.isConnected()) {
                if(message == Messages.FINISHED_OPERATION.ordinal()){
                    System.out.println("Finished operation waiting for publisher's next input");
                    message = waitForUserNodePrompt();
                }
                else if (message == Messages.NOTIFY.ordinal()) {
                    System.out.println("Notify message was received by publisher: " + publisher_connection.getInetAddress().getHostName());
                    receiveFile();
                    FinishedOperation();
                    message = waitForUserNodePrompt();
                }
            }

        }

        public void notifyPublisher(Topic topic){

        }

        public void removeConnection(){
            Publisher_Connections.remove(this);
        }

        public void shutdownConnection(){
            System.out.println("Shutted connection: " + publisher_connection.getInetAddress());
            removeConnection();
            try{
                if(os != null){
                    os.close();
                }
                if(is != null){
                    is.close();
                }
                if(publisher_connection != null){
                    publisher_connection.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private class Consumer_Connection implements Runnable {

        private ObjectInputStream is;
        private ObjectOutputStream ous;
        private Socket consumer_connection;


        public Consumer_Connection(Socket consumer_connection){
            try {
                this.consumer_connection = consumer_connection;
                ous = new ObjectOutputStream(consumer_connection.getOutputStream());
                is = new ObjectInputStream(consumer_connection.getInputStream());
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

        public void ServerUnsubscribeRequest(){
            try {
                System.out.println("Serving unsubscribe request for client: " + consumer_connection.getInetAddress().getHostName());
                String topic_name = is.readUTF();
                UserNode new_cons = (UserNode) is.readObject();
                System.out.println("Topic name: " + topic_name);
                System.out.println("Unsubscribing user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " from topic: " + topic_name);
                UnsubscribeFromTopic(Topics.get(Topics.indexOf(topic_name)), new_cons);
            }catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in unsubscribe");
                shutdownConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in unsubscribe");
                shutdownConnection();
            }
        }

        public void ServeRegisterRequest(){
            //TODO subscribe function
            try {
                System.out.println("Serving register request for client: " + consumer_connection.getInetAddress().getHostName());
                String topic_name = is.readUTF();
                UserNode new_cons = (UserNode) is.readObject();
                System.out.println("Topic name: " + topic_name);
                System.out.println("Registering user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " to topic: " + topic_name);
                addConsumerToTopic(Topics.get(Topics.indexOf(topic_name)), new_cons);
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
                System.out.println("Waiting for user node prompt in consumer connection");
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
                for (Tuple<String,int[]> val: BrokerList) {
                    System.out.println("Sending Broker List size: " + BrokerList.size());
                    ous.writeInt(BrokerList.size());
                    ous.flush();
                    System.out.println("Sending broker's IP: " + val.getValue1());
                    ous.writeUTF(val.getValue1());
                    ous.flush();
                    int i;
                    for (i = 0; i < val.getValue2().length; i++) {
                        System.out.println("Sending broker's port: " + val.getValue2()[i]);
                        ous.writeInt(val.getValue2()[0]);
                        ous.flush();
                        ous.writeInt(val.getValue2()[1]);
                        ous.flush();
                    }
                    if(i == 2) {
                        System.out.println("Finished sending ports");
                        FinishedOperation();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Shutting down connection in send broker list...");
                shutdownConnection();
            }
        }

        public void sendIdList(){
            try{
                System.out.println("Sending ID List");
                ous.writeInt(Messages.SENDING_ID_LIST.ordinal());
                ous.flush();
                for (int i = 0; i < id_list.size(); i++) {
                    System.out.println("Sending ID List size: " + id_list.size());
                    ous.writeInt(id_list.size());
                    ous.flush();
                    System.out.println("Sending ID: " + id_list.get(i));
                    ous.writeInt(id_list.get(i));
                    ous.flush();
                }
            }catch(IOException e){
                System.out.println("Error in sending list");
                e.printStackTrace();
                shutdownConnection();
            }
        }

        public void sendTopicList(){

        }

        public void removeConnection(){
            Consumer_Connections.remove(this);
        }

        public void shutdownConnection(){
            System.out.println("Shutted connection: " + consumer_connection.getInetAddress());
            removeConnection();
            try{
                if(ous != null){
                    ous.close();
                }
                if(is != null){
                    is.close();
                }
                if(consumer_connection != null){
                    consumer_connection.close();
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
            System.out.println("Server established connection with client: " + consumer_connection.getInetAddress().getHostAddress());
            int message;
            message = waitForUserNodePrompt();
            System.out.println("Received message from client: " + message);
            while (consumer_connection.isConnected()) {
                if (message == Messages.FINISHED_OPERATION.ordinal()) {
                    System.out.println("Finished operation waiting for next input");
                    message = waitForUserNodePrompt();
                } else if (message == Messages.GET_BROKER_LIST.ordinal()) {
                    if(BrokerList.isEmpty()){
                        readBrokerListFromConfigFile();
                    }
                    sendBrokerList();
                    FinishedOperation();
                    System.out.println("Finished sending brokers");
                    sendIdList();
                    FinishedOperation();
                    System.out.println("Finished sending id list");
                    message = waitForUserNodePrompt();
                } else if (message == Messages.REGISTER.ordinal()) {
                    ServeRegisterRequest();
                    //TODO call pull method
                    FinishedOperation();
                    message = waitForUserNodePrompt();
                } else if (message == Messages.UNSUBSCRIBE.ordinal()) {
                    ServerUnsubscribeRequest();
                    FinishedOperation();
                    message = waitForUserNodePrompt();
                }
            }

        }

        public Socket getSocket(){
            return consumer_connection;
        }

    }

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

    /* below methods accept the topic name from the input streams and find the corresponding object */
    public void addConsumerToTopic(Topic topic, UserNode consumer){
        //this if condition checks whether there's an topic that the new consumer can subscribe to
        if(Topics.contains(topic)){
            topic.addSubscription(consumer);
        }else{ // this is the case where the topic does not exist and the new topic must be inserted in the hash map
            Topics.add(topic);
            topic.addSubscription(consumer);
        }
    }

    public void UnsubscribeFromTopic(Topic topic, UserNode consumer){
        if(Topics.contains(topic)){
            topic.removeSubscription(consumer);
        }
    }



    public void notifyBrokersOnChanges(){

    }



    //public List<Broker> getBrokerList() { return BrokerList; }

    public List<Tuple<String, MultimediaFile>> getMessage_queue(){
        return message_queue;
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args) {
        if(args.length <= 2) {
            System.out.println("You did not provide an ip address or a port numbers");
        }
        else {
            String ip = args[0];
            int consumer_port = Integer.valueOf(args[1]);
            int service_port = Integer.valueOf(args[2]);
            Broker broker = new Broker(ip, consumer_port,service_port);
            broker.startBroker();
        }
    }

}
