package com.example.vrdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.video.txvideolib.VideoPlayerView;

public class Main2Activity extends AppCompatActivity {
    String url = "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        VideoPlayerView playerView = findViewById(R.id.video_player);
        playerView.playWithModel(null, "10", "100",
                url);
    }
}
