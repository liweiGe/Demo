package com.example.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.test.tflite.Classifier;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnCheckedListener;
import com.zhihu.matisse.listener.OnSelectedListener;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;
    private TextView mPicPath;
    private Classifier classifier;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();
        try {
            classifier = Classifier.create(this, Classifier.Device.CPU, 8);
            Toast.makeText(this, "初始化完毕", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (classifier == null) {
            Toast.makeText(this, "鉴黄系统初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkMyPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {

                            Matisse.from(Main2Activity.this)
                                    .choose(MimeType.ofImage(), false)
                                    .countable(true)
                                    .capture(true)
                                    .captureStrategy(
                                            new CaptureStrategy(true, "com.example.test.fileprovider", "test"))
                                    .maxSelectable(1)
                                    .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
//                                    .gridExpectedSize(
//                                            getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                    .thumbnailScale(0.85f)
                                    .imageEngine(new Glide4Engine())    // for glide-V4
                                    .originalEnable(true)
                                    .maxOriginalSize(10)
                                    .autoHideToolbarOnSingleTap(true)
                                    .forResult(REQUEST_CODE_CHOOSE);


                        } else {
                            Toast.makeText(Main2Activity.this, "权限", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> strings = Matisse.obtainPathResult(data);
            path = strings.get(0);
            mPicPath.setText(path);
//            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)));
        }
    }


    public void selectedPic(View view) {
        checkMyPermission();
    }

    private void initView() {
        mPicPath = (TextView) findViewById(R.id.pic_path);
    }

    public void start(View view) {
        if (classifier != null) {
            long start = System.currentTimeMillis();
            Classifier.NsfwBean nsfwBean = classifier.run(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(path),
                    224,
                    224,
                    false
            ));

            long end = System.currentTimeMillis();
            String result = "一点都没意思,挑张暴露的嘛";
            if (nsfwBean.getNsfw() > 0.9f) {
                result = "有点黄哦";
            }
            Toast.makeText(this, "耗时: " + (end - start) + "ms"
                    + "   结果:" + result, Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "耗时: " + (end - start) + "ms" + "   结果:" + nsfwBean.toString(), Toast.LENGTH_SHORT).show();
//            Log.i("weige", "start: " + (end - start) + "ms" + "   结果:" + nsfwBean.toString());
        }
    }
}
