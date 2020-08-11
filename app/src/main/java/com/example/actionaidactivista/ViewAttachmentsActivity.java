package com.example.actionaidactivista;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class ViewAttachmentsActivity extends AppCompatActivity {

    private CardView imageCard;
    private CardView videoCard;
    private CardView audioCard;
    private SimpleExoPlayerView videoExoPlayerView;
    private SimpleExoPlayer videoExoPlayer;
    private SimpleExoPlayerView audioExoPlayerView;
    private SimpleExoPlayer audioExoPlayer;
    private ImageView imageView;
    private Intent intent;
    private String picUrl;
    private String vidUrl;
    private String audUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //set UI
            setContentView(R.layout.activity_view_attachments);
            //set title
            try {
                getSupportActionBar().setTitle("Application Attachments");
            } catch (Exception e) {
                System.out.println(e);
            }
            //init UI components
            imageCard = (CardView) findViewById(R.id.pic_card_view);
            videoCard = (CardView) findViewById(R.id.vid_card_view);
            audioCard = (CardView) findViewById(R.id.aud_card_view);
            videoExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.videoExo);
            audioExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.audioExo);
            imageView = (ImageView) findViewById(R.id.imageView);

            //get urls
            intent = getIntent();
            picUrl = intent.getStringExtra("image");
            audUrl = intent.getStringExtra("audio");
            vidUrl = intent.getStringExtra("video");

            //hide the controls
            imageCard.setVisibility(View.GONE);
            audioCard.setVisibility(View.GONE);
            videoCard.setVisibility(View.GONE);
            //if there is no media then do not show the controls
            if (picUrl.equalsIgnoreCase("N/A") && vidUrl.equalsIgnoreCase("N/A") && audUrl.equalsIgnoreCase("N/A")) {
                methods.showAlert("No media", "No motivational media was uploaded.", this);
                return;
            }
            if (!picUrl.equalsIgnoreCase("N/A")) {
                imageCard.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .applyDefaultRequestOptions(methods.requestOptions(R.drawable.ic_menu_camera))
                        .load(picUrl)
                        .into(imageView);
            }
            if (!vidUrl.equalsIgnoreCase("N/A")) {
                videoCard.setVisibility(View.VISIBLE);
                play(vidUrl,"vid");
            }
            if (!audUrl.equalsIgnoreCase("N/A")) {
                audioCard.setVisibility(View.VISIBLE);
                play(audUrl,"aud");
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show();
        }
    }

    private void play(String url, String vid_aud) {
        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
            Uri uri;
            if (vid_aud.equalsIgnoreCase("vid")) {
                videoExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
                uri = Uri.parse(url);
            } else {
                audioExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
                uri = Uri.parse(url);
            }
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            if (vid_aud.equalsIgnoreCase("vid")) {
                videoExoPlayerView.setPlayer(videoExoPlayer);
                videoExoPlayer.prepare(mediaSource);
                videoExoPlayer.setPlayWhenReady(false);
            } else {
                audioExoPlayerView.setPlayer(audioExoPlayer);
                audioExoPlayer.prepare(mediaSource);
                audioExoPlayer.setPlayWhenReady(false);
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), this);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (audioExoPlayer != null) {
                audioExoPlayer.release();
            }
            if (videoExoPlayer != null) {
                videoExoPlayer.release();
            }
        }catch (Exception e){
            System.out.println(e);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            if (audioExoPlayer != null) {
                audioExoPlayer.release();
            }
            if (videoExoPlayer != null) {
                videoExoPlayer.release();
            }
        }catch (Exception e){
            System.out.println(e);
        }
        super.onDestroy();
    }

}
