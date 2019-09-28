package com.video.txvideolib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.video.txvideolib.controller.TCVodControllerBase;
import com.video.txvideolib.controller.TCVodControllerLarge;
import com.video.txvideolib.controller.TCVodControllerSmall;

import java.lang.reflect.Constructor;

/**
 * 播放器控件,对播放器做的一层封装 ,核心是 TXVodPlayer,真正的播放内核
 */

public class VideoPlayerView extends RelativeLayout implements ITXVodPlayListener,
        ITXLivePlayListener {

    private Context mContext;

    private int mPlayMode = SuperPlayerConst.PLAYMODE_WINDOW;
    private boolean mLockScreen = false;

    private TXCloudVideoView mTXCloudVideoView;
    private TCVodControllerLarge mVodControllerLarge;
    private TCVodControllerSmall mVodControllerSmall;

    private ViewGroup.LayoutParams mLayoutParamWindowMode;
    private ViewGroup.LayoutParams mLayoutParamFullScreenMode;
    private LayoutParams mVodControllerLargeParams;
    // 点播播放器
    private TXVodPlayer mVodPlayer;

    private OnSuperPlayerViewCallback mPlayerViewCallback;
    private int mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
    private int mCurrentPlayType;
    private String mCurrentPlayVideoURL;

    public VideoPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;

        ViewGroup mRootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, false);
        mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.cloud_video_view);
        mVodControllerLarge = (TCVodControllerLarge) mRootView.findViewById(R.id.controller_large);
        mVodControllerSmall = (TCVodControllerSmall) mRootView.findViewById(R.id.controller_small);

        mVodControllerLargeParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mVodControllerLarge.setVodController(mVodController);
        mVodControllerSmall.setVodController(mVodController);


        removeAllViews();
        mRootView.removeView(mTXCloudVideoView);
        mRootView.removeView(mVodControllerSmall);
        mRootView.removeView(mVodControllerLarge);

        addView(mTXCloudVideoView);
        if (mPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
            addView(mVodControllerLarge);
            mVodControllerLarge.hide();
        } else if (mPlayMode == SuperPlayerConst.PLAYMODE_WINDOW) {
            addView(mVodControllerSmall);
            mVodControllerSmall.hide();
        }

        post(new Runnable() {
            @Override
            public void run() {
                if (mPlayMode == SuperPlayerConst.PLAYMODE_WINDOW) {
                    mLayoutParamWindowMode = getLayoutParams();
                }
                try {
                    // 依据上层Parent的LayoutParam类型来实例化一个新的fullscreen模式下的LayoutParam
                    Class parentLayoutParamClazz = getLayoutParams().getClass();
                    Constructor constructor = parentLayoutParamClazz.getDeclaredConstructor(int.class, int.class);
                    mLayoutParamFullScreenMode = (ViewGroup.LayoutParams) constructor.newInstance(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 初始化点播播放器
     *
     * @param context
     */
    private void initVodPlayer(Context context) {
        if (mVodPlayer != null)
            return;
        mVodPlayer = new TXVodPlayer(context);

        SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();

        TXVodPlayConfig mVodPlayConfig = new TXVodPlayConfig();
        mVodPlayConfig.setCacheFolderPath(Environment.getExternalStorageDirectory().getPath() + "/txcache");
        mVodPlayConfig.setMaxCacheItems(config.maxCacheItem);

        mVodPlayer.setConfig(mVodPlayConfig);
        mVodPlayer.setRenderMode(config.renderMode);
        mVodPlayer.setVodListener(this);
        mVodPlayer.enableHardwareDecode(config.enableHWAcceleration);
    }

    /**
     * 播放视频的总入口,供外部调用
     */
    public void playWithModel(String cover, String time, String playCount, String videoUrl) {
        SuperPlayerModel model = new SuperPlayerModel();
        model.coverUrl = cover;
        model.duration = time;
        model.url = videoUrl;
        playWithModel(model);
    }


    private void playWithModel(final SuperPlayerModel modelV3) {
        stopPlay();

        initVodPlayer(getContext());
        // 传统URL模式播放
        String videoURL = modelV3.url;

        if (TextUtils.isEmpty(videoURL)) {
            Toast.makeText(this.getContext(), "播放视频失败，播放连接为空", Toast.LENGTH_SHORT).show();
            return;
        }
        // 点播播放器：播放点播文件
        mVodPlayer.setPlayerView(mTXCloudVideoView);

        playVodURL(videoURL);


        mCurrentPlayType = SuperPlayerConst.PLAYTYPE_VOD;

        mVodControllerSmall.updatePlayType(SuperPlayerConst.PLAYTYPE_VOD);
        mVodControllerLarge.updatePlayType(SuperPlayerConst.PLAYTYPE_VOD);

        mVodControllerSmall.updateVideoProgress(0, 0);
        mVodControllerLarge.updateVideoProgress(0, 0);

    }

    /**
     * 封面由外部图片框架展示,内部只提供控件
     * @return
     */
    public ImageView getCover() {
        if (mVodControllerSmall != null) {
            return mVodControllerSmall.getVideoCoverImage();
        } else {
            return null;
        }
    }


    /**
     * 播放点播
     */
    private void playVodURL(String url) {
        mCurrentPlayVideoURL = url;

        if (mVodPlayer != null) {
            mVodPlayer.setAutoPlay(true);
            mVodPlayer.setVodListener(this);
            int ret = mVodPlayer.startPlay(url);
            if (ret == 0) {
                mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
            }
        }
    }


    public void resume() {
        if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            if (mVodPlayer != null) {
                mVodPlayer.resume();

            }
        }
    }


    public void pause() {
        if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
            if (mVodPlayer != null) {
                mVodPlayer.pause();
            }
        }
    }

    public void resetPlayer() {
        stopPlay();
    }

    private void stopPlay() {
        if (mVodPlayer != null) {
            mVodPlayer.setVodListener(null);
            mVodPlayer.stopPlay(false);
        }
        mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
    }

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    public void setPlayerViewCallback(OnSuperPlayerViewCallback callback) {
        mPlayerViewCallback = callback;
    }


    private boolean mChangeHWAcceleration;
    private int mSeekPos;

    private void fullScreen(boolean isFull) {//控制是否全屏显示
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            View decorView = activity.getWindow().getDecorView();
            if (isFull) {
                //隐藏虚拟按键，并且全屏
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

        }
    }

    /**
     * 播放器控制
     */
    private TCVodControllerBase.VodController mVodController = new TCVodControllerBase.VodController() {
        /**
         * 请求播放模式：窗口/全屏/悬浮窗
         * @param requestPlayMode
         */
        @Override
        public void onRequestPlayMode(int requestPlayMode) {
            if (mPlayMode == requestPlayMode)
                return;

            if (mLockScreen) //锁屏
                return;

            if (requestPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                fullScreen(true);
            } else {
                fullScreen(false);
            }

            mVodControllerSmall.hide();
            mVodControllerLarge.hide();
            //请求全屏模式
            if (requestPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {

                if (mLayoutParamFullScreenMode == null)
                    return;

                removeView(mVodControllerSmall);
                addView(mVodControllerLarge, mVodControllerLargeParams);
                setLayoutParams(mLayoutParamFullScreenMode);
                rotateScreenOrientation(SuperPlayerConst.ORIENTATION_LANDSCAPE);

                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.onStartFullScreenPlay();
                }
            }
            // 请求窗口模式
            else if (requestPlayMode == SuperPlayerConst.PLAYMODE_WINDOW && mPlayMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                // 当前是全屏模式
                removeView(mVodControllerLarge);
                addView(mVodControllerSmall);
                setLayoutParams(mLayoutParamWindowMode);
                rotateScreenOrientation(SuperPlayerConst.ORIENTATION_PORTRAIT);

                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback.onStopFullScreenPlay();
                }
            }

            mPlayMode = requestPlayMode;
        }

        /**
         * 返回
         * @param playMode
         */
        @Override
        public void onBackPress(int playMode) {
            // 当前是全屏模式，返回切换成窗口模式
            if (playMode == SuperPlayerConst.PLAYMODE_FULLSCREEN) {
                onRequestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
        }

        @Override
        public void resume() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.resume();
                }
            }
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PLAY;
            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerLarge.updateReplay(false);
            mVodControllerSmall.updateReplay(false);
        }

        @Override
        public void pause() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.pause();
                }
            }
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);
        }

        @Override
        public float getDuration() {
            return mVodPlayer.getDuration();
        }


        @Override
        public void seekTo(int position) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                if (mVodPlayer != null) {
                    mVodPlayer.seek(position);
                }
            } else {
                mCurrentPlayType = SuperPlayerConst.PLAYTYPE_LIVE_SHIFT;
                mVodControllerSmall.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
                mVodControllerLarge.updatePlayType(SuperPlayerConst.PLAYTYPE_LIVE_SHIFT);
            }

        }

        @Override
        public boolean isPlaying() {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                return mVodPlayer.isPlaying();
            } else {
                return mCurrentPlayState == SuperPlayerConst.PLAYSTATE_PLAY;
            }
        }


        /**
         * 是否启用硬件加速
         * @param isAccelerate
         */
        @Override
        public void onHWAcceleration(boolean isAccelerate) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_VOD) {
                mChangeHWAcceleration = true;
                if (mVodPlayer != null) {
                    mVodPlayer.enableHardwareDecode(isAccelerate);

                    mSeekPos = (int) mVodPlayer.getCurrentPlaybackTime();

                    stopPlay();
                    playVodURL(mCurrentPlayVideoURL);
                }
            }

        }


        /**
         * 重新播放
         */
        @Override
        public void onReplay() {
            if (!TextUtils.isEmpty(mCurrentPlayVideoURL)) {
                playVodURL(mCurrentPlayVideoURL);
            }

            if (mVodControllerLarge != null) {
                mVodControllerLarge.updateReplay(false);
            }
            if (mVodControllerSmall != null) {
                mVodControllerSmall.updateReplay(false);
            }
        }

    };

    /**
     * 旋转屏幕方向
     *
     * @param orientation
     */
    private void rotateScreenOrientation(int orientation) {
        switch (orientation) {
            case SuperPlayerConst.ORIENTATION_LANDSCAPE:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case SuperPlayerConst.ORIENTATION_PORTRAIT:
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    /**
     * 点播播放器回调
     *
     * @param player
     * @param event  事件id.id类型请参考 {@linkplain TXLiveConstants#PLAY_EVT_CONNECT_SUCC 播放事件列表}.
     * @param param
     */
    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) { //视频播放开始
            mVodControllerSmall.dismissBackground();

            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);


        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            if (mChangeHWAcceleration) { //切换软硬解码器后，重新seek位置

                mVodController.seekTo(mSeekPos);
                mChangeHWAcceleration = false;
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            mCurrentPlayState = SuperPlayerConst.PLAYSTATE_PAUSE;
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);

            mVodControllerSmall.updateReplay(true);
            mVodControllerLarge.updateReplay(true);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            mVodControllerSmall.updateVideoProgress(progress / 1000, duration / 1000);
            mVodControllerLarge.updateVideoProgress(progress / 1000, duration / 1000);
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {// 播放点播文件失败
            mVodPlayer.stopPlay(true);
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);

            Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
        if (event < 0
                && event != TXLiveConstants.PLAY_ERR_HLS_KEY
                && event != TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
            mVodPlayer.stopPlay(true);
            mVodControllerSmall.updatePlayState(false);
            mVodControllerLarge.updatePlayState(false);
            Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

    /**
     * 直播播放器回调
     *
     * @param event 事件id.id类型请参考 {@linkplain TXLiveConstants#PUSH_EVT_CONNECT_SUCC 播放事件列表}.
     * @param param
     */
    @Override
    public void onPlayEvent(int event, Bundle param) {

        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) { //视频播放开始
            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            mVodControllerSmall.updateLiveLoadingState(false);
            mVodControllerLarge.updateLiveLoadingState(false);

            mVodControllerSmall.updatePlayState(true);
            mVodControllerLarge.updatePlayState(true);

            mVodControllerSmall.updateReplay(false);
            mVodControllerLarge.updateReplay(false);
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            if (mCurrentPlayType == SuperPlayerConst.PLAYTYPE_LIVE_SHIFT) {  // 直播时移失败，返回直播

                Toast.makeText(mContext, "时移失败,返回直播", Toast.LENGTH_SHORT).show();
                mVodControllerSmall.updateReplay(false);
                mVodControllerLarge.updateReplay(false);
                mVodControllerSmall.updateLiveLoadingState(false);
                mVodControllerLarge.updateLiveLoadingState(false);
            } else {
                stopPlay();
                mVodControllerSmall.updatePlayState(false);
                mVodControllerLarge.updatePlayState(false);
                mVodControllerSmall.updateReplay(true);
                mVodControllerLarge.updateReplay(true);
                if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                    Toast.makeText(mContext, "网络不给力,点击重试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            mVodControllerSmall.updateLiveLoadingState(true);
            mVodControllerLarge.updateLiveLoadingState(true);

        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
        } else if (event == TXLiveConstants.PLAY_EVT_CHANGE_ROTATION) {
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_STREAM_SWITCH_SUCC) {
            Toast.makeText(mContext, "清晰度切换成功", Toast.LENGTH_SHORT).show();
            return;
        } else if (event == TXLiveConstants.PLAY_ERR_STREAM_SWITCH_FAIL) {
            Toast.makeText(mContext, "清晰度切换失败", Toast.LENGTH_SHORT).show();
            return;
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            mVodControllerSmall.updateVideoProgress(progress / 1000, duration / 1000);
            mVodControllerLarge.updateVideoProgress(progress / 1000, duration / 1000);
        }

    }

    @Override
    public void onNetStatus(Bundle status) {

    }

    public void requestPlayMode(int playMode) {
        if (playMode == SuperPlayerConst.PLAYMODE_WINDOW) {
            if (mVodController != null) {
                mVodController.onRequestPlayMode(SuperPlayerConst.PLAYMODE_WINDOW);
            }
        }
    }


    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getPlayMode() {
        return mPlayMode;
    }

    /**
     * 获取当前播放状态
     *
     * @return
     */
    public int getPlayState() {
        return mCurrentPlayState;
    }

    /**
     * SuperPlayerView的回调接口
     */
    public interface OnSuperPlayerViewCallback {

        /**
         * 开始全屏播放
         */
        void onStartFullScreenPlay();

        /**
         * 结束全屏播放
         */
        void onStopFullScreenPlay();

    }

    public void release() {
        if (mVodControllerSmall != null) {
            mVodControllerSmall.release();
        }
        if (mVodControllerLarge != null) {
            mVodControllerLarge.release();
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
}
