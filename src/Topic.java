import com.sun.org.apache.xpath.internal.operations.Mult;

import java.io.Serializable;
import java.util.*;

public class Topic implements Serializable {

    private String name;
    private String publisher;
    private Set<Consumer> subscribedUsers;
    //private ArrayList<MultimediaFile> files;

    Topic(String name,String publisher){
        this.name = name;
        this.publisher = publisher;
        subscribedUsers = new HashSet<Consumer>();
        //files = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public String getPublisher(){
        return publisher;
    }

    public Set<Consumer> getSubscribedUsers(){
        return subscribedUsers;
    }

    public void addSubscription(Consumer new_cons){
        subscribedUsers.add(new_cons);
    }

    public void removeSubscription(Consumer to_be_removed){
        subscribedUsers.remove(to_be_removed);
    }

    public void addFile(MultimediaFile file){
        //files.add(file);
    }
}
