package com.example.chitchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.Tools.Topic;

import java.util.ArrayList;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.TopicHolder> {

    private ArrayList<Topic> topics;
    private Context context;
    private onUserClickListener onUserClickListener;

    public TopicsAdapter(ArrayList<Topic> topics, Context context, TopicsAdapter.onUserClickListener onUserClickListener) {
        this.topics = topics;
        this.context = context;
        this.onUserClickListener = onUserClickListener;
    }

    interface onUserClickListener{
        void onUserClicked(int position); // position inside array list
    }

    @NonNull
    @Override
    public TopicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.topic_holder,parent,false);
        return new TopicHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicHolder holder, int position) {
        holder.textView.setText(topics.get(position).getName());
        //TODO get image from broker and inflate
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
            textView = itemView.findViewById(R.id.topic_name);
            imageView = imageView.findViewById(R.id.topic_image);
        }


    }
}
