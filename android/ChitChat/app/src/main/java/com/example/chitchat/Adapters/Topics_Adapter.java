package com.example.chitchat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.R;

import java.util.ArrayList;

public class Topics_Adapter extends RecyclerView.Adapter<Topics_Adapter.TopicHolder> {

    private ArrayList<String> topics;
    private Context context;
    private onUserClickListener onUserClickListener;

    public ArrayList<String> getTopics() {
        return topics;
    }

    public Topics_Adapter(ArrayList<String> topics, Context context, onUserClickListener onUserClickListener) {
        this.topics = topics;
        this.onUserClickListener = onUserClickListener;
        this.context = context;
    }

    public interface onUserClickListener{
        void onUserClicked(View v,int position); // position inside array list
    }

    @NonNull
    @Override
    public TopicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_holder, parent, false);
        return new TopicHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicHolder holder, int position) {
        holder.textView.setText(topics.get(position));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public void addTopic(String topic_name){
        if(!topics.contains(topic_name)) {
            topics.add(topic_name);
            notifyItemInserted(topics.size() - 1);
        }
    }

    public void removeTopic(String topic_name){
        if(topics.contains(topic_name)) {
            int position = topics.indexOf(topic_name);
            topics.remove(topic_name);
            notifyItemRemoved(position);
        }
    }

    protected class TopicHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ImageView imageView;

        public TopicHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO this will open the chatroom for the specific topic
                    onUserClickListener.onUserClicked(v,getAdapterPosition());
                }
            });
            textView = itemView.findViewById(R.id.text_for_topic_image);
            imageView = itemView.findViewById(R.id.topic_image);
        }


    }
}
