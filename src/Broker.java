import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;
public class Broker extends Node implements Runnable{
    private ArrayList<Consumer> registeredUsers = new ArrayList<Consumer>();
    private ArrayList<Publisher> registeredPublishers = new ArrayList<Publisher>();
    private Queue<Tuple<String,Value>> message_queue = new LinkedList<Tuple<String,Value>>();
    private Socket socket;
    private class ClientConnectionHandler implements Runnable{
        ObjectInputStream in;
        ObjectOutputStream out;
        private Socket client;
        private Socket publisher;
        public ClientConnectionHandler(Socket client){
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
    private class PublisherConnectionHandler implements Runnable{
        ObjectInputStream in;
        ObjectOutputStream out;
        private Socket publisher;
        public PublisherConnectionHandler(Socket client){
            this.publisher = publisher;
        }
        @Override
        public void run(){

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