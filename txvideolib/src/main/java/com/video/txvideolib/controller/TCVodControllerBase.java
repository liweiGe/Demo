package com.video.txvideolib.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.video.txvideolib.R;
import com.video.txvideolib.SuperPlayerConst;
import com.video.txvideolib.utils.TCTimeUtils;
import com.video.txvideolib.utils.VideoGestureUtil;
import com.video.txvideolib.view.TCVideoProgressLayout;
import com.video.txvideolib.view.TCVolumeBrightnessProgressLayout;

import java.lang.ref.WeakReference;

/**
 * Created by liyuejiao on 2018/7/3.
 */

public abstract class TCVodControllerBase extends RelativeLayout implements SeekBar.OnSeekBarChangeListener,View.OnClickListener  {

    protected LayoutInflater mLayoutInflater;
    protected VodController mVodController;
    protected GestureDetector mGestureDetector;
    private boolean isShowing;
    protected boolean mLockScreen;

    protected int mPlayType;
    protected long mLivePushDuration;

    protected TextView mTvCurrent;
    protected TextView mTvDuration;
    protected SeekBar mSeekBarProgress;
    protected LinearLayout mLayoutReplay;
    protected ProgressBar mPbLiveLoading;

    protected VideoGestureUtil mVideoGestureUtil;
    protected TCVolumeBrightnessProgressLayout mGestureVolumeBrightnessProgressLayout;
    protected TCVideoProgressLayout mGestureVideoProgressLayout;

    protected HideViewControllerViewRunnable mHideViewRunnable;
    protected boolean mIsChangingSeekBarProgress; // 标记状态，避免SeekBar由于视频播放的update而跳动


    public TCVodControllerBase(Context context) {
        super(context);
        init();
    }

    public TCVodControllerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCVodControllerBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHideViewRunnable = new HideViewControllerViewRunnable(this);
        mLayoutInflater = LayoutInflater.from(getContext());
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mLockScreen) return false;
                changePlayState();
                show();
                if (mHideViewRunnable != null) {
                    TCVodControllerBase.this.getHandler().removeCallbacks(mHideViewRunnable);
                    TCVodControllerBase.this.getHandler().postDelayed(mHideViewRunnable, 7000);
                }
                return true;
            }


            //如果双击的话，则onSingleTapConfirmed不会执行
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                onToggleControllerView();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (mLockScreen) return false;
                if (downEvent == null || moveEvent == null) {
                    return false;
                }
                if (mVideoGestureUtil != null && mGestureVolumeBrightnessProgressLayout != null) {
                    mVideoGestureUtil.check(mGestureVolumeBrightnessProgressLayout.getHeight(), downEvent, moveEvent, distanceX, distanceY);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if (mLockScreen) return true;
                if (mVideoGestureUtil != null) {
                    mVideoGestureUtil.reset(TCVodControllerBase.this.getWidth(), mSeekBarProgress.getProgress());
                }
                return true;
            }

        });
        mGestureDetector.setIsLongpressEnabled(false);

        mVideoGestureUtil = new VideoGestureUtil(getContext());
        mVideoGestureUtil.setVideoGestureListener(new VideoGestureUtil.VideoGestureListener() {
            @Override
            public void onBrightnessGesture(float newBrightness) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) (newBrightness * 100));
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.ic_light_max);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onVolumeGesture(float volumeProgress) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout.setImageResource(R.drawable.ic_volume_max);
                    mGestureVolumeBrightnessProgressLayout.setProgress((int) volumeProgress);
                    mGestureVolumeBrightnessProgressLayout.show();
                }
            }

            @Override
            public void onSeekGesture(int progress) {
                mIsChangingSeekBarProgress = true;
                if (mGestureVideoProgressLayout != null) {

                    if (progress > mSeekBarProgress.getMax()) {
                        progress = mSeekBarProgress.getMax();
                    }
                    if (progress < 0) {
                        progress = 0;
                    }
                    mGestureVideoProgressLayout.setProgress(progress);
                    mGestureVideoProgressLayout.show();

                    float percentage = ((float) progress) / mSeekBarProgress.getMax();
                    float currentTime = (mVodController.getDuration() * percentage);
                    mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime) + " / " + TCTimeUtils.formattedTime((long) mVodController.getDuration()));

                    onGestureVideoProgress(progress);

                }
                if (mSeekBarProgress != null)
                    mSeekBarProgress.setProgress(progress);
            }
        });
    }


    public void updateVideoProgress(long current, long duration) {
        if (current < 0) {
            current = 0;
        }
        if (duration < 0) {
            duration = 0;
        }
        if (mTvCurrent != null) mTvCurrent.setText(TCTimeUtils.formattedTime(current));

        float percentage = duration > 0 ? ((float) current / (float) duration) : 1.0f;
        if (current == 0) {
            mLivePushDuration = 0;
            percentage = 0;
        }


        if (percentage >= 0 && percentage <= 1) {
            if (mSeekBarProgress != null) {
                int progress = Math.round(percentage * mSeekBarProgress.getMax());
                if (!mIsChangingSeekBarProgress)
                    mSeekBarProgress.setProgress(progress);
            }
            if (mTvDuration != null) mTvDuration.setText(TCTimeUtils.formattedTime(duration));
        }

    }

    public void setVodController(VodController vodController) {
        mVodController = vodController;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null)
            mGestureDetector.onTouchEvent(event);

        if (!mLockScreen) {
            if (event.getAction() == MotionEvent.ACTION_UP && mVideoGestureUtil != null && mVideoGestureUtil.isVideoProgressModel()) {
                int progress = mVideoGestureUtil.getVideoProgress();
                if (progress > mSeekBarProgress.getMax()) {
                    progress = mSeekBarProgress.getMax();
                }
                if (progress < 0) {
                    progress = 0;
                }
                mSeekBarProgress.setProgress(progress);


                int seekTime = 0;
                float percentage = progress * 1.0f / mSeekBarProgress.getMax();

                seekTime = (int) (percentage * mVodController.getDuration());

                mVodController.seekTo(seekTime);
                mIsChangingSeekBarProgress = false;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.getHandler().removeCallbacks(mHideViewRunnable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            this.getHandler().postDelayed(mHideViewRunnable, 7000);
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mGestureVideoProgressLayout != null && fromUser) {
            mGestureVideoProgressLayout.show();
            float percentage = ((float) progress) / seekBar.getMax();
            float currentTime = (mVodController.getDuration() * percentage);

            mGestureVideoProgressLayout.setTimeText(TCTimeUtils.formattedTime((long) currentTime) + " / " + TCTimeUtils.formattedTime((long) mVodController.getDuration()));
            mGestureVideoProgressLayout.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.getHandler().removeCallbacks(mHideViewRunnable);
    }

    /**
     * 拖动seekbar结束时,获取seekbar当前进度,进行seek操作,最后更新seekbar进度
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        int curProgress = seekBar.getProgress();
        int maxProgress = seekBar.getMax();

        switch (mPlayType) {
            case SuperPlayerConst.PLAYTYPE_VOD:
                if (curProgress >= 0 && curProgress <= maxProgress) {
                    // 关闭重播按钮
                    updateReplay(false);
                    float percentage = ((float) curProgress) / maxProgress;
                    int position = (int) (mVodController.getDuration() * percentage);
                    mVodController.seekTo(position);
                    mVodController.resume();
                }
                break;
            case SuperPlayerConst.PLAYTYPE_LIVE:
            case SuperPlayerConst.PLAYTYPE_LIVE_SHIFT:
                updateLiveLoadingState(true);
                int seekTime = (int) (mLivePushDuration * curProgress * 1.0f / maxProgress);

                mVodController.seekTo(seekTime);
                break;
        }
        this.getHandler().postDelayed(mHideViewRunnable, 7000);
    }

    public void updateReplay(boolean replay) {
        if (mLayoutReplay != null) {
            mLayoutReplay.setVisibility(replay ? View.VISIBLE : View.GONE);
        }
    }

    public void updateLiveLoadingState(boolean loading) {
        if (mPbLiveLoading != null) {
            mPbLiveLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 重新播放
     */
    protected void replay() {
        updateReplay(false);
        mVodController.onReplay();
    }


    /**
     * 切换播放状态
     */
    protected void changePlayState() {
        // 播放中
        if (mVodController.isPlaying()) {
            mVodController.pause();
            show();
        }
        // 未播放
        else if (!mVodController.isPlaying()) {
            updateReplay(false);
            mVodController.resume();
            show();
        }
    }

    protected void onToggleControllerView() {
        if (!mLockScreen) {
            if (isShowing) {
                hide();
            } else {
                show();
                if (mHideViewRunnable != null) {
                    this.getHandler().removeCallbacks(mHideViewRunnable);
                    this.getHandler().postDelayed(mHideViewRunnable, 7000);
                }
            }
        }
    }

    public void show() {
        isShowing = true;
        onShow();
    }

    public void hide() {
        isShowing = false;
        onHide();
    }

    public void release() {

    }


    abstract void onShow();

    abstract void onHide();

    public void updatePlayType(int playType) {
        mPlayType = playType;
    }


    protected void onGestureVideoProgress(int currentProgress) {

    }

    public interface VodController {

        void onRequestPlayMode(int requestPlayMode);

        void onBackPress(int playMode);

        void resume();

        void pause();

        float getDuration();

        void seekTo(int position);

        boolean isPlaying();

        void onHWAcceleration(boolean isAccelerate);

        void onReplay();
    }


    private static class HideViewControllerViewRunnable implements Runnable {
        public WeakReference<TCVodControllerBase> mWefControlBase;

        public HideViewControllerViewRunnable(TCVodControllerBase base) {
            mWefControlBase = new WeakReference<>(base);
        }

        @Override
        public void run() {
            if (mWefControlBase != null && mWefControlBase.get() != null) {
                mWefControlBase.get().hide();
            }
        }
    }
}
