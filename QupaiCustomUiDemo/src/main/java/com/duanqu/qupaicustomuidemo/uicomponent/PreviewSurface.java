package com.duanqu.qupaicustomuidemo.uicomponent;

import android.hardware.Camera;
import android.view.*;
import com.duanqu.qupai.android.camera.*;
import com.duanqu.qupai.utils.MathUtil;

public class PreviewSurface implements SurfaceHolder.Callback,
        ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener,
        View.OnTouchListener,
        CameraClient.Callback,
        CaptureRequest.OnCaptureRequestResultListener {

    private SurfaceView  mSurfaceView;
    private CameraClient mCameraClient;

    public PreviewSurface(SurfaceView view, int w, int h, CameraClient camera_client) {

        view.setOnTouchListener(this);
        view.getHolder().setFixedSize(w, h);
        view.getHolder().addCallback(this);
        mSurfaceView = view;

        mDetector      = new GestureDetector(view.getContext(), this);
        mScaleDetector = new ScaleGestureDetector(view.getContext(), this);

        camera_client.setCallback(this);
        mCameraClient = camera_client;
    }

    public void setFocusAreaMediator(FocusAreaMediator auto_focus) {
        mFocusAreaMediator = auto_focus;
        mCameraClient.setAutoFocusCallback(mFocusAreaMediator);
    }

    private ZoomIndicator mZoomIndicator;
    public void setZoomIndicator(ZoomIndicator zoom_indicator) {
        mZoomIndicator = zoom_indicator;
    }

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;

    private FocusAreaMediator mFocusAreaMediator;

    private boolean mZoomEnabled  = false;
    private float   mMaxZoomLevel = 3;


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {

        float x = motionEvent.getX() / mSurfaceView.getWidth();
        float y = motionEvent.getY() / mSurfaceView.getHeight();

        if (!mCameraClient.autoFocus(x, y, null)) {
            return false;
        }

        if(mFocusAreaMediator != null) {
            mFocusAreaMediator.onAutoFocusStart(motionEvent.getX(), motionEvent.getY());
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (mZoomEnabled) {
            float zoom = mCameraClient.getZoomRatio() * scaleGestureDetector.getScaleFactor();
            mCameraClient.setZoom(MathUtil.clamp(zoom, 1, mMaxZoomLevel));
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mDetector.onTouchEvent(motionEvent);
        mScaleDetector.onTouchEvent(motionEvent);
        return true;
    }

    private CameraSurfaceController mCameraController;

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mCameraController = mCameraClient.addSurface(surfaceHolder);
        mCameraController.setDisplayMethod(CameraSurfaceController.CenterAlign| CameraSurfaceController.FullScreen | CameraSurfaceController.ScaleEnabled);
        mCameraController.setVisible(true);
        mCameraClient.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        mCameraController.setResolution(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        mCameraController.setVisible(false);
        mCameraController = null;

        mCameraClient.removeSurface(surfaceHolder);
        mCameraClient.stopPreview();
    }

    @Override
    public void onDeviceAttach(CameraClient client) {
        CameraCharacteristics chara = client.getCharacteristics();
        mZoomEnabled = chara.lensFacing == Camera.CameraInfo.CAMERA_FACING_BACK;

        /* config session request */
        mCameraClient.getSessionRequest().jpeg_quality = 100;
        mCameraClient.setPreviewSize(new Size(640,480));
        /*
         * Preview size should keep the same aspect.
         * We query folder9.16:9(1280x720) first, 4:3(640x480) second.
         * */
//        Size prev_size = getFixedPreviewSize(chara,640, 480);
//
//        mCameraClient.getSessionRequest().previewWidth   = prev_size.width;
//        mCameraClient.getSessionRequest().previewHeight  = prev_size.height;
//        if(Build.MODEL.equals("MI 3")) {
//            mCameraClient.getSessionRequest().videoStabilization = false;
//        }

    }

    @Override
    public void onSessionAttach(CameraClient client) {

        if (!mCameraClient.autoFocus(0.5f, 0.5f, null)) {
            return ;
        }

        if(mFocusAreaMediator != null) {
            mFocusAreaMediator.onAutoFocusStart(mSurfaceView.getWidth() / 2, mSurfaceView.getHeight() / 2);
        }
    }

    @Override
    public void onFrameBack(CameraClient client) {
//        for (CameraClient.Callback cb : _CallbackList) {
//            cb.onFrameBack(client);
//        }
    }

    @Override
    public void onCaptureUpdate(CameraClient client) {
    }

    @Override
    public void onSessionDetach(CameraClient client) {
        if(mFocusAreaMediator != null) {
            mFocusAreaMediator.cancel();
        }
    }

    @Override
    public void onDeviceDetach(CameraClient client) {
        mZoomEnabled = false;
    }

    @Override
    public void onCaptureResult(CaptureRequest request) {
        if(mZoomIndicator != null) {
            mZoomIndicator.update(mCameraClient);
        }
    }

    private Size getFixedPictureSize(CameraCharacteristics p, int width, int height) {

        int pict_width  = 0;
        int pict_height = 0;
        for(Size size : p.supportedPictureSizes) {
            if(size.width * height == width * size.height && size.width * size.height >= width * height) {
                if(pict_width == 0 || pict_height == 0 || pict_width * pict_height > size.width * size.height) {
                    pict_width = size.width;
                    pict_height = size.height;
                }
            }
        }

        if(pict_width != 0 && pict_height != 0) {
            return new Size(pict_width, pict_height);
        } else {
            return null;
        }
    }

    private Size getFixedPreviewSize(CameraCharacteristics p, int width, int height) {

        int preview_width  = width;
        int preview_height = height;
        int diff_best = width * height;
        for(Size size : p.previewSizeList) {
            int diff = Math.abs(width * height - size.width * size.height);
            if(size.width * height == width * size.height && diff < diff_best) {
                preview_width  = size.width;
                preview_height = size.height;
                diff_best = diff;
            }
        }

        if(diff_best != width * height) {
            return new Size(preview_width, preview_height);
        } else {
            return  null;
        }
    }
}
