package com.duanqu.qupaicustomuidemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import com.duanqu.qupai.bean.PhotoModule;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.ThumbnailExportOptions;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.permission.AppSettingsDialog;
import com.duanqu.qupai.permission.EasyPermissions;
import com.duanqu.qupaicustomuidemo.Auth.AuthTest;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientImpl;
import com.duanqu.qupaicustomuidemo.photocompose.render.PhotoProgressActivity;
import com.duanqu.qupaicustomuidemo.trim.drafts.ImportActivity;
import com.duanqu.qupaicustomuidemo.utils.Constant;
import com.duanqu.qupaicustomuidemo.videocompose.VideoProgressActivity;
import com.duanqu.qupaisdk.tools.SingnatureUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        EasyPermissions.PermissionCallbacks {
    private String TAG =  "MainActivity";
    private final int REQUEST_CODE_GALLERY_PHOTO = 1001;
    private final int REQUEST_CODE_GALLERY_VIDEO = 1002;

    private final int RC_SETTINGS_SCREEN  = 1002;

    private static final int RC_CAMERA_PERM = 123;
    private static final int RC_EXTERNAL_STORAGE = 124;

    EditText width;
    EditText height;
    Button btn_auth;
    Button btn_record;
    Button btn_trim;
    Button btn_photo_fade;
    Button btn_video_compose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        width =(EditText) findViewById(R.id.edit_output_video_width);
        height = (EditText) findViewById(R.id.edit_output_video_height);

        btn_auth = (Button) findViewById(R.id.btn_auth);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_trim = (Button) findViewById(R.id.btn_trim);
        btn_photo_fade = (Button) findViewById(R.id.btn_photo_fade);
        btn_video_compose = (Button) findViewById(R.id.btn_video_compose);
        btn_auth.setOnClickListener(this);
        btn_record.setOnClickListener(this);
        btn_trim.setOnClickListener(this);
        btn_photo_fade.setOnClickListener(this);
        btn_video_compose.setOnClickListener(this);

    }

        @Override
    public void onClick(View v) {
            ((QupaiApplication)getApplication()).initVideoClientInfo(
                    width.getText().toString().equals("")? 0 :Integer.valueOf(width.getText().toString()),
                    height.getText().toString().equals("")? 0 :Integer.valueOf(height.getText().toString()));
        switch (v.getId()) {
            case R.id.btn_auth:
                Log.d("SIGN", SingnatureUtils.getSingInfo(v.getContext()));
                //鉴权，请先务必调用鉴权，并且鉴权成功.如果鉴权失败不要调用拍摄.
                AuthTest.getInstance().initAuth(v.getContext(), Constant.APP_KEY, Constant.APP_SECRET, Constant.SPACE);
                break;
            case R.id.btn_record:
                cameraAndAudioTask();
                break;
            case R.id.btn_trim:
                readExternalStorage();
                break;
            case R.id.btn_photo_fade:
                GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY_PHOTO, GalleryFinal.MediaType.MediaPhoto, GalleryFinal.getCoreConfig().getFunctionConfig(),
                        mOnHanlderResultCallback);
                break;
            case R.id.btn_video_compose:
                GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY_VIDEO, GalleryFinal.MediaType.MediaVideo, GalleryFinal.getCoreConfig().getFunctionConfig(),
                        mOnHanlderResultCallback);
                Log.e("QupaiComposeTaskImpl" , "start"+System.currentTimeMillis());
                break;
        }
    }


    public void cameraAndAudioTask() {
        String[] perms = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            new RecordActivity2.Request(new VideoSessionClientFactoryImpl(), null)
                    .startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permissions_tips_camera_audio),
                    RC_CAMERA_PERM, perms);
        }
    }

    public void readExternalStorage(){
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
            new ImportActivity.Request(new VideoSessionClientFactoryImpl(), null)
                    .startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permissions_tips_storage),
                    RC_EXTERNAL_STORAGE, perms);
        }
    }


    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null && reqeustCode == REQUEST_CODE_GALLERY_PHOTO) {
                RenderRequest request = new PhotoProgressActivity.Request(new VideoSessionClientFactoryImpl(), null)
                        .setPhotoList(addAllList(resultList))
                        .setRenderMode(RenderRequest.RENDER_MODE_EXPORT_THUMBNAIL_COMPOSE);

                request.startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_THUMBNAIL_COMPOSE);
            }else if(resultList != null && reqeustCode == REQUEST_CODE_GALLERY_VIDEO){
                RenderRequest request = new VideoProgressActivity.Request(new VideoSessionClientFactoryImpl(), null)
                        .setPhotoList(addAllList(resultList))
                        .setRenderMode(RenderRequest.RENDER_MODE_EXPORT_THUMBNAIL_COMPOSE);

                request.startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO_COMPOSE);
            }
            GalleryFinal.clearOnHanlderResultCallback();
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 使用趣拍SDK内部的PhotoModule 开发者建议直接使用PhotoModule
     *
     * @param resultList 选择的图片路径
     * @return
     */
    private List<PhotoModule> addAllList(List<PhotoInfo> resultList) {
        List<PhotoModule> photoModuleList = null;
        photoModuleList = new ArrayList<>();
        for (PhotoInfo photoInfo : resultList) {
            PhotoModule photoModule = new PhotoModule();
            photoModule.setPhotoId(photoInfo.getPhotoId());
            photoModule.setWidth(photoInfo.getWidth());
            photoModule.setHeight(photoInfo.getHeight());
            photoModule.setPhotoPath(photoInfo.getPhotoPath());
            photoModule.setVideoPath(photoInfo.getVideoPath());
            photoModuleList.add(photoModule);
        }
        return photoModuleList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        if(requestCode == RC_CAMERA_PERM){
            new RecordActivity2.Request(new VideoSessionClientFactoryImpl(), null)
                    .startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        } else if (requestCode == RC_EXTERNAL_STORAGE) {
            new ImportActivity.Request(new VideoSessionClientFactoryImpl(), null)
                    .startForResult(MainActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.qupai_message_camera_acquisition_failure))
                    .setTitle(null)
                    .setPositiveButton(getString(R.string.qupai_camera_permission))
                    .setNegativeButton(getString(R.string.qupai_cancel), null /* click listener */)
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }
    }

}
