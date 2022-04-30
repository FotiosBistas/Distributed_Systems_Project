package UserNode;
import Tools.Topic;
import Tools.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileName implements Serializable {
    private String profileName;
    private Map<Topic,ArrayList<Value>> userVideoFilesMap;
    private Map<Topic,Integer> subscribedConversations;

    ProfileName(String profileName) {
        this.profileName = profileName;
        userVideoFilesMap = new HashMap<>();
        subscribedConversations = new HashMap<>();
    }

    private void addToVideoFilesMap(Topic topic, Value val){
        userVideoFilesMap.get(topic).add(val);
    }

    private void addSubscription(Topic topic, int val){subscribedConversations.put(topic,val);}

}
