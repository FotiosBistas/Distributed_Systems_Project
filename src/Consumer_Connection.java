import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class Consumer_Connection implements Runnable {

    private ObjectInputStream is;
    private ObjectOutputStream ous;
    private Socket consumer_connection;
    private Broker broker;

    public Consumer_Connection(Socket consumer_connection,Broker broker){
        try {
            this.consumer_connection = consumer_connection;
            this.broker = broker;
            ous = new ObjectOutputStream(consumer_connection.getOutputStream());
            is = new ObjectInputStream(consumer_connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not connect");
            shutdownConnection();
        }
    }

    public void ServerUnsubscribeRequest(){
        try {
            System.out.println("Serving unsubscribe request for client: " + consumer_connection.getInetAddress().getHostName());
            String topic_name = is.readUTF();
            UserNode new_cons = (UserNode) is.readObject();
            System.out.println("Topic name: " + topic_name);
            Topic topic = null;
            for (int i = 0; i < broker.getTopics().size(); i++) {
                if(topic_name.equals(broker.getTopics().get(i).getName())){
                    topic = broker.getTopics().get(i);
                }
            }
            System.out.println("Unsubscribing user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " from topic: " + topic_name);
            broker.UnsubscribeFromTopic(topic, new_cons);
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in unsubscribe");
            shutdownConnection();
        }
    }

    public void ServeRegisterRequest(){
        //TODO subscribe function
        try {
            System.out.println("Serving register request for client: " + consumer_connection.getInetAddress().getHostName());
            String topic_name = is.readUTF();
            UserNode new_cons = (UserNode) is.readObject();
            System.out.println("Topic name: " + topic_name);
            Topic topic = null;
            for (int i = 0; i < broker.getTopics().size(); i++) {
                if(topic_name.equals(broker.getTopics().get(i).getName())){
                    topic = broker.getTopics().get(i);
                }
            }
            System.out.println("Registering user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " to topic: " + topic_name);
            broker.addConsumerToTopic(topic, new_cons);
            //someone can subscribe and unsubscribe
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("Shutting down connection in register...");
            shutdownConnection();
        }
    }

    public int waitForUserNodePrompt(){
        try {
            System.out.println("Waiting for user node prompt in consumer connection");
            return is.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            shutdownConnection();
        }
        return -1;
    }


    public void FinishedOperation(){
        try {
            ous.writeInt(Messages.FINISHED_OPERATION.ordinal());
            ous.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            shutdownConnection();
        }
    }


    public void sendBrokerList(){
        try {
            System.out.println("Sending broker list");
            ous.writeInt(Messages.SENDING_BROKER_LIST.ordinal());
            ous.flush();
            for (Tuple<String,int[]> val: broker.getBrokerList()) {
                System.out.println("Sending Broker List size: " + broker.getBrokerList().size());
                ous.writeInt(broker.getBrokerList().size());
                ous.flush();
                System.out.println("Sending broker's IP: " + val.getValue1());
                ous.writeUTF(val.getValue1());
                ous.flush();
                int i;
                for (i = 0; i < val.getValue2().length; i++) {
                    System.out.println("Sending broker's port: " + val.getValue2()[i]);
                    ous.writeInt(val.getValue2()[0]);
                    ous.flush();
                    ous.writeInt(val.getValue2()[1]);
                    ous.flush();
                }
                if(i == 2) {
                    System.out.println("Finished sending ports");
                    FinishedOperation();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in send broker list...");
            shutdownConnection();
        }
    }

    public void sendIdList(){
        try{
            System.out.println("Sending ID List");
            ous.writeInt(Messages.SENDING_ID_LIST.ordinal());
            ous.flush();
            for (int i = 0; i < broker.getId_list().size(); i++) {
                System.out.println("Sending ID List size: " + broker.getId_list().size());
                ous.writeInt(broker.getId_list().size());
                ous.flush();
                System.out.println("Sending ID: " + broker.getId_list().get(i));
                ous.writeInt(broker.getId_list().get(i));
                ous.flush();
            }
        }catch(IOException e){
            System.out.println("Error in sending list");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void sendTopicList(){

    }

    public void removeConnection(){
        broker.getConsumer_Connections().remove(this);
    }

    public void shutdownConnection(){
        System.out.println("Shutted connection: " + consumer_connection.getInetAddress());
        removeConnection();
        try{
            if(ous != null){
                ous.close();
            }
            if(is != null){
                is.close();
            }
            if(consumer_connection != null){
                consumer_connection.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        HandleRequest();
    }

    public void HandleRequest() {
        System.out.println("Server established connection with client: " + consumer_connection.getInetAddress().getHostAddress());
        int message;
        message = waitForUserNodePrompt();
        System.out.println("Received message from client: " + message);
        while (consumer_connection.isConnected()) {
            if (message == Messages.FINISHED_OPERATION.ordinal()) {
                System.out.println("Finished operation waiting for next input");
                message = waitForUserNodePrompt();
            } else if (message == Messages.GET_BROKER_LIST.ordinal()) {
                if(broker.getBrokerList().isEmpty()){
                    broker.readBrokerListFromConfigFile();
                }
                sendBrokerList();
                FinishedOperation();
                System.out.println("Finished sending brokers");
                sendIdList();
                FinishedOperation();
                System.out.println("Finished sending id list");
                message = waitForUserNodePrompt();
            } else if (message == Messages.REGISTER.ordinal()) {
                ServeRegisterRequest();
                //TODO call pull method
                FinishedOperation();
                message = waitForUserNodePrompt();
            } else if (message == Messages.UNSUBSCRIBE.ordinal()) {
                ServerUnsubscribeRequest();
                FinishedOperation();
                message = waitForUserNodePrompt();
            }
        }

    }

    public Socket getSocket(){
        return consumer_connection;
    }

}
