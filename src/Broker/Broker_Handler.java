package Broker;

import Logging.ConsoleColors;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import Tools.Messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Broker_Handler implements Runnable{

    private final Socket broker_connection;
    private final Broker caller_broker;
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    Broker_Handler(Socket broker_connection, Broker caller_broker){
        try {
            this.broker_connection = broker_connection;
            this.caller_broker = caller_broker;
            localinputStream = new ObjectInputStream(broker_connection.getInputStream());
            localoutputStream = new ObjectOutputStream(broker_connection.getOutputStream());
        }catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error while trying to construct inter broker communications" + ConsoleColors.RESET);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("Established connection with broker: " + broker_connection.getInetAddress());
        while (broker_connection.isConnected()) {
            Integer message = GeneralUtils.waitForNodePrompt(localinputStream,broker_connection);
            if(message == null){
                shutdownConnection();
                break;
            }else if(message > Messages.values().length){
                System.out.println(ConsoleColors.RED + "received erroneous index" + ConsoleColors.RESET);
                continue;
            }
            Messages message_received = Messages.values()[message];
            switch (message_received){
                case FINISHED_OPERATION:
                    System.out.println("Received finished operation message");
                    break;
                case SHARE_FILE:
                    String topic_name = BrokerUtils.receiveTopicName(localinputStream,localoutputStream,broker_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }

                    break;
                case SHARE_STORY:
                    String topic_name = BrokerUtils.receiveTopicName(localinputStream,localoutputStream,broker_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case SHARE_TOPIC:
                    String topic_name = BrokerUtils.receiveTopicName(localinputStream,localoutputStream,broker_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case SHARE_SUBSCRIBER:
                    String topic_name = BrokerUtils.receiveTopicName(localinputStream,localoutputStream,broker_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                default:
                    System.out.println("No known message type was received");
                    break;
            }
        }
    }

    private void shutdownConnection(){
        try {
            if (localinputStream != null) {
                System.out.println("Shutting down local input stream");
                localinputStream.close();
            }
            if (localoutputStream != null) {
                System.out.println("Shutting down local output stream");
                localoutputStream.close();
            }
            if (broker_connection != null) {
                System.out.println("Shutting down local socket");
                broker_connection.close();
            }
        }catch (SocketException socketException){
            System.out.println(ConsoleColors.RED + "Socket error while trying to shutdown publisher" + ConsoleColors.RESET);
        }catch (IOException ioException) {
            System.out.println("Error while shutting down connection");
            ioException.printStackTrace();
        }
    }
}
