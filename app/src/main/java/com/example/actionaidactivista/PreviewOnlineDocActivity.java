package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.exoplayer2.text.webvtt.WebvttDecoder;

public class PreviewOnlineDocActivity extends AppCompatActivity {

    private String mUrl;
    private String mType;//doc type word,pdf etc
    private String mMode;//defines whether its library item or application doc e.g cv
    private Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set UI
            setContentView(R.layout.activity_preview_online_doc);
            mIntent = getIntent();
            mUrl = mIntent.getStringExtra("url");
            mType = mIntent.getStringExtra("type");
            mMode = mIntent.getStringExtra("mode");

            WebView webView = (WebView)findViewById(R.id.previewDoc);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(mUrl);

        }catch (Exception e){
            Toast.makeText(this,"Error loading UI",Toast.LENGTH_SHORT).show();
        }
    }
}
