import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Consumer implements Serializable {

    private boolean exit = false;

    private Socket consumer_socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;

    private String ip;
    private int port;
    private String name;

    public Consumer(String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
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
            consumer_socket = new Socket("localhost",1234);
            is = new ObjectInputStream(consumer_socket.getInputStream());
            os = new ObjectOutputStream(consumer_socket.getOutputStream());
            InputHandler inputHandler = new InputHandler();
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
            killclient();
        }

    }

    @Override
    public String toString() {
        return "Consumer{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    public void killclient(){
        System.out.println("Ending client: " + name);
        exit = true;
        try{
            if(is != null) {
                is.close();
            }
            if(os != null) {
                os.close();
            }
            if(consumer_socket != null) {
                consumer_socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private class InputHandler implements Runnable{


        @Override
        public void run() {
            try{
                Scanner sc = new Scanner(System.in);
                System.out.println("I'm the client: " + name + " and i have connected to the server");
                while(!exit) {
                    System.out.println("1.Register to topic");
                    System.out.println("2.Disconnect from topic");
                    System.out.println("3.Show conversation data");
                    System.out.println("0.Exit");
                    System.out.println("Type the number");
                    int userinput = sc.nextInt();
                    String topic_name;
                    switch (userinput){
                        case 1:
                            System.out.println("Registering...");
                            os.writeUTF("Register");
                            os.flush();
                            System.out.println("What topic are you interested in?");
                            topic_name = sc.next();
                            os.writeUTF(topic_name);
                            os.flush();
                            //output_stream.writeObject(Consumer.this);
                            //output_stream.flush();
                            break;
                        case 2:
                            System.out.println("Disconnecting from topic...");
                            System.out.println("Disconnect from what topic?");
                            topic_name = sc.nextLine();
                            os.writeUTF(topic_name);
                            os.flush();
                            break;
                        case 3:
                            break;
                        case 0:
                            System.out.println("Ending the connection with the server...");
                            killclient();
                            break;
                        default:
                            System.out.println("Invalid Request... Try again");
                            continue;
                    }
                }
            } catch (UnknownHostException e) {
                System.out.println("Terminating client...");
                e.printStackTrace();
                killclient();
            } catch (IOException e) {
                System.out.println("Terminating client...");
                e.printStackTrace();
                killclient();
            }catch(Exception e){
                System.out.println("Terminating client...");
                e.printStackTrace();
                killclient();
            }
        }
    }

    public void BrokerResponses(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messagebroker;
                System.out.println("Opened thread to receive messages from broker");
                while(consumer_socket.isConnected()){
                    System.out.println("Waiting...");
                    try {
                        messagebroker = is.readUTF();
                        System.out.println("Received message from broker: " + messagebroker);
                    } catch (IOException e) {
                        System.out.println("Terminating client...");
                        e.printStackTrace();
                        killclient();
                    }
                }
            }
        }).start();
    }

    public void disconnect(String topic){

    }


    public void showConversationData(String topic, int value){

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
            consumer.BrokerResponses();
            // We create a thread object giving a runnable as a parameter
            // and we call the start method of the created object
        }
    }
}