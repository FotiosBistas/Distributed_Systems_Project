package Tools;
import java.io.Serializable;
import java.util.*;
public class Topic implements Serializable{

    private String name;
    private ArrayList<String> subscribedUsers;
    //user and index of message queue
    private HashMap<String,Integer> last_message;
    private ArrayList<Value> message_queue;

    public Topic(String name){
        this.name = name;
        this.subscribedUsers = new ArrayList<>();
        this.last_message = new HashMap<>();
        this.message_queue = new ArrayList<>();
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

    public void printSubscribers(){
        System.out.println(subscribedUsers);
    }

    public synchronized void addSubscription(String new_cons){
        subscribedUsers.add(new_cons);
        last_message.put(new_cons,0);
    }

    public synchronized void removeSubscription(String new_cons){
        subscribedUsers.remove(new_cons);
        last_message.remove(new_cons);
    }

    public synchronized void addToMessageQueue(Value message){
        message_queue.add(message);
    }

    public boolean isUserSubscribed(String user){
        return subscribedUsers.contains(user);
    }

    public ArrayList<Value> findLatestMessages(String user){
        int index = last_message.get(user);
        System.out.println("Later message index: " + index);
        ArrayList<Value> temp = new ArrayList<>();
        for (int i = index; i < message_queue.size(); i++) {
            temp.add(message_queue.get(i));
        }
        last_message.put(user,message_queue.size());
        return temp;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                '}';
    }
}
