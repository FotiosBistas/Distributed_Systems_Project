import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
public class UserNode{
    protected String IPaddress;
    protected int port;

    Node(){

    }

    Node(String IPaddress,int port){
        this.port = port;
        this.IPaddress = IPaddress;
    }


    public String getIPaddress() {
        return IPaddress;
    }

    public int getPort() {
        return port;
    }

    Socket connect(String address,int port){
        //while(true) {
            try {
                Socket socket = new Socket(address, port);
                return socket;
            } catch (UnknownHostException e) {
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
    /*void init(int port){

    }

    void updateNodes(){


    }*/
}