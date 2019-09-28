package com.video.txvideolib.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.video.txvideolib.R;
import com.video.txvideolib.SuperPlayerConst;
import com.video.txvideolib.view.TCPointSeekBar;
import com.video.txvideolib.view.TCVideoProgressLayout;
import com.video.txvideolib.view.TCVolumeBrightnessProgressLayout;

import java.lang.ref.WeakReference;

/**
 * Created by liyuejiao on 2018/7/3.
 * <p>
 * 超级播放器全屏控制界面
 */
public class TCVodControllerLarge extends TCVodControllerBase
        implements View.OnClickListener,  TCPointSeekBar.OnSeekBarPointClickListener {

    private RelativeLayout mLayoutTop;
    private LinearLayout mLayoutBottom;
    private ImageView mIvBack;
    private ImageView mIvPause;
    //    private TextView mTvCurrent;
//    private TextView mTvDuration;
//    private SeekBar mSeekBarProgress;
    private ImageView mIvLock;
    private ImageView mIvMore;
    private HideLockViewRunnable mHideLockViewRunnable;


    public TCVodControllerLarge(Context context) {
        super(context);
        initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public TCVodControllerLarge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    /**
     * 显示播放控制界面
     */
    @Override
    void onShow() {
        mLayoutTop.setVisibility(View.VISIBLE);
        mLayoutBottom.setVisibility(View.VISIBLE);
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideLockViewRunnable);
        }
        mIvLock.setVisibility(VISIBLE);

    }

    /**
     * 隐藏播放控制界面
     */
    @Override
    void onHide() {
        mLayoutTop.setVisibility(View.GONE);
        mLayoutBottom.setVisibility(View.GONE);
        mIvLock.setVisibility(GONE);

    }

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

//    public void updateVideoProgress(long current, long duration) {
//        mTvCurrent.setText(TCTimeUtils.formattedTime(current));
//        if (duration > 0) {
//            float percentage = current / duration;
//            if (percentage >= 0 && percentage <= 1) {
//                int progress = Math.round(percentage * mSeekBarProgress.getMax());
//                mSeekBarProgress.setProgress(progress);
//                mTvDuration.setText(TCTimeUtils.formattedTime(current);
//            }
//        }
//    }

    /**
     * 进度定时器
     */
//    @Override
//    void onTimerTicker() {
//        switch (mPlayType) {
//            case SuperPlayerConst.PLAYTYPE_VOD:
//                updateVodVideoProgress();
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE:
//                mTvCurrent.setText(TCTimeUtils.formattedTime(mLivePlayTime));
//                break;
//            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
//                mTvCurrent.setText(TCTimeUtils.formattedTime(mLiveShiftTime));
//                break;
//        }
//    }
    private void initViews(Context context) {
        mHideLockViewRunnable = new HideLockViewRunnable(this);
        mLayoutInflater.inflate(R.layout.vod_controller_large, this);

        mLayoutTop = (RelativeLayout) findViewById(R.id.layout_top);
        mLayoutTop.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutBottom.setOnClickListener(this);
        mLayoutReplay = (LinearLayout) findViewById(R.id.layout_replay);

        mIvBack =  findViewById(R.id.iv_back);
        mIvLock =  findViewById(R.id.iv_lock);
        mIvPause = findViewById(R.id.iv_pause);
        mIvMore =  findViewById(R.id.iv_more);
        mTvCurrent = findViewById(R.id.tv_current);
        mTvDuration =  findViewById(R.id.tv_duration);

        mSeekBarProgress = (TCPointSeekBar) findViewById(R.id.seekbar_progress);
        mSeekBarProgress.setProgress(0);
        mSeekBarProgress.setOnPointClickListener(this);
        mSeekBarProgress.setOnSeekBarChangeListener(this);

        mLayoutReplay.setOnClickListener(this);
        mIvLock.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mIvPause.setOnClickListener(this);
        mIvMore.setOnClickListener(this);

        mGestureVolumeBrightnessProgressLayout = (TCVolumeBrightnessProgressLayout) findViewById(R.id.gesture_progress);
        mGestureVideoProgressLayout = (TCVideoProgressLayout) findViewById(R.id.video_progress_layout);

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_back) {
            mVodController.onBackPress(SuperPlayerConst.PLAYMODE_FULLSCREEN);

        } else if (i == R.id.iv_pause) {
            changePlayState();

        } else if (i == R.id.iv_more) {
            showMoreView();

        } else if (i == R.id.iv_lock) {
            changeLockState();

        } else if (i == R.id.layout_replay) {
            replay();
        }
    }

    /**
     * 改变锁屏状态
     */
    private void changeLockState() {
        mLockScreen = !mLockScreen;
        mIvLock.setVisibility(VISIBLE);
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideLockViewRunnable);
            this.getHandler().postDelayed(mHideLockViewRunnable, 3000);
        }
        if (mLockScreen) {
            mIvLock.setImageResource(R.drawable.ic_player_lock);
            hide();
            mIvLock.setVisibility(VISIBLE);
        } else {
            mIvLock.setImageResource(R.drawable.ic_player_unlock);
            show();
        }
    }




    /**
     * 显示右侧更多设置
     */
    private void showMoreView() {
        hide();
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            release();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }


    @Override
    public void onProgressChanged(TCPointSeekBar seekBar, int progress, boolean isFromUser) {
        super.onProgressChanged(seekBar, progress, isFromUser);
        // 加载点播缩略图
        if (isFromUser && mPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            setThumbnail(progress);
        }
    }

    @Override
    protected void onGestureVideoProgress(int progress) {
        super.onGestureVideoProgress(progress);
        setThumbnail(progress);
    }


    private void setThumbnail(int progress) {
        float percentage = ((float) progress) / mSeekBarProgress.getMax();
        float seekTime = (mVodController.getDuration() * percentage);
    }

    //
    @Override
    public void onSeekBarPointClick(final View view, final int pos) {
        if (mHideLockViewRunnable!=null) {
            this.getHandler().removeCallbacks(mHideViewRunnable);
            this.getHandler().postDelayed(mHideViewRunnable, 7000);
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

    /**
     * 更新直播播放时间和进度
     *
     * @param baseTime
     */
//    public void updateLivePlayTime(long baseTime) {
//        super.updateLivePlayTime(baseTime);
//        mTvCurrent.setText(TCTimeUtils.formattedTime(mLivePlayTime));
//    }

    /**
     * 更新直播回看播放时间
     *
     * @param liveshiftTime
     */
//    public void updateLiveShiftPlayTime(long liveshiftTime) {
//        super.updateLiveShiftPlayTime(liveshiftTime);
//        mTvCurrent.setText(TCTimeUtils.formattedTime(mLiveShiftTime));
//    }

    /**
     * 更新播放类型
     *
     * @param playType
     */
    public void updatePlayType(int playType) {
        super.updatePlayType(playType);
        switch (playType) {
            case SuperPlayerConst.PLAYTYPE_VOD:

                mTvDuration.setVisibility(View.VISIBLE);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:

                mTvDuration.setVisibility(View.GONE);

                mSeekBarProgress.setProgress(100);
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:

                mTvDuration.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    protected void onToggleControllerView() {
        super.onToggleControllerView();
        if (mLockScreen) {
            mIvLock.setVisibility(VISIBLE);
            if (mHideLockViewRunnable!=null) {
                this.getHandler().removeCallbacks(mHideLockViewRunnable);
                this.getHandler().postDelayed(mHideLockViewRunnable, 7000);
            }
        }
    }

    private static class HideLockViewRunnable implements Runnable{
        private WeakReference<TCVodControllerLarge> mWefControllerLarge;

        public HideLockViewRunnable(TCVodControllerLarge controller) {
            mWefControllerLarge = new WeakReference<>(controller);
        }
        @Override
        public void run() {
            if (mWefControllerLarge!=null && mWefControllerLarge.get()!=null) {
                mWefControllerLarge.get().mIvLock.setVisibility(GONE);
            }
        }
    }
}
