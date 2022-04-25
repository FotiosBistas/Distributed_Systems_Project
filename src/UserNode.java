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
    private List<Tuple<String,Byte>> message_list;

    UserNode(String ip,int port){
        this.ip = ip;
        this.port = port;
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
            consumer.BrokerResponses();
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


    public Tuple<String, int[]> hashTopic(String topic){
        // hash the topic and choose the correct broker
        int identifier = SHA1.hextoInt(topic,BrokerList.size()*100);
        int index = 0;
        for(int i = 0 ; i < BrokerIds.size() ; i++){
            // when you find the first broker that has id larger than the topics value then you use the previous broker
            if(BrokerIds.get(i) > identifier){
                if(i == 0){
                    break;
                }
                index = i - 1;
                break;
            }
        }
        return BrokerList.get(index);
    }

    public static void main(String[] args) {
        if(args.length <= 1){
            System.out.println("You didn't provide ip or port number");
        }else {
            UserNode user = new UserNode(args[0], Integer.parseInt(args[1]));
            user.connect();
        }
    }

}
