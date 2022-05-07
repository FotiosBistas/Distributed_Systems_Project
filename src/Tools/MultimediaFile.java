package Tools;
import java.io.*;
import java.lang.invoke.MutableCallSite;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class MultimediaFile extends Value implements Serializable {
    private final String multimediaFileName;
    private long length;
    private String actual_date;
    private ArrayList<Chunk> multimediaFileChunk = new ArrayList<>();
    private final int identifier;


    public String getMultimediaFileName() {
        return multimediaFileName;
    }

    public long getLength() {
        return length;
    }

    public String getActual_date() {
        return actual_date;
    }

    public ArrayList<Chunk> getMultimediaFileChunk() {
        return multimediaFileChunk;
    }

    public int getIdentifier() {
        return identifier;
    }

    public MultimediaFile(String publisher,String multimediaFileName){
        super(publisher);
        this.multimediaFileName = multimediaFileName;
        System.out.println("Filename is: " + multimediaFileName);
        System.out.println("User's profile is: " + publisher);
        Path path = FileSystems.getDefault().getPath(multimediaFileName);
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            this.length = attr.size();
            long cTime = attr.creationTime().toMillis();
            ZonedDateTime t = Instant.ofEpochMilli(cTime).atZone(ZoneId.of("UTC"));
            this.actual_date = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        splitFile(new File(multimediaFileName));
        this.identifier = this.hashCode();
    }

    public MultimediaFile(String publisher,String dateCreated,String multimediaFileName,String actual_date,long length,ArrayList<Chunk> multimediaFileChunk){
        super(publisher, dateCreated);
        this.multimediaFileName = multimediaFileName;
        this.actual_date = actual_date;
        this.length = length;
        this.multimediaFileChunk = multimediaFileChunk;
        this.identifier =  this.hashCode();
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
                    Chunk chunk = new Chunk(counter++,sizeofchunks, max_seq,buffer.clone());
                    multimediaFileChunk.add(chunk);
                }else{
                    int actual_size = (int) (sizeofchunks - ((long) sizeofchunks * max_seq - length) + 1);
                    System.out.println(actual_size);
                    Chunk chunk = new Chunk(counter++, actual_size,max_seq,buffer.clone());
                    multimediaFileChunk.add(chunk);
                }

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

    @Override
    public String toString() {
        return  super.toString() + "MultimediaFile{" +
                "multimediaFileName='" + multimediaFileName + '\'' +
                ", length=" + length +
                ", actual_date='" + actual_date + '\'' +
                ", multimediaFileChunk=" + multimediaFileChunk +
                ", identifier=" + identifier +
                '}';
    }

    @Override
    public int hashCode(){
        int result = 1;
        final int prime = 31;
        result = prime*result + this.getPublisher().hashCode() + this.getDateCreated().hashCode() + this.getMultimediaFileName().hashCode() + this.getActual_date().hashCode() + this.getMultimediaFileChunk().hashCode();
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
        final MultimediaFile file = (MultimediaFile) obj;
        if(this.identifier == file.identifier){
            return true;
        }
        return false;
    }
}
