package NetworkUtilities;

import Logging.ConsoleColors;
import Tools.*;
import UserNode.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class UserNodeUtils {

    /**
     * Sends request message to show conversation data.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer showConversationData(ObjectOutputStream localoutputStream){
        return GeneralUtils.sendMessage(Messages.SHOW_CONVERSATION_DATA,localoutputStream);
    }

    /**
     * Sends a Message type GET_TOPIC_LIST from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer getTopicList(ObjectOutputStream localoutputStream){
        return GeneralUtils.sendMessage(Messages.GET_TOPIC_LIST,localoutputStream);
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
     * Sends a Message type PUSH_FILE from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer push_file(ObjectOutputStream localoutputStream) {
        return GeneralUtils.sendMessage(Messages.PUSH_FILE,localoutputStream);
    }

    /**
     * Sends a Message type PUSH_MESSAGE from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer push_message(ObjectOutputStream localoutputStream) {
        return GeneralUtils.sendMessage(Messages.PUSH_MESSAGE,localoutputStream);
    }

    /**
     * Notifies the broker that there is a new message so publisher and broker can synchronize.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer notifyBrokersNewMessage(ObjectOutputStream localoutputStream){
        return GeneralUtils.sendMessage(Messages.NOTIFY,localoutputStream);
    }

    /**
     * Sends a Message type GET_ID_LIST from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer getIDList(ObjectOutputStream localoutputStream){
        return GeneralUtils.sendMessage(Messages.GET_ID_LIST,localoutputStream);
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
     * @param topic_name Accepts the topic name that the user wants to subscribe to.
     * @param cons Accepts a consumer(UserNode instance).
     * @return Returns -1 if everything goes well. If the broker is not the right broker it returns the index in the consumer broker list. If an error occurs it returns null.
     */
    public static Integer register(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket, String topic_name, UserNode cons){
        if(GeneralUtils.sendMessage(Messages.REGISTER,localoutputStream) == null){
            return null;
        }
        System.out.println("The topic that you are going to be subscribed to: ");
        System.out.println(ConsoleColors.PURPLE + topic_name + ConsoleColors.RESET);
        if(topic_name == null){
            return null;
        }
        System.out.println(ConsoleColors.PURPLE + "Sending topic name" + ConsoleColors.RESET);
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
            //System.out.println("Sending topic name");
            //if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
            //    return null;
            //}
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
     * @param topic_name Accepts the topic name that the user wants to subscribe to.
     * @param cons Accepts a consumer(UserNode instance).
     * @return Returns -1 if everything goes well. If the broker is not the right broker it returns the index in the consumer broker list. If an error occurs it returns null.
     */
    public static Integer unsubscribe(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,String topic_name,UserNode cons){
        if(GeneralUtils.sendMessage(Messages.UNSUBSCRIBE,localoutputStream) == null){
            return null;
        }
        System.out.println("Disconnecting from topic...");
        System.out.println(ConsoleColors.PURPLE + topic_name + ConsoleColors.RESET);
        if(topic_name == null){
            return null;
        }
        System.out.println(ConsoleColors.PURPLE + "Sending topic name" + ConsoleColors.RESET);
        if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
            return null;
        }
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
            //System.out.println("Sending topic name");
            //if(GeneralUtils.sendMessage(topic_name,localoutputStream) == null){
            //    return null;
            //}
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
            System.out.println("\033[0;34m" + "Waiting to receive sending broker list message" + "\033[0m");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if(messagebroker == null){
                return null;
            }else if(messagebroker != Messages.SENDING_BROKER_LIST.ordinal()){
                UserNodeUtils.getBrokerList(localoutputStream);
            }else{
                System.out.println("\033[0;33m" + "Stopping the wait for sending the broker list message because it was just received" + "\033[0m");
                break;
            }
        }
        Integer size;
        System.out.println( "\033[0;34m" + "Waiting to receive broker list size" + "\033[0m");
        if((size = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
            return null;
        }else if(size <= 0){
            System.out.println("\033[0;31m" + "Received empty broker list" + "\033[0m");
            return null;
        }
        System.out.println("Size of list: " + size);
        while(true){
            System.out.println("In while loop for the broker list...");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream, socket);
            if((messagebroker == Messages.FINISHED_OPERATION.ordinal())){
                System.out.println("Received finished operation message in the while loop for the broker list");
                break;
            }
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
                }else if(port <= 0){
                    System.out.println("\033[0;31m" + "Received wrong port number" + "\033[0m");
                    return null;
                }
                System.out.println("Received port: " + port);
                port_list.add(port);
                if(port_list.size()>=2){
                    while(true) {
                        System.out.println("Waiting for finished operation message by the broker in the while loop for sending port array");
                        messagebroker = GeneralUtils.waitForNodePrompt(localinputStream, socket);
                        if (messagebroker == null) {
                            System.out.println("\033[0;31m" + "Received null message while waiting for finished operation message in the port while" + "\033[0m");
                            return null;
                        }else if(messagebroker == Messages.FINISHED_OPERATION.ordinal()){
                            break;
                        }
                    }
                }
            }
            int[] ports = new int[2];
            for (int i = 0; i < port_list.size(); i++) {
                ports[i] = port_list.get(i);
            }
            cons.getBrokerList().add(new Tuple<String,int[]>(IP, ports));
            System.out.println(cons.getBrokerList());
        }
        System.out.println("Broker sent the Broker List");
        return -1;
    }

    public static Integer notifyFailure() {
        return -1;
    }


    /**
     * Sends a text message to the network using the output stream.
     * @param message Accepts a text message class instance.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendTextMessage(Text_Message message, ObjectOutputStream localoutputStream) {
        System.out.println("Sending the publisher of the text message");
        if(GeneralUtils.sendMessage(message.getPublisher(),localoutputStream) == null) {
            return null;
        }
        System.out.println("Sending the creation date for the message");
        if(GeneralUtils.sendMessage(message.getDateCreated(),localoutputStream) == null) {
            return null;
        }
        System.out.println("Sending the contents of the text message");
        if(GeneralUtils.sendMessage(message.getContents(),localoutputStream) == null){
            return null;
        }
        return -1;
    }

    /**
     * Sends a multimedia file to the network using the output stream. Instead of sending the whole file it sends its chunks and its metadata.
     * @param file Accepts a multimedia file class instance.
     * @param localoutputStream Accepts the local output stream.
     * @return Returns -1 if everything goes well. Returns null if an error occurs.
     */
    public static Integer sendFile(MultimediaFile file, ObjectOutputStream localoutputStream) {

        ArrayList<Chunk> chunks = file.getMultimediaFileChunk();
        System.out.println("Sending the file name: " + file.getMultimediaFileName());
        if(GeneralUtils.sendMessage(file.getMultimediaFileName(),localoutputStream) == null) {
            return null;
        }
        System.out.println("Sending date that the file was sent to the network: " + file.getDateCreated());
        if(GeneralUtils.sendMessage(file.getDateCreated(),localoutputStream) == null){
            return null;
        }
        System.out.println("Sending date that the file was created: " + file.getDateCreated());
        if(GeneralUtils.sendMessage(file.getActual_date(),localoutputStream) == null){
            return null;
        }
        System.out.println("Sending publisher name: " + file.getPublisher());
        if(GeneralUtils.sendMessage(file.getPublisher(),localoutputStream) == null){
            return null;
        }
        System.out.println("Sending file's length: " + file.getLength());
        if(GeneralUtils.sendMessage(file.getLength(),localoutputStream) == null){
            return null;
        }
        System.out.println("Informing broker how many chunks there are: " + file.getMultimediaFileChunk().size());
        if(GeneralUtils.sendMessage(file.getMultimediaFileChunk().size(),localoutputStream) == null){
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
            if(GeneralUtils.sendMessage(chunks.get(i),localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Does all the necessary operations in order to push a message or a file given from the command line to the brokers:
     * 1.) Finds the proper broker to push the data to
     * 2.) If the connected broker is the right broker it pushes the file.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param topic_name Accepts the topic_name that it wants to push to.
     * @param pub Accepts a node to access its name and other necessary fiels.
     * @return Returns -1 if everything goes well. Returns null if an error occurs. If the connected broker is the wrong broker it returns its index.
     */
    public static Integer push(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket, String topic_name, UserNode pub, int file_or_text, String contents_file_name) {
        System.out.println("Requesting for proper broker from the connection");
        notifyBrokersNewMessage(localoutputStream);


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
            ArrayList<Topic> topics = receiveTopicList(localoutputStream,localinputStream,socket);
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
                System.out.println("0.Send file");
                System.out.println("1.Send text message");
                Scanner sc;
                switch (file_or_text){
                    case 0:
                        push_message(localoutputStream);
                        Text_Message new_text = new Text_Message(pub.getName(),contents_file_name);
                        sendTextMessage(new_text,localoutputStream);
                        break;
                    case 1:
                        push_file(localoutputStream);
                        MultimediaFile new_file = new MultimediaFile(contents_file_name, pub.getName());
                        sendFile(new_file,localoutputStream);
                        break;
                }

            } else {
                System.out.println("User is not subscribed to topic and can't post there");
                return null;
            }
            return -1;
        }else{
            return null;
        }
    }

    /**
     * Receives topic list from the connected broker.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the topic list if everything goes well. If an error occurs it returns null.
     */
    public static ArrayList<Topic> receiveTopicList(ObjectOutputStream localoutputStream, ObjectInputStream localinputStream, Socket socket){
        ArrayList<Topic> topic_list = new ArrayList<>();
        Integer messagebroker = -1;
        while(true){
            System.out.println("\033[0;34m" + "Waiting to receive sending topic list message" + "\033[0m");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if(messagebroker == null){
                return null;
            }else if(messagebroker != Messages.SENDING_TOPIC_LIST.ordinal()){
                UserNodeUtils.getTopicList(localoutputStream);
            }else{
                System.out.println("\033[0;33m" + "Stopping the wait for sending the topic list message because it was just received" + "\033[0m");
                break;
            }
        }
        Integer size;
        System.out.println( "\033[0;34m" + "Waiting to receive topic list size" + "\033[0m");
        if((size = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
            return null;
        }else if(size <= 0){
            System.out.println("\033[0;31m" + "Received empty topic list" + "\033[0m");
            return null;
        }
        System.out.println("The size of the list is: " + size);
        while (true) {
            System.out.println("Receiving topic list");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if(messagebroker == null){
                return null;
            } else if (messagebroker == Messages.FINISHED_OPERATION.ordinal()) {
                System.out.println("Received finished operation message from broker inside the receive topic list method");
                break;
            }
            System.out.println("Reading topic...");
            Topic temp;
            if((temp = (Topic) GeneralUtils.readObject(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("The topic is: " + temp);
            topic_list.add(temp);
            System.out.println("Waiting to receive finished operation message from broker inside the receive topic list method");
        }
        return topic_list;
    }


    /**
     * Receives the ID list from the connected broker.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the topic list if everything goes well. If an error occurs it returns null.
     */
    public static Integer receiveIDList(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream,Socket socket,UserNode cons){
        Integer messagebroker = -1;
        while(true){
            System.out.println("Waiting to receive sending ID list message");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if(messagebroker == null){
                return null;
            }else if(messagebroker != Messages.SENDING_ID_LIST.ordinal()){
                UserNodeUtils.getIDList(localoutputStream);
            }else{
                System.out.println("\033[0;33m" + "Stopping the wait for sending the ID list message because it was just received" + "\033[0m");
                break;
            }
        }
        Integer size;
        if((size = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
            return null;
        }else if(size <= 0){
            System.out.println("\033[0;31m" + "Received empty ID list" + "\033[0m");
            return null;
        }
        System.out.println("Received size: " + size);
        while(true) {
            System.out.println("Broker is sending its ID List");
            messagebroker = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if ((messagebroker == Messages.FINISHED_OPERATION.ordinal())) {
                System.out.println("Received finished operation message in while loop sending id list");
                break;
            }
            Integer ID;
            if((ID = GeneralUtils.waitForNodePrompt(localinputStream,socket)) == null){
                return null;
            }else if(ID < 0){
                System.out.println("\033[0;31m" + "Received negative ID" + "\033[0m");
                return null;
            }
            System.out.println("Received ID: " + ID);
            cons.getBroker_ids().add(ID);
        }
        return -1;
    }



    public static Integer receiveConversationData(ObjectOutputStream localoutputStream,ObjectInputStream localinputStream,Socket socket,String topic_name){
        System.out.println("Requesting for proper broker from the connection");
        if(showConversationData(localoutputStream) == null){
            return null;
        }


        if (GeneralUtils.sendMessage(topic_name, localoutputStream) == null) {
            return null;
        }
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
            ArrayList<Topic> topics = receiveTopicList(localoutputStream,localinputStream,socket);
            if(topics == null){
                return null;
            }else if(topics.isEmpty()){
                System.out.println("\033[0;31m" + "Received empty list" + "\033[0m");
                return null;
            }
            Topic temp = null;
            for (Topic topic : topics) {
                if (topic.getName().equals(topic_name)) {
                    System.out.println("Found the right topic: " + topic.getName());
                    temp = topic;
                    break;
                }
            }
            if(temp == null){
                System.out.println(ConsoleColors.RED + "There isn't a topic with topic name: " + topic_name + ConsoleColors.RESET);
                return null;
            }
            System.out.println(ConsoleColors.PURPLE + "Showing message queue: " + ConsoleColors.RESET);
            System.out.println(temp.getMessage_queue());
        }
        return -1;
    }

}
