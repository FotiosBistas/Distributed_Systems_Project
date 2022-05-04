package Tools;

import java.io.Serializable;

public class Text_Message extends Value implements Serializable {
    private final String contents;

    public String getContents() {
        return contents;
    }

    public Text_Message(String date_created, String publisher, String contents){
        super(publisher,date_created);
        this.contents = contents;
    }

    public Text_Message(String publisher,String contents) {
        super(publisher);
        this.contents = contents;
    }

    @Override
    public String toString() {
        return super.toString() + " Text_Message{" +
                "contents='" + contents + '\'' +
                '}';
    }


}
