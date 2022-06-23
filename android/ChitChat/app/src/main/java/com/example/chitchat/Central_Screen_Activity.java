package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.example.chitchat.UserNode.NetworkingForConsumer;
import com.example.chitchat.UserNode.UserNode;

import java.util.ArrayList;

public class Central_Screen_Activity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private ArrayList<String> topics = new ArrayList<>();

    private ProgressBar progressBar;
    private UserNode userNode;
    private Topics_Adapter topicsAdapter;


    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public UserNode getUserNode() {
        return userNode;
    }

    public Topics_Adapter getTopicsAdapter() {
        return topicsAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central_screen);

        progressBar = (ProgressBar) this.findViewById(R.id.indeterminateBar);
        //retrieve object of user node from connect activity
        this.userNode = (UserNode) getIntent().getSerializableExtra("User Node");
        //operations 1,2,3 are get broker list ,get  id list and send nickname
        new NetworkingForConsumer(this,userNode).execute(1,2,3);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        Topics_Adapter topicsAdapter = new Topics_Adapter(topics, this);
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
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Central_Screen_Activity.this);
                    final EditText topic_name = new EditText(Central_Screen_Activity.this);
                    topic_name.setInputType(InputType.TYPE_CLASS_TEXT);
                    alertDialog.setView(topic_name);
                    alertDialog.setMessage("Subscribe to what topic?").setTitle("Subscribe");
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new NetworkingForConsumer(Central_Screen_Activity.this,topic_name.getText().toString(),
                                    userNode).execute(4);

                        }
                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
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