package Broker;

import NetworkUtilities.BrokerUtils;
import NetworkUtilities.GeneralUtils;
import Tools.Messages;
import Tools.Topic;
import UserNode.UserNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Consumer_Handler implements Runnable {

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



    @Override
    public void run() {
        System.out.println("Server established connection with client: " + consumer_connection.getInetAddress().getHostAddress());
        while (consumer_connection.isConnected()) {
            Integer message = GeneralUtils.waitForNodePrompt(localinputStream,consumer_connection);
            if(message == null){
                shutdownConnection();
                return;
            }else if(message > Messages.values().length){
                System.out.println("\033[0;31m" + "Out of enum bounds message received " + "\033[0m");
                continue;
            }
            String topic_name;
            Boolean correct;
            System.out.println("\033[0;35m" + "Received index: " + message + "\033[0m");
            Messages message_received = Messages.values()[message];
            switch (message_received){
                case FINISHED_OPERATION:
                    System.out.println("Received finished operation message");
                    break;
                case GET_BROKER_LIST:
                    if(BrokerUtils.sendBrokerList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case GET_ID_LIST:
                    if(BrokerUtils.sendIdList(localoutputStream,this.broker) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case SENDING_NICK_NAME:
                    if(BrokerUtils.receiveNickname(localinputStream,consumer_connection) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case REGISTER:
                    if(BrokerUtils.ServeRegisterRequest(localinputStream,localoutputStream,consumer_connection,this.broker) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case UNSUBSCRIBE:
                    if(BrokerUtils.ServeUnsubscribeRequest(localinputStream,localoutputStream,consumer_connection,this.broker) == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    break;
                case PULL:
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    topic_name = BrokerUtils.receiveTopicName(localinputStream,localoutputStream,consumer_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    String user_name = GeneralUtils.readUTFString(localinputStream,consumer_connection);
                    if(user_name == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }

                    correct = BrokerUtils.isCorrectBroker(localoutputStream,this.broker,topic_name);
                    if(correct == null){
                        shutdownConnection();
                        return;
                    } else if(!correct){
                        shutdownConnection();
                        return;
                    }

                    if(BrokerUtils.servePullRequest(localoutputStream,localinputStream,consumer_connection,topic_name,user_name,broker) == null){
                        shutdownConnection();
                        return;
                    }
                case SHOW_CONVERSATION_DATA:
                    topic_name =  BrokerUtils.receiveTopicName(localinputStream,localoutputStream,consumer_connection);
                    if(topic_name == null){
                        shutdownConnection();
                        return;
                    }
                    if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                        shutdownConnection();
                        return;
                    }
                    correct = BrokerUtils.isCorrectBroker(localoutputStream,this.broker,topic_name);
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
    public void removeConnection(){
        broker.getConsumer_Handlers().remove(this);
    }

    /**
     * Terminates the local socket along with it's corresponding input and output streams.It throws a IO exception if something goes wrong.
     */
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
        }catch (SocketException socketException){
            System.out.println("\033[0;31m" + "Connection was either closed already or a socket error occurred" + "\033[0m");
        } catch(IOException e){
            e.printStackTrace();
        }
    }


}
