import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MultimediaFile implements Serializable {
    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private long length;
    //private String framerate;
    //private String frameWidth;
    //private String frameHeight;
    private ArrayList<byte[]> multimediaFileChunk;

    MultimediaFile(String filename,String profileName){
        this.multimediaFileName = filename;
        this.profileName = profileName;
        Path path = FileSystems.getDefault().getPath(filename);
        splitFile(new File(filename));
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

    public void splitFile(File f){
        int id = 0; // this is the ID of each chunks
        int sizeofchunks = 512000;
        try(FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            byte[] buffer = new byte[sizeofchunks];
            while((bytesAmount = bis.read(buffer))>0){
                multimediaFileChunk.add(buffer);
                buffer = new byte[sizeofchunks];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
