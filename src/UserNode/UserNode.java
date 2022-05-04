package UserNode;

import Logging.ConsoleColors;
import NetworkUtilities.GeneralUtils;
import Tools.Messages;
import Tools.Topic;
import Tools.Tuple;
import Tools.Value;
import sun.nio.ch.ThreadPool;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserNode implements Serializable {

    private String ip;
    private int port;
    private String name;

    private ProfileName prof_name;
    private boolean exit;
    private boolean erroneousinput = true;
    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> BrokerIds = new ArrayList<>();
    private HashMap<String, ArrayList<Value>> message_list = new HashMap<>();
    private List<String> SubscribedTopics = new ArrayList<>();

    UserNode(String ip,int port,String name){
        this.ip = ip;
        this.port = port;
        this.name = name;
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


    public void tryagain(){connect();}

    public void connect(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::checkMessageList,0,10, TimeUnit.SECONDS);
        try{
            NetworkingForConsumer consumer;
            NetworkingForPublisher publisher;
            Thread t1;
            ArrayList<Thread> temp = new ArrayList<>();
            for (int i = 4; i < 7; i++) {
                consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, i);
                t1 = new Thread(consumer);
                t1.start();
                temp.add(t1);
            }
            for (int i = 0; i < temp.size(); i++)
            {
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
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 1,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 2:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 2,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 3:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 3,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 4:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        System.out.println("0.Publish message");
                        System.out.println("1.Publish file");
                        int operation = sc.nextInt();
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

                        }
                        NetworkingForPublisher publish = new NetworkingForPublisher(new Socket("192.168.1.5", 1235), this,topic_name,operation,con_file_name);
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

    public synchronized void addNewMessage(String topic_name, Value new_value) {
        if(message_list.containsKey(topic_name)) {
            ArrayList<Value> values = message_list.get(topic_name);
            for (int i = 0; i < values.size(); i++) {
                if(values.get(i).equals(new_value)){
                    return;
                }
            }
            message_list.get(topic_name).add(new_value);
        }else{
            message_list.put(topic_name,new ArrayList<Value>());
            message_list.get(topic_name).add(new_value);
        }
    }

    public synchronized void addNewSubscription(String topic_name){
        SubscribedTopics.add(topic_name);
    }

    public synchronized void removeSubscription(String topic_name){
        SubscribedTopics.remove(topic_name);
    }


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

        public void startNewConnection(Tuple<String,int[]> new_broker){
            System.out.println("The new IP is: " + new_broker.getValue1());
            String IP = new_broker.getValue1();
            System.out.println("The new port is: " + new_broker.getValue2()[1]);
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

        /**
         * uses the topic class to get the subscribed users and sends them the latest message
         */
        public void pull(String topic){

            //start a connection with the appropriate broker and ask it for the topic's message list and the topic itself
            while(true) {
                if (GeneralUtils.sendMessage(Messages.PULL, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                Integer message_broker = GeneralUtils.waitForNodePrompt(localinputStream,pull_request);
                if(message_broker == null){
                    shutdownConnection();
                    return;
                }else if(message_broker == Messages.FINISHED_OPERATION.ordinal()){
                    //System.out.println(ConsoleColors.PURPLE + "Received Finished Operation inside pull" + ConsoleColors.RESET);
                    break;
                }
            }
            while(true) {
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                Integer message_broker = GeneralUtils.waitForNodePrompt(localinputStream,pull_request);
                if(message_broker == null){
                    shutdownConnection();
                    return;
                }else if(message_broker == Messages.FINISHED_OPERATION.ordinal()){
                    //System.out.println(ConsoleColors.PURPLE + "Received Finished Operation inside pull" + ConsoleColors.RESET);
                    break;
                }
            }
            Integer message_broker = GeneralUtils.waitForNodePrompt(localinputStream,pull_request);
            if(message_broker == null){
                return;
            }else if(message_broker == Messages.I_AM_THE_CORRECT_BROKER.ordinal()){
                Topic temp_topic = (Topic) GeneralUtils.readObject(localinputStream,pull_request);
                if(temp_topic == null){
                    return;
                }else{
                    ArrayList<Value> messages = temp_topic.getMessage_queue();
                    for (Value val:messages) {
                        addNewMessage(topic,val);
                    }
                    System.out.println(message_list);
                }
            }else if(message_broker == Messages.I_AM_NOT_THE_CORRECT_BROKER.ordinal()) {
                //waiting for broker to send the index of the correct broker in the broker list
                //System.out.println("Receiving the index for the correct broker");
                Integer index;
                if ((index = GeneralUtils.waitForNodePrompt(localinputStream, pull_request)) == null) {
                    return;
                }
                //System.out.println("The index received is: " + index);
                startNewConnection(BrokerList.get(index));
            }

        }

        @Override
        public void run() {
            pull(topic);
        }

        public void shutdownConnection(){
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


    /**
     * Sends a pull request periodically to the corresponding broker port.
     */
    public void checkMessageList(){
        for(String topic: SubscribedTopics) {
            try {
                Pull_Request request = new Pull_Request(topic,new Socket("192.168.1.5",1234));
                Thread thread = new Thread(request);
                thread.start();
            } catch (IOException ioException) {
                System.out.println(ConsoleColors.RED + "Error constructing pull request for topic: " + topic + ConsoleColors.RESET);
            }
        }

    }


    public static void main(String[] args) {
        if(args.length <= 2){
            System.out.println("You didn't provide ip or port number or the name");
        }else {
            UserNode user = new UserNode(args[0], Integer.parseInt(args[1]),args[2]);
            user.connect();
            //user.checkMessageList();
        }
    }

}
