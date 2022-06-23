package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.example.chitchat.Tools.MultimediaFile;
import com.example.chitchat.Tools.Multimedia_File_Android;
import com.example.chitchat.Tools.Text_Message;
import com.example.chitchat.Tools.Value;
import com.example.chitchat.UserNode.UserNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Logging.ConsoleColors;

public class Message_List_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Message_List_Adapter message_list_adapter;

    private ImageButton send;
    private ImageButton open_gallery;
    private EditText text_message;

    private UserNode userNode;
    private String topic_name;


    private List<Value> messageList;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        send = (ImageButton) findViewById(R.id.send_button_chatroom);
        open_gallery = (ImageButton) findViewById(R.id.open_gallery_button_chatroom);
        text_message = (EditText) findViewById(R.id.enter_message_chatroom);
        messageList = new ArrayList<>();


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            this.userNode = (UserNode) extras.get("User Node");
            this.topic_name = (String) extras.get("Topic Name");
        }
        /*messageList.add(new MultimediaFile("Fotis","C:\\Users\\fotis\\OneDrive\\Desktop\\sent_files\\kitten.jpg"));
        messageList.add(new MultimediaFile("Kostas Kakoutopoulos","C:\\Users\\fotis\\OneDrive\\Desktop\\sent_files\\bruno_bottoming"));*/

        recyclerView = (RecyclerView) findViewById(R.id.recycler_chatroom);
        message_list_adapter = new Message_List_Adapter(this,messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(message_list_adapter);



        //when you press send button send to server and create it locally
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMessage();
            }
        });

        open_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void getConversationData(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Multimedia_File_Android createMultimediaFile(Context  context, String file_name){
        String extension = "";
        int i = file_name.lastIndexOf('.');
        if(i > 0){
            Multimedia_File_Android multimedia_file_android = new Multimedia_File_Android(GlobalVariables.getInstance().getUsername(), file_name,this);
            return multimedia_file_android;
        }else{
            System.out.println("Your file name should only include 1 dot");
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createMessage(){
        String contents = text_message.getText().toString();
        message_list_adapter.addMessage(new Text_Message(GlobalVariables.getInstance().getUsername(),contents));
        text_message.getText().clear();
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        gallery.setType("image/* video/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(gallery,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_choices,menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int result,Intent data) {
        if(result == RESULT_OK){
            Uri selectedMediaUri = data.getData();
            if(selectedMediaUri.toString().contains("image")) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedMediaUri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(selectedMediaUri.toString().contains("video")){

            }
        }
        super.onActivityResult(requestCode, result, data);
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