package com.example.vrdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.zph.glpanorama.GLPanorama;

/**
 * 使用openGl实现
 */
public class OpenGLActivity extends AppCompatActivity {
    private GLPanorama mGLPanorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl);
        mGLPanorama = (GLPanorama) findViewById(R.id.mGLPanorama);
        //传入全景图片
        mGLPanorama.setGLPanorama(R.drawable.imggugong);
    }

}
