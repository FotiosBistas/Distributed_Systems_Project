package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.chitchat.Tools.Text_Message;
import com.example.chitchat.Tools.Value;

import java.util.ArrayList;
import java.util.List;

public class Message_List_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Message_List_Adapter message_list_adapter;

    private List<Value> messageList;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        messageList = new ArrayList<>();
        GlobalVariables.getInstance().setUsername("Fotis");
        messageList.add(new Text_Message("Fotis","Hello there"));
        recyclerView = (RecyclerView) findViewById(R.id.recycler_chatroom);
        message_list_adapter = new Message_List_Adapter(this,messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(message_list_adapter);
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
        PopupMenu popup = new PopupMenu(Message_List_Activity.this,menu_item_view);
        popup.getMenuInflater().inflate(R.menu.action_bar_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_bar_menu_subscribe_to_topic){
                    //give the topic name you want to subscribe to
                    return true;
                }else if(item.getItemId() == R.id.action_bar_menu_logout){
                    //go back to connect screen if log out is pressed
                    Intent intent = new Intent(Message_List_Activity.this,Connect_Activity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }


    /**
     * The main toolbar menu displayed for the app
     * @param item item selected from the toolbar menu
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile) {
            startActivity(new Intent(Message_List_Activity.this, Profile_Activity.class));
        }else if(item.getItemId() == R.id.add_circle){
            createPopUpMenu();
        }
        return super.onOptionsItemSelected(item);
    }
}