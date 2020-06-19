package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.security.spec.ECField;

public class MainActivity extends AppCompatActivity {

    private LinearLayout feed;
    private LinearLayout register;
    private LinearLayout library;
    private LinearLayout contact;
    private LinearLayout opportunity;
    private LinearLayout upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            feed = (LinearLayout)findViewById(R.id.llFeed);
            register = (LinearLayout)findViewById(R.id.llRegister);
            library = (LinearLayout)findViewById(R.id.llLibrary);
            contact = (LinearLayout)findViewById(R.id.llContact);
            opportunity = (LinearLayout)findViewById(R.id.llOpportunity);
            upload = (LinearLayout)findViewById(R.id.llUpload);

            feed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent upload = new Intent(MainActivity.this,RegistrationActivity.class);
                    startActivity(upload);
                }
            });

            library.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            opportunity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent upload = new Intent(MainActivity.this,ActivityUploadActivity.class);
                    startActivity(upload);
                }
            });
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
