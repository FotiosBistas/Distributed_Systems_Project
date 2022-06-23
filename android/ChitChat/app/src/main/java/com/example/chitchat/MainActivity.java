package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText$InspectionCompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chitchat.UserNode.UserNode;

public class MainActivity extends AppCompatActivity {

    private EditText edit_username,edit_port,edit_ip;
    private Button connect_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //fields that are filled by the user
        edit_username = findViewById(R.id.editusername);
        edit_ip = findViewById(R.id.editIP);
        edit_port = findViewById(R.id.editPort);
        String username = edit_username.getText().toString();
        String ip_address = edit_ip.getText().toString();
        String port_number = edit_port.getText().toString();
        //TODO check if this stores the state
        //if already logged in a previous session go instantly to menu
        if(username.isEmpty() && ip_address.isEmpty() && port_number.isEmpty()){
            startActivity(new Intent(MainActivity.this,Menu_Activity.class));
            finish();
        }

        //will tap this to connect to the service
        connect_button = findViewById(R.id.connectbutton);

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check to see if user inserted all values

                if(username.isEmpty() ||ip_address.isEmpty() || port_number.isEmpty()){
                    //throw error message
                    Toast.makeText(MainActivity.this, "You did not provide username,ip or port number ", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleConnect(username,ip_address,port_number);
            }
        });
    }

    private void handleConnect(String username,String ip_address,String port_number){
        //create the user with the specified parameters given by the user
        Toast.makeText(MainActivity.this, "User connected successfully", Toast.LENGTH_SHORT).show();
        UserNode user = new UserNode(username,Integer.parseInt(port_number),ip_address);
        startActivity(new Intent(MainActivity.this,Menu_Activity.class));
        //user.connect();
    }
}