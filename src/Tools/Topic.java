package Tools;
import java.io.Serializable;
import java.util.*;
public class Topic implements Serializable{

    private final String name;
    private final HashMap<String,Integer> last_message = new HashMap<>();
    private final ArrayList<String> subscribedUsers = new ArrayList<>();
    private final ArrayList<Value> message_queue = new ArrayList<>();

    public Topic(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSubscribedUsers() {
        return subscribedUsers;
    }

    public ArrayList<Value> getMessage_queue() {
        return message_queue;
    }

    public HashMap<String, Integer> getLast_message() {
        return last_message;
    }

    public void printSubscribers(){
        System.out.println(subscribedUsers);
    }

    public synchronized void addSubscription(String new_cons){
        if(!isUserSubscribed(new_cons)){
            subscribedUsers.add(new_cons);
            last_message.put(new_cons,0);
        }
    }

    public synchronized void removeSubscription(String new_cons){
        if(isUserSubscribed(new_cons)) {
            subscribedUsers.remove(new_cons);
            last_message.remove(new_cons);
        }
    }


    public synchronized void addToMessageQueue(Value message){
        message_queue.add(message);
    }

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


    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                '}';
    }
}
