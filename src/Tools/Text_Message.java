package Tools;

import java.io.Serializable;

public class Text_Message extends Value implements Serializable {
    private final String contents;
    private final int identifier;

    public String getContents() {
        return contents;
    }

    public Text_Message(String date_created, String publisher, String contents){
        super(publisher,date_created);
        this.contents = contents;
        this.identifier = hashCode();
    }

    public Text_Message(String publisher,String contents) {
        super(publisher);
        this.contents = contents;
        this.identifier = hashCode();
    }


    @Override
    public String toString() {
        return super.toString() + " Text_Message{" +
                "contents='" + contents + '\'' +
                ", identifier=" + identifier +
                '}';
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = result*prime + this.getContents().hashCode() + this.getDateCreated().hashCode() + this.getPublisher().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }

        if(obj.getClass() != this.getClass()){
            return false;
        }
        final Text_Message message = (Text_Message) obj;
        if(this.identifier == message.identifier){
            return true;
        }
        return false;
    }

}
