package com.example.vrdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.video.txvideolib.VideoPlayerView;

public class Main2Activity extends AppCompatActivity {
    String url = "http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4";
    private VideoPlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        playerView = findViewById(R.id.video_player);
        ImageView cover = playerView.getCover();
        cover.setImageResource(R.drawable.ic_launcher_background);
    }

    public void play(View view) {
        playerView.playWithModel(null, "10", "100",
                url);
    }
}
