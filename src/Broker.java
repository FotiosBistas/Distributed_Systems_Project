import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.MutableCallSite;
import java.net.*;
import java.util.ArrayList;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;
public class Broker extends Node implements Runnable{
    private ArrayList<Consumer> registeredUsers = new ArrayList<Consumer>();
    private ArrayList<Publisher> registeredPublishers = new ArrayList<Publisher>();
    private Queue<Tuple<String,MultimediaFile>> message_queue = new LinkedList<Tuple<String,MultimediaFile>>();
    private Socket socket;
    private static int id = 0;
    //TODO check out if it can be done this way
    public Broker(){
        super();
        id++;
    }

    public Broker(String ipaddress,int port){
        super(port,ipaddress);
        id++;
    }

    public int getId() {
        return id;
    }
    private class ConnectionHandler implements Runnable{
        ObjectInputStream in;
        ObjectOutputStream out;
        private Socket client;
        private Socket publisher;
        public ConnectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run(){
            //accepts the client's connection
            try {
                in = new ObjectInputStream(client.getInputStream());
                out = new ObjectOutputStream(client.getOutputStream(),true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void run() {

    }
    public void calculateKeys(){
    }
    public void fitlerConsumers(String){

    }
    public void notifyBrokersOnChanges(){

    }
    public void notifyPublisher(String){

    }
    public void pull(String){

    }


}