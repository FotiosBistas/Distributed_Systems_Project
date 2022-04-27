
package Broker;
import Tools.Messages;
import Tools.Tuple;
import Tools.Topic;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

class Publisher_Handler implements Runnable{
    private Socket publisher_connection;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;
    private final int chunksize = 512*1024;
    private final Broker broker;

    Publisher_Handler(Socket publisher_connection, Broker broker){
        this.publisher_connection = publisher_connection;
        this.broker = broker;
        try {
            localinputStream = new ObjectInputStream(publisher_connection.getInputStream());
            localoutputStream = new ObjectOutputStream(publisher_connection.getOutputStream());
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            System.out.println("Error in constructor shutting down connection...");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    /**
     * Receives each chunk for a specific file.
     * It does that using a custom ACK system for each chunk on the broker side and the publisher side.
     */
    //TODO message list inside broker
    public void receiveFile(){

        try {
            System.out.println("Receiving file...");
            String file_name = localinputStream.readUTF();
            String new_file = file_name.substring(file_name.lastIndexOf("\\")+1);
            System.out.println("Received file: " + new_file);
            int number_of_chunks = localinputStream.readInt();
            System.out.println("You will receive: " + number_of_chunks + " chunks");
            String path_for_broker = "C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\";
            System.out.println(path_for_broker + new_file);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
            System.out.println("Receiving file...");
            byte[] buffer = new byte[chunksize];
            ArrayList<byte[]> chunks = new ArrayList<>();
            while(true) {
                if(number_of_chunks == chunks.size()){
                    System.out.println("Finished receiving: " + chunks.size() + "chunks");
                    fileOutputStream.close();
                    break;
                }
                int index = localinputStream.readInt();
                System.out.println("You are receiving chunk: " + index);
                int actual_size = localinputStream.readInt();
                System.out.println("Actual size of the incoming chunk is: " + actual_size);
                localinputStream.readFully(buffer,0,actual_size);
                byte[] temp = buffer.clone();
                fileOutputStream.write(temp,0,actual_size);
                fileOutputStream.flush();
                chunks.add(temp);
                /*System.out.println("Sending received chunk ack");
                localoutputStream.writeInt(Messages.RECEIVED_CHUNK.ordinal());
                localoutputStream.flush();
                while(true){
                    System.out.println("Waiting to receive the received ack message");
                    int message_from_publisher = localinputStream.readInt();
                    if(Messages.RECEIVED_ACK.ordinal() != message_from_publisher){
                        localoutputStream.writeInt(Messages.RECEIVED_CHUNK.ordinal());
                        localoutputStream.flush();
                        System.out.println("Waiting to receive the received ack message");
                    }else{
                        System.out.println("Client received ack");
                        break;
                    }
                }*/
                System.out.println("Chunks size now is: " + chunks.size());
            }
            System.out.println("Finished receiving file");
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            System.out.println("Shutting down in receive file...");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public String receiveTopicName(){
        try{
            System.out.println("Receiving the topic name");
            String topic_name = localinputStream.readUTF();
            System.out.println(topic_name);
            return topic_name;
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
            return null;
        }catch (IOException e){
            System.out.println("Error while receiving topic name");
            e.printStackTrace();
            shutdownConnection();
            return null;
        }
    }

    public void sendTopicList(){
        try {
            System.out.println("Sending topic list...");
            localoutputStream.writeInt(Messages.SENDING_TOPIC_LIST.ordinal());
            localoutputStream.flush();
            for (int i = 0; i < broker.getTopics().size(); i++) {
                System.out.println("Sending topic list size: " + broker.getTopics().size());
                localoutputStream.writeInt(broker.getTopics().size());
                localoutputStream.flush();
                System.out.println("Sending topic: " + broker.getTopics().get(i));
                localoutputStream.writeObject(broker.getTopics().get(i));
                localoutputStream.flush();
            }
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        }catch(IOException e){
            System.out.println("Error while trying to send topic list in publisher connection");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public int waitForUserNodePrompt(){
        try {
            System.out.println("Waiting for user node prompt in publisher connection");
            return localinputStream.readInt();
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch (IOException e) {
            System.out.println("Shutting down connection in wait for user node...");
            shutdownConnection();
        }
        return -1;
    }

    public void sendMessage(Messages message_type){
        try{
            System.out.println("Sending message: " + message_type);
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        } catch(IOException e){
            System.out.println("Error in sending message");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void sendMessage(int index){
        try{
            localoutputStream.writeInt(index);
            localoutputStream.flush();
        }catch (SocketException socketException) {
            System.out.println("Socket error");
            shutdownConnection();
        }catch(IOException e){
            System.out.println("Error in sending message");
            e.printStackTrace();
            shutdownConnection();
        }
    }

    public void FinishedOperation(){
        try {
            System.out.println("Sending finished operation message");
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

    @Override
    public void run() {
        System.out.println("Established connection with publisher: " + publisher_connection.getInetAddress());
        int message;
        message = waitForUserNodePrompt();
        System.out.println("Received message from publisher: " + message);
        while (publisher_connection.isConnected()) {
            if(message == Messages.FINISHED_OPERATION.ordinal()){
                System.out.println("Finished operation waiting for publisher's next input");
                message = waitForUserNodePrompt();
            }
            else if (message == Messages.NOTIFY.ordinal()) {
                System.out.println("Notify message was received by publisher: " + publisher_connection.getInetAddress().getHostName());
                receiveFile();
                FinishedOperation();
                message = waitForUserNodePrompt();
            }else if(message == Messages.GET_TOPIC_LIST.ordinal()){
                System.out.println("Get topic list message was received by publisher: " + publisher_connection.getInetAddress().getHostName());
                sendTopicList();
                FinishedOperation();
                message = waitForUserNodePrompt();
            } else if(message == Messages.SEND_APPROPRIATE_BROKER.ordinal()){
                System.out.println("Send me the topic name to find appropriate broker");
                String topic_name =  receiveTopicName();
                FinishedOperation();
                int index = broker.hashTopic(topic_name);
                Tuple<String,int[]> brk = broker.getBrokerList().get(index);
                System.out.println("Brk IP: " + brk.getValue1());
                System.out.println("Brk ports: " + Arrays.toString(brk.getValue2()));
                System.out.println("Local Broker IP: " + broker.getIp());
                if(brk.getValue1().equals(broker.getIp())){
                    System.out.println("They have equal IPs");
                    if(broker.getConsumer_port() == brk.getValue2()[0] &&
                    broker.getPublisher_port() == brk.getValue2()[1] && broker.getBroker_port() == brk.getValue2()[2]){
                        System.out.println("The broker is correct");
                        sendMessage(Messages.I_AM_THE_CORRECT_BROKER);
                        sendMessage(index);
                    }else{
                        System.out.println("The broker is not correct");
                        sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER);
                        sendMessage(index);
                        shutdownConnection();
                        return;
                    }
                }else{
                    sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER);
                    sendMessage(index);
                    shutdownConnection();
                    return;
                }
                message = waitForUserNodePrompt();
            }
        }

    }

    public void notifyPublisher(Topic topic){

    }

    public void removeConnection(){
        broker.getPublisher_Handlers().remove(this);
    }

    public void shutdownConnection(){
        System.out.println("Shutted connection: " + publisher_connection.getInetAddress());
        removeConnection();
        try{
            if(localoutputStream != null){
                localoutputStream.close();
            }
            if(localinputStream != null){
                localinputStream.close();
            }
            if(publisher_connection != null){
                publisher_connection.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
