
package Broker;


import Logging.ConsoleColors;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import Tools.Messages;
import Tools.MultimediaFile;
import Tools.Story;
import Tools.Text_Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Publisher_Handler implements Runnable{
    private Socket publisher_connection;
    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;
    private final int chunksize = 512*1024;
    private final Broker broker;
    private String topic_name;

    public Publisher_Handler(Socket publisher_connection, Broker broker){
        this.publisher_connection = publisher_connection;
        this.broker = broker;
        try {
            localoutputStream = new ObjectOutputStream(publisher_connection.getOutputStream());
            localinputStream = new ObjectInputStream(publisher_connection.getInputStream());
        }catch (SocketException socketException) {
            System.out.println("\033[0;31m" + "Socket error" + "\033[0m");
            shutdownConnection();
        } catch (IOException e) {
            System.out.println("\033[0;31m" + "Error in constructor shutting down connection..." + "\033[0m");
            shutdownConnection();
        }
    }

    /**
     * Receives the messages that have to do with the publisher services from the Messages ENUMERATOR and
     * serves each request appropriately.
     */
    @Override
    public void run() {
        System.out.println("Established connection with publisher: " + publisher_connection.getInetAddress());
        while (publisher_connection.isConnected()) {
            Integer message = GeneralUtils.waitForNodePrompt(localinputStream,publisher_connection);
            if(message == null){
                shutdownConnection();
                break;
            }else if(message > Messages.values().length){
                System.out.println(ConsoleColors.RED + "received erroneous index" + ConsoleColors.RESET);
                continue;
            }
            Messages message_received = Messages.values()[message];
            System.out.println("Message received ordinal: " + message_received.ordinal());
            switch (message_received){
                case FINISHED_OPERATION:
                    System.out.println("Received finished operation message");
                    break;
                case PUSH_FILE:
                    System.out.println(ConsoleColors.PURPLE + "Notified by publisher that there is a new file" + ConsoleColors.RESET);
                    MultimediaFile new_file;
                    if((new_file = BrokerUtils.receiveFile(localinputStream,publisher_connection)) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        broker.addToMessageQueue(new_file,topic_name);
                        shutdownConnection();
                        return;
                    }
                    broker.addToMessageQueue(new_file,topic_name);
                    shutdownConnection();
                    break;
                case PUSH_MESSAGE:
                    System.out.println(ConsoleColors.PURPLE + "Notified by publisher that there is a new message" + ConsoleColors.RESET);
                    Text_Message new_text_message;
                    if((new_text_message = BrokerUtils.receiveTextMessage(localinputStream,publisher_connection)) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        broker.addToMessageQueue(new_text_message,topic_name);
                        shutdownConnection();
                        return;
                    }
                    broker.addToMessageQueue(new_text_message,topic_name);
                    shutdownConnection();
                    break;
                case PUSH_STORY:
                    System.out.println(ConsoleColors.PURPLE + "Notified by publisher that there is a new story" + ConsoleColors.RESET);
                    Story new_story;
                    if((new_story = BrokerUtils.receiveStory(localinputStream,publisher_connection)) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        broker.addToMessageQueue(new_story,topic_name);
                        shutdownConnection();
                        return;
                    }
                    broker.addToMessageQueue(new_story,topic_name);
                    shutdownConnection();
                    break;
                case GET_TOPIC_LIST:
                    if(BrokerUtils.sendTopicList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case NOTIFY:
                    topic_name =  BrokerUtils.receiveTopicName(localinputStream,localoutputStream,publisher_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    Boolean correct = BrokerUtils.isCorrectBroker(localoutputStream,this.broker,topic_name);
                    if(correct == null){
                        shutdownConnection();
                        return;
                    } else if(!correct){
                        shutdownConnection();
                        return;
                    }
                    if(BrokerUtils.sendTopicList(localoutputStream,broker) == null){
                        shutdownConnection();
                        return;
                    };
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    };
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
    private void removeConnection(){
        broker.getPublisher_Handlers().remove(this);
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
    private void shutdownConnection() {
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
        }catch (SocketException socketException){
            System.out.println(ConsoleColors.RED + "Socket error while trying to shutdown publisher" + ConsoleColors.RESET);
        }catch (IOException ioException) {
            System.out.println("Error while shutting down connection");
            ioException.printStackTrace();
        }
    }
}
