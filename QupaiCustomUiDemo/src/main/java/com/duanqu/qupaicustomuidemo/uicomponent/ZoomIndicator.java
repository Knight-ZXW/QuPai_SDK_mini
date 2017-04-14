package com.duanqu.qupaicustomuidemo.uicomponent;

import android.view.View;
import android.widget.TextView;
import com.duanqu.qupai.android.camera.CameraClient;
import com.duanqu.qupaicustomuidemo.R;

public class ZoomIndicator implements Runnable{
    private final TextView mTextView;
    private final String mFormatText;

    public ZoomIndicator(TextView view) {

        mTextView = view;
        mFormatText = mTextView.getResources().getString(R.string.qupai_camera_zoom_indicator);
    }

    private float _CurrentZoom = 1.0f;

    public static final int HIDE_DELAY_MS = 1000;

    public void update(CameraClient client) {

        if (_CurrentZoom == client.getZoomRatio()) {
            return;
        }
        _CurrentZoom = client.getZoomRatio();
        mTextView.setText(String.format(mFormatText, client.getZoomRatio()));

        mTextView.setVisibility(View.VISIBLE);
        mTextView.removeCallbacks(this);
        mTextView.postDelayed(this, HIDE_DELAY_MS);
    }

    @Override
    public void run() {
        mTextView.setVisibility(View.GONE);
    }
}
