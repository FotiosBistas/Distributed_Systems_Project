import java.io.*;
import java.lang.invoke.MutableCallSite;
import java.net.*;
import java.util.ArrayList;
import java.security.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
//broker implements serializable due to
public class Broker implements Runnable,Serializable{
    private List<Consumer> registeredUsers = new ArrayList<Consumer>();
    private List<Publisher> registeredPublishers = new ArrayList<Publisher>();
    private Queue<Tuple<String,MultimediaFile>> message_queue = new LinkedList<Tuple<String,MultimediaFile>>();
    private List<Broker> BrokerList = new ArrayList<Broker>();
    private ServerSocket server;
    private String ip;
    private int port;
    private final int  maxBrokers = 3;
    private boolean done = false;
    private int id = 0;

    public Broker(String ip,String port){
        this.ip = ip;
        this.port = port;
        this.id = SHA1.hextoInt(SHA1.encrypt(String.valueOf(port) + ip),maxBrokers);
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            while(true){
                Socket client = server.accept();
                Thread action = new ActionsForBroker(client);
                action.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ActionsForBroker extends Thread {

        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket connected_socket;

        public ActionsForBroker(Socket connection){
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connected_socket = connection;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            HandleRequest();
        }


        public void HandleRequest() {
            try {

                out.writeUTF("Server established connection with client: " + socket.getInetAddress().getHostAddress());
                out.flush();
                String request = in.readUTF();
                if (request.equals("Get Broker List")) {

                    out.writeObject(BrokerList);
                    out.flush();
                } else if (request.equals("Register")) {

                } else if (request.equals("Push")) {
                    String topic = in.readUTF();
                } else if (request.equals("Pull")) {

                } else if (request.equals("Unsubscribe")) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void notifyBrokersOnChanges(){

    }

    public void notifyPublisher(String topic){

    }

    public void pull(String topic){

    }

    public List<Broker> getBrokerList() { return BrokerList; }

    public Queue<Tuple<String,MultimediaFile>> getMessage_queue(){
        return message_queue;
    }

    public int getId() {
        return id;
    }

}