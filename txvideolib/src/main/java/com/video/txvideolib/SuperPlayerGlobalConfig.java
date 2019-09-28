package com.video.txvideolib;

import com.tencent.rtmp.TXLiveConstants;

/**
 * Created by yuejiaoli on 2018/7/4.
 * 超级播放器全局配置类
 */

public class SuperPlayerGlobalConfig {
    private static SuperPlayerGlobalConfig sInstance;

    private SuperPlayerGlobalConfig() {
    }

    public static SuperPlayerGlobalConfig getInstance() {
        if (sInstance == null) {
            sInstance = new SuperPlayerGlobalConfig();
        }
        return sInstance;
    }

    /**
     * 是否开启硬件加速 （ 默认开启硬件加速 ）这个参数可能会导致黑屏一下
     */
    public boolean enableHWAcceleration = true;
    /**
     * 默认播放填充模式 （ 默认播放模式为 自适应模式 ）
     */
    public int renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
    /**
     * 播放器最大缓存个数 （ 默认缓存 5 ）
     */
    public int maxCacheItem = 5;

}
