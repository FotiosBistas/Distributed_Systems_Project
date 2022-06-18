package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class Central_Screen_Activity extends AppCompatActivity{
    RecyclerView recyclerView;
    ArrayList<String> topics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");
        topics.add("asdfasdfa");

        setContentView(R.layout.activity_central_screen);
        recyclerView = findViewById(R.id.recycler_view);
        TopicsAdapter topicsAdapter = new TopicsAdapter(topics, this);
        recyclerView.setAdapter(topicsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_choices,menu);

        return true;
    }

    /**
     * Creates the pop up menu when the plus icon is pressed on the action bar
     */
    public void createPopUpMenu(){
        View menu_item_view = (View) findViewById(R.id.add_circle);
        PopupMenu popup = new PopupMenu(Central_Screen_Activity.this,menu_item_view);
        popup.getMenuInflater().inflate(R.menu.action_bar_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_bar_menu_subscribe_to_topic){
                    //give the topic name you want to subscribe to
                    return true;
                }else if(item.getItemId() == R.id.action_bar_menu_logout){
                    //go back to connect screen if log out is pressed
                    Intent intent = new Intent(Central_Screen_Activity.this,Connect_Activity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile) {
            startActivity(new Intent(Central_Screen_Activity.this, Profile_Activity.class));
        }else if(item.getItemId() == R.id.add_circle){
            createPopUpMenu();
        }
        return super.onOptionsItemSelected(item);
    }
    
}