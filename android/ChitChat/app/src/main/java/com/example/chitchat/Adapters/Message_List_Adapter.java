package com.example.chitchat.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.R;
import com.example.chitchat.Tools.Chunk;
import com.example.chitchat.Tools.MultimediaFile;
import com.example.chitchat.Tools.Multimedia_File_Android;
import com.example.chitchat.Tools.Story_Android;
import com.example.chitchat.Tools.Text_Message;
import com.example.chitchat.Tools.Value;
import com.example.chitchat.UserNode.Android_User_Node;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Message_List_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //those two static and final variables indicate with what message are we filling the recycler view
    private static final int VIEW_TEXT_RECEIVED_MESSAGE = 1;
    private static final int VIEW_TEXT_SENT_MESSAGE = 2;
    private static final int VIEW_IMAGE_RECEIVED_MESSAGE = 3;
    private static final int VIEW_IMAGE_SENT_MESSAGE = 4;
    private static final int VIEW_VIDEO_RECEIVED_MESSAGE = 5;
    private static final int VIEW_VIDEO_SENT_MESSAGE = 6;



    private Context context;
    private List<Value> message_list;
    private Android_User_Node androidUserNode;

    public Message_List_Adapter(Context context, List<Value> message_list,Android_User_Node androidUserNode){
        this.context = context;
        this.message_list = message_list;
        this.androidUserNode = androidUserNode;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case VIEW_TEXT_RECEIVED_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_received_text_message,parent,false);
                return new Text_Received_Message_Holder(view);
            case VIEW_TEXT_SENT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_sent_text_message,parent,false);
                return new Text_Sent_Message_Holder(view);
            case VIEW_IMAGE_RECEIVED_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_received_image,parent,false);
                return new Image_Received_Message_Holder(view);
            case VIEW_IMAGE_SENT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_sent_image,parent,false);
                return new Image_Sent_Message_Holder(view);
            case VIEW_VIDEO_RECEIVED_MESSAGE:
                return null;
            case VIEW_VIDEO_SENT_MESSAGE:
                return null;
            default:
                return null;
        }
    }

    public void addMessage(Value value){
        message_list.add(value);
        notifyItemInserted(message_list.size() - 1);
    }

    public void addMessages(ArrayList<Value> message_list){
        this.message_list = message_list;
        notifyItemRangeInserted(this.message_list.size() - 1, message_list.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Value message = (Value) message_list.get(position);
        switch(holder.getItemViewType()){
            case VIEW_TEXT_RECEIVED_MESSAGE:
                //bind the values of the message to the view
                ((Text_Received_Message_Holder) holder).bind((Text_Message) message);
                break;
            case VIEW_TEXT_SENT_MESSAGE:
                //bind the values of the message to the view
                ((Text_Sent_Message_Holder) holder).bind((Text_Message) message);
                break;
            case VIEW_IMAGE_RECEIVED_MESSAGE:
                ((Image_Received_Message_Holder) holder).bind((Multimedia_File_Android) message);
                break;
            case VIEW_IMAGE_SENT_MESSAGE:
                ((Image_Sent_Message_Holder) holder).bind((Multimedia_File_Android) message);
                break;
            case VIEW_VIDEO_RECEIVED_MESSAGE:
                break;
            case VIEW_VIDEO_SENT_MESSAGE:
                break;
        }
    }

    private String findextensionType(Multimedia_File_Android file){
        String extension = "";
        int i = file.getFile_name().lastIndexOf('.');
        if (i > 0) {
            extension = file.getFile_name().substring(i+1);
        }
        return extension;
    }

    private byte[] copyBytes(Multimedia_File_Android message){
        //get the maximum number of chunks for the specific multimedia file
        int max_sequence = message.getChunks().get(0).getMax_sequence_number();
        int chunk_size = message.getChunks().get(0).getChunk_size();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for(int i = 0; i< message.getChunks().size();i++){
            //add the specific chunk to the file byte array
            byte[] chunk = message.getChunks().get(i).getChunk();
            try {
                byteArrayOutputStream.write(chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int getItemViewType(int position) {
        Value value = (Value) message_list.get(position);
        String current_username = androidUserNode.getName();
        //check if the publisher name is the same as the current user running the app.
        if(value.getPublisher().equals(current_username)){
            if(value instanceof Text_Message){
                return VIEW_TEXT_SENT_MESSAGE;
            }else{
                Multimedia_File_Android file = (Multimedia_File_Android) value;
                if(findextensionType(file).equals("jpg")){
                    return VIEW_IMAGE_SENT_MESSAGE;
                }else{
                    return VIEW_VIDEO_SENT_MESSAGE;
                }
               //if(findextensionType(file).equals("mp4")){
            }
        }else{
            if(value instanceof Text_Message){
                return VIEW_TEXT_RECEIVED_MESSAGE;
            }else{
                Multimedia_File_Android file = (Multimedia_File_Android) value;
                if(findextensionType(file).equals("jpg")){
                    System.out.println("Found received jpg image");
                    return VIEW_IMAGE_RECEIVED_MESSAGE;
                }else{
                    return VIEW_VIDEO_RECEIVED_MESSAGE;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return message_list.size();
    }

    protected class Text_Received_Message_Holder extends RecyclerView.ViewHolder{
        TextView message_content,timeText,name_of_sender,date_of_message;
        public Text_Received_Message_Holder(@NonNull View itemView) {
            super(itemView);
            message_content = (TextView) itemView.findViewById(R.id.received_text_message_content);
            timeText = (TextView) itemView.findViewById(R.id.received_text_message_timestamp);
            date_of_message = (TextView) itemView.findViewById(R.id.received_text_message_date);
            name_of_sender = (TextView) itemView.findViewById(R.id.received_text_message_sender);
        }
        void bind(Text_Message text_message){
            message_content.setText(text_message.getContents());
            //date is in format date__time
            timeText.setText(text_message.getDateCreated().split(" ")[1]);
            date_of_message.setText(text_message.getDateCreated().split(" ")[0]);
            name_of_sender.setText(text_message.getPublisher());
        }

    }

    protected class Text_Sent_Message_Holder extends RecyclerView.ViewHolder{
        TextView message_content,timeText,date_of_message;
        public Text_Sent_Message_Holder(@NonNull View itemView) {
            super(itemView);
            message_content = itemView.findViewById(R.id.sent_text_message_content);
            timeText = itemView.findViewById(R.id.sent_text_message_timestamp);
            date_of_message = itemView.findViewById(R.id.sent_text_message_date);
        }

        void bind(Text_Message text_message){
            message_content.setText(text_message.getContents());
            //date is in format date__time
            timeText.setText(text_message.getDateCreated().split(" ")[1]);
            date_of_message.setText(text_message.getDateCreated().split(" ")[0]);
        }
    }

    protected class Image_Received_Message_Holder extends RecyclerView.ViewHolder{
        ImageView image_received;
        TextView date_received,timestamp,name_of_sender;
        public Image_Received_Message_Holder(@NonNull View itemView) {
            super(itemView);
            date_received = itemView.findViewById(R.id.received_image_message_date);
            timestamp = itemView.findViewById(R.id.received_image_message_timestamp);
            name_of_sender = itemView.findViewById(R.id.received_image_message_sender);
            image_received = (ImageView) itemView.findViewById(R.id.received_image_message_contents);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        void bind(Multimedia_File_Android message){

            //create bitmap through the multimedia file's byte array.
            byte[] file = copyBytes(message);
            Bitmap bmp = BitmapFactory.decodeByteArray(file,0,file.length);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            image_received.setImageBitmap(Bitmap.createScaledBitmap(bmp,width,height,false));
            //date is in format date__time
            date_received.setText(message.getDateCreated().split(" ")[0]);
            timestamp.setText(message.getDateCreated().split(" ")[1]);
            name_of_sender.setText(message.getPublisher());
        }
    }

    protected class Image_Sent_Message_Holder extends RecyclerView.ViewHolder{
        ImageView image_sent;
        TextView date_sent,timestamp;
        public Image_Sent_Message_Holder(@NonNull View itemView) {
            super(itemView);
            date_sent = itemView.findViewById(R.id.sent_image_message_date);
            timestamp = itemView.findViewById(R.id.sent_image_message_timestamp);
            image_sent = itemView.findViewById(R.id.sent_image_message_contents);
        }
        void bind(Multimedia_File_Android message){
            //create bitmap through the multimedia file's byte array.
            byte[] file = copyBytes(message);
            Bitmap bmp = BitmapFactory.decodeByteArray(file,0,file.length);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            System.out.println("Width: " + width + " height: " + height);
            image_sent.setImageBitmap(Bitmap.createScaledBitmap(bmp,width,height,false));
            date_sent.setText(message.getDateCreated().split(" ")[0]);
            timestamp.setText(message.getDateCreated().split(" ")[1]);
        }
    }

    protected class Video_Received_Message_Holder extends RecyclerView.ViewHolder{
        VideoView video_received;
        TextView date_received,timestamp,name_of_sender;
        public Video_Received_Message_Holder(@NonNull View itemView) {
            super(itemView);
            date_received = itemView.findViewById(R.id.received_video_message_date);
            timestamp = itemView.findViewById(R.id.received_video_message_timestamp);
            name_of_sender = itemView.findViewById(R.id.received_video_message_sender);
        }
        void bind(Multimedia_File_Android message){
            //date is in format date__time
            date_received.setText(message.getDateCreated().split(" ")[0]);
            timestamp.setText(message.getDateCreated().split(" ")[1]);
        }
    }

    protected class Video_Sent_Message_Holder extends RecyclerView.ViewHolder{
        VideoView video_sent;
        TextView date_sent,timestamp;
        public Video_Sent_Message_Holder(@NonNull View itemView) {
            super(itemView);
            date_sent = itemView.findViewById(R.id.sent_video_message_date);
            timestamp = itemView.findViewById(R.id.sent_video_message_timestamp);
        }
        void bind(Multimedia_File_Android message){
            date_sent.setText(message.getDateCreated().split(" ")[0]);
            timestamp.setText(message.getDateCreated().split(" ")[1]);

        }
    }
}
