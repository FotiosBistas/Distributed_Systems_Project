package com.example.chitchat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chitchat.R;
import com.example.chitchat.UserNode.UserNode;

public class Connect_Activity extends AppCompatActivity {

    private EditText edit_username,edit_port,edit_ip;
    private Button connect_button;
    private UserNode userNode;
    private final int PERMISSIONS_CODE = 1;
    private String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        //fields that are filled by the user
        edit_username= findViewById(R.id.editusername);
        edit_ip = findViewById(R.id.editIP);
        edit_port = findViewById(R.id.editPort);
        //if already logged in a previous session go instantly to menu
        if(!(edit_username.getText().toString().isEmpty() && edit_ip.getText().toString().isEmpty() && edit_port.getText().toString().isEmpty())){
            startActivity(new Intent(Connect_Activity.this, Central_Screen_Activity.class));
            finish();
        }

        //will tap this to connect to the service
        connect_button = findViewById(R.id.connectbutton);

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check to see if user inserted all values

                if(edit_username.getText().toString().isEmpty() ||edit_ip.getText().toString().isEmpty() || edit_port.getText().toString().isEmpty()){
                    //throw error message
                    Toast.makeText(Connect_Activity.this, "You did not provide username,ip or port number ", Toast.LENGTH_SHORT).show();
                    return;
                }
                //create the user with the specified parameters given by the user
                Connect_Activity.this.userNode = new UserNode(edit_ip.getText().toString(),Integer. parseInt(edit_port.getText().toString()),edit_username.getText().toString());
                System.out.println("starting async task");
                Intent intent = new Intent(Connect_Activity.this, Central_Screen_Activity.class);
                intent.putExtra("User Node",userNode);
                startActivity(intent);
                finish();
            }
        });
    }







}