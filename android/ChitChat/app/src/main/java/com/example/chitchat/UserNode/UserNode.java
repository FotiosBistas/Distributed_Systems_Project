package com.example.chitchat.UserNode;

import Logging.ConsoleColors;
import com.example.chitchat.NetworkUtilities.UserNodeUtils;
import com.example.chitchat.Tools.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class UserNode implements Serializable {

    private String ip;
    private int port;
    private String name;

    private static final long serialVersionUID = -4L;

    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> BrokerIds = new ArrayList<>();
    private final HashMap<String, ArrayList<MultimediaFile>> file_list = new HashMap<>();
    private final HashMap<String,ArrayList<Text_Message>> message_list = new HashMap<>();
    private final HashMap<String, ArrayList<Story>> story_list = new HashMap<>();
    private final List<String> SubscribedTopics = new ArrayList<>();

    public UserNode(String ip,int port,String name){
        this.ip = ip;
        this.port = port;
        this.name = name;
    }


    public void setBrokerList(ArrayList<Tuple<String,int[]>> BrokerList){
        this.BrokerList = BrokerList;
    }


    public List<Tuple<String,int[]>> getBrokerList(){
        return BrokerList;
    }

    public List<Integer> getBroker_ids(){return BrokerIds;}

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName(){return name;}



    /**
     * Called in the pull method inside the user node utils class. Adds a new text message to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_text_message Accepts the text message object we received.
     */
    public synchronized void addNewMessage(String topic_name, Text_Message new_text_message) {
        if(message_list.containsKey(topic_name)) {
            ArrayList<Text_Message> values = message_list.get(topic_name);
            values.add(new_text_message);
        }else{
            message_list.put(topic_name,new ArrayList<Text_Message>());
            message_list.get(topic_name).add(new_text_message);
        }
    }

    /**
     * Called in the pull method inside the user node utils class. Adds a new story to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_story Accepts the story object we received.
     */
    public synchronized void addNewStory(String topic_name,Story new_story){
        if(story_list.containsKey(topic_name)) {
            ArrayList<Story> stories = story_list.get(topic_name);
            stories.add(new_story);
        }else{
            story_list.put(topic_name,new ArrayList<Story>());
            story_list.get(topic_name).add(new_story);
        }
    }

    /**
     * Called in the pull method inside the user node utils class. Adds a new file to the queue of the consumer.
     * @param topic_name Accepts the topic that received a new message.
     * @param new_file Accepts the file object we received.
     */
    public synchronized void addNewFile(String topic_name,MultimediaFile new_file){
        if(file_list.containsKey(topic_name)) {
            ArrayList<MultimediaFile> files = file_list.get(topic_name);
            files.add(new_file);
        }else{
            file_list.put(topic_name,new ArrayList<MultimediaFile>());
            file_list.get(topic_name).add(new_file);
        }
    }

    private synchronized void removeFromMessageQueue(String topic_name,Text_Message old_text_message){
        ArrayList<Text_Message> temp = message_list.get(topic_name);
        temp.remove(old_text_message);
    }

    private synchronized void removeFromFileQueue(String topic_name,MultimediaFile old_file){
        ArrayList<MultimediaFile> temp = file_list.get(topic_name);
        temp.remove(old_file);
    }

    private synchronized void removeFromStoryQueue(String topic_name,Story old_story){
        ArrayList<Story> temp = story_list.get(topic_name);
        temp.remove(old_story);
    }

    protected synchronized void addNewSubscription(String topic_name){
        if(!SubscribedTopics.contains(topic_name)) {
            SubscribedTopics.add(topic_name);
        }
    }

    protected synchronized void removeSubscription(String topic_name){
        if(SubscribedTopics.contains(topic_name)) {
            SubscribedTopics.remove(topic_name);
        }
    }
}
