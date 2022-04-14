import java.util.ArrayList;
import java.util.HashMap;

public class ProfileName {
    private String profileName;
    private HashMap<String,ArrayList<MultimediaFile>> userVideoFilesMap;
    private HashMap<String,Integer> subscribedConversations;

    ProfileName(String profileName) {
        this.profileName = profileName;
    }

    private void addToVideoFilesMap(String topic, MultimediaFile file){
        userVideoFilesMap.get(topic).add(file);
    }

    private void addSubscription(String topic, int val){
        subscribedConversations.put(topic,val);
    }

}
