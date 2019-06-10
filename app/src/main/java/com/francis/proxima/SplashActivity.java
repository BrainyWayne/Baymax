package com.francis.proxima;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    /**
     * *
     * * Francis Eshun 2019
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }, 2000);
    }
}
