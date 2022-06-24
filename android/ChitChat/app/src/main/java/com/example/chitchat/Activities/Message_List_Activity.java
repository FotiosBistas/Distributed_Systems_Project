package com.example.chitchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.example.chitchat.R;
import com.example.chitchat.Tools.Multimedia_File_Android;
import com.example.chitchat.Tools.Text_Message;
import com.example.chitchat.Tools.Value;
import com.example.chitchat.UserNode.NetworkingForConsumer;
import com.example.chitchat.UserNode.NetworkingForPublisher;
import com.example.chitchat.UserNode.Android_User_Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.chitchat.Adapters.Message_List_Adapter;
import com.example.chitchat.UserNode.Pull_request;

public class Message_List_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Message_List_Adapter message_list_adapter;
    private ProgressBar progressBar;

    public ProgressBar getProgressBar(){
        return progressBar;
    }

    /**
     * called at the end of async task with request type 6 to add the received elements to the recycler view
     * @return the message list adapter
     */
    public Message_List_Adapter getMessage_list_adapter() {
        return message_list_adapter;
    }

    private ImageButton send;
    private ImageButton open_gallery;
    private EditText text_message;

    private Android_User_Node androidUserNode;
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
        progressBar = (ProgressBar) findViewById(R.id.message_list_bar);
        messageList = new ArrayList<>();


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            this.androidUserNode = (Android_User_Node) extras.get("User Node");
            this.topic_name = (String) extras.get("Topic Name");
        }


        recyclerView = (RecyclerView) findViewById(R.id.recycler_chatroom);
        message_list_adapter = new Message_List_Adapter(this,messageList,this.androidUserNode);
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

        getConversationData();
    }

    private void getConversationData(){
        //receive conversation data
        NetworkingForConsumer networkingForConsumer = new NetworkingForConsumer(this,topic_name, androidUserNode);
        networkingForConsumer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,6);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Multimedia_File_Android createMultimediaFile(Context  context, String file_name){
        String extension = "";
        int i = file_name.lastIndexOf('.');
        if(i > 0){
            //Multimedia_File_Android multimedia_file_android = new Multimedia_File_Android(GlobalVariables.getInstance().getUsername(), file_name,this);
            //return multimedia_file_android;
            return null; 
        }else{
            System.out.println("Your file name should only include 1 dot");
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createMessage(){
        String contents = text_message.getText().toString();
        message_list_adapter.addMessage(new Text_Message(androidUserNode.getName(),contents));
        //push text_message = 0
        NetworkingForPublisher networkingForPublisher = new NetworkingForPublisher(this,topic_name, androidUserNode,contents);
        networkingForPublisher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,0);
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
        getMenuInflater().inflate(R.menu.chatroom_action_bar_menu,menu);
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
        if(item.getItemId() == R.id.refresh_chat) {
            new Pull_request();
        }else if(item.getItemId() == R.id.go_back){
            Intent intent = new Intent(Message_List_Activity.this,Central_Screen_Activity.class);
            intent.putExtra("User Node",androidUserNode);
            intent.putExtra("message list activity",true);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}