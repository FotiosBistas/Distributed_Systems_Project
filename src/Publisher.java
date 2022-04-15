import java.math.BigInteger;
import java.util.ArrayList;


public class Publisher extends Node implements Runnable{
    private ProfileName name ;

    public Publisher(ProfileName name, String ipaddress, int port){
        super(ipaddress,port);
        this.name = name;
    }

    public void push(String topic,MultimediaFile val){
        // publisher needs to see the available brokers
        Broker brk = hashTopic(topic);
        // choose one
        brk.getMessage_queue().add(new Tuple<String,MultimediaFile>(topic,val));

    }
    public void notifyFailure(Broker brk){

    }
    public void notifyBrokersNewMessage(String){

    }
    public Broker hashTopic(String topic){
        // hash the topic and choose the correct broker
        int identifier = SHA1.hextoInt(topic,3);
        for(Broker val:BrokerList){
            if(val.getId() == identifier){
                return val;
            }
        }
        return null;
    }

    @Override
    public void run() {

    }
}