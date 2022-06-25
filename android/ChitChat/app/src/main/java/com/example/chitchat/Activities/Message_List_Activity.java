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
            System.out.println("This topic name: " + this.topic_name);
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
    private void createMessage(){
        String contents = text_message.getText().toString();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int result,Intent data) {
        if(result == RESULT_OK){
            Uri selectedMediaUri = data.getData();
            if(selectedMediaUri.toString().contains("image")) {
                Multimedia_File_Android multimedia_file_android = new Multimedia_File_Android(androidUserNode.getName(),selectedMediaUri,
                        Message_List_Activity.this,"image");
                new NetworkingForPublisher(Message_List_Activity.this,this.topic_name,this.androidUserNode,multimedia_file_android).execute(1);
            }else if(selectedMediaUri.toString().contains("video")){
                Multimedia_File_Android multimedia_file_android = new Multimedia_File_Android(androidUserNode.getName(),selectedMediaUri,
                        Message_List_Activity.this,"video");
            }
        }
        super.onActivityResult(requestCode, result, data);
    }


    /**
     * The main toolbar menu displayed for the app
     * @param item item selected from the toolbar menu
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.refresh_chat) {
            new Pull_request(Message_List_Activity.this,this.topic_name,this.androidUserNode).execute();
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