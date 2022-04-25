
import java.io.Serializable;
import java.util.*;
class Topic implements Serializable{

    private String name;
    private String publisher;
    private ArrayList<String> subscribedUsers;
    private HashMap<String,Integer> last_message;
    private ArrayList<Value> message_queue;

    Topic(String name,String publisher){
        this.name = name;
        this.publisher = publisher;

    }

    public String getName(){
        return name;
    }

    public String getPublisher(){
        return publisher;
    }

    public ArrayList<String> getSubscribedUsers(){
        return subscribedUsers;
    }

    public void addSubscription(String new_cons){
        subscribedUsers.add(new_cons);
        last_message.put(new_cons,0);
    }

    public void removeSubscription(String new_cons){
        subscribedUsers.remove(new_cons);
        last_message.remove(new_cons);
    }

    public void addToMessageQueue(Value message){
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

}
