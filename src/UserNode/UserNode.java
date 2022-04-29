package UserNode;
import Tools.Value;
import Tools.Tuple;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserNode implements Serializable {

    private String ip;
    private int port;
    private String name;
    private ProfileName prof_name;


    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> BrokerIds = new ArrayList<>();
    private HashMap<String,Value> message_list;

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
            // threads for consumer requests and responses from the first random broker
            NetworkingForConsumer consumer = new NetworkingForConsumer(new Socket("192.168.1.5",1234),this);
            Thread t1 = new Thread(consumer);
            t1.start();

        }catch(ConnectException e){
            System.out.println("No response from broker try again");
            try {
                Thread.sleep(40000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryagain();
        } catch (IOException e) {
            System.out.println("Terminating client...");
            e.printStackTrace();
        }
    }

    public synchronized void addNewMessage(String topic_name, Value new_value){
        message_list.put(topic_name,new_value);
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
            user.checkMessageList();
        }
    }

}
