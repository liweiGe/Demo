package com.example.test;

import android.os.Bundle;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.utils.widget.ImageFilterView;

public class MainActivity extends AppCompatActivity {

    private ImageFilterView mImageFilter;
    private AppCompatSeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mImageFilter = (ImageFilterView) findViewById(R.id.image_filter);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentage = (progress + 0.0f) / 100f;
                mImageFilter.setSaturation(percentage);
                mImageFilter.setWarmth(percentage);
                mImageFilter.setContrast(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
