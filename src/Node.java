import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Node{
    protected ArrayList<Broker> BrokerList = new ArrayList<Broker>();
    protected String IPaddress;
    protected int port;
    //TODO private socket
    Node(){

    }

    Node(int port, String IPaddress){
        this.port = port;
        this.IPaddress = IPaddress;
    }


    public String getIPaddress() {
        return IPaddress;
    }

    public int getPort() {
        return port;
    }

    ArrayList<Broker> getBrokerList(){
        return BrokerList;
    }

    Socket connect(String address,int port){
        try{
            Socket socket = new Socket(address,port);
            return socket;
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void disconnect(Socket socket){
        try{
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    void init(int port){

    }

    void updateNodes(){


    }
}