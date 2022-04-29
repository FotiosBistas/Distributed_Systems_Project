package NetworkUtilities;

import Tools.Messages;
import Broker.Broker;
import Tools.Topic;
import Tools.Tuple;
import UserNode.UserNode;


import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class BrokerUtils {
    /**
     * Sends a Message type GET_BROKER_LIST from the Messages ENUM found in the tools package.
     * @param localoutputStream accepts the local output stream.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer getBrokerList(ObjectOutputStream localoutputStream) {
        return GeneralUtils.sendMessage(Messages.GET_BROKER_LIST,localoutputStream);
    }

    /**
     * Sends the topic list of the broker that is in the parameter list.
     * @param localoutputStream accepts the local output stream.
     * @param broker accepts the broker that we want to receive the topic list from.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendTopicList(ObjectOutputStream localoutputStream,Broker broker) {
        if (GeneralUtils.sendMessage(Messages.SENDING_TOPIC_LIST,localoutputStream) == null) {
            return null;
        }
        int list_size = broker.getTopics().size();
        for (int i = 0; i < list_size; i++) {
            System.out.println("Sending topic list size...");
            if (GeneralUtils.sendMessage(list_size,localoutputStream) == null) {
                return null;
            }
            System.out.println("Sending topic: " + broker.getTopics().get(i));
            if (GeneralUtils.sendMessage(broker.getTopics().get(i),localoutputStream) == null) {
                return null;
            }
        }
        return -1;
    }

    /**
     * Finds the correct broker for the specific topic name.
     * @param localoutputStream Accepts the local output stream.
     * @param broker Accepts the broker that we want to receive the topic list from. This broker created the specific handler.
     * @param topic Accepts the topic name to find the appropriate broker for the specific topic name
     * @return True if the broker is the responsible broker for the topic. False if the broker is not responsible for the topic. If it returns null there was an error.
     */
    public static Boolean isCorrectBroker(ObjectOutputStream localoutputStream, Broker broker, String topic) {
        GeneralUtils.FinishedOperation(localoutputStream);
        int index = broker.hashTopic(topic);
        Tuple<String, int[]> brk = broker.getBrokerList().get(index);
        System.out.println("Brk IP: " + brk.getValue1());
        System.out.println("Brk ports: " + Arrays.toString(brk.getValue2()));
        System.out.println("Local Broker IP: " + broker.getIp());
        if (brk.getValue1().equals(broker.getIp())) {
            System.out.println("They have equal IPs");
            if (broker.getConsumer_port() == brk.getValue2()[0] &&
                    broker.getPublisher_port() == brk.getValue2()[1] && broker.getBroker_port() == brk.getValue2()[2]) {
                System.out.println("The broker is correct. Sending index of the broker.");
                if (GeneralUtils.sendMessage(Messages.I_AM_THE_CORRECT_BROKER,localoutputStream) == null) {
                    return null;
                }
                if (GeneralUtils.sendMessage(index,localoutputStream) == null) {
                    return null;
                }
                return true;
            } else {
                System.out.println("The broker is not correct. Sending index of the correct broker");
                if (GeneralUtils.sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER,localoutputStream) == null) {
                    return null;
                }
                if (GeneralUtils.sendMessage(index,localoutputStream) == null) {
                    return null;
                }
                return false;
            }
        } else {
            if (GeneralUtils.sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER,localoutputStream) == null) {
                return null;
            }
            if (GeneralUtils.sendMessage(index,localoutputStream) == null) {
                return null;
            }
            return false;
        }
    }

    /**
     * Reads the topic name from the input stream.
     * @param localinputStream  Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the corresponding socket of the streams.
     * @return Returns the string that is read from the input stream if everything worked properly.If it returns null there was an error.
     */
    public static String receiveTopicName(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        String topic_name = GeneralUtils.readUTFString(localinputStream,socket);
        if (topic_name == null) {
            return null;
        }
        System.out.println("Received topic name: " + topic_name);
        return topic_name;
    }

    /**
     * Receives all the chunk for the specific file that is read from the input stream.
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer receiveFile(ObjectInputStream localinputStream,Socket socket) {
        try {
            System.out.println("Receiving file...");
            String file_name = GeneralUtils.readUTFString(localinputStream,socket);
            if (file_name == null) {
                return null;
            }
            String new_file = file_name.substring(file_name.lastIndexOf("\\") + 1);
            System.out.println("Received file: " + new_file);
            Integer number_of_chunks = GeneralUtils.waitForNodePrompt(localinputStream,socket);
            if (number_of_chunks == null) {
                return null;
            }
            System.out.println("You will receive: " + number_of_chunks + " chunks");
            String path_for_broker = "C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\";
            System.out.println(path_for_broker + new_file);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
            System.out.println("Receiving file...");
            byte[] buffer = new byte[512 * 1024];
            ArrayList<byte[]> chunks = new ArrayList<>();
            while (true) {
                //if you received all the chunks end the operation
                if (number_of_chunks == chunks.size()) {
                    System.out.println("Finished receiving: " + chunks.size() + "chunks");
                    fileOutputStream.close();
                    break;
                }
                Integer index = GeneralUtils.waitForNodePrompt(localinputStream,socket);
                if(index == null){
                    return null;
                }
                System.out.println("You are receiving chunk: " + index);
                Integer actual_size = GeneralUtils.waitForNodePrompt(localinputStream,socket);
                if(actual_size == null){
                    return null;
                }
                System.out.println("Actual size of the incoming chunk is: " + actual_size);
                if((buffer = GeneralUtils.readBuffer(localinputStream,buffer,0,actual_size)) == null){
                    return null;
                }
                byte[] temp = buffer.clone();
                fileOutputStream.write(temp, 0, actual_size);
                fileOutputStream.flush();
                chunks.add(temp);
                System.out.println("Chunks size now is: " + chunks.size());
            }
            System.out.println("Finished receiving file");
            return -1;
        } catch (IOException ioException) {
            System.out.println("asdfasdf");
            return null;
        }
    }


    /**
     * Sends the broker list using a for loop. When sending each broker it must also send its two port numbers. The broker list contains Tuples of type <String,int[]> string being the IP and int[] are the ports.
     * @param localoutputStream accepts the local output stream.
     * @param broker Accepts the broker that we want to receive the broker list from. This broker created the specific handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer sendBrokerList(ObjectOutputStream localoutputStream, Broker broker) {
        GeneralUtils.sendMessage(Messages.SENDING_TOPIC_LIST, localoutputStream);
        for (Tuple<String, int[]> val : broker.getBrokerList()) {
            System.out.println("Sending Broker List size: " + broker.getBrokerList().size());
            if(GeneralUtils.sendMessage(broker.getBrokerList().size(),localoutputStream) == null){
                return null;
            }
            System.out.println("Sending broker's IP: " + val.getValue1());
            if(GeneralUtils.sendMessage(val.getValue1(),localoutputStream) == null){
                return null;
            }
            int i;
            for (i = 0; i < val.getValue2().length; i++) {
                System.out.println("Sending broker's ports: " + val.getValue2()[i]);
                if(GeneralUtils.sendMessage(val.getValue2()[i],localoutputStream) == null){
                    return null;
                }
            }
            if (i == 3) {
                System.out.println("Finished sending ports");
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    return null;
                }
            }
        }
        return -1;
    }

    /**
     * Sends the ID list of the brokers. IDs are the identifier that have came up from the SHA1 hashing.
     * @param localoutputStream accepts the local output stream.
     * @param broker Accepts the broker that we want to receive the id list from. This broker created the specific handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer sendIdList(ObjectOutputStream localoutputStream,Broker broker) {
        GeneralUtils.sendMessage(Messages.SENDING_ID_LIST, localoutputStream);
        for (int i = 0; i < broker.getId_list().size(); i++) {
            if(GeneralUtils.sendMessage(broker.getId_list().size(), localoutputStream) == null){
                return null;
            }
            if(GeneralUtils.sendMessage(broker.getId_list().get(i), localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    public static Integer ServerUnsubscribeRequest(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,Broker broker){

        System.out.println("Serving unsubscribe request");
        String topic_name;
        if((topic_name = GeneralUtils.readUTFString(localinputStream,socket)) == null){
            return null;
        }
        Boolean correct = isCorrectBroker(localoutputStream,broker,topic_name);
        if(correct) {
            UserNode new_cons = (UserNode) localinputStream.readObject();
            System.out.println("Topic name: " + topic_name);
            Topic topic = null;
            for (int i = 0; i < broker.getTopics().size(); i++) {
                if (topic_name.equals(broker.getTopics().get(i).getName())) {
                    topic = broker.getTopics().get(i);
                }
            }
            System.out.println("Unsubscribing user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " from topic: " + topic_name);
            broker.UnsubscribeFromTopic(topic, new_cons.getName());
        }
    }
}
