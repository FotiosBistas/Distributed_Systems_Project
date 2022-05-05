package Tools;
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
    private final ArrayList<Value> message_queue = new ArrayList<>();


    private final ArrayList<Story> story_queue = new ArrayList<>();
    private final HashMap<String,Integer> last_story = new HashMap<>();

    public Topic(String name){
        this.name = name;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::checkExpiredStories,0,20, TimeUnit.SECONDS);
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

    public ArrayList<Value> getMessage_queue() {
        return message_queue;
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
        }
    }

    public synchronized void removeSubscription(String new_cons){
        if(isUserSubscribed(new_cons)) {
            subscribedUsers.remove(new_cons);
            last_message.remove(new_cons);
            last_story.remove(new_cons);
        }
    }


    public synchronized void addToMessageQueue(Value message){
        message_queue.add(message);
    }

    public synchronized void addToStoryQueue(Story story){ story_queue.add(story);}

    public boolean isUserSubscribed(String user){
        return subscribedUsers.contains(user);
    }

    public ArrayList<Value> findLatestMessage(String user){
        System.out.println("Finding newest messages for user: " + user);
        int index = last_message.get(user);
        ArrayList<Value> new_messages = new ArrayList<>();
        boolean found_new_messages = false;
        for (int i = index; i < message_queue.size(); i++) {
            new_messages.add(message_queue.get(i));
            found_new_messages = true;
        }
        System.out.println("The number of new messages are: " + new_messages.size());
        System.out.println(last_message);
        if(found_new_messages) {
            last_message.put(user,last_message.getOrDefault(user,0) + new_messages.size());
        }
        System.out.println(last_message);
        return new_messages;
    }

    public ArrayList<Story> findLatestStory(String user){
        System.out.println("Finding newest stories for user: " + user);
        int index = last_message.get(user);
        ArrayList<Story> new_stories = new ArrayList<>();
        boolean found_new_messages = false;
        for (int i = index; i < story_queue.size(); i++) {
            new_stories.add(story_queue.get(i));
            found_new_messages = true;
        }
        System.out.println("The number of new stories are: " + new_stories.size());
        System.out.println(last_message);
        if(found_new_messages) {
            last_message.put(user,last_message.getOrDefault(user,0) + new_stories.size());
        }
        System.out.println(last_message);
        return new_stories;
    }

    public void ExpireStory(Story story){
        System.out.println("Checking time in story");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime story_time = LocalDateTime.parse(story.getExpiration_date(),dtf);

        if(now.isAfter(story_time)){
            story.setExpired(true);
        }
    }

    public void checkExpiredStories(){
        System.out.println("Finding expired stories");
        for (Story story:story_queue) {
            ExpireStory(story);
            if(story.isExpired()){
                story_queue.remove(story);
                for (String user:subscribedUsers) {
                    last_story.put(user, last_story.getOrDefault(user,0) -1);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                '}';
    }
}
