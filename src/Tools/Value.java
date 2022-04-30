package Tools;

import java.io.Serializable;

public class Value implements Serializable {
    private final MultimediaFile file;
    private final String message;

    Value(String message){
        this.message = message;
        this.file = null;
    }

    Value(MultimediaFile file){
        this.file = file;
        this.message = null;
    }

    Value(MultimediaFile file,String message){
        this.file = file;
        this.message = message;
    }
}
