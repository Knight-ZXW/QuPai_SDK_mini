package com.duanqu.qupaicustomuidemo.uicomponent;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.duanqu.qupai.android.camera.CameraClient;
import com.duanqu.qupai.camera.PreviewSource9;
import com.duanqu.qupai.camera.PreviewSource9CameraLink;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.media.AudioCapture;
import com.duanqu.qupai.media.Recorder9;
import com.duanqu.qupai.project.Clip;
import com.duanqu.qupai.recorder.ClipManager;
import com.duanqu.qupai.recorder.DisplayRotationObserver;
import com.duanqu.qupai.recorder.RecorderTask;

public
class RecordSession{

//    private final View _View;
    private static final String ASPECT_RATIO    = "ASPECT_RATIO";
    private boolean mIsRecording = false;
    private Recorder9 mRecorder = new Recorder9();
    private Clip _CurrentData;
    private ClipManager mClipManager;
    private CameraClient mCameraClient;
    private final AudioCapture _AudioSource = new AudioCapture(); //声音源

    private final PreviewSource9CameraLink _PreviewQueueLinkl;
    private Recorder9.OnFeedbackListener mFeedbackListener;
    private final RotationObserver _RotationObserver;
    private SharedPreferences _Sharedprefer;
    private VideoSessionCreateInfo videoInfo;
    private boolean isBeautyOn;

    public void setBeautyOn(boolean on) {
        isBeautyOn = on;
    }

    public boolean getBeautyOn() {
        return isBeautyOn;
    }

    private final int _IFrameInterval;
    private final int _VideoWidth;

    public int getVideoWidth() { return _VideoWidth; }

    private final int _VideoHeight;

    public int getVideoHeight() { return _VideoHeight; }

    private final int _PhotoWidth;

    public int getPhotoWidth(){
        return _PhotoWidth;
    }

    private final int _PhotoHeight;

    public int getPhotoHeight(){
        return _PhotoHeight;
    }

    public void setBeautyProgress(int progress) {
        _Sharedprefer.edit().putInt(ASPECT_RATIO, progress).commit();
    }

    public int getBeautyProgress() {
        return _Sharedprefer.getInt(ASPECT_RATIO, videoInfo.getBeautyProgress());
    }

    public RecordSession(Context context , ClipManager clip_manager, CameraClient camera_client, ProjectOptions options ,VideoSessionCreateInfo info) {
        mClipManager  = clip_manager;
        mCameraClient = camera_client;
        videoInfo = info;
        setBeautyOn(videoInfo.getBeautySkinOn());

        _VideoWidth = options.videoWidth;
        _VideoHeight=options.videoHeight;
        _PhotoWidth = options.photoWidth;
        _PhotoHeight = options.photoHeight;
        _IFrameInterval = options.iFrameInterval;

        _PreviewQueueLinkl = new PreviewSource9CameraLink(mCameraClient);
        mCameraClient.addOutput(_PreviewQueueLinkl);
        mCameraClient.setContentSize(_VideoWidth, _VideoHeight);

        _Sharedprefer  = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        _RotationObserver = new RotationObserver(context);
        mRecorder.setFeedbackListener(_FeedbackListener);
    }

    public void onResume() {
        _AudioSource.create();
        _RotationObserver.start();
        mRecorder.setVideoRotation(_RotationObserver.getRotation());
    }

    public void onPause() {

        requestStop();
        _AudioSource.destroy();
        mRecorder.join();
        _RotationObserver.stop();
    }

    public void onDestroy() {
        _AudioSource.release();
        mRecorder.onDestroy();
    }

    /**
     * 开始录制
     * @return
     */
    public boolean requestStart() {
        if(mIsRecording) {
            return false;
        }

        mClipManager.setLastClipSelected(false);
        if (!mClipManager.isReady()) {
            return false;
        }

        mRecorder.enableMediaCodec(false);
        PreviewSource9 video_source = _PreviewQueueLinkl.getSource();
        if (video_source == null || !video_source.isReady()) {
            return false;
        }
        mRecorder.setVideoSource(video_source);

        if(mClipManager.getClipCount()==0){
            mRecorder.setFirstRotationBool(true);
        }
        if (mClipManager.getRemainingDuration() <= 0) {
            return false;
        }

        String output_path = mClipManager.newFilename(".mov");

        if (output_path == null) {
            return false;
        }

        mRecorder.setOutputPath(output_path, "mov");
        mRecorder.setAudioSource(_AudioSource);
        mRecorder.setDurationLimit(mClipManager.getRemainingDuration());

        _CurrentData = mRecorder.startRecord();
        if (_CurrentData == null) {
            return false;
        }
        mClipManager.onRecordStart(_CurrentData);

        Log.w(Recorder9.TAG, "requestRecorderStart OK");

        mIsRecording = true;
        return true;
    }

    /**
     * 停止录制
     */
    public void requestStop() {
        if(!mIsRecording) {
            return;
        }

        mRecorder.stopRecord();
        _CurrentData.setDurationMilli(mRecorder.getClipDuration());
        mClipManager.onRecordStop(_CurrentData);
        mRecorder.setVideoSource(null);
        mRecorder.setRecorder(null);
        mRecorder.setAudioSource(null);
        mIsRecording = false;
    }

    public void setCameraContentSize(int width,int height){
        mCameraClient.setContentSize(width,height);
    }


    public void setFeedbackListener(Recorder9.OnFeedbackListener listener) {
        this.mFeedbackListener = listener;
    }

    private Recorder9.OnFeedbackListener _FeedbackListener = new Recorder9.OnFeedbackListener() {
        @Override
        public void onLimitReached(Recorder9 rec, long timestamp) {
            if(mFeedbackListener != null) {
                mFeedbackListener.onLimitReached(rec, timestamp);
            }
        }

        @Override
        public void onProgress(Recorder9 rec, long timestamp) {
            mClipManager.onRecordProgress(timestamp);
            if(mFeedbackListener != null) {
                mFeedbackListener.onProgress(rec, timestamp);
            }
        }

        @Override
        public void OnCompletion(Recorder9 rec) {
            if(mFeedbackListener != null) {
                mFeedbackListener.OnCompletion(rec);
            }
        }

        @Override
        public void OnRecorderTaskCompletion(Recorder9 rec, RecorderTask task) {
            String file = task.getVideoFile();
            for (int i = mClipManager.getClipCount() - 1; i >= 0; --i) {
                if (mClipManager.getClip(i).getPath().equals(file)) {
                    mClipManager.getClip(i).setState(Clip.State.COMPLETED);
                    break;
                }
            }
            if(mFeedbackListener != null) {
                mFeedbackListener.OnRecorderTaskCompletion(rec, task);
            }
        }

        @Override
        public void onError(Recorder9 rec, Throwable tr) {
            if(mFeedbackListener != null) {
                mFeedbackListener.onError(rec, tr);
            }
        }
    };

    private class RotationObserver extends DisplayRotationObserver {

        public RotationObserver(Context context) {
            super(context);
        }

        @Override
        protected void onRotationChange(int rotation) {
            mRecorder.setVideoRotation(rotation);
        }

    }
}
