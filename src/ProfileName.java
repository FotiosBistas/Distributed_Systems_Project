import com.sun.org.apache.xpath.internal.operations.Mult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileName {
    private String profileName;
    private Map<Topic,ArrayList<MultimediaFile>> userVideoFilesMap;
    //private Map<Topic,Integer> subscribedConversations;

    ProfileName(String profileName) {
        this.profileName = profileName;
        userVideoFilesMap = new HashMap<Topic,ArrayList<MultimediaFile>>();
        //subscribedConversations = new HashMap<Topic,Integer>();
    }

    private void addToVideoFilesMap(Topic topic, MultimediaFile file){
        userVideoFilesMap.get(topic).add(file);
    }

    //private void addSubscription(Topic topic, int val){subscribedConversations.put(topic,val);}

}
