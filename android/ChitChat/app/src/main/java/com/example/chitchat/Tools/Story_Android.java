package com.example.chitchat.Tools;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Story_Android extends Multimedia_File_Android{

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
    public Story_Android(String publisher, String file_name) {
        super(publisher, file_name);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        LocalDateTime expiration_date = date.plusMinutes(1);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = Hash();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Story_Android(String publisher, String date_created, String file_name, String actual_date_created, long size, ArrayList<Chunk> chunks, String expiration_date) {
        super(publisher, date_created, file_name, actual_date_created, size, chunks);
        this.expiration_date = expiration_date;
        this.identifier = Hash();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Story_Android(String publisher, Uri selected_media_uri, Context context, String image_or_video) {
        super(publisher, selected_media_uri, context, image_or_video);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(getDateCreated(),formatter);
        LocalDateTime expiration_date = date.plusMinutes(1);
        this.expiration_date = expiration_date.format(formatter);
        this.identifier = Hash();
    }

    public Story_Android(Story story){
        super(story);
        this.expiration_date = story.getExpiration_date();
        this.identifier = story.getIdentifier();
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
        final Story_Android story = (Story_Android) obj;
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
