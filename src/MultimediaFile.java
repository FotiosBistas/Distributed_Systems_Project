import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MultimediaFile implements Serializable {
    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private long length;
    //private String framerate;
    //private String frameWidth;
    //private String frameHeight;
    private byte[] multimediaFileChunk;
    MultimediaFile(Path path,String profileName){
        Path filename = path.getFileName();
        this.multimediaFileName = filename.toString();
        this.profileName = profileName;
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            this.length = attr.size();
            long cTime = attr.creationTime().toMillis();
            ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.of("UTC"));
            this.dateCreated = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
