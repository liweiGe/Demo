package com.example.newanimdemo;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private NestedScrollView mScrollView;
    private ImageView mImageHeader;

    private AutoTransition mSet;
    private RelativeLayout mRlContent;


    private void initView() {
        mScrollView = (NestedScrollView) findViewById(R.id.scroll_view);
        mImageHeader = (ImageView) findViewById(R.id.image_header);
        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    if (!expand) return;
                    reduce();
                } else {
                    if (expand) return;
                    expand();
                }
            }
        });
        mRlContent = (RelativeLayout) findViewById(R.id.rl_content);
    }

    boolean expand = true;

    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void expand() {

        //设置伸展状态时的布局
        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) mImageHeader.getLayoutParams();
        LayoutParams.height = dp2px(300);
        mImageHeader.setLayoutParams(LayoutParams);
        //开始动画
        beginDelayedTransition(mRlContent);
        expand = true;
    }

    private void reduce() {
        //设置收缩状态时的布局
        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) mImageHeader.getLayoutParams();
        LayoutParams.height = dp2px(200);
        mImageHeader.setLayoutParams(LayoutParams);
        //开始动画
        beginDelayedTransition(mRlContent);
        expand = false;
    }

    void beginDelayedTransition(ViewGroup view) {
        mSet = new AutoTransition();
        mSet.setDuration(1000);
        TransitionManager.beginDelayedTransition(view, mSet);
    }
}
