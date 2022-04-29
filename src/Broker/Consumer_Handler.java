package Broker;
import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import UserNode.UserNode;
import Broker.Broker;
import Tools.Topic;
import Tools.Messages;
import Tools.Tuple;
import sun.nio.ch.Net;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

class Consumer_Handler implements Runnable {

    private ObjectInputStream localinputStream;
    private ObjectOutputStream localoutputStream;
    private final Socket consumer_connection;
    private final Broker broker;
    private String nickname;


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



    public void ServeRegisterRequest(){
        try {
            System.out.println("Serving register request for client: " + consumer_connection.getInetAddress().getHostName());
            String topic_name = localinputStream.readUTF();
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


    public void receiveNickname() {
        try {
            System.out.println("Receiving client's nickname");
            this.nickname = localinputStream.readUTF();
            System.out.println("Client's nickname is: " + this.nickname);
        } catch (SocketException socketException){
            System.out.println("Socket error");
            shutdownConnection();
        }catch (IOException e){
            System.out.println("Error in receive nickname");
            shutdownConnection();
        }
    }

    @Override
    public void run() {
        while (consumer_connection.isConnected()) {
            System.out.println("Server established connection with client: " + consumer_connection.getInetAddress().getHostAddress());
            Integer message = GeneralUtils.waitForNodePrompt(localinputStream,consumer_connection);
            if(message == null){
                shutdownConnection();
                break;
            }
            Messages message_received = Messages.values()[message];
            switch (message_received){
                case FINISHED_OPERATION:
                    System.out.println("Received finished operation message");
                    break;
                case GET_BROKER_LIST:
                    if(BrokerUtils.sendBrokerList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        break;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    if(BrokerUtils.sendIdList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case SENDING_NICK_NAME:
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case REGISTER:
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case UNSUBSCRIBE:
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                case SHOW_CONVERSATION_DATA:
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        break;
                    }
                    break;
                default:
                    System.out.println("No known message type was received");
                    break;
            }

            if (message == Messages.FINISHED_OPERATION.ordinal()) {
                System.out.println("Finished operation waiting for next input");
                message = waitForUserNodePrompt();
            } else if (message == Messages.GET_BROKER_LIST.ordinal()) {
                System.out.println("Finished sending brokers");
                sendIdList();
                FinishedOperation();
                System.out.println("Finished sending id list");
                message = waitForUserNodePrompt();
            } else if (message == Messages.SENDING_NICK_NAME.ordinal()) {
                receiveNickname();
                FinishedOperation();
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


}
