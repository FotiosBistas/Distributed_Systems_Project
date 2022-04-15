import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Consumer extends Node{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket = new Socket();

    public Consumer(){

    }

    public void disconnect(String topic){

    }
    public Broker findBroker(String topic){
        /*
        int identifier = SHA1.hextoInt(topic,3);
        for(Broker val:BrokerList){
            if(val.getId() == identifier){
                return val;
            }
        }
        return null;
        */
    }
    public void register(String topic){
        try{
            // consumer asks a random broker if it's responsible for the specific topic
            socket = connect(BrokerList.get(0).IPaddress,BrokerList.get(0).port);
        }catch(IOException e){

        }
    }
    public void showConversationData(String topic, int value){

    }
}
