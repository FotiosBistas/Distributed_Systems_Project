import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Topic implements Serializable {

    private String name;
    private String publisher;
    private List<Consumer> subscribedUsers;


    Topic(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getPublisher(){
        return publisher;
    }
}
