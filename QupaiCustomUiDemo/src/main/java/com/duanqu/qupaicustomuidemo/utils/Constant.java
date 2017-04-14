package com.duanqu.qupaicustomuidemo.utils;

import java.util.UUID;

public class Constant {
    /**
     * 水印本地路径，文件必须为rgba格式的PNG图片
     */
    public static  String WATER_MARK_PATH ="assets://Qupai/watermark/qupai-logo.png";

    public static final String APP_KEY = "2083bf8fe6ff0cc";
    public static final String APP_SECRET = "3cbb377e2ece4a508b75eb00549fb06b";
    public static String accessToken;//accessToken 通过调用授权接口得到
    public static final String SPACE = UUID.randomUUID().toString().replace("-",""); //存储目录 demo使用一个随机的32位值，建议使用uid(用户id)作为space,这样在后台看到的就是用户上传的视频
    public static int shareType = 0; //是否公开 0公开分享 1私有(default)
    public static String domain="customui.s.qupai.me";

    public static String tags = "tags";
    public static String description = "description";
}

