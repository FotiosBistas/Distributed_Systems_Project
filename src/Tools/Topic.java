package Tools;
import Logging.ConsoleColors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Topic implements Serializable{

    private final String name;
    private final ArrayList<String> subscribedUsers = new ArrayList<>();


    private final HashMap<String,Integer> last_message = new HashMap<>();
    private final HashMap<String,Integer> last_file = new HashMap<>();
    private final ArrayList<Text_Message> message_queue = new ArrayList<>();
    private final ArrayList<MultimediaFile> file_queue = new ArrayList<>();

    private final ArrayList<Story> story_queue = new ArrayList<>();
    private final HashMap<String,Integer> last_story = new HashMap<>();

    public Topic(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSubscribedUsers() {
        return subscribedUsers;
    }

    public HashMap<String, Integer> getLast_message() {
        return last_message;
    }

    public ArrayList<Text_Message> getMessage_queue() {
        return message_queue;
    }

    public ArrayList<MultimediaFile> getFile_queue() {
        return file_queue;
    }

    public ArrayList<Story> getStory_queue() {
        return story_queue;
    }

    public HashMap<String, Integer> getLast_story() {
        return last_story;
    }

    public void printSubscribers(){
        System.out.println(subscribedUsers);
    }


    public synchronized void addSubscription(String new_cons){
        if(!isUserSubscribed(new_cons)){
            subscribedUsers.add(new_cons);
            last_message.put(new_cons,0);
            last_story.put(new_cons,0);
            last_file.put(new_cons,0);
        }
    }

    public synchronized void removeSubscription(String new_cons){
        if(isUserSubscribed(new_cons)) {
            subscribedUsers.remove(new_cons);
            last_message.remove(new_cons);
            last_story.remove(new_cons);
            last_file.remove(new_cons);
        }
    }


    public synchronized void addToMessageQueue(Text_Message message){
        System.out.println(ConsoleColors.PURPLE + "Adding new message to message queue" + ConsoleColors.RESET);
        message_queue.add(message);
    }

    public synchronized void addToFileQueue(MultimediaFile file){
        System.out.println(ConsoleColors.PURPLE + "Adding new file to file queue" + ConsoleColors.RESET);
        file_queue.add(file);
    }

    public synchronized void addToStoryQueue(Story story){
        System.out.println(ConsoleColors.PURPLE + "Adding new story to story queue" + ConsoleColors.RESET);
        story_queue.add(story);
    }

    public boolean isUserSubscribed(String user){
        return subscribedUsers.contains(user);
    }

    /**
     * Finds the text messages that the specific user hasn't received.
     * @param user Accepts the user name.
     * @return returns the new text message found.
     */
    public ArrayList<Text_Message> findLatestMessage(String user){
        //System.out.println("Finding newest messages for user: " + user);
        int index = last_message.get(user);
        ArrayList<Text_Message> new_messages = new ArrayList<>();
        boolean found_new_messages = false;
        for (int i = index; i < message_queue.size(); i++) {
            new_messages.add(message_queue.get(i));
            found_new_messages = true;
        }
        //System.out.println("The number of new messages are: " + new_messages.size());
        if(found_new_messages) {
            last_message.put(user,last_message.getOrDefault(user,0) + new_messages.size());
        }
        return new_messages;
    }

    /**
     * Finds the files that the specific user hasn't received.
     * @param user Accepts the user name.
     * @return returns the new text message found.
     */
    public ArrayList<MultimediaFile> findLatestFile(String user){
        //System.out.println("Finding newest files for user: " + user);
        int index = last_file.get(user);
        ArrayList<MultimediaFile> new_files = new ArrayList<>();
        boolean found_new_messages = false;
        for (int i = index; i < file_queue.size(); i++) {
            new_files.add(file_queue.get(i));
            found_new_messages = true;
        }
        //System.out.println("The number of new messages are: " + new_files.size());
        if(found_new_messages) {
            last_file.put(user,last_file.getOrDefault(user,0) + new_files.size());
        }
        return new_files;
    }

    /**
     * Finds the stories that the specific user hasn't received.
     * @param user Accepts the user name.
     * @return returns the new text message found.
     */
    public ArrayList<Story> findLatestStory(String user){
        //System.out.println("Finding newest stories for user: " + user);
        int index = last_story.get(user);
        ArrayList<Story> new_stories = new ArrayList<>();
        boolean found_new_messages = false;
        for (int i = index; i < story_queue.size(); i++) {
            new_stories.add(story_queue.get(i));
            found_new_messages = true;
        }
        //System.out.println("The number of new stories are: " + new_stories.size());
        if(found_new_messages) {
            last_story.put(user,last_story.getOrDefault(user,0) + new_stories.size());
        }
        return new_stories;
    }

    /**
     * Checks if the current date is after the story expiration date and expires the story so it can be removed from the story list.
     * @param story Accepts the specific story.
     */
    private synchronized void ExpireStory(Story story){
        System.out.println("Checking time in story");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime story_time = LocalDateTime.parse(story.getExpiration_date(),dtf);

        if(now.isAfter(story_time)){
            story.setExpired(true);
            System.out.println("Story expired: " + story);
        }
    }

    /**
     * Gets called every specific interval specified in the constructor of the topic.
     * If a story is expired it removes it from the story queue.
     */
    public void checkExpiredStories(){
        System.out.println("Finding expired stories");
        int i = 0;
        while(true){
            if(i >= story_queue.size()){
                break;
            }
            Story story = story_queue.get(i);
            ExpireStory(story);
            if(story.isExpired()){
                story_queue.remove(story);
                int j = 0;
                while(true){
                    if(j >= subscribedUsers.size()){
                        break;
                    }
                    String user = subscribedUsers.get(j);
                    last_story.put(user, last_story.getOrDefault(user,0) -1);
                    j++;
                }
            }
            i++;
        }
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }

        if(obj.getClass() != this.getClass()){
            return false;
        }
        final Topic topic = (Topic) obj;
        return this.name.equals(topic.getName());
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", subscribedUsers=" + subscribedUsers +
                ", message_queue=" + message_queue +
                ", file_queue=" + file_queue +
                ", story_queue=" + story_queue +
                '}';
    }
}
