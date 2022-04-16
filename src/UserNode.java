import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserNode implements Runnable{

    private String ip;
    private int port;
    private Socket requestSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private List<Broker> ListOfBrokers;


    public UserNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    @Override
    public void run() {
        try {
            requestSocket = new Socket("192.168.1.5",1234);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            //asks random broker for the broker list
            //broker will respond with ip address and port
            out.writeUTF("GetBrokerList");
            out.flush();
            while(in.read() == 0){}
            ListOfBrokers = (ArrayList<Broker>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
