package UserNode;

import Logging.ConsoleColors;
import Tools.Tuple;
import Tools.Value;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;

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
            Thread t1;
            Scanner sc = new Scanner(System.in);
            int userinput = -1;
            while (!exit) {
                while(erroneousinput) {
                    sc = new Scanner(System.in);
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
                        // threads for consumer requests and responses from the first random broker
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 3);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 4:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 4);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 5:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 5);
                        t1 = new Thread(consumer);
                        t1.start();
                        erroneousinput = true;
                        break;
                    case 6:
                        consumer = new NetworkingForConsumer(new Socket("192.168.1.5", 1234), this, 6);
                        t1 = new Thread(consumer);
                        t1.start();
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


    /**
     * Sends a pull request periodically to the corresponding broker port.
     */
    public void checkMessageList(){
        new Thread(()->{
            while (true) {

            }
        }).start();
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
