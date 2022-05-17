package Broker;

import Logging.ConsoleColors;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import NetworkUtilities.UserNodeUtils;
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
    private final Operation operation;

    enum Operation{
        SHARE_TOPIC, //0
        SHARE_FILE, //1
        SHARE_STORY,//2
        SHARE_TEXT_MESSAGE,//3
        SHARE_SUBSCRIBER,//4
        SHARE_DISCONNECT//5
    }

    SendObject(Socket request_socket, Broker caller_broker,Operation operation, Object object_to_be_sent,String topic){
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

        switch (operation) {
            case SHARE_TOPIC -> {
                System.out.println("Sharing topic");
                if (BrokerUtils.sendShareTopicMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            case SHARE_FILE -> {
                System.out.println("Sharing file");
                if (BrokerUtils.sendShareFileMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (UserNodeUtils.sendFile((MultimediaFile) object_to_be_sent, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            case SHARE_STORY -> {
                System.out.println("Sharing story");
                if (BrokerUtils.sendShareStoryMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (UserNodeUtils.sendStory((Story) object_to_be_sent, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            case SHARE_TEXT_MESSAGE -> {
                System.out.println("Sharing text message");
                if (BrokerUtils.sendShareTextMessageMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (UserNodeUtils.sendTextMessage((Text_Message) object_to_be_sent, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            case SHARE_SUBSCRIBER -> {
                System.out.println("Sharing subscriber");
                if (BrokerUtils.sendShareSubscriberMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(object_to_be_sent, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            case SHARE_DISCONNECT -> {
                System.out.println("Sharing disconnect");
                if (BrokerUtils.sendShareDisconnectMessage(localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(topic, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(caller_broker.getId(), localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
                if (GeneralUtils.sendMessage(object_to_be_sent, localoutputStream) == null) {
                    shutdownConnection();
                    return;
                }
            }
            default ->
                    System.out.println(ConsoleColors.RED + "No known message types was received" + ConsoleColors.RESET);
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
