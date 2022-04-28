package Tools;
import UserNode.UserNode;
import Broker.Broker;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkUtils {



    /**
     * Reads a UTF string from the input stream using the inputStream.readUTF() method.Shutdowns the connection if a exception is thrown.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns null as an error string.
     */
    public static String readUTFString(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket){
        try {
            System.out.println("Waiting to read UTF type");
            String message = localinputStream.readUTF();
            System.out.println("Received message: " + message + " from node: " + socket.getInetAddress());
            return message;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        } catch (IOException e) {
            System.out.println("Shutting down connection in read UTF string...");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        }
    }

    /**
     * Waits until an int input is read by the input stream using the inputStream.readInt() method.Shutdowns the connection if a exception is thrown.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the input read from the local input stream. If it catches an exception it returns -1 as an error int.
     */
    public static Integer waitForNodePrompt(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        try {
            System.out.println("Waiting for node prompt");
            int message = localinputStream.readInt();
            System.out.println("Received message: " + message + " from node: " + socket.getInetAddress());
            return message;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        } catch (IOException e) {
            System.out.println("Shutting down connection in wait for node prompt...");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        }
    }

    /**
     * Sends a serializable object through the output stream. Shutdowns the connection if a exception is thrown.
     * @param message           Accepts any int and sends it as a message.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket            accepts the corresponding socket of the streams.
     */
    public static Integer sendMessage(Object message, ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        try {
            System.out.println("Sending Message: " + message);
            localoutputStream.writeObject(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;

        } catch (IOException e) {
            System.out.println("Error in send message");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        }
    }

    /**
     * Sends int message type. Shutdowns the connection if a exception is thrown.
     * @param message           Accepts any int and sends it as a message.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket            accepts the corresponding socket of the streams.
     */
    public static Integer sendMessage(int message, ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        try {
            System.out.println("Sending Message: " + message);
            localoutputStream.writeInt(message);
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        } catch (IOException e) {
            System.out.println("Error in send message");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        }
    }

    /**
     * Sends any messages from the Message ENUM found in the tools package. Shutdowns the connection if a exception is thrown.
     *
     * @param message_type      Accepts any message type from the Messages ENUM found in the tools package.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket            accepts the corresponding socket of the streams.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendMessage(Messages message_type, ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        try {
            System.out.println("Sending Message: " + message_type);
            localoutputStream.writeInt(message_type.ordinal());
            localoutputStream.flush();
            return -1;
        } catch (SocketException socketException) {
            System.out.println("Socket error");
            //shutdownConnection(localinputStream, localoutputStream, socket);
            return null;
        } catch (IOException e) {
            System.out.println("Error in send message");
            //shutdownConnection(localinputStream, localoutputStream, socket);\
            return null;
        }
    }

    /**
     * Sends a Message type FINISHED_OPERATION from the Messages ENUM found in the tools package.
     *
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket            accepts the corresponding socket of the streams.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer FinishedOperation(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        return sendMessage(Messages.FINISHED_OPERATION, localinputStream, localoutputStream, socket);
    }

    /**
     * Sends a Message type GET_BROKER_LIST from the Messages ENUM found in the tools package.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer getBrokerList(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        System.out.println("Sending message: " + Messages.GET_BROKER_LIST);
        return sendMessage(Messages.GET_BROKER_LIST, localinputStream, localoutputStream, socket);
    }

    /**
     * Sends the topic list of the broker that is in the parameter list.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @param broker accepts the broker that we want to receive the topic list from.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendTopicList(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket,Broker broker) {
        if(sendMessage(Messages.SENDING_TOPIC_LIST, localinputStream, localoutputStream, socket) == null){
            return null;
        }
        int list_size = broker.getTopics().size();
        for (int i = 0; i < list_size; i++) {
            System.out.println("Sending topic list size...");
            if(sendMessage(list_size, localinputStream, localoutputStream, socket) == null){
                return null;
            }
            System.out.println("Sending topic: " + broker.getTopics().get(i));
            if(sendMessage(broker.getTopics().get(i), localinputStream, localoutputStream, socket) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Finds the correct broker for the specific topic name.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @param broker accepts the broker that we want to receive the topic list from.
     * @param topic accepts the topic name to find the appropriate broker for the specific topic name
     * @return True if the broker is the responsible broker for the topic. False is the publisher must publish to another broker. If it returns null there was an error.
     */
    public static Boolean isCorrectBroker(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket,Broker broker,String topic){
        NetworkUtils.FinishedOperation(localinputStream,localoutputStream,socket);
        int index = broker.hashTopic(topic);
        Tuple<String,int[]> brk = broker.getBrokerList().get(index);
        System.out.println("Brk IP: " + brk.getValue1());
        System.out.println("Brk ports: " + Arrays.toString(brk.getValue2()));
        System.out.println("Local Broker IP: " + broker.getIp());
        if(brk.getValue1().equals(broker.getIp())){
            System.out.println("They have equal IPs");
            if(broker.getConsumer_port() == brk.getValue2()[0] &&
                    broker.getPublisher_port() == brk.getValue2()[1] && broker.getBroker_port() == brk.getValue2()[2]){
                System.out.println("The broker is correct. Sending index of the broker.");
                if(NetworkUtils.sendMessage(Messages.I_AM_THE_CORRECT_BROKER,localinputStream,localoutputStream,socket) == null){
                    return null;
                };
                if(NetworkUtils.sendMessage(index,localinputStream,localoutputStream,socket) == null){
                    return null;
                };
                return true;
            }else{
                System.out.println("The broker is not correct. Sending index of the correct broker");
                if(NetworkUtils.sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER,localinputStream,localoutputStream,socket) == null){
                    return null;
                }
                if(NetworkUtils.sendMessage(index,localinputStream,localoutputStream,socket) == null){
                    return null;
                }
                return false;
            }
        }else{
            if(NetworkUtils.sendMessage(Messages.I_AM_NOT_THE_CORRECT_BROKER,localinputStream,localoutputStream,socket) == null){
                return null;
            }
            if(NetworkUtils.sendMessage(index,localinputStream,localoutputStream,socket) == null){
                return null;
            }
            return false;
        }
    }

    /**
     * Reads the topic name from the input stream.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return Returns the string that is read from the input stream if everything worked properly.If it returns null there was an error.
     */
    public static String receiveTopicName(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket){
        String topic_name = readUTFString(localinputStream,localoutputStream,socket);
        if(topic_name == null){
            return null;
        }
        System.out.println("Received topic name: " + topic_name);
        return topic_name;
    }

    public static void notifyPublisher(Topic topic){

    }

    /**
     * Receives all the chunk for the specific file that is read from the input stream.
     * @param localinputStream  accepts the local input stream.
     * @param localoutputStream accepts the local output stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer receiveFile(ObjectInputStream localinputStream, ObjectOutputStream localoutputStream, Socket socket) {
        try {
            System.out.println("Receiving file...");
            String file_name = readUTFString(localinputStream, localoutputStream, socket);
            if (file_name == null) {
                return null;
            }
            String new_file = file_name.substring(file_name.lastIndexOf("\\") + 1);
            System.out.println("Received file: " + new_file);
            Integer number_of_chunks = waitForNodePrompt(localinputStream, localoutputStream, socket);
            if (number_of_chunks == null) {
                return null;
            }
            System.out.println("You will receive: " + number_of_chunks + " chunks");
            String path_for_broker = "C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\";
            System.out.println(path_for_broker + new_file);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
            System.out.println("Receiving file...");
            byte[] buffer = new byte[512*1024];
            ArrayList<byte[]> chunks = new ArrayList<>();
            while (true) {
                //if you received all the chunks end the operation
                if (number_of_chunks == chunks.size()) {
                    System.out.println("Finished receiving: " + chunks.size() + "chunks");
                    fileOutputStream.close();
                    break;
                }
                int index = localinputStream.readInt();
                System.out.println("You are receiving chunk: " + index);
                int actual_size = localinputStream.readInt();
                System.out.println("Actual size of the incoming chunk is: " + actual_size);
                localinputStream.readFully(buffer, 0, actual_size);
                byte[] temp = buffer.clone();
                fileOutputStream.write(temp, 0, actual_size);
                fileOutputStream.flush();
                chunks.add(temp);
                System.out.println("Chunks size now is: " + chunks.size());
            }
            System.out.println("Finished receiving file");
            return -1;
        }catch (IOException ioException){
            System.out.println("asdfasdf");
            return null;
        }
    }
}
