import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class MultimediaFile implements Serializable {
    private String multimediaFileName;
    private String profileName;
    private String dateCreated;
    private long length;
    //private String framerate;
    //private String frameWidth;
    //private String frameHeight;
    private ArrayList<Chunk> multimediaFileChunk = new ArrayList<>();

    public ArrayList<Chunk> getChunks(){
        return multimediaFileChunk;
    }

    public String getMultimediaFileName(){
        return multimediaFileName;
    }


    MultimediaFile(String filename,String profileName){
        this.multimediaFileName = filename;
        this.profileName = profileName;
        System.out.println("Filename is :" + filename);
        System.out.println("User's profile is: " + profileName);
        Path path = FileSystems.getDefault().getPath(filename);
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
        splitFile(new File(filename));
    }

    public void splitFile(File f){
        int sizeofchunks = 512*1024;
        try(FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            byte[] buffer = new byte[sizeofchunks];
            int counter = 1;
            System.out.println("Length is: " + length);
            double ceil = (double)(length)/(double)sizeofchunks;
            int max_seq = (int) Math.ceil(ceil);
            System.out.println("Max sequence number is: " + max_seq);
            while((bytesAmount = bis.read(buffer,0,sizeofchunks))>0){
                if(counter != max_seq) {
                    Chunk chunk = new Chunk(counter++,sizeofchunks, max_seq,buffer);
                    multimediaFileChunk.add(chunk);
                    System.out.println(chunk);
                }else{
                    long rem = (long) sizeofchunks *max_seq;
                    System.out.println(rem);
                    long difference = rem - length;
                    System.out.println();
                    long actual_size =  (sizeofchunks - difference);
                    System.out.println(actual_size);
                    Chunk chunk = new Chunk(counter++, actual_size,max_seq,buffer);
                    multimediaFileChunk.add(chunk);
                    System.out.println(chunk);
                }
                // create a new pointer because when the new data gets written on the buffer all the buffers change
                System.out.println("Created chunk: " + multimediaFileChunk.size() +  " for file: " + multimediaFileName);

            }
            System.out.println("Created: " + multimediaFileChunk.size() + " chunks for file: " + multimediaFileName);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
