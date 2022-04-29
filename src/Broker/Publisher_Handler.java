
package Broker;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
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

    @Override
    public void run() {
        System.out.println("Established connection with publisher: " + publisher_connection.getInetAddress());
        while (publisher_connection.isConnected()) {
            Integer message = GeneralUtils.waitForNodePrompt(localinputStream,publisher_connection);
            if(message == null){
                shutdownConnection();
                break;
            }
            Messages message_received = Messages.values()[message];
            switch (message_received){
                case FINISHED_OPERATION:
                    System.out.println("Received finished operation message");
                    break;
                case NOTIFY:
                    if(BrokerUtils.receiveFile(localinputStream,publisher_connection) == null){
                        shutdownConnection();
                        break;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case GET_TOPIC_LIST:
                    if(BrokerUtils.sendTopicList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        break;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case SEND_APPROPRIATE_BROKER:
                    String topic_name =  BrokerUtils.receiveTopicName(localinputStream,localoutputStream,publisher_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        break;
                    }
                    Boolean correct = BrokerUtils.isCorrectBroker(localoutputStream,this.broker,topic_name);
                    if(correct == null){
                        shutdownConnection();
                        break;
                    } else if(!correct){
                        shutdownConnection();
                        break;
                    }
                    break;
                default:
                    System.out.println("No known message type was received");
                    break;
            }
        }
    }

    /**
     * Removes the connection from the array list of connections inside the broker class.
     */
    public void removeConnection(){
        broker.getPublisher_Handlers().remove(this);
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    public void shutdownConnection() {
        System.out.println("Shutting down connection");
        removeConnection();
        try {
            if (localinputStream != null) {
                System.out.println("Shutting down local input stream");
                localinputStream.close();
            }
            if (localoutputStream != null) {
                System.out.println("Shutting down local output stream");
                localoutputStream.close();
            }
            if (publisher_connection != null) {
                System.out.println("Shutting down local socket");
                publisher_connection.close();
            }
        } catch (IOException ioException) {
            System.out.println("Error while shutting down connection");
            ioException.printStackTrace();
        }
    }
}
