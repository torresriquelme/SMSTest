package com.torresriquelme.smstest;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    //Actualizado desde el Laptop personal


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void goToInbox(View v){
        Intent intent = new Intent(MainActivity.this, ReceiveSMSActivity.class);
        startActivity(intent);
    }


    public void goToCompose(View v){
        Intent intent = new Intent(MainActivity.this, SendSMSActivity.class);
        startActivity(intent);
    }
    
}
