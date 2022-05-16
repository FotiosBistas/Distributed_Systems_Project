package Broker;

import Logging.ConsoleColors;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import Tools.*;
import UserNode.UserNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SendObject implements Runnable{
    private ObjectOutputStream localoutputStream;
    private ObjectInputStream localinputStream;
    private final Socket request_socket;
    private final Broker caller_broker;

    private final Object object_to_be_sent;

    private final String topic;
    private final int operation;

    enum Operation{
        SHARE_TOPIC,
        SHARE_FILE,
        SHARE_STORY,
        SHARE_TEXT_MESSAGE,
        SHARE_SUBSCRIBER
    }

    SendObject(Socket request_socket, Broker caller_broker, int operation, Object object_to_be_sent,String topic){
        try {
            this.operation = operation;
            this.request_socket = request_socket;
            this.caller_broker = caller_broker;
            this.object_to_be_sent = object_to_be_sent;
            this.topic = topic;
            localinputStream = new ObjectInputStream(request_socket.getInputStream());
            localoutputStream = new ObjectOutputStream(request_socket.getOutputStream());
        }catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error while trying to construct send object" + ConsoleColors.RESET);
            throw new RuntimeException(e);
        }
    }



    @Override
    public void run() {
        if(operation > Messages.values().length){
            System.out.println(ConsoleColors.RED + "received erroneous index" + ConsoleColors.RESET);
            return;
        }
        Operation enum_operation = Operation.values()[operation];
        switch (enum_operation){
            case SHARE_FILE:
                if(BrokerUtils.sendShareFileMessage(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(topic,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(caller_broker.getId(),localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(object_to_be_sent,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case SHARE_STORY:
                if(BrokerUtils.sendShareStoryMessage(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(topic,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(caller_broker.getId(),localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(object_to_be_sent,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case SHARE_SUBSCRIBER:
                if(BrokerUtils.sendShareSubscriberMessage(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(topic,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(caller_broker.getId(),localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(object_to_be_sent,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case SHARE_TEXT_MESSAGE:
                if(BrokerUtils.sendShareTextMessageMessage(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(topic,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(caller_broker.getId(),localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(object_to_be_sent,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
            case SHARE_TOPIC:
                if(BrokerUtils.sendShareTopicMessage(localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(topic,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(caller_broker.getId(),localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                if(GeneralUtils.sendMessage(object_to_be_sent,localoutputStream) == null){
                    shutdownConnection();
                    return;
                }
                break;
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
            if (request_socket != null) {
                System.out.println("Shutting down local socket");
                request_socket.close();
            }
        }catch (SocketException socketException){
            System.out.println(ConsoleColors.RED + "Socket error while trying to shutdown publisher" + ConsoleColors.RESET);
        }catch (IOException ioException) {
            System.out.println("Error while shutting down connection");
            ioException.printStackTrace();
        }
    }
}
