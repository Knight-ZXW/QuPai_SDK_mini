package com.duanqu.qupaicustomuidemo.upload;

import android.content.Context;
import android.util.Log;

import com.duanqu.qupai.bean.QupaiUploadTask;
import com.duanqu.qupai.upload.QupaiUploadListener;
import com.duanqu.qupai.upload.UploadService;
import com.duanqu.qupai.utils.Constant;

import java.io.File;
import java.util.UUID;

public class RecordUpload {
    private String TAG= "RecordUpload";
    private String videoUrl;

    private static RecordUpload instance;

    public static RecordUpload getInstance() {
        if (instance == null) {
            instance = new RecordUpload();
        }
        return instance;
    }

    /**
     * 开始上传
     */
    public void startUpload(Context context, String videoFile,String thum) {
        UploadService uploadService = UploadService.getInstance();
        uploadService.setQupaiUploadListener(new QupaiUploadListener() {
            @Override
            public void onUploadProgress(String uuid, long uploadedBytes, long totalBytes) {
                int percentsProgress = (int) (uploadedBytes * 100 / totalBytes);
                Log.e(TAG, "uuid:" + uuid + "data:onUploadProgress" + percentsProgress);
            }

            @Override
            public void onUploadError(String uuid, int errorCode, String message) {
                Log.e(TAG, "uuid:" + uuid + "onUploadError" + errorCode + message);
            }

            @Override
            public void onUploadComplte(String uuid, int responseCode, String responseMessage) {
                //http://{DOMAIN}/v/{UUID}.mp4?token={ACCESS-TOKEN}

                //这里返回的uuid是你创建上传任务时生成的uuid.开发者可以使用其他作为标识
                //videoUrl返回的是上传成功的video地址
                videoUrl = com.duanqu.qupaicustomuidemo.utils.Constant.domain + "/v/" + responseMessage + ".mp4" + "?token=" + com.duanqu.qupaicustomuidemo.utils.Constant.accessToken;
                Log.e("TAG", "data:onUploadComplte" +  com.duanqu.qupaicustomuidemo.utils.Constant.domain +"/v/"+ responseMessage + ".jpg" + "?token=" + com.duanqu.qupaicustomuidemo.utils.Constant.accessToken);
                Log.e("TAG", "data:onUploadComplte" +  com.duanqu.qupaicustomuidemo.utils.Constant.domain +"/v/"+ responseMessage + ".mp4" + "?token=" + com.duanqu.qupaicustomuidemo.utils.Constant.accessToken);
            }
        });
        String uuid = UUID.randomUUID().toString();
        startUpload(createUploadTask(context, uuid, new File(videoFile), new File(thum),
                com.duanqu.qupaicustomuidemo.utils.Constant.accessToken, Constant.space, com.duanqu.qupaicustomuidemo.utils.Constant.shareType, com.duanqu.qupaicustomuidemo.utils.Constant.tags, com.duanqu.qupaicustomuidemo.utils.Constant.description));
    }

    /**
     * 创建一个上传任务
     * @param context
     * @param uuid        随机生成的上传任务id
     * @param _VideoFile  完整视频文件
     * @param _Thumbnail  缩略图
     * @param accessToken 通过调用鉴权得到token
     * @param space        开发者生成的Quid，必须要和token保持一致
     * @param share       是否公开 0公开分享 1私有(default) 公开类视频不需要AccessToken授权
     * @param tags        标签 多个标签用 "," 分隔符
     * @param description 视频描述
     * @return
     */
    public QupaiUploadTask createUploadTask(Context context, String uuid, File _VideoFile, File _Thumbnail, String accessToken,
                                             String space, int share, String tags, String description) {
        UploadService uploadService = UploadService.getInstance();
        return uploadService.createTask(context, uuid, _VideoFile, _Thumbnail,
                accessToken, space, share, tags, description);
    }


    /**
     * 开始上传
     * @param data 上传任务的task
     */
    private void startUpload(QupaiUploadTask data) {
        try {
            UploadService uploadService = UploadService.getInstance();
            uploadService.startUpload(data);
        } catch (IllegalArgumentException exc) {
            Log.d("upload", "Missing some arguments. " + exc.getMessage());
        }
    }
}

