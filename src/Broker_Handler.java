import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

class Broker_Handler implements Runnable{

    private Socket broker_connection;
    private Broker broker;
    private ObjectInputStream is;
    private ObjectOutputStream os;

    Broker_Handler(Socket broker_connection, Broker broker){
        try{
            this.broker_connection = broker_connection;
            this.broker = broker;
            is = new ObjectInputStream(broker_connection.getInputStream());
            os = new ObjectOutputStream(broker_connection.getOutputStream());
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Error starting broker connection");
            TerminateBrokerConnection();
        }
    }

    public void receiveTopicList(){

    }

    public void receiveBrokerList() {
        try {
            int messagebroker = -1;
            while (true) {
                System.out.println("In while loop for the broker list...");
                if ((messagebroker == Messages.FINISHED_OPERATION.ordinal())) {
                    System.out.println("Received finished operation message in the while loop for the broker list");
                    break;
                }
                int size = is.readInt();
                System.out.println("Size of list: " + size);
                String ip = is.readUTF();
                System.out.println("Broker's ip: " + ip);
                System.out.println("Sending broker's ports...");
                ArrayList<Integer> temp = new ArrayList<>();
                while (true) {
                    System.out.println("In while loop for the broker's ports...");
                    if ((messagebroker == Messages.FINISHED_OPERATION.ordinal())) {
                        System.out.println("Received finished operation message in the while loop for the broker's ports");
                        break;
                    }
                    int port = is.readInt();
                    System.out.println("Received port: " + port);
                    temp.add(port);
                    if (temp.size() >= 3) {
                        System.out.println("Waiting for finished operation message by the broker in the while loop for sending port array");
                        messagebroker = readInput();
                    }
                }
                int[] ports = new int[3];
                ports[0] = temp.get(0);
                ports[1] = temp.get(1);
                ports[2] = temp.get(2);
                Tuple<String,int[]> new_broker = new Tuple<String, int[]>(ip, ports);
                boolean exists = false;
                for (Tuple<String, int[]> val:broker.getBrokerList()) {
                    if(val.getValue1().equals(new_broker.getValue1())) {
                        for (int i = 0; i < val.getValue2().length; i++) {
                            if (val.getValue2()[i] == ports[i]) {
                                exists = true;
                            }
                        }
                    }else{
                        exists = true;
                    }
                }
                if(!exists){
                    broker.getBrokerList().add(new_broker);
                }
                if (broker.getBrokerList().size() >= size) {
                    System.out.println("Waiting for finished operation message by the broker in the while loop for sending broker list");
                    messagebroker = readInput();
                }
                System.out.println("Broker received broker list");
            }
        } catch (IOException e) {
            System.out.println("Error in receiving broker list");
            e.printStackTrace();
            TerminateBrokerConnection();
        }
    }

    public void FinishedOperation(){
        try {
            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            TerminateBrokerConnection();
        }
    }

    public Integer readInput(){
        try{
            System.out.println("Waiting to receive message by broker: " + broker_connection.getInetAddress());
            return is.readInt();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Error in read input");
            TerminateBrokerConnection();
        }
        return null;
    }

    @Override
    public void run() {
        int message_from_broker = readInput();
        while(!broker_connection.isClosed()){
           if(message_from_broker == Messages.FINISHED_OPERATION.ordinal()){
               System.out.println("Waiting for next input from broker");
               message_from_broker = readInput();
           }else if(message_from_broker == Messages.SENDING_BROKER_LIST.ordinal()){
                receiveBrokerList();
                FinishedOperation();
                receiveIdList();
                FinishedOperation();
                message_from_broker = readInput();
           }else if(message_from_broker == Messages.NEW_TOPIC.ordinal()){
                receiveTopicList();
                message_from_broker = readInput();
           }else if(message_from_broker == Messages.CLOSE_CONNECTION.ordinal()){
               System.out.println("Closing connection...");
               TerminateBrokerConnection();
               break;
           }else if(message_from_broker == Messages.WAITING_FOR_ACK.ordinal()){
               System.out.println("Sending ACK");
               sendACK();
               message_from_broker = readInput();
           }
        }
    }

    private void receiveIdList() {
        System.out.println("Broker is sending its ID List");
        try {
            int messagebroker = -1;
            while (true) {
                if ((messagebroker == Messages.FINISHED_OPERATION.ordinal())) {
                    System.out.println("Received finished operation message in while loop sending id list");
                    break;
                }
                int size = is.readInt();
                System.out.println("Received size: " + size);
                int id = is.readInt();
                System.out.println("Received ID: " + id);
                broker.getId_list().add(id);
                if (broker.getId_list().size() >= size) {
                    System.out.println("Waiting for finished operation message by the broker in the while loop for sending id list");
                    messagebroker = readInput();
                }
            }
            System.out.println("Broker sent the id list");
            os.writeInt(Messages.FINISHED_OPERATION.ordinal());
            os.flush();
        }catch (IOException e){
            System.out.println("Error in sending id list");
            e.printStackTrace();
            TerminateBrokerConnection();
        }
    }

    public void sendACK(){
        try{
            System.out.println("Sending message type: " + Messages.ACK + " with ordinal number: " + Messages.ACK.ordinal());
            os.writeInt(Messages.ACK.ordinal());
            os.flush();
        }catch (IOException e){
            System.out.println("Error in sending ack");
            e.printStackTrace();
            TerminateBrokerConnection();
        }
    }

    public void TerminateBrokerConnection(){
        System.out.println("Shutting down broker connection...");
        try {
            if(broker_connection != null) {
                broker_connection.close();
            }
            if(is != null){
                is.close();
            }
            if(os != null){
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in shutting down connection");
        }


    }

}
