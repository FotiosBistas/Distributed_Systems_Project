package UserNode;

import Logging.ConsoleColors;
import NetworkUtilities.GeneralUtils;
import NetworkUtilities.UserNodeUtils;
import Tools.*;
import jdk.internal.loader.AbstractClassLoaderValue;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserNode implements Serializable {

    private String ip;
    private int port;
    private String name;



    private final String receive_data_path = "C:\\Users\\fotis\\OneDrive\\Desktop";
    private final String receive_files_directory;

    private String messages_directory;
    private String stories_directory;
    private String files_directory;

    private final String default_ip_address = "192.168.1.5";

    private boolean exit;
    private boolean erroneousinput = true;
    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> BrokerIds = new ArrayList<>();
    private final HashMap<String, ArrayList<MultimediaFile>> file_list = new HashMap<>();
    private final HashMap<String,ArrayList<Text_Message>> message_list = new HashMap<>();
    private final HashMap<String, ArrayList<Story>> story_list = new HashMap<>();
    private final List<String> SubscribedTopics = new ArrayList<>();

    UserNode(String ip,int port,String name){
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.receive_files_directory = this.receive_data_path + "\\receive_files\\";
        createDirectories();
    }

    private void createDirectories(){
        File dir = new File(receive_files_directory);
        Boolean was_created = false;
        if(!dir.exists()) {
            was_created = dir.mkdir();
            if (!was_created) {
                System.out.println(ConsoleColors.RED + "Directory: " + receive_files_directory + " was not created" + ConsoleColors.RESET);
                return;
            }
        }
        this.messages_directory = this.receive_files_directory + "\\messages\\";
        dir = new File(messages_directory);
        was_created = false;
        if(!dir.exists()) {
            was_created = dir.mkdir();
            if (!was_created) {
                System.out.println(ConsoleColors.RED + "Directory: " + messages_directory + " was not created" + ConsoleColors.RESET);
                return;
            }
        }
        this.stories_directory = this.receive_files_directory + "\\stories\\";
        dir = new File(stories_directory);
        was_created = false;
        if(!dir.exists()) {
            was_created = dir.mkdir();
            if (!was_created) {
                System.out.println(ConsoleColors.RED + "Directory: " + stories_directory + " was not created" + ConsoleColors.RESET);
                return;
            }
        }
        this.files_directory = this.receive_files_directory + "\\files\\";
        dir = new File(files_directory);
        was_created = false;
        if(!dir.exists()) {
            was_created = dir.mkdir();
            if (!was_created) {
                System.out.println(ConsoleColors.RED + "Directory: " + files_directory + " was not created" + ConsoleColors.RESET);
                return;
            }
        }
    }

    public void setBrokerList(ArrayList<Tuple<String,int[]>> BrokerList){
        this.BrokerList = BrokerList;
    }

    public void setBrokerIds(ArrayList<Integer> BrokerIds){
        this.BrokerIds = BrokerIds;
    }

    public List<Tuple<String,int[]>> getBrokerList(){
        return BrokerList;
    }

    public List<Integer> getBroker_ids(){return BrokerIds;}

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName(){return name;}


    private void tryagain(){connect();}

    /**
     * Responsible for opening connections and sending requests other than pull to the brokers. It uses Networking for consumer class and networking for publisher to achieve that.
     */
    public void connect(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
        executor.scheduleAtFixedRate(this::checkMessageList,0,1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(this::clearMessageQueue,0,30,TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(this::clearStoryQueue,0,30,TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(this::clearFileQueue,0,30,TimeUnit.SECONDS);
        try{
            NetworkingForConsumer consumer;
            NetworkingForPublisher publisher;
            Thread t1;
            ArrayList<Thread> temp = new ArrayList<>();
            for (int i = 4; i < 7; i++) {
                consumer = new NetworkingForConsumer(new Socket(default_ip_address, 1234), this, i);
                t1 = new Thread(consumer);
                t1.start();
                temp.add(t1);
            }
            //blocks until id list,broker list and send nickname finish
            for (int i = 0; i < temp.size(); i++) {
                temp.get(i).join();
            }
            erroneousinput = true;
            int userinput = -1;
            String topic_name;
            Scanner sc;
            while (!exit) {
                sc = new Scanner(System.in);
                while(erroneousinput) {
                    try {
                        System.out.println("1.Register to topic");
                        System.out.println("2.Unsubscribe from topic");
                        System.out.println("3.Show conversation data");
                        System.out.println("4.Push");
                        System.out.println("5.Exit");
                        System.out.println("Enter an int from the above options");
                        userinput = sc.nextInt();
                        erroneousinput = false;
                    } catch (InputMismatchException inputMismatchException) {
                        System.out.println(ConsoleColors.RED + "You gave a wrong input type try again" + ConsoleColors.RESET);
                        break;
                    }
                }
                if(erroneousinput){
                    continue;
                }
                switch (userinput) {
                    case 1:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket(default_ip_address, 1234), this, 1,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 2:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket(default_ip_address, 1234), this, 2,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 3:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket(default_ip_address, 1234), this, 3,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 4:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        boolean error = true;
                        int operation = -1;
                        while(error) {
                            sc = new Scanner(System.in);
                            try {
                                System.out.println("0.Publish message");
                                System.out.println("1.Publish file");
                                System.out.println("2.Publish story");
                                operation = sc.nextInt();
                                error = false;
                            } catch (InputMismatchException inputMismatchException) {
                                System.out.println(ConsoleColors.RED + "You gave a wrong input while trying to push" + ConsoleColors.RESET);
                                break;
                            }
                        }
                        if(error){
                            continue;
                        }
                        String con_file_name = null;
                        switch (operation){
                            case 0:
                                System.out.println("Give the contents of the message");
                                con_file_name = sc.next();
                                break;
                            case 1:
                                System.out.println("Give the file name");
                                con_file_name = sc.next();
                                break;
                            case 2:
                                System.out.println("Give the file name you want to publish as a story");
                                con_file_name = sc.next();
                                break;
                        }
                        NetworkingForPublisher publish = new NetworkingForPublisher(new Socket(default_ip_address, 1235), this,topic_name,operation,con_file_name);
                        Thread t = new Thread(publish);
                        t.start();
                        erroneousinput = true;
                        break;
                    case 5:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid Request... Try again");
                        erroneousinput = true;
                }
            }
        }catch(ConnectException e){
            System.out.println(ConsoleColors.RED + "No response from broker try again" + ConsoleColors.RESET);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryagain();
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Terminating client..." + ConsoleColors.RESET);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called in the pull method inside the user node utils class. Adds a new text message to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_text_message Accepts the text message object we received.
     */
    public synchronized void addNewMessage(String topic_name, Text_Message new_text_message) {
        if(message_list.containsKey(topic_name)) {
            ArrayList<Text_Message> values = message_list.get(topic_name);
            values.add(new_text_message);
        }else{
            message_list.put(topic_name,new ArrayList<Text_Message>());
            message_list.get(topic_name).add(new_text_message);
        }
    }

    /**
     * Called in the pull method inside the user node utils class. Adds a new story to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_story Accepts the story object we received.
     */
    public synchronized void addNewStory(String topic_name,Story new_story){
        if(story_list.containsKey(topic_name)) {
            ArrayList<Story> stories = story_list.get(topic_name);
            stories.add(new_story);
        }else{
            story_list.put(topic_name,new ArrayList<Story>());
            story_list.get(topic_name).add(new_story);
        }
    }

    /**
     * Called in the pull method inside the user node utils class. Adds a new file to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_file Accepts the file object we received.
     */
    public synchronized void addNewFile(String topic_name,MultimediaFile new_file){
        if(file_list.containsKey(topic_name)) {
            ArrayList<MultimediaFile> files = file_list.get(topic_name);
            files.add(new_file);
        }else{
            file_list.put(topic_name,new ArrayList<MultimediaFile>());
            file_list.get(topic_name).add(new_file);
        }
    }

    private synchronized void removeFromMessageQueue(String topic_name,Text_Message old_text_message){
        ArrayList<Text_Message> temp = message_list.get(topic_name);
        temp.remove(old_text_message);
    }

    private synchronized void removeFromFileQueue(String topic_name,MultimediaFile old_file){
        ArrayList<MultimediaFile> temp = file_list.get(topic_name);
        temp.remove(old_file);
    }

    private synchronized void removeFromStoryQueue(String topic_name,Story old_story){
        ArrayList<Story> temp = story_list.get(topic_name);
        temp.remove(old_story);
    }

    protected synchronized void addNewSubscription(String topic_name){
        if(!SubscribedTopics.contains(topic_name)) {
            SubscribedTopics.add(topic_name);
        }
    }

    protected synchronized void removeSubscription(String topic_name){
        if(SubscribedTopics.contains(topic_name)) {
            SubscribedTopics.remove(topic_name);
        }
    }

    /**
     * Writes the buffer array of each object chunk,calling the chunk.getChunk() method,to the specific file in the parameter list. The chunks are for the multimedia files or stories received using the pull request class.
     * @param chunks Accepts an array list containing chunk objects
     * @param file Accepts the file that the data is going to get written to.
     */
    private void writeChunks(ArrayList<Chunk> chunks,File file){
        try(FileOutputStream fos = new FileOutputStream (file)) {
            for (Chunk chunk : chunks) {
                System.out.println("Writing chunk: ");
                System.out.println(chunk);
                fos.write(chunk.getChunk(),0,chunk.getActual_length());
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Writes the metadata of a text message received by the pull request along with its contents.
     * @param message Accepts the text message that was received by the pull request.
     * @param file Accepts the file that the data is going to get written to.
     */
    private void writeTextMessage(Text_Message message,File file){
        try{
            System.out.println("Writing text message");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((message.getPublisher() + "\n").getBytes(StandardCharsets.UTF_8));
            fos.write((message.getDateCreated() + "\n").getBytes(StandardCharsets.UTF_8));
            fos.write((message.getContents() + "\n").getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Every interval that is defined in the connect() method this method clears the story queue and writes the stories in a specific directory.
     */
    private void clearStoryQueue() {
        System.out.println("Clearing story queue");
        for (Map.Entry<String,ArrayList<Story>> entry : story_list.entrySet()) {
            String topic = entry.getKey();
            ArrayList<Story> temp = entry.getValue();
            int i = 0;
            while(true) {
                if(i >= temp.size()){
                    break;
                }
                String file_name = temp.get(i).getIdentifier() + temp.get(i).getMultimediaFileName();


                String stories_topic_directory = stories_directory + topic;
                File dir = new File(stories_topic_directory);
                if (!dir.exists()) {
                    boolean was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + stories_topic_directory + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                String user_stories_directory = stories_topic_directory + "\\" + this.name + "\\";
                dir = new File(user_stories_directory);
                if (!dir.exists()) {
                    boolean was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + user_stories_directory + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                File file = new File(dir, file_name);
                System.out.println("Writing to directory: " + user_stories_directory);
                System.out.println("Writing story: " + file_name);
                try {
                    file.createNewFile();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                writeChunks(temp.get(i).getMultimediaFileChunk(), file);
                removeFromStoryQueue(topic, temp.get(i));
                i++;
            }
        }
    }

    /**
     * Every interval that is defined in the connect() method this method clears the file queue and writes the files in a specific directory.
     */
    private void clearFileQueue(){
        System.out.println("Clearing file queue");
        for(Map.Entry<String,ArrayList<MultimediaFile>> entry : file_list.entrySet()){
            String topic = entry.getKey();
            ArrayList<MultimediaFile> temp = entry.getValue();
            System.out.println(temp);
            int i = 0;
            while(true) {
                if(i >= temp.size()){
                    break;
                }

                String file_name = temp.get(i).getIdentifier() + temp.get(i).getMultimediaFileName();

                String topic_file_directory = this.files_directory + topic;
                File dir = new File(topic_file_directory);
                if(!dir.exists()){
                    boolean was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + topic_file_directory + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                String user_file_dir = topic_file_directory +  "\\" + this.name + "\\";
                dir = new File(user_file_dir);
                if(!dir.exists()){
                    boolean was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + user_file_dir + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                File file = new File(dir,file_name);
                System.out.println("Writing to directory: " + user_file_dir);
                System.out.println("Writing multimedia file: " + file_name);
                try {
                    file.createNewFile();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                writeChunks(temp.get(i).getMultimediaFileChunk(),file);
                removeFromFileQueue(topic,temp.get(i));
                i++;
            }
        }
    }

    /**
     * Every interval that is defined in the connect() method this method clears the file queue and writes the files in a specific directory.
     */
    private void clearMessageQueue(){
        System.out.println("Clearing message queue");
        for(Map.Entry<String,ArrayList<Text_Message>> entry : message_list.entrySet()){
            String topic = entry.getKey();
            ArrayList<Text_Message> temp = entry.getValue();
            int i = 0;
            while(true) {
                //declares the filename of the message
                //the unique identifier for the message is the filename
                String filename = temp.get(i).getIdentifier()  + ".txt";
                if(i >= temp.size()){
                    break;
                }
                boolean was_created = false;
                String topic_message_dir = messages_directory + topic;
                File dir = new File(topic_message_dir);
                if(!dir.exists()){
                    was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + topic_message_dir + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                String user_message_directory = topic_message_dir + "\\" + this.name + "\\";
                dir = new File(user_message_directory);
                if(!dir.exists()){
                    was_created = dir.mkdir();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Directory: " + user_message_directory + " was not created" + ConsoleColors.RESET);
                        return;
                    }
                }
                File file = new File(dir,filename);
                System.out.println("Writing to directory: " + user_message_directory);
                System.out.println("Writing text message: " + filename);
                try {
                    was_created = file.createNewFile();
                    if(!was_created){
                        System.out.println(ConsoleColors.RED + "Were not able to construct text message: " + filename + ConsoleColors.RESET);
                        return;
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                writeTextMessage(temp.get(i),file);
                removeFromMessageQueue(topic,temp.get(i));
                i++;
            }
        }
    }

    /**
     * For loops all the topics and sends a pull request to the first random broker that we choose. Then inside the pull request class it find the correct broker and servers the pull request.
     */
    private void checkMessageList(){
        for(String topic: SubscribedTopics) {
            //System.out.println(SubscribedTopics);
            try {
                Pull_Request request = new Pull_Request(topic,new Socket(default_ip_address,1234));
                Thread thread = new Thread(request);
                thread.start();
            } catch (IOException ioException) {
                System.out.println(ConsoleColors.RED + "Error constructing pull request for topic: " + topic + ConsoleColors.RESET);
            }
        }

    }

    /**
     * This class implements all the functionality needed for pulling new messages.
     */
    private class Pull_Request implements Runnable{

        private String topic;

        private Socket pull_request;
        private ObjectOutputStream localoutputStream;
        private ObjectInputStream localinputStream;


        Pull_Request(String topic,Socket socket){
            this.topic = topic;
            try{
                pull_request = socket;
                localoutputStream = new ObjectOutputStream(pull_request.getOutputStream());
                localinputStream = new ObjectInputStream(pull_request.getInputStream());
            }catch (SocketException socketException){
                System.out.println(ConsoleColors.RED + "Socket error in pull request" + ConsoleColors.RESET);
                shutdownConnection();
            } catch (IOException ioException) {
                System.out.println(ConsoleColors.RED + "IO error in pull request" + ConsoleColors.RESET);
                shutdownConnection();
            }
        }

        /**
         * If the topic that we want to pull data from is assigned to a different broker then we want to open a connection with that new broker.
         * @param new_broker Accepts the new broker that we want to establish a connection with.
         */
        private void startNewConnection(Tuple<String,int[]> new_broker){
            //System.out.println("The new IP is: " + new_broker.getValue1());
            String IP = new_broker.getValue1();
            //System.out.println("The new port is: " + new_broker.getValue2()[0]);
            Integer port = new_broker.getValue2()[0];
            Pull_Request new_request = null;
            try {
                new_request = new Pull_Request(topic,new Socket(IP,port));
                shutdownConnection();
            } catch (ConnectException connectException){
                System.out.println(ConsoleColors.RED + "Could not connect to the new broker" + ConsoleColors.RESET);
                shutdownConnection();
            } catch (IOException ioException) {
                System.out.println(ConsoleColors.RED + "IO error while trying to connect to the new broker" + ConsoleColors.RESET);
                shutdownConnection();
            }
            Thread t = new Thread(new_request);
            t.start();
        }


        @Override
        public void run() {
            Integer return_type = UserNodeUtils.pull(localoutputStream,localinputStream,pull_request,topic,UserNode.this);
            if(return_type == null){
                System.out.println(ConsoleColors.RED + "An error occured inside the run of the pull request" + ConsoleColors.RESET);
                return;

            }else if(return_type != -1) { // the pull method returned an index for the correct broker
                startNewConnection(UserNode.this.BrokerList.get(return_type));
            }
        }

        /**
         * Closes the streams and the socket.
         */
        private void shutdownConnection(){
            System.out.println(ConsoleColors.RED + "Shutting down connection in pull request" + ConsoleColors.RESET);
            try{
                if(pull_request != null){
                    pull_request.close();
                }
                if(localinputStream != null){
                    localinputStream.close();
                }
                if(localoutputStream != null){
                    localoutputStream.close();
                }
            }catch (SocketException socketException){
                System.out.println(ConsoleColors.RED + "Socket error while trying to shutdown connection. The socket might have been already closed");
            }catch (IOException ioException){
                System.out.println(ConsoleColors.RED + "Error while trying to shutdown connection.");
            }
        }
    }





    public static void main(String[] args) {
        if(args.length <= 2){
            System.out.println("You didn't provide ip or port number or the name");
        }else {
            UserNode user = new UserNode(args[0], Integer.parseInt(args[1]),args[2]);
            user.connect();
        }
    }

}
