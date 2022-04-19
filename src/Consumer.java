import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Consumer implements Serializable {



    private String ip;
    private int port;
    private String name;
    private List<Tuple<String,Integer>> brokerList = new ArrayList<Tuple<String,Integer>>();

    public Consumer(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    //public void setBrokerList(ArrayList<Broker> brokerList){
    //  this.brokerList = brokerList;
    //}

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

    public void tryagain(){
        startConsumer();
    }

    public void startConsumer(){
        try {

            NetworkingForConsumer inputHandler = new NetworkingForConsumer(new Socket("localhost",1234),this);
            inputHandler.BrokerResponses();
            Thread t = new Thread(inputHandler);
            t.start();
        } catch(ConnectException e){
            System.out.println("No response from broker try again");
            try {
                Thread.sleep(40000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            tryagain();
        } catch (IOException e) {
            System.out.println("Terminating client...");
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Consumer{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    public static void main(String[] args) {
        if(args[0] == null){
            System.out.println("You didn't provide an IP address");
        }
        else if(args[1] == null){
            System.out.println("You didn't provide a port number");
        }else {
            Consumer consumer = new Consumer("localhost", 21312, "Fotis");
            /* Correct way to start threads */
            consumer.startConsumer();
            //consumer.BrokerResponses();
            // We create a thread object giving a runnable as a parameter
            // and we call the start method of the created object
        }
    }
}