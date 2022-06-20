package com.example.chitchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Topics_Adapter extends RecyclerView.Adapter<Topics_Adapter.TopicHolder> {

    private ArrayList<String> topics;
    private Context context;
    private onUserClickListener onUserClickListener;

    public Topics_Adapter(ArrayList<String> topics, Context context) {
        this.topics = topics;
        this.context = context;
    }

    interface onUserClickListener{
        void onUserClicked(int position); // position inside array list
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

    protected class TopicHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ImageView imageView;

        public TopicHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO this will open the chatroom for the specific topic
                }
            });
            textView = itemView.findViewById(R.id.text_for_topic_image);
            imageView = itemView.findViewById(R.id.topic_image);
        }


    }
}
