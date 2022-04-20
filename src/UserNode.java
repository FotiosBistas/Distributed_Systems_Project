import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserNode {

    private String ip;
    private int port;
    private String name;
    private ProfileName prof_name;


    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,Integer>> brokerList = new ArrayList<>();
    private List<Integer> broker_ids = new ArrayList<>();

    UserNode(String ip,int port){
        this.ip = ip;
        this.port = port;
    }

    public List<Tuple<String,Integer>> getBrokerList(){
        return brokerList;
    }

    public String getName(){
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


    public void startUserNode(){
        try{
            // threads for consumer requests and responses from broker
            NetworkingForConsumer consumer = new NetworkingForConsumer(new Socket("localhost",1234),this);
            consumer.BrokerResponses();
            Thread t1 = new Thread(consumer);
            t1.start();

            //threads for publisher requests and responses from broker
            NetworkingForPublisher publisher = new NetworkingForPublisher(new Socket("localhost",1234),this);
            //publisher.BrokerResponses();
            Thread t2 = new Thread(publisher);
            t2.start();
        }catch(IOException e){

        }
    }

    public Tuple<String,Integer> hashTopic(Topic topic){
        // hash the topic and choose the correct broker
        int identifier = SHA1.hextoInt(topic.getName(),brokerList.size()*100);
        int index = 0;
        for(int i = 0 ; i < broker_ids.size() ; i++){
            // when you find the first broker that has id larger than the topics value then you use the previous broker
            if(broker_ids.get(i) > identifier){
                if(i == 0){
                    break;
                }
                index = i - 1;
                break;
            }
        }
        return brokerList.get(index);
    }


}
