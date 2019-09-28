package com.video.txvideolib.controller;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.video.txvideolib.R;
import com.video.txvideolib.SuperPlayerConst;
import com.video.txvideolib.view.TCVideoProgressLayout;
import com.video.txvideolib.view.TCVolumeBrightnessProgressLayout;


/**
 * Created by liyuejiao on 2018/7/3.
 * <p>
 * 超级播放器小窗口控制界面
 */
public class TCVodControllerSmall extends TCVodControllerBase implements View.OnClickListener {
    private static final String TAG = "TCVodControllerSmall";
    private LinearLayout mLayoutBottom;
    private ImageView mIvPause;
    private ImageView mIvFullScreen;

    private ImageView mBackground;


    public TCVodControllerSmall(Context context) {
        super(context);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TCVodControllerSmall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    /**
     * 显示播放控制界面
     */
    @Override
    void onShow() {
        mLayoutBottom.setVisibility(View.VISIBLE);

    }

    /**
     * 隐藏播放控制界面
     */
    @Override
    void onHide() {
        mLayoutBottom.setVisibility(View.GONE);
    }


    private void initViews() {
        mLayoutInflater.inflate(R.layout.vod_controller_small, this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.layout_replay);
        mIvPause = (ImageView) findViewById(R.id.iv_pause);
        mTvCurrent = (TextView) findViewById(R.id.tv_current);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mSeekBarProgress = findViewById(R.id.seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setMax(100);
        mIvFullScreen = (ImageView) findViewById(R.id.iv_fullscreen);


        mIvPause.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);

        mLayoutReplay.setOnClickListener(this);


        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout)findViewById(R.id.gesture_progress);

        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.video_progress_layout);

        mBackground = (ImageView)findViewById(R.id.small_iv_background);
    }


    public void dismissBackground() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mBackground.getVisibility() != View.VISIBLE) return;
                ValueAnimator alpha = ValueAnimator.ofFloat(1.0f, 0.0f);
                alpha.setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (Float) animation.getAnimatedValue();
                        mBackground.setAlpha(value);
                        if (value == 0) {
                            mBackground.setVisibility(GONE);
                        }
                    }
                });
                alpha.start();
            }
        });
    }

    public void showBackground() {
        mBackground.setVisibility(VISIBLE);

    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.layout_top) {
            onBack();
        } else if (i == R.id.iv_pause) {
            changePlayState();
        } else if (i == R.id.iv_fullscreen) {
            fullScreen();
        } else if (i == R.id.layout_replay) {
            replay();
        }
    }

    /**
     * 返回窗口模式
     */
    private void onBack() {
        mVodController.onBackPress(SuperPlayerConst.PLAYMODE_WINDOW);
    }

    /**
     * 全屏
     */
    private void fullScreen() {
        mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_FULLSCREEN);
    }



//    @Override
//    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//    }
//
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//
//    }
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        // 拖动seekbar结束时,获取seekbar当前进度,进行seek操作,最后更新seekbar进度
//        int curProgress = seekBar.getProgress();
//        int maxProgress = seekBar.getMax();
//
//        switch (mPlayType) {
//            case SuperPlayerConst.PLAYTYPE_VOD:
//                if (curProgress >= 0 && curProgress < maxProgress) {
//                    // 关闭重播按钮
//                    updateReplay(false);
//                    float percentage = ((float) curProgress) / maxProgress;
//                    int position = (int) (mVodController.getDuration() * percentage);
//                    mVodController.seekTo(position);
//                    mVodController.resume();
//                }
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE:
//            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
//                updateLiveLoadingState(true);
////                mTrackTime = mLivePlayTime * curProgress / maxProgress;
//                TXCLog.i(TAG, "onStopTrackingTouch time:" + mTrackTime);
////                mVodController.onBackToRecord(mLiveBaseTime, mTrackTime);
//                break;
//        }
//    }



    /**
     * 更新点播播放进度
     */
//    public void updateVodVideoProgress() {
//        float curTime = mVodController.getCurrentPlaybackTime();
//        float durTime = mVodController.getDuration();
//
//        if (durTime > 0 && curTime <= durTime) {
//            float percentage = curTime / durTime;
//            if (percentage >= 0 && percentage <= 1) {
//                int progress = Math.round(percentage * mSeekBarProgress.getMax());
//                mSeekBarProgress.setProgress(progress);
//
//                if (durTime >= 0 && curTime <= durTime) {
//                    mTvCurrent.setText(TCTimeUtils.formattedTime((long) curTime));
//                    mTvDuration.setText(TCTimeUtils.formattedTime((long) durTime));
//                }
//            }
//        }
//    }

    /**
     * 更新播放UI
     *
     * @param isStart
     */
    public void updatePlayState(boolean isStart) {
        // 播放中
        if (isStart) {
            mIvPause.setImageResource(R.drawable.ic_vod_pause_normal);
        }
        // 未播放
        else {
            mIvPause.setImageResource(R.drawable.ic_vod_play_normal);
        }
    }

    /**
     * 更新重新播放按钮状态
     *
     * @param replay
     */
//    public void updateReplay(boolean replay) {
//        if (replay) {
//            mLayoutReplay.setVisibility(View.VISIBLE);
//        } else {
//            mLayoutReplay.setVisibility(View.GONE);
//        }
//    }

//    /**
//     * 更新直播播放时间和进度
//     *
//     * @param baseTime
//     */
//    public void updateLivePlayTime(long baseTime) {
//        super.updateLivePlayTime(baseTime);
//        mTvCurrent.setText(TCTimeUtils.formattedTime(mLivePlayTime));
//    }



    /**
     * 更新播放类型
     *
     * @param playType
     */
    public void updatePlayType(int playType) {
        super.updatePlayType(playType);
        mTvDuration.setVisibility(View.VISIBLE);
    }
}
