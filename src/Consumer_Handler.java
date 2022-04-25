import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class Consumer_Handler implements Runnable {

    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;
    private final Socket consumer_connection;
    private final Broker broker;


    public Socket getConsumer_connection() {
        return consumer_connection;
    }

    public Broker getBroker() {
        return broker;
    }

    public ObjectInputStream getLocalinputStream() {
        return localinputStream;
    }

    public ObjectOutputStream getLocaloutputStream() {
        return localoutputStream;
    }

    public Consumer_Handler(Socket consumer_connection, Broker broker){
        this.consumer_connection = consumer_connection;
        this.broker = broker;
        try {
            localoutputStream = new ObjectOutputStream(consumer_connection.getOutputStream());
            localinputStream = new ObjectInputStream(consumer_connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not connect");
            shutdownConnection();
        }
    }

    public void ServerUnsubscribeRequest(){
        try {
            System.out.println("Serving unsubscribe request for client: " + consumer_connection.getInetAddress().getHostName());
            String topic_name = localinputStream.readUTF();
            UserNode new_cons = (UserNode) localinputStream.readObject();
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
            String topic_name = localinputStream.readUTF();
            UserNode new_cons = (UserNode) localinputStream.readObject();
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
            return localinputStream.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            shutdownConnection();
        }
        return -1;
    }


    public void FinishedOperation(){
        try {
            localoutputStream.writeInt(Messages.FINISHED_OPERATION.ordinal());
            localoutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in finished operation...");
            shutdownConnection();
        }
    }

    public void sendTopicList(){

    }

    public void removeConnection(){
        broker.getConsumer_Handlers().remove(this);
    }

    public void shutdownConnection(){
        System.out.println("Shutted connection: " + consumer_connection.getInetAddress());
        removeConnection();
        try{
            if(localoutputStream != null){
                localoutputStream.close();
            }
            if(localinputStream != null){
                localinputStream.close();
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
                Shared_Network_Methods.sendBrokerList(Consumer_Handler.this);
                FinishedOperation();
                System.out.println("Finished sending brokers");
                Shared_Network_Methods.sendIdList(Consumer_Handler.this);
                FinishedOperation();
                System.out.println("Finished sending id list");
                message = waitForUserNodePrompt();
            } else if (message == Messages.REGISTER.ordinal()) {
                ServeRegisterRequest();
                FinishedOperation();
                message = waitForUserNodePrompt();
            } else if (message == Messages.UNSUBSCRIBE.ordinal()) {
                ServerUnsubscribeRequest();
                FinishedOperation();
                message = waitForUserNodePrompt();
            }
        }

    }

}
