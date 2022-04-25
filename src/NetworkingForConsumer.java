import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

class NetworkingForConsumer implements Runnable{

    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket request_socket;
    private UserNode cons;
    boolean exit = false;
    private static Scanner sc = new Scanner(System.in);


    public NetworkingForConsumer(Socket request_socket,UserNode cons){
        this.request_socket = request_socket;
        this.cons = cons;
        try {
            os = new ObjectOutputStream(request_socket.getOutputStream());
            is = new ObjectInputStream(request_socket.getInputStream());
            getBrokerList();
            System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
        } catch (IOException e) {
            e.printStackTrace();
            TerminateConsumerConnection();
        }
    }

    public void getBrokerList(){
        try {
            System.out.println("Requesting for broker list");
            os.writeInt(Messages.GET_BROKER_LIST.ordinal());
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating client in get broker list...");
            TerminateConsumerConnection();
            e.printStackTrace();
        }
    }

    public void unsubscribe(){
        try {
            System.out.println("Unsubscribing from topic...");
            os.writeInt(Messages.UNSUBSCRIBE.ordinal());
            os.flush();
            System.out.println("Disconnect from what topic?");
            String topic_name = sc.next();
            os.writeUTF(topic_name);
            os.flush();
            os.writeObject(cons);
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating client in unsubscribe...");
            TerminateConsumerConnection();
            e.printStackTrace();
        }
    }

    public void showConversationData(){
        try{
            System.out.println("Requesting to show conversation data history");
            os.writeInt(Messages.SHOW_CONVERSATION_DATA.ordinal());
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating client in show conversation data...");
            TerminateConsumerConnection();
            e.printStackTrace();
        }
    }

    public void register(){
        try {
            System.out.println("Registering...");
            os.writeInt(Messages.REGISTER.ordinal());
            os.flush();
            System.out.println("What topic are you interested in?");
            String topic_name = sc.next();
            //find the appropriate broker for the specific topic
            //System.out.println("Finding appropriate broker...");
            //Tuple<String,int[]> brk = cons.hashTopic(topic_name);
            //request_socket = new Socket(brk.getValue1(), brk.getValue2()[0]);
            os.writeUTF(topic_name);
            os.flush();
            System.out.println("Writing consumer object...");
            os.writeObject(cons);
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating client in subscribe...");
            TerminateConsumerConnection();
            e.printStackTrace();
        }
    }

    public void FinishedOperation(){
        try {
            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            TerminateConsumerConnection();
        }
    }

    public int waitForUserNodePrompt(){
        try {
            System.out.println("Waiting for user node prompt in consumer connection");
            return is.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            TerminateConsumerConnection();
        }
        return -1;
    }

    public void TerminateConsumerConnection(){
        System.out.println("Ending client: " + cons.getName());
        exit = true;
        try{
            if(is != null) {
                System.out.println("Input stream was not null so we had to close it");
                is.close();
                is = null;
            }
            if(os != null) {
                System.out.println("Output stream was not null so we had to close it");
                os.close();
                os = null;
            }
            if(request_socket != null) {
                System.out.println("Socket was not closed so we had to close it");
                request_socket.close();
                request_socket = null;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void notifyThread(){
        System.out.println("Waking up networking for consumer");
        notifyAll();
    }

    public synchronized void push(){
        System.out.println("Pushing operation has started");
        try {
            NetworkingForPublisher publish = new NetworkingForPublisher(new Socket("192.168.1.5", 1235), cons,this);
            Thread t = new Thread(publish);
            t.start();
            //waits until input is given by the publisher and servers the push request in the background
            wait();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
            System.out.println("Error in push call");
            TerminateConsumerConnection();
        }
    }

    public void pull(String topic){

    }

    @Override
    public void run() {
        while(!exit) {
            System.out.println("1.Register to topic");
            System.out.println("2.Unsubscribe from topic");
            System.out.println("3.Show conversation data");
            System.out.println("4.Push");
            System.out.println("0.Exit");
            System.out.println("Enter an int from the above options");
            int userinput = sc.nextInt();
            switch (userinput){
                case 1:
                    register();
                    FinishedOperation();
                    break;
                case 2:
                    unsubscribe();
                    FinishedOperation();
                    break;
                case 3:
                    showConversationData();
                    FinishedOperation();
                    break;
                case 4:
                    push();
                    FinishedOperation();
                    break;
                case 0:
                    System.out.println("Terminating connection with server in run");
                    TerminateConsumerConnection();
                    break;
                default:
                    System.out.println("Invalid Request... Try again");
            }
        }
    }
    public void BrokerResponses(){
        new Thread(() -> {
            int messagebroker = -1;
            System.out.println("Opened thread to receive messages from broker");
            while (!exit) {
                try{
                    if(messagebroker == -1) {
                        System.out.println("Waiting for message by the broker...");
                        messagebroker = is.readInt();
                        System.out.println("Received message from broker: " + messagebroker);
                    }else if(messagebroker == Messages.SENDING_BROKER_LIST.ordinal()){
                        System.out.println("Received message that the broker list is being sent");
                        while(true){
                            System.out.println("In while loop for the broker list...");
                            if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                                System.out.println("Received finished operation message in the while loop for the broker list");
                                break;
                            }
                            int size = is.readInt();
                            System.out.println("Size of list: " + size);
                            String ip = is.readUTF();
                            System.out.println("Broker's ip: " + ip);
                            System.out.println("Sending broker's ports...");
                            ArrayList<Integer> temp = new ArrayList<>();
                            while(true) {
                                System.out.println("In while loop for the broker's ports...");
                                if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                                    System.out.println("Received finished operation message in the while loop for the broker's ports");
                                    break;
                                }
                                int port = is.readInt();
                                System.out.println("Received port: " + port);
                                temp.add(port);
                                if(temp.size()>=3){
                                    System.out.println("Waiting for finished operation message by the broker in the while loop for sending port array");
                                    messagebroker = is.readInt();
                                }
                            }
                            messagebroker = -1;
                            System.out.println("Set message broker to: " + messagebroker);
                            int[] ports = new int[3];
                            ports[0] = temp.get(0);
                            ports[1] = temp.get(1);
                            ports[2] = temp.get(2);
                            cons.getBrokerList().add(new Tuple<String, int[]>(ip, ports));
                            System.out.println(cons.getBrokerList());
                            if(cons.getBrokerList().size() >= size) {
                                System.out.println("Waiting for finished operation message by the broker in the while loop for sending broker list");
                                messagebroker = is.readInt();
                            }

                        }
                        System.out.println("Broker sent the Broker List");
                        os.writeInt(Messages.FINISHED_OPERATION.ordinal());
                        os.flush();
                        messagebroker = -1;

                    }else if(messagebroker == Messages.SENDING_ID_LIST.ordinal()) {
                        System.out.println("Broker is sending its ID List");
                        while(true) {
                            if ((messagebroker == Messages.FINISHED_OPERATION.ordinal())) {
                                System.out.println("Received finished operation message in while loop sending id list");
                                break;
                            }
                            int size = is.readInt() ;
                            System.out.println("Received size: " + size);
                            int id = is.readInt();
                            System.out.println("Received ID: " + id);
                            cons.getBroker_ids().add(id);
                            if (cons.getBroker_ids().size() >= size) {
                                System.out.println("Waiting for finished operation message by the broker in the while loop for sending id list");
                                messagebroker = is.readInt();
                            }
                        }
                        System.out.println("Broker sent the id list");
                        os.writeInt(Messages.FINISHED_OPERATION.ordinal());
                        os.flush();
                        messagebroker = -1;
                    }


                }catch (SocketException e){
                    System.out.println("Socket was closed before");
                    TerminateConsumerConnection();
                }
                catch (Exception e) {
                    System.out.println("Terminating client in broker responses...");
                    e.printStackTrace();
                    TerminateConsumerConnection();
                }
            }
        }).start();
    }
}
