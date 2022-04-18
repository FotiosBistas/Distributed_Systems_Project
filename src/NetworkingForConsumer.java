import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

class NetworkingForConsumer implements Runnable{
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket request_socket;
    private Consumer cons;
    boolean exit;
    public NetworkingForConsumer(Socket request_socket,Consumer cons){
        this.request_socket = request_socket;
        this.cons = cons;
        try {
            os = new ObjectOutputStream(request_socket.getOutputStream());
            is = new ObjectInputStream(request_socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killclient(){
        System.out.println("Ending client: " + cons.getName());
        exit = true;
        try{
            if(is != null) {
                is.close();
            }
            if(os != null) {
                os.close();
            }
            if(request_socket != null) {
                request_socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            Scanner sc = new Scanner(System.in);
            System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
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
                        System.out.println("Writing consumer object...");
                        os.writeObject(cons);
                        os.flush();
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
                        break;
                    default:
                        System.out.println("Invalid Request... Try again");
                        killclient();
                        continue;
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Terminating client...");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Terminating client...");
            e.printStackTrace();

        }catch(Exception e){
            System.out.println("Terminating client...");
            e.printStackTrace();
        }
    }
    public void BrokerResponses(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messagebroker;
                System.out.println("Opened thread to receive messages from broker");
                while(request_socket.isConnected()){
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
}
