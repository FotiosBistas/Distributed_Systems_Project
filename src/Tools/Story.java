package Tools;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Story extends MultimediaFile implements Serializable {

    private boolean isExpired = false;
    private final String expiration_date;
    private final int identifier;


    public boolean isExpired() {
        return isExpired;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public Story(String publisher, String multimediaFileName){
        super(publisher,multimediaFileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        System.out.println(date);
        LocalDateTime expiration_date = date.plusDays(1);
        System.out.println(expiration_date);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = hashCode();
    }

    public Story(String publisher, String date_created, String mutlimediaFileName, String actual_date, long length, ArrayList<Chunk> multimediaFileChunk, String expiration_date){
        super(publisher,date_created,mutlimediaFileName,actual_date,length,multimediaFileChunk);
        this.expiration_date = expiration_date;
        this.identifier = hashCode();
    }

    public Story(MultimediaFile multimediaFile){
        super(multimediaFile.getPublisher(),multimediaFile.getDateCreated(),multimediaFile.getMultimediaFileName(),multimediaFile.getActual_date(), multimediaFile.getLength(), multimediaFile.getMultimediaFileChunk());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        LocalDateTime expiration_date = date.plusDays(1);
        System.out.println(expiration_date);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = hashCode();
    }


    public int hashCode(){
        int result = 1;
        final int prime = 31;
        result = prime*result + expiration_date.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }

        if(obj.getClass() != this.getClass()){
            return false;
        }
        final Story story = (Story) obj;
        if(this.identifier == story.identifier){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + " Story{" +
                "isExpired=" + isExpired +
                ", expiration_date='" + expiration_date + '\'' +
                '}';
    }
}
