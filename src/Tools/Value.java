package Tools;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Value implements Serializable {
    private final String publisher;
    private final String dateCreated;

    public String getPublisher() {
        return publisher;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public Value(String publisher, String dateCreated) {
        this.publisher = publisher;
        this.dateCreated = dateCreated;
    }

    public Value(String publisher){
        this.publisher = publisher;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.dateCreated = dtf.format(now);
    }

    @Override
    public String toString() {
        return "Value{" +
                "publisher='" + publisher + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }
}
