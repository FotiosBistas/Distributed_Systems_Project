import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Consumer extends UserNode implements Runnable{

    private List<Tuple<Topic,Byte>> chunks_to_be_received;
    private List<Topic> available_topics;

    public Consumer(String ip, int port) {
        super(ip, port);
    }

    private class InputHandler implements Runnable{
        private ObjectInputStream is;
        private ObjectOutputStream os;
        private Socket connection;

        @Override
        public void run() {

        }
    }

    public void run() {
        Socket requestSocket;
        try{

            Scanner sc = new Scanner(System.in);
            System.out.println("What do you want to do?");
            String userinput = sc.nextLine();
            if(userinput == "register"){

            }else if(userinput == "disconnect"){

            }else{// show conversation data

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(String topic){

    }

    public void connect(){

    }

    public Broker findBroker(Topic topic){



        int identifier = SHA1.hextoInt(topic.getName(),3);
        for(Broker val:BrokerList){
            if(val.getId() == identifier){
                return val;
            }
        }
        return null;

    }
    public void register(Topic topic){



            socket = connect(BrokerList.get(0).IPaddress,BrokerList.get(0).port);

    }
    public void showConversationData(String topic, int value){

    }
}
