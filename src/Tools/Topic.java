package Tools;
import java.io.Serializable;
import java.util.*;
public class Topic implements Serializable{

    private String name;
    private ArrayList<String> subscribedUsers = new ArrayList<>();
    private ArrayList<Value> message_queue = new ArrayList<>();

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

    public void printSubscribers(){
        System.out.println(subscribedUsers);
    }

    public synchronized void addSubscription(String new_cons){
        if(!isUserSubscribed(new_cons)){
            subscribedUsers.add(new_cons);
        }
    }

    public synchronized void removeSubscription(String new_cons){
        if(isUserSubscribed(new_cons)) {
            subscribedUsers.remove(new_cons);
        }
    }


    public synchronized void addToMessageQueue(Value message){
        message_queue.add(message);
    }

    public boolean isUserSubscribed(String user){
        return subscribedUsers.contains(user);
    }


    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                '}';
    }
}
