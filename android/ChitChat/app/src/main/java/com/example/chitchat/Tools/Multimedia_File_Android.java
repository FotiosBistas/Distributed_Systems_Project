package com.example.chitchat.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.chitchat.NetworkUtilities.GeneralUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import Logging.ConsoleColors;

public class Multimedia_File_Android extends Value{

    private long size;
    private String file_name;
    private String actual_date_created;
    private ArrayList<Chunk> chunks = new ArrayList<>();
    private Object file_key;

    private final String internal_storage = "/storage/emulated/0/sent_files/";
    private final int chunk_size = 1024*512;
    private final int identifier;

    public long getSize() {
        return size;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getActual_date_created() {
        return actual_date_created;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public Object getFile_key() {
        return file_key;
    }

    public String getInternal_storage() {
        return internal_storage;
    }

    public int getChunk_size() {
        return chunk_size;
    }

    public int getIdentifier() {
        return identifier;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Multimedia_File_Android(String publisher, String file_name) {
        super(publisher);
        //context is to get the internal storage allocated for the app
        this.file_name = file_name;

        Path file = FileSystems.getDefault().getPath(internal_storage,file_name);
        try {
            BasicFileAttributes attributes = Files.readAttributes(file,BasicFileAttributes.class);
            this.actual_date_created = String.valueOf(attributes.creationTime());
            this.size = attributes.size();
            this.file_key = attributes.fileKey();
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED + "Error trying to read file attributes" + ConsoleColors.RESET);
            e.printStackTrace();
        }
        //store the file to the apps cache so it can access it during create chunk phase

        File new_file = new File(internal_storage + file_name);
        //we create cache file to be able to read it
        //File cached_file = File.createTempFile(internal_storage + file_name,null,context.getCacheDir());
        createChunks(new_file);
        this.identifier = hashCode();
    }

    public Multimedia_File_Android(MultimediaFile multimediaFile){
        super(multimediaFile.getPublisher(),multimediaFile.getDateCreated());
        this.file_name = multimediaFile.getMultimediaFileName();
        this.chunks = multimediaFile.getMultimediaFileChunk();
        this.size = multimediaFile.getLength();
        this.actual_date_created = multimediaFile.getActual_date();
        this.identifier = multimediaFile.getIdentifier();
    }

    //copy constructor
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Multimedia_File_Android(String publisher, String date_created, String file_name, String actual_date_created, long size, ArrayList<Chunk> chunks){
        super(publisher,date_created);
        this.file_name = file_name;
        this.actual_date_created = actual_date_created;
        this.size = size;
        this.chunks = chunks;
        this.identifier =  this.hashCode();
    }

    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Multimedia_File_Android(String publisher, Uri selected_media_uri, Context context, String image_or_video){
        super(publisher);
        if(selected_media_uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(selected_media_uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    this.file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    this.size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    this.actual_date_created = getDateCreated();
                }
            }finally {
                assert cursor != null;
                cursor.close();
            }
        }
        if(image_or_video.equals("image")) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selected_media_uri);


                /*int size = bitmap.getRowBytes() * bitmap.getHeight();

                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                bitmap.copyPixelsToBuffer(byteBuffer);
                byte[] buffer = byteBuffer.array();*/
                if(findextensionType(this).equals("jpg")){
                    System.out.println("Creating chunks for jpg image");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                    byte[] buffer = byteArrayOutputStream.toByteArray();
                    createChunks(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            String data = MediaStore.Video.Media.DATA;
            System.out.println(data);
        }


        this.identifier = hashCode();

    }

    private String findextensionType(Multimedia_File_Android file){
        String extension = "";
        int i = file.getFile_name().lastIndexOf('.');
        if (i > 0) {
            extension = file.getFile_name().substring(i+1);
        }
        return extension;
    }

    /**
     * Checks if a volume containing external storage is available for read and write.
     * @return true if yes false if not
     */
    private boolean isExternalStorageWritable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Checks if a volume containing external storage is available to at least read.
     * @return true if yes false if not
     */
    private boolean isExternalStorageReadable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    private void createChunks(byte[] array){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);
        int bytesAmount = 0;
        byte[] buffer = new byte[chunk_size];
        int counter = 1;
        //calculates the maximum number of chunks for the specific files
        double ceil = (double)(size)/(double)chunk_size;
        int max_seq = (int) Math.ceil(ceil);
        while((bytesAmount = byteArrayInputStream.read(buffer,0,chunk_size)) > 0){
            if(counter != max_seq) {
                Chunk chunk = new Chunk(counter++,chunk_size, max_seq,buffer.clone());
                chunks.add(chunk);
            }else{
                //the last chunk might not be equal to 512KB so we must calculate its actual size
                int actual_size = (int) (chunk_size - ((long) chunk_size * max_seq - size) + 1);
                Chunk chunk = new Chunk(counter++, actual_size - 1,max_seq,buffer.clone());
                chunks.add(chunk);
            }
        }

        System.out.println("Created: " + counter + " chunks");
    }

   private void createChunks(File file){
        try(FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            byte[] buffer = new byte[chunk_size];
            int counter = 1;
            //calculates the maximum number of chunks for the specific files
            double ceil = (double)(size)/(double)chunk_size;
            int max_seq = (int) Math.ceil(ceil);
            while((bytesAmount = bis.read(buffer,0,chunk_size))>0){
                if(counter != max_seq) {
                    Chunk chunk = new Chunk(counter++,chunk_size, max_seq,buffer.clone());
                    chunks.add(chunk);
                }else{
                    //the last chunk might not be equal to 512KB so we must calculate its actual size
                    int actual_size = (int) (chunk_size - ((long) chunk_size * max_seq - size) + 1);
                    System.out.println(actual_size);
                    Chunk chunk = new Chunk(counter++, actual_size,max_seq,buffer.clone());
                    chunks.add(chunk);
                }
            }
            System.out.println("Created: " + chunks.size() + " chunks for file: " + file_name);
            bis.close();
            boolean result = file.delete();
            if(result){
                System.out.println(ConsoleColors.RED + "file was deleted successfully" + ConsoleColors.RESET);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @NonNull
    @Override
    public String toString() {
        return  super.toString() + "MultimediaFile{" +
                "multimediaFileName='" + file_name + '\'' +
                ", size=" + size +
                ", actual_date='" + actual_date_created + '\'' +
                ", multimediaFileChunk=" + chunks +
                ", identifier=" + identifier +
                '}';
    }

    @Override
    public int hashCode(){
        int result = 1;
        final int prime = 31;
        result = prime*result + this.getPublisher().hashCode() + this.getDateCreated().hashCode() + this.getFile_name().hashCode() + this.getDateCreated().hashCode() + this.getChunks().hashCode();
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
        final Multimedia_File_Android file = (Multimedia_File_Android) obj;
        return this.identifier == file.identifier;
    }

}
