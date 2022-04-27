package Broker;
import UserNode.UserNode;
import Broker.Broker;
import Tools.Topic;
import Tools.Messages;
import Tools.Tuple;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

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
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        }  catch (IOException e) {
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
            broker.UnsubscribeFromTopic(topic, new_cons.getName());
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
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
            broker.addConsumerToTopic(topic, new_cons.getName());
            //someone can subscribe and unsubscribe
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
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
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in wait for user node...");
            shutdownConnection();
        }
        return -1;
    }

    public void sendIdList(){
        try {
            System.out.println("Sending message type: " + Messages.SENDING_ID_LIST + " with ordinal number: " + Messages.SENDING_ID_LIST.ordinal());
            getLocaloutputStream().writeInt(Messages.SENDING_ID_LIST.ordinal());
            getLocaloutputStream().flush();
            for (int i = 0; i < getBroker().getId_list().size(); i++) {
                System.out.println("Sending ID List size: " + getBroker().getId_list().size());
                getLocaloutputStream().writeInt(getBroker().getId_list().size());
                getLocaloutputStream().flush();
                System.out.println("Sending ID: " + getBroker().getId_list().get(i));
                getLocaloutputStream().writeInt(getBroker().getId_list().get(i));
                getLocaloutputStream().flush();
            }
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            System.out.println("Error in sending list");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void sendBrokerList() {
        try {
            System.out.println("Sending message type: " + Messages.SENDING_BROKER_LIST + " with ordinal number: " + Messages.SENDING_BROKER_LIST.ordinal());
            getLocaloutputStream().writeInt(Messages.SENDING_BROKER_LIST.ordinal());
            getLocaloutputStream().flush();
            for (Tuple<String, int[]> val : getBroker().getBrokerList()) {
                System.out.println("Sending Broker List size: " + getBroker().getBrokerList().size());
                getLocaloutputStream().writeInt(getBroker().getBrokerList().size());
                getLocaloutputStream().flush();
                System.out.println("Sending broker's IP: " + val.getValue1());
                getLocaloutputStream().writeUTF(val.getValue1());
                getLocaloutputStream().flush();
                int i;
                for (i = 0; i < val.getValue2().length; i++) {
                    System.out.println("Sending broker's ports: " + val.getValue2()[i]);
                    getLocaloutputStream().writeInt(val.getValue2()[i]);
                    getLocaloutputStream().flush();
                }
                if (i == 3) {
                    System.out.println("Finished sending ports");
                    FinishedOperation();
                }
            }
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Shutting down connection in send broker list...");
            shutdownConnection();
        }
    }


    public void FinishedOperation(){
        try {
            localoutputStream.writeInt(Messages.FINISHED_OPERATION.ordinal());
            localoutputStream.flush();
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
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
        } catch(IOException e){
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
                sendBrokerList();
                FinishedOperation();
                System.out.println("Finished sending brokers");
                sendIdList();
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
            }else if (message == Messages.SHOW_CONVERSATION_DATA.ordinal()){

            }
        }

    }

}
