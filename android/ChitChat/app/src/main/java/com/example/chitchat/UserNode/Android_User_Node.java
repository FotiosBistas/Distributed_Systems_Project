package com.example.chitchat.UserNode;


import com.example.chitchat.Tools.*;

import java.io.*;
import java.util.*;
import java.util.List;


public class Android_User_Node implements Serializable {

    private String ip;
    private int port;
    private String name;

    private ArrayList<Value> general_message_list;

    public List<String> getSubscribedTopics() {
        return SubscribedTopics;
    }

    //this message list is used when receiving conversation data
    private ArrayList<Value> temp_message_list;

    private Text_Message temp_message;
    private Multimedia_File_Android temp_multimedia_file_android;
    private Story temp_story;

    public Text_Message getTemp_message() {
        return temp_message;
    }

    public void setTemp_message(Text_Message temp_message) {
        this.temp_message = temp_message;
    }

    public Multimedia_File_Android getTemp_multimedia_file_android() {
        return temp_multimedia_file_android;
    }

    public void setTemp_multimedia_file_android(Multimedia_File_Android temp_multimedia_file_android) {
        this.temp_multimedia_file_android = temp_multimedia_file_android;
    }

    public Story getTemp_story() {
        return temp_story;
    }

    public void setTemp_story(Story temp_story) {
        this.temp_story = temp_story;
    }

    //used when opening a topic's chat and gets set by receive conversation data in userNodeUtils.java
    public void setTemp_message_list(ArrayList<Value> temp_message_list) {
        this.temp_message_list = temp_message_list;
    }

    //called by get Message_List_Activity when requesting for the conversation data
    public ArrayList<Value> getTemp_message_list() {
        return temp_message_list;
    }

    private static final long serialVersionUID = -4L;

    //Broker list should be sorted by ids of brokers
    private List<Tuple<String,int[]>> BrokerList = new ArrayList<>();
    private List<Integer> BrokerIds = new ArrayList<>();
    private final HashMap<String, ArrayList<MultimediaFile>> file_list = new HashMap<>();
    private final HashMap<String,ArrayList<Text_Message>> message_list = new HashMap<>();
    private final HashMap<String, ArrayList<Story>> story_list = new HashMap<>();
    private final List<String> SubscribedTopics = new ArrayList<>();

    public Android_User_Node(String ip, int port, String name){
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

    @Override
    public String toString() {
        return "UserNode{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", name='" + name + '\'' +
                '}';
    }
}
