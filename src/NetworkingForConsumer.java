import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

class NetworkingForConsumer implements Runnable{
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket request_socket;
    private Consumer cons;
    boolean exit = false;
    public NetworkingForConsumer(Socket request_socket,Consumer cons){
        this.request_socket = request_socket;
        this.cons = cons;
        try {
            os = new ObjectOutputStream(request_socket.getOutputStream());
            is = new ObjectInputStream(request_socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
            killclient();
        }
    }

    public void killclient(){
        System.out.println("Ending client: " + cons.getName());
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
        try{
            Scanner sc = new Scanner(System.in);
            System.out.println("I'm the client: " + cons.getName() + " and i have connected to the server");
            //System.out.println("Requesting for broker list");
            //os.writeUTF("GetBrokerList");
            //os.flush();
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
                        System.out.println("Terminating connection with server in run");
                        killclient();
                        break;
                    default:
                        System.out.println("Invalid Request... Try again");
                        break;
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Terminating client networking run...");
            killclient();
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Terminating client networking run...");
            killclient();
            e.printStackTrace();

        }catch(Exception e){
            System.out.println("Terminating client networking run...");
            killclient();
            e.printStackTrace();
        }
    }
    public void BrokerResponses(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messagebroker;
                System.out.println("Opened thread to receive messages from broker");
                while (!exit) {
                    System.out.println("Waiting...");
                    try {
                        messagebroker = is.readUTF();
                        System.out.println("Received message from broker: " + messagebroker);
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
