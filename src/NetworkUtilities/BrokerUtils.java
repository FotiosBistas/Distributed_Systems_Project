package NetworkUtilities;


import Broker.Broker;
import Tools.*;
import UserNode.UserNode;
import SHA1.SHA1;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class BrokerUtils {

    /**
     * Sends the topic list of the broker that is in the parameter list.
     * @param localoutputStream accepts the local output stream.
     * @param broker accepts the broker that we want to receive the topic list from.
     * @return returns the exit value of the program -1 indicating success and null indicating error.
     */
    public static Integer sendTopicList(ObjectOutputStream localoutputStream, Broker broker) {
        if (GeneralUtils.sendMessage(Messages.SENDING_TOPIC_LIST,localoutputStream) == null) {
            return null;
        }
        int list_size = broker.getTopics().size();
        System.out.println("Sending topic list size...");
        if (GeneralUtils.sendMessage(list_size,localoutputStream) == null) {
            return null;
        }
        for (int i = 0; i < list_size; i++) {
            if (GeneralUtils.sendMessage(Messages.SENDING_TOPIC_LIST,localoutputStream) == null) {
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
        int index = broker.hashTopic(topic);
        Tuple<String, int[]> brk = broker.getBrokerList().get(index);
        System.out.println("Brk IP: " + brk.getValue1());
        System.out.println("Brk ports: " + Arrays.toString(brk.getValue2()));
        System.out.println("Local Broker IP: " + broker.getIp());
        if (brk.getValue1().equals(broker.getIp())) {
            System.out.println("They have equal IPs");
            if (broker.getConsumer_port() == brk.getValue2()[0] &&
                    broker.getPublisher_port() == brk.getValue2()[1]) {
                System.out.println("The broker is correct. Sending index of the broker.");
                if (GeneralUtils.sendMessage(Messages.I_AM_THE_CORRECT_BROKER,localoutputStream) == null) {
                    return null;
                }
                //if (GeneralUtils.sendMessage(index,localoutputStream) == null) {
                //    return null;
                //}
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
     * Receives the contents of a text message file along with its metadata.
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Text_Message receiveTextMessage(ObjectInputStream localinputStream, Socket socket) {
        System.out.println("Receiving text message...");
        System.out.println("Receiving publisher");
        String publisher;
        if((publisher = GeneralUtils.readUTFString(localinputStream,socket)) == null) {
            return null;
        }
        String date_created;
        if((date_created = GeneralUtils.readUTFString(localinputStream,socket)) == null){
            return null;
        }
        String contents;
        if((contents = GeneralUtils.readUTFString(localinputStream,socket)) == null){
            return null;
        }
        Text_Message new_text_message = new Text_Message(publisher,date_created,contents);
        System.out.println(new_text_message);
        return new_text_message;
    }

    public static Integer sendID(ObjectOutputStream objectOutputStream,Broker caller_broker){
        return GeneralUtils.sendMessage(caller_broker.getId(),objectOutputStream);
    }

    public static Integer sendShareTopicMessage(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.SHARE_TOPIC,objectOutputStream);
    }

    public static Integer sendShareFileMessage(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.SHARE_FILE,objectOutputStream);
    }

    public static Integer sendShareStoryMessage(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.SHARE_STORY,objectOutputStream);
    }

    public static Integer sendShareTextMessageMessage(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.SHARE_TEXT_MESSAGE,objectOutputStream);
    }
    public static Integer sendShareSubscriberMessage(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.SHARE_SUBSCRIBER,objectOutputStream);
    }

    public static Integer sendRegister(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.REGISTER,objectOutputStream);
    }

    public static Integer sendDisconnect(ObjectOutputStream objectOutputStream){
        return GeneralUtils.sendMessage(Messages.UNSUBSCRIBE,objectOutputStream);
    }

    /**
     * Receives all the chunks for the specific file that is read from the input stream.
     * @param localinputStream  accepts the local input stream.
     * @param socket accepts the corresponding socket of the streams.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static MultimediaFile receiveFile(ObjectInputStream localinputStream, Socket socket) {
        System.out.println("Receiving file...");
        String file_name = GeneralUtils.readUTFString(localinputStream,socket);
        if (file_name == null) {
            return null;
        }
        System.out.println("Receiving the date that the file was sent to the network...");
        String date_created = GeneralUtils.readUTFString(localinputStream,socket);
        if (date_created == null) {
            return null;
        }
        System.out.println("Receiving the actual date of the file...");
        String actual_date = GeneralUtils.readUTFString(localinputStream,socket);
        if (actual_date == null) {
            return null;
        }
        System.out.println("Receiving publisher name...");
        String publisher = GeneralUtils.readUTFString(localinputStream,socket);
        if (publisher == null) {
            return null;
        }
        System.out.println("Receiving file's size...");
        Long size = GeneralUtils.readLong(localinputStream,socket);
        if (size == null) {
            return null;
        }
        String new_file = file_name.substring(file_name.lastIndexOf("\\") + 1);
        System.out.println("Received file: " + new_file);
        Integer number_of_chunks = GeneralUtils.waitForNodePrompt(localinputStream,socket);
        if (number_of_chunks == null) {
            return null;
        }
        System.out.println("You will receive: " + number_of_chunks + " chunks");
        //String path_for_broker = "C:\\Users\\fotis\\OneDrive\\Desktop\\receive_files\\";
        //System.out.println(path_for_broker + new_file);
        //FileOutputStream fileOutputStream = new FileOutputStream(new File(path_for_broker + new_file));
        System.out.println("Receiving file...");
        Chunk received_chunk;
        ArrayList<Chunk> chunks = new ArrayList<>();
        while (true) {
            //if you received all the chunks end the operation
            if (number_of_chunks == chunks.size()) {
                System.out.println("Finished receiving: " + chunks.size() + "chunks");
                //fileOutputStream.close();
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
            if((received_chunk = (Chunk) GeneralUtils.readObject(localinputStream,socket)) == null){
                return null;
            }
            //fileOutputStream.write(temp, 0, actual_size);
            //fileOutputStream.flush();
            chunks.add(received_chunk);
            System.out.println("Chunks size now is: " + chunks.size());
        }
        System.out.println("Finished receiving file");
        MultimediaFile new_m_file = new MultimediaFile(publisher,date_created,new_file,actual_date,size,chunks);
        return new_m_file;
    }

    /**
     * Receives story by calling receive file and after it returns the file it creates the story.
     * @param localinputStream Accepts the local input stream
     * @param socket Accepts the local socket.
     * @return Returns the new story if everything works properly. If an error occurs it returns null.
     */
    public static Story receiveStory(ObjectInputStream localinputStream,Socket socket){
        MultimediaFile new_file = receiveFile(localinputStream,socket);
        if(new_file == null){
            return null;
        }
        System.out.println("Finished receiving story");
        Story new_Story = new Story(new_file);
        return new_Story;
    }

    /**
     * Sends the broker list using a for loop. When sending each broker it must also send its two port numbers. The broker list contains Tuples of type <String,int[]> string being the IP and int[] are the ports.
     * @param localoutputStream accepts the local output stream.
     * @param broker Accepts the broker that we want to receive the broker list from. This broker created the specific handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer sendBrokerList(ObjectOutputStream localoutputStream, Broker broker) {
        if(GeneralUtils.sendMessage(Messages.SENDING_BROKER_LIST, localoutputStream) == null){
            return null;
        }
        System.out.println("\033[0;32m" + "Sending Broker List size: " + broker.getBrokerList().size() + "\033[0m");
        if(GeneralUtils.sendMessage(broker.getBrokerList().size(),localoutputStream) == null){
            return null;
        }
        for (Tuple<String, int[]> val : broker.getBrokerList()) {
            if(GeneralUtils.sendMessage(Messages.SENDING_BROKER_LIST, localoutputStream) == null){
                return null;
            }
            System.out.println("\033[0;32m" + "Sending broker's IP: " + val.getValue1() + "\033[0m");
            if(GeneralUtils.sendMessage(val.getValue1(),localoutputStream) == null){
                return null;
            }
            int i;
            for (i = 0; i < val.getValue2().length; i++) {
                System.out.println("\033[0;32m" + "Sending broker's ports: " + val.getValue2()[i] + "\033[0m");
                if(GeneralUtils.sendMessage(val.getValue2()[i],localoutputStream) == null){
                    return null;
                }
            }
            if (i == 2) {
                System.out.println("\033[0;32m" + "Finished sending ports" + "\033[0m");
                if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                    return null;
                }
            }
        }
        return -1;
    }

    /**
     * Sends the ID list of the brokers. IDs are the identifier that have came up from the SHA1 hashing.
     * @param localoutputStream Accepts the local output stream.
     * @param broker Accepts the broker that we want to receive the id list from. This broker created the specific handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer sendIdList(ObjectOutputStream localoutputStream,Broker broker) {
        if(GeneralUtils.sendMessage(Messages.SENDING_ID_LIST, localoutputStream) == null){
            return null;
        }
        if(GeneralUtils.sendMessage(broker.getId_list().size(), localoutputStream) == null){
            return null;
        }
        for (int i = 0; i < broker.getId_list().size(); i++) {
            if(GeneralUtils.sendMessage(Messages.SENDING_ID_LIST, localoutputStream) == null){
                return null;
            }
            if(GeneralUtils.sendMessage((int)broker.getId_list().get(i), localoutputStream) == null){
                return null;
            }
        }
        return -1;
    }

    /**
     * Serves unsubscribe request for the specific consumer that is read through the input stream to the corresponding topic that is read from the input stream.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param broker Accepts the broker that is responsible for the handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer ServeUnsubscribeRequest(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,Broker broker){

        System.out.println("\033[0;32m" + "Serving unsubscribe request" + "\033[0m");
        String topic_name;
        if((topic_name = GeneralUtils.readUTFString(localinputStream,socket)) == null){
            return null;
        }
        Boolean correct;
        if((correct = isCorrectBroker(localoutputStream,broker,topic_name)) == null){
            return null;
        }
        if(correct) {
            UserNode new_cons;
            if((new_cons = (UserNode) GeneralUtils.readObject(localinputStream,socket)) == null){
                return null;
            }
            System.out.println("\033[0;32m" + "Topic name: " + topic_name + "\033[0m");
            Topic topic = null;
            for (int i = 0; i < broker.getTopics().size(); i++) {
                if (topic_name.equals(broker.getTopics().get(i).getName())) {
                    topic = broker.getTopics().get(i);
                }
            }
            if(topic == null){
                System.out.println( "\033[0;31m" + "There is no such topic in the topic list of the broker" + "\033[0m");
                if(GeneralUtils.sendMessage(Messages.NO_SUCH_TOPIC,localoutputStream) == null){
                    return null;
                }
                return null;
            }
            System.out.println("\033[0;32m" + "Unsubscribing user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " from topic: " + topic_name + "\033[0m");
            broker.UnsubscribeFromTopic(topic, new_cons.getName());
        }else{
            System.out.println("\033[0;31m" + "This is not the correct broker for the topic" + "\033[0m");
            return null;
        }
        return -1;
    }

    /**
     * Serves subscribe request for the specific consumer that is read through the input stream to the corresponding topic that is read from the input stream. If the topic doesn't exist in the broker it creates a new one with the
     * specific name.
     * @param localinputStream Accepts the local input stream.
     * @param localoutputStream Accepts the local output stream.
     * @param socket Accepts the local socket.
     * @param broker Accepts the broker that is responsible for the handler.
     * @return Returns -1 if everything worked properly.If it returns null there was an error.
     */
    public static Integer ServeRegisterRequest(ObjectInputStream localinputStream,ObjectOutputStream localoutputStream,Socket socket,Broker broker){
        String topic_name;
        if((topic_name = GeneralUtils.readUTFString(localinputStream,socket)) == null){
            return null;
        }
        Boolean correct;
        if((correct = BrokerUtils.isCorrectBroker(localoutputStream,broker,topic_name)) == null){
            return null;
        }
        if(correct) {
            UserNode new_cons;
            if ((new_cons = (UserNode) GeneralUtils.readObject(localinputStream, socket)) == null) {
                return null;
            }
            System.out.println("Topic name: " + topic_name);
            Topic topic = null;
            for (int i = 0; i < broker.getTopics().size(); i++) {
                if (topic_name.equals(broker.getTopics().get(i).getName())) {
                    topic = broker.getTopics().get(i);
                }
            }
            if(topic == null){
                System.out.println( "\033[0;31m" + "There is no such topic in the topic list of the broker" + "\033[0m");
                if(GeneralUtils.sendMessage(Messages.NO_SUCH_TOPIC,localoutputStream) == null){
                    return null;
                }
                System.out.println("Creating new topic");
                broker.createTopic(topic_name,new_cons.getName());
                return null;
            }
            if(GeneralUtils.FinishedOperation(localoutputStream) == null){
                return null;
            }
            System.out.println("Registering user with IP: " + new_cons.getIp() + " and port: " + new_cons.getPort() + " to topic: " + topic_name);
            broker.addConsumerToTopic(topic, new_cons.getName());
        }else{
            System.out.println("\033[0;31m" + "This is not the correct broker for the topic" + "\033[0m");
            return null;
        }
        return -1;
    }

    /**
     * Receives the nickname of the user node and hashes it for broker to be able to uniquely identify the user node.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @return Returns the nickname if everything worked properly.If it returns null there was an error.
     */
    public static String receiveNickname(ObjectInputStream localinputStream, Socket socket) {
        System.out.println( "\033[0;32m" + "Receiving client's nickname" + "\033[0m");
        String nickname;
        if((nickname = SHA1.encrypt(GeneralUtils.readUTFString(localinputStream,socket))) == null){
            return null;
        }
        System.out.println("\033[0;32m" + "Client's nickname is: " + nickname + "\033[0m");
        return nickname;
    }


    /**
     * Serves the pull request for the specific topic in the parameter list. It sends its message queue.
     * @param localinputStream Accepts the local input stream.
     * @param socket Accepts the local socket.
     * @param topic_name Accepts the topic name we want to serve the topic request for.
     * @return Returns -1 if everything goes well. If it returns null there was an error.
     */
    public static Integer servePullRequest(ObjectOutputStream localoutputStream,ObjectInputStream localinputStream,Socket socket,String topic_name,String user_name,Broker broker){
        System.out.println("Topic name: " + topic_name);
        Topic topic = null;
        for (int i = 0; i < broker.getTopics().size(); i++) {
            if (topic_name.equals(broker.getTopics().get(i).getName())) {
                topic = broker.getTopics().get(i);
            }
        }
        if(topic == null){
            System.out.println( "\033[0;31m" + "There is no such topic in the topic list of the broker" + "\033[0m");
            return null;
        }
        if(GeneralUtils.sendMessage(topic.findLatestMessage(user_name),localoutputStream) == null){
            return null;
        }
        if(GeneralUtils.sendMessage(topic.findLatestFile(user_name),localoutputStream) == null){
            return null;
        }
        if(GeneralUtils.sendMessage(topic.findLatestStory(user_name),localoutputStream) == null){
            return null;
        }

        return -1;
    }

}
