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
    private Consumer cons;
    boolean exit = false;
    private static Scanner sc = new Scanner(System.in);


    public NetworkingForConsumer(Socket request_socket,Consumer cons){
        this.request_socket = request_socket;
        this.cons = cons;
        try {
            os = new ObjectOutputStream(request_socket.getOutputStream());
            is = new ObjectInputStream(request_socket.getInputStream());
            System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
            System.out.println("Requesting for broker list");
            os.writeInt(Messages.GET_BROKER_LIST.ordinal());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            killclient();
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
            killclient();
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
            killclient();
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
            os.writeUTF(topic_name);
            os.flush();
            System.out.println("Writing consumer object...");
            os.writeObject(cons);
            os.flush();
        }catch(IOException e){
            System.out.println("Terminating client in subscribe...");
            killclient();
            e.printStackTrace();
        }
    }

    public void killclient(){
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

    @Override
    public void run() {
        while(!exit) {
            System.out.println("1.Register to topic");
            System.out.println("2.Unsubscribe from topic");
            System.out.println("3.Show conversation data");
            System.out.println("0.Exit");
            System.out.println("Type the number");
            int userinput = sc.nextInt();
            String topic_name;
            switch (userinput){
                case 1:
                    register();
                    break;
                case 2:
                    unsubscribe();
                    break;
                case 3:
                    showConversationData();
                    break;

                case 0:
                    System.out.println("Terminating connection with server in run");
                    killclient();
                    break;
                default:
                    System.out.println("Invalid Request... Try again");
                    break;
            }
        }
    }
    public void BrokerResponses(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int messagebroker = -1;
                System.out.println("Opened thread to receive messages from broker");
                while (!exit) {
                    try {
                        if(messagebroker == -1) {
                            System.out.println("Waiting...");
                            messagebroker = is.readInt();
                            System.out.println("Received message from broker: " + messagebroker);
                        }else if(messagebroker == Messages.SENDING_BROKER_LIST.ordinal()){
                            System.out.println("Received message that the broker list is being sent");
                            while(true){
                                if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                                    break;
                                }
                                int size = Integer.parseInt(is.readUTF());
                                String ip = is.readUTF();
                                System.out.println("Broker's ip: " + ip);
                                int port = Integer.parseInt(is.readUTF());
                                System.out.println("Broker's port: " + port);
                                System.out.println("Inserting broker's port and IP address");
                                cons.getBrokerList().add(new Tuple<String, Integer>(ip, port));
                                System.out.println(cons.getBrokerList());
                                if(cons.getBrokerList().size() >= size) {
                                    messagebroker = is.readInt();
                                }
                            }
                            System.out.println("Broker sent all the broker list");
                            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
                            os.flush();
                            messagebroker = -1;
                            break;


                        }else if(messagebroker == Messages.SEND_LIST_SIZE.ordinal()){
                            System.out.println("Broker asked for your list size");
                            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
                            os.flush();
                            messagebroker = -1;
                        }

                    }catch (SocketException e){
                        System.out.println("Socket was closed before");
                        killclient();
                    }
                    catch (Exception e) {
                        System.out.println("Terminating client in broker responses...");
                        e.printStackTrace();
                        killclient();
                    }
                }
            }
        }).start();
    }
}
