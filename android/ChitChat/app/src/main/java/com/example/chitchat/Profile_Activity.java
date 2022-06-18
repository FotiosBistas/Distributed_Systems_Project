package com.example.chitchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class Profile_Activity extends AppCompatActivity {

    private Button upload_image_button;
    private ImageView profile_image;
    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //TODO send to the brokers so the brokers notify all new users
        upload_image_button = findViewById(R.id.upload_image_button);

        profile_image = findViewById(R.id.profile_image);
        //upload button to broker
        upload_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Profile_Activity.this,"No functionality yet",Toast.LENGTH_SHORT).show();
                uploadImage();
                return;
            }
        });

        //if image is pressed open gallery and upload photo
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //knows that we'll pick something from the intent
                Intent image_intent = new Intent(Intent.ACTION_PICK);
                //we pick image
                image_intent.setType("image/*");
                startActivityForResult(image_intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imagePath = data.getData();
            getImageInImageView();

        }
    }

    private void uploadImage(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        //received image received from broker and end progress dialog
        //progressDialog.dismiss();
    }

    private void updateProfilePicture(){

    }

    private void getImageInImageView() {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        profile_image.setImageBitmap(bitmap);
    }
}