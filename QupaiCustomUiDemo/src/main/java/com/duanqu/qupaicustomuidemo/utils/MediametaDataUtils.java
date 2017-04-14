package com.duanqu.qupaicustomuidemo.utils;

import android.media.MediaMetadataRetriever;
import android.widget.Toast;

import com.duanqu.transcode.bean.VideoBean;

import java.io.File;
import java.util.HashMap;

/**
 * Created by yan on 2016/12/8.
 */

public class MediametaDataUtils {

    public static VideoBean getVideoMetedata(File mUri) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            if (mUri == null) {
                return null;
            }
            VideoBean videoBean = new VideoBean();
            mmr.setDataSource(mUri.getPath());

            videoBean.setWidth(Integer.parseInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));//宽
            videoBean.setHeight(Integer.parseInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));//高
            videoBean.setDuration(Long.parseLong(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)));
            videoBean.setRotation(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)));
            return videoBean;
        } catch (Exception ex) {
            return null;
        } finally {
            mmr.release();
        }
    }

}
