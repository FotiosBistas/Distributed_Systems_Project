package com.example.chitchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Tools.MultimediaFile;
import com.example.chitchat.Tools.Story;
import com.example.chitchat.Tools.Text_Message;
import com.example.chitchat.Tools.Value;

import org.w3c.dom.Text;

import java.util.List;

public class Message_List_Adapter extends RecyclerView.Adapter {
    //those two static and final variables indicate with what message are we filling the recycler view
    private static final int VIEW_RECEIVED_MESSAGE = 1;
    private static final int VIEW_SENT_MESSAGE = 2;

    private Context context;
    private List<Value> message_list;


    Message_List_Adapter(Context context,List<Value> message_list){
        this.context = context;
        this.message_list = message_list;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEW_SENT_MESSAGE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_chat_other,parent,false);
            return new Sent_Message_Holder(view);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_chat_me,parent,false);
            return new Received_Message_Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Value message = (Value) message_list.get(position);

        if(holder.getItemViewType() == VIEW_SENT_MESSAGE){
            ((Sent_Message_Holder) holder).bind(message);
        }else{
            ((Received_Message_Holder) holder).bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Value value = (Value) message_list.get(position);
        String current_username = GlobalVariables.getInstance().getUsername();
        if(value.getPublisher().equals(current_username)){
            return VIEW_SENT_MESSAGE;
        }else {
            return VIEW_RECEIVED_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return message_list.size();
    }

    protected class Received_Message_Holder extends RecyclerView.ViewHolder{
        TextView message_content,timeText,name_of_sender;
        public Received_Message_Holder(@NonNull View itemView) {
            super(itemView);

            message_content = (TextView) itemView.findViewById(R.id.text_other_message);
            timeText = (TextView) itemView.findViewById(R.id.message_timestamp_other);
            name_of_sender = (TextView) itemView.findViewById(R.id.identifier_for_user_that_sent_message);
        }
        void bind(Value message){
            if(message instanceof Text_Message) {
                Text_Message text_message = (Text_Message) message;
                message_content.setText(text_message.getContents());
                timeText.setText(text_message.getDateCreated());
                name_of_sender.setText(text_message.getPublisher());
            }
        }
    }

    protected class Sent_Message_Holder extends RecyclerView.ViewHolder{
        TextView message_content,timeText;
        public Sent_Message_Holder(@NonNull View itemView) {
            super(itemView);
            message_content = (TextView) itemView.findViewById(R.id.text_my_message);
            timeText = (TextView) itemView.findViewById(R.id.message_timestamp_me);
        }

        void bind(Value message){
            if(message instanceof Text_Message) {
                Text_Message text_message = (Text_Message) message;
                message_content.setText(text_message.getContents());
                timeText.setText(text_message.getDateCreated());
            }
        }



    }
}
