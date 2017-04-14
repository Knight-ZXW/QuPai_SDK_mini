package com.duanqu.qupaicustomuidemo.uicomponent;

import android.view.View;
import com.duanqu.qupai.android.camera.CameraClient;


public class CameraSwitch implements View.OnClickListener{

    private CameraClient mCameraClient;

    public CameraSwitch(View view, CameraClient camera_client) {

        view.setOnClickListener(this);
        mCameraClient = camera_client;
    }

    @Override
    public void onClick(View view) {
        mCameraClient.nextCamera();
    }
}
