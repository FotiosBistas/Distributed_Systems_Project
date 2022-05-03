package UserNode;

import Logging.ConsoleColors;
import NetworkUtilities.GeneralUtils;
import Tools.Messages;
import Tools.Topic;
import Tools.Tuple;
import Tools.Value;

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
    private HashMap<String, Value> message_list;
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
        try{
            NetworkingForConsumer consumer;
            NetworkingForPublisher publisher;
            Thread t1;
            int userinput = -1;
            String topic_name;
            Scanner sc;
            while (!exit) {
                sc = new Scanner(System.in);
                while(erroneousinput) {
                    try {
                        System.out.println("0.Send Broker List");
                        System.out.println("1.Send ID list");
                        System.out.println("2.Send Nickname");
                        System.out.println("3.Register to topic");
                        System.out.println("4.Unsubscribe from topic");
                        System.out.println("5.Show conversation data");
                        System.out.println("6.Push");
                        System.out.println("7.Exit");
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
                    case 0:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 0);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 1:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 1);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 2:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 2);
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
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 4,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 5:
                        System.out.println("Give the topic name...");
                        topic_name = sc.next();
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 5,topic_name);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 6:
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
                    case 7:
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
                Thread.sleep(40000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryagain();
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Terminating client..." + ConsoleColors.RESET);
            e.printStackTrace();
        }
    }

    public synchronized void addNewMessage(String topic_name, Value new_value) {
        message_list.put(topic_name, new_value);
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

        public void startNewConnection(Tuple<String,int[]> new_broker,int operation){
            System.out.println("The new IP is: " + new_broker.getValue1());
            String IP = new_broker.getValue1();
            System.out.println("The new port is: " + new_broker.getValue2()[1]);
            Integer port = new_broker.getValue2()[1];
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
            if(GeneralUtils.sendMessage(Messages.PULL,localoutputStream) == null){
                shutdownConnection();
                return;
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
        while(true) {
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
