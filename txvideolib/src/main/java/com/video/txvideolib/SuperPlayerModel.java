package com.video.txvideolib;


/**
 * Created by hans on 2019/3/25.
 * <p>
 * 使用点播SDK有以下三种方式：
 * 1. 使用腾讯云FileId播放模式，仅需填写appid以及fileId即可简单进行播放。（更多高级用法， SuperPlayerVideoId} 以及腾讯云官网文档
 * <p>
 * 2. 使用传统URL模式播放，仅需填写URL即可进行播放。
 * <p>
 * 3. 多码率视频播放模式。
 */
public class SuperPlayerModel {

    /**
     * ------------------------------------------------------------------
     * 播放方式2： 直接使用URL播放  支持直播:RTMP、FLV封装格式  点播：MP4、Dash等常见封装格式 使用腾讯云直播时移功能则需要填写appId
     * ------------------------------------------------------------------
     */
    public String title;//视频标题
    public String url;   // 视频URL
    public String duration; //视频时长
    public String coverUrl;  //封面图

}
