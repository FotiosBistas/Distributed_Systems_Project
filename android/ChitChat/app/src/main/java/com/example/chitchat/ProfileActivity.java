package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {

    private Button log_out_button;
    private Button upload_image_button;
    private ImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        log_out_button = findViewById(R.id.log_out_button);
        upload_image_button = findViewById(R.id.upload_image_button);
        profile_image = findViewById(R.id.profile_image);
        //if log out button is pressed return to main activity
        log_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        //if image is pressed open gallery and upload photo
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent image_intent = new Intent(Intent.ACTION_PICK);
                image_intent.setType("image/*");
                startActivityForResult(image_intent,1);
            }
        });


    }
}