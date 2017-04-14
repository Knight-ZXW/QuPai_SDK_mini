package com.duanqu.qupaicustomuidemo.editor.api;

import android.content.Context;

import com.duanqu.qupaicustomuidemo.BuildConfig;
import com.duanqu.qupaicustomuidemo.R;

/**
 * 作者：administrator on 2016/9/26 16:19
 */
public class Api {
    /** MV资源接口 */
    public static final String MV_RESOURCE_CATEGORY = "/api/res/type/3"; //1: 字体 2: 动图 3:imv 4:滤镜 5:音乐 6:字幕
    /** FONT资源接口 */
    public static final String FONT_RESOURCE_CATEGORY = "/api/res/type/1";
    /** CAPTION资源接口 */
    public static final String CAPTION_RESOURCE_CATEGORY = "/api/res/type/6";
    /** DIY资源接口 */
    public static final String DIY_RESOURCE_CATEGORY = "/api/res/type/2";
    /** 获取单个字体资源 */
    public static final String FONT_SIGNAL_RESOURCE_CATEGORY = "/api/res/get/1";

    private static Api instance;

    public static Api getInstance() {
        if(instance == null) {
            instance = new Api();
        }

        return instance;
    }

    public String getApiUrl(String category) {
        String baseUrl = BuildConfig.BASE_API_URL;

        return baseUrl + category;
    }

}
