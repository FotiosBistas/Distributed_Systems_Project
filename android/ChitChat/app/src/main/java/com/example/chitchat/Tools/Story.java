package com.example.chitchat.Tools;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Story extends MultimediaFile implements Serializable {

    private boolean isExpired = false;
    private final String expiration_date;
    private final int identifier;

    private static final long serialVersionUID = -1L;


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Story(String publisher, String multimediaFileName){
        super(publisher,multimediaFileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        LocalDateTime expiration_date = date.plusMinutes(1);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = Hash();
    }

    public Story(String publisher, String date_created, String mutlimediaFileName, String actual_date, long length, ArrayList<Chunk> multimediaFileChunk, String expiration_date){
        super(publisher,date_created,mutlimediaFileName,actual_date,length,multimediaFileChunk);
        this.expiration_date = expiration_date;
        this.identifier = Hash();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Story(MultimediaFile multimediaFile){
        super(multimediaFile.getPublisher(),multimediaFile.getDateCreated(),multimediaFile.getMultimediaFileName(),multimediaFile.getActual_date(), multimediaFile.getLength(), multimediaFile.getMultimediaFileChunk());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        LocalDateTime expiration_date = date.plusMinutes(1);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = Hash();
    }

    public int Hash(){
        return expiration_date.hashCode() + super.getIdentifier();
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
        return this.identifier == story.identifier;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " Story{" +
                "isExpired=" + isExpired +
                ", expiration_date='" + expiration_date + '\'' +
                ", identifier=" + identifier +
                '}';
    }
}
