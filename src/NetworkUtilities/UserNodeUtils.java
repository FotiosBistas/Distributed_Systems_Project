package NetworkUtilities;

import Tools.*;
import UserNode.NetworkingForConsumer;
import UserNode.UserNode;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserNodeUtils {

    /**
     * Sends request message to show conversation data.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer showConversationData(ObjectOutputStream localoutputStream){
        if(GeneralUtils.sendMessage(Messages.SHOW_CONVERSATION_DATA,localoutputStream) == null){
            return null;
        }
        return -1;
    }

    /**
     * Sends a Message type GET_BROKER_LIST from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer getBrokerList(ObjectOutputStream localoutputStream) {
        return GeneralUtils.sendMessage(Messages.GET_BROKER_LIST,localoutputStream);
    }

    /**
     * Sends the nickname of the consumer for the broker to be able to uniquely identify the user node.
     * @param localoutputStream Accepts the local output stream.
     * @param cons Accepts a user node object but refers to a consumer node.
     * @return Returns -1 if all goes well. Returns null if an error occurs.
     */
    public static Integer sendNickname(ObjectOutputStream localoutputStream,UserNode cons) {
        if(GeneralUtils.sendMessage(Messages.SENDING_NICK_NAME, localoutputStream) == null){
            return null;
        }
        if(GeneralUtils.sendMessage(cons.getName(), localoutputStream) == null){
            return null;
        }
        return -1;
    }

    /**
     * Requests to register user to the topic using the proper broker.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param sc Accepts a scanner instance in order to read the topic name for the command line.
     * @param cons Accepts a consumer(UserNode instance).
     * @return Returns -1 if everything goes well. If the broker is not the right broker it returns the index in the consumer broker list. If an error occurs it returns null.
     */
    public static Integer register(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream,Socket socket,Scanner sc, UserNode cons){
        if(GeneralUtils.sendMessage(Messages.REGISTER,localoutputStream) == null){
            return null;
        }
        System.out.println("What topic are you interested in?");
        String topic_name = sc.next();
        if(topic_name == null){
            return null;
        }
        if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
            return null;
        }
        Integer message = GeneralUtils.waitForNodePrompt(localinputStream,socket);
        if(message == null){
            return null;
        }
        else if(message == Messages.I_AM_NOT_THE_CORRECT_BROKER.ordinal()){
            System.out.println("\033[0;31m" + "This broker is not the right broker" + "\033[0m");
            Integer index;
            if((index = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
                return null;
            }else if(index > 2 || index < 0){
                System.out.println("\033[0;31m" + "Received erroneous index. Negative or a large number" + "\033[0m");
                return null;
            }
            System.out.println("Read index: " + index);
            return index;
        }else if(message == Messages.I_AM_THE_CORRECT_BROKER.ordinal()){
            System.out.println("Sending topic name");
            if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
                return null;
            }
            System.out.println("Writing consumer object...");
            if(GeneralUtils.sendMessage(cons,localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Sends request to unsubscribe from the specific topic using the proper broker.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param sc Accepts a scanner instance in order to read the topic name for the command line.
     * @param cons Accepts a consumer(UserNode instance).
     * @return Returns -1 if everything goes well. If the broker is not the right broker it returns the index in the consumer broker list. If an error occurs it returns null.
     */
    public static Integer unsubscribe(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,Scanner sc,UserNode cons){
        if(GeneralUtils.sendMessage(Messages.UNSUBSCRIBE,localoutputStream) == null){
            return null;
        }
        System.out.println("Disconnect from what topic?");
        String topic_name = sc.next();
        Integer message = GeneralUtils.waitForNodePrompt(localinputStream,socket);
        if(message == null){
            return null;
        } else if(message == Messages.I_AM_NOT_THE_CORRECT_BROKER.ordinal()){
            System.out.println("\033[0;31m" + "This broker is not the right broker" + "\033[0m");
            Integer index = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if(index == null){
                return null;
            }else if(index > 2 || index < 0){
                System.out.println("\033[0;31m" + "Received erroneous index. Negative or a large number" + "\033[0m");
                return null;
            }
            System.out.println("Read index: " + index);
            return index;
        }else if(message == Messages.I_AM_THE_CORRECT_BROKER.ordinal()){
            System.out.println("Sending topic name");
            if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
                return null;
            }
            System.out.println("Writing consumer object...");
            if(GeneralUtils.sendMessage(cons,localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Receives the broker list from the broker that the consumer first connects to.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param cons Accepts a consumer instance(UserNode object)
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer receiveBrokerList(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,UserNode cons){
        Integer messagebroker = -1;
        while(true){
            System.out.println("In while loop for the broker list...");
            if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                System.out.println("Received finished operation message in the while loop for the broker list");
                break;
            }
            Integer size;
            if((size = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("Size of list: " + size);
            String IP;
            if((IP = GeneralUtils.readUTFString(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("Broker's ip: " + IP);
            System.out.println("Sending broker's ports...");
            ArrayList<Integer> port_list = new ArrayList<>();
            while(true) {
                System.out.println("In while loop for the broker's ports...");
                if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                    System.out.println("Received finished operation message in the while loop for the broker's ports");
                    break;
                }
                Integer port = GeneralUtils.waitForNodePrompt(localinputStream,socket);
                if(port == null){
                    return null;
                }
                System.out.println("Received port: " + port);
                port_list.add(port);
                if(port_list.size()>=3){
                    System.out.println("Waiting for finished operation message by the broker in the while loop for sending port array");
                    messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
                    if(messagebroker == null){
                        return null;
                    }
                }
            }
            int[] ports = new int[3];
            for (int i = 0; i < port_list.size(); i++) {
                ports[i] = port_list.get(i);
            }
            cons.getBrokerList().add(new Tuple<>(IP, ports));
            System.out.println(cons.getBrokerList());
            if(cons.getBrokerList().size() >= size) {
                System.out.println("Waiting for finished operation message by the broker in the while loop for sending broker list");
                messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
                if(messagebroker == null){
                    return null;
                }
            }

        }
        System.out.println("Broker sent the Broker List");
        return -1;
    }

    /**
     * Notifies the broker that there is a new message so publisher and broker can synchronize.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer notifyBrokersNewMessage(ObjectOutputStream localoutputStream){
        System.out.println("Notifying broker that there is a new message");
        if(GeneralUtils.sendMessage(Messages.NOTIFY,localoutputStream) == null){
            return null;
        }
        return -1;
    }

    public static Integer notifyFailure() {
        return -1;
    }

    /**
     * Sends a multimedia file to the network using the output stream. Instead of sending the whole file it sends its chunks.
     * @param file Accepts a multimedia file class instance.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendFile(MultimediaFile file,ObjectOutputStream localoutputStream) {

        ArrayList<Chunk> chunks = file.getChunks();
        System.out.println("Sending the file name: " + file.getMultimediaFileName());
        if(GeneralUtils.sendMessage(file.getMultimediaFileName(),localoutputStream) == null) {
            return null;
        }
        System.out.println("Informing broker how many chunks there are: " + file.getChunks().size());
        if(GeneralUtils.sendMessage(file.getChunks().size(),localoutputStream) == null){
            return null;
        }
        for (int i = 0; i < chunks.size(); i++) {
            System.out.println(i + " Chunk is being sent");
            if(GeneralUtils.sendMessage(i,localoutputStream) == null) {
                return null;
            }
            System.out.println("Sending its actual length...");
            if(GeneralUtils.sendMessage(chunks.get(i).getActual_length(),localoutputStream) == null){
                return null;
            }
            System.out.println("Sending the chunk");
            if(GeneralUtils.sendMessage(chunks.get(i).getChunk(),localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Does all the necessary operations in order to push a file given from the command line to the brokers:
     * 1.) Finds the proper broker to push the data to
     * 2.) If the connected broker is the right broker it pushes the file.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param sc Accepts a scanner instance in order to read from the command line.
     * @param pub Accepts a node to access its name and other necessary fiels.
     * @param thread_continue Resumes the thread for consumer in order to send the file in the background.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer push(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,Scanner sc,UserNode pub,NetworkingForConsumer thread_continue) {
        System.out.println("Requesting for proper broker from the connection");
        if (GeneralUtils.sendMessage(Messages.SEND_APPROPRIATE_BROKER, localoutputStream) == null) {
            return null;
        }

        System.out.println("Please give the name of the topic");
        String topic_name = sc.next();
        if (GeneralUtils.sendMessage(topic_name, localoutputStream) == null) {
            return null;
        }
        boolean subscribed_user = false;
        System.out.println("Waiting to receive finished operation message that the broker received the topic name");
        Integer broker_message;
        if ((broker_message = GeneralUtils.waitForNodePrompt(localinputStream, socket)) == null) {
            return null;
        } else if (broker_message == Messages.FINISHED_OPERATION.ordinal()) {
            System.out.println("Broker received the topic name");
        }
        System.out.println("Waiting to receive correct broker message from the broker");
        if ((broker_message = GeneralUtils.waitForNodePrompt(localinputStream, socket)) == null) {
            return null;
        } else if (Messages.I_AM_NOT_THE_CORRECT_BROKER.ordinal() == broker_message) {
            //waiting for broker to send the index of the correct broker in the broker list
            System.out.println("Receiving the index for the correct broker");
            Integer index;
            if ((index = GeneralUtils.waitForNodePrompt(localinputStream, socket)) == null) {
                return null;
            }
            System.out.println("The index received is: " + index);
            return index;
        } else if (Messages.I_AM_THE_CORRECT_BROKER.ordinal() == broker_message) {
            if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                return null;
            }
            System.out.println("Requesting for topic list from the broker");
            if(GeneralUtils.sendMessage(Messages.GET_TOPIC_LIST,localoutputStream) == null){
                return null;
            }
            ArrayList<Topic> topics = receiveTopicList(localinputStream,socket);
            if(topics == null){
                return null;
            }else if(topics.isEmpty()){
                System.out.println("\033[0;31m" + "Received empty list" + "\033[0m");
                return null;
            }
            for (Topic topic : topics) {
                topic.printSubscribers();
                System.out.println("This publisher's name is: " + pub.getName());
                if (topic.getName().equals(topic_name)) {
                    System.out.println("Found the right topic: " + topic.getName());
                    if (topic.isUserSubscribed(pub.getName())) {
                        System.out.println("This publisher is subcribed to the topic");
                        subscribed_user = true;
                        break;
                    }
                }
            }
            if (subscribed_user) {
                notifyBrokersNewMessage(localoutputStream);
                System.out.println("Give the name of the file");
                String filename = sc.next();
                thread_continue.notifyThread();
                MultimediaFile new_file = new MultimediaFile(filename, "Fotis");
                sendFile(new_file,localoutputStream);
            } else {
                System.out.println("User is not subscribed to topic and can't post there");
                thread_continue.notifyThread();
                return null;
            }
        }
        return -1;
    }

    /**
     * Receives topic list from the connected broker.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the topic list if everything goes well. If an error occurs it returns null.
     */
    public static ArrayList<Topic> receiveTopicList(ObjectInputStream localinputStream, Socket socket){
        ArrayList<Topic> topic_list = new ArrayList<>();
        /*int message_broker = localinputStream.readInt();
        if (message_broker != Messages.SENDING_TOPIC_LIST.ordinal()) {
            while (true) {
                System.out.println("Requesting for broker list again");
                localoutputStream.writeInt(Messages.GET_TOPIC_LIST.ordinal());
                localoutputStream.flush();
                message_broker = localinputStream.readInt();
                if (message_broker == Messages.SENDING_TOPIC_LIST.ordinal()) {
                    System.out.println("Received sending topic list");
                    break;
                }
            }
        }*/
        while (true) {
            System.out.println("Receiving topic list");
            //wait till you are sure that broker is sending the broker list
            System.out.println("Received message that the topic list is being sent and now accepting elements");
            System.out.println("Reading topic list size...");
            Integer size;
            if((size = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("The size of the list is: " + size);
            System.out.println("Reading topic...");
            Topic temp;
            if((temp = (Topic) GeneralUtils.readObject(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("The topic is: " + temp);
            topic_list.add(temp);
            if (topic_list.size() >= size) {
                System.out.println("Waiting to receive finished operation message from broker inside the receive topic list method");
                Integer message_broker;
                if((message_broker = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
                    return null;
                } else if (message_broker == Messages.FINISHED_OPERATION.ordinal()) {
                    System.out.println("Received finished operation message from broker inside the receive topic list method");
                    break;
                }
            }
        }
        return topic_list;
    }

}
