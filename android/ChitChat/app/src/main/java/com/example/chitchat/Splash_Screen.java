package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class Splash_Screen extends AppCompatActivity {

    private static int SPLASH_TIMER = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        //overlap on main window for 3000 milliseconds
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Intent intent = new Intent(Splash_Screen.this, Connect_Activity.class);
                Intent intent = new Intent(Splash_Screen.this, Central_Screen_Activity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIMER);
    }
}