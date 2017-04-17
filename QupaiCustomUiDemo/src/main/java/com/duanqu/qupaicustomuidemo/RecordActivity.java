package com.duanqu.qupaicustomuidemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.minisdk.RecorderCallback;
import com.duanqu.qupai.minisdk.RecorderInterface;
import com.duanqu.qupai.minisdk.view.DisplayRotationObserver;
import com.duanqu.qupai.minisdk.view.RecordView;
import com.duanqu.qupai.permission.AppSettingsDialog;
import com.duanqu.qupai.permission.EasyPermissions;
import com.duanqu.qupaicustomuidemo.editor.EditorActivity;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupaicustomuidemo.trim.drafts.ImportActivity;
import com.duanqu.qupaicustomuidemo.uicomponent.CountDownSwitch;
import com.duanqu.qupaicustomuidemo.uicomponent.CountDownTips;
import com.duanqu.qupaicustomuidemo.uicomponent.TimeProgress;
import com.duanqu.qupaicustomuidemo.uicomponent.TimelineTimeLayout;
import com.duanqu.qupaicustomuidemo.utils.MySystemParams;
import com.duanqu.qupaicustomuidemo.widget.RotateImageView;

import java.io.Serializable;
import java.util.List;


public class RecordActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private String projectpath;
    private RecorderInterface.ReturnCode ret;//每个操作的返回状态
    RecorderInterface.QupaiSwitch lightSwitch = RecorderInterface.QupaiSwitch.CLOSE; //记录闪光灯开关状态

    boolean isResume; //记录activity 状态

    //记录视频录制状态
    private enum State {
        START,
        RESUME,
        PAUSE,
        STOP
    }

    private RecordView mRecordView;
    private ViewMonitor mViewMonitor = new ViewMonitor();

    private ImageView mBackspace;
    private ImageView mIvDeleteClip;
    private ImageView mIvRecord;
    private ImageView mIvNextStep;
    private RotateImageView mSwitchLight;
    private RotateImageView mCameraSwitch;
    private RotateImageView mIvCountDown;
    private ImageView mChooseBgMusic;

    RotationObserver rotationObserver;
    private boolean isSupportSensor = false;
    private int defaultRotate = 0;
    private int recordRotate = 0;
    private boolean isPausing = false;
    private long startTime = 0l; //上次录制开始的时刻
    private boolean isDeleteActive = false;
    long recorderProgressTime; //录制回调时 当前progress 对应的系统时间
    long videoDuration = 0; //
    private State recordState = State.STOP;

    TimelineTimeLayout timeLayout;
    private CountDownSwitch countDownSwitch;

    //最大最小时长
    private long maxTimeLength = 8000;
    private long minTimeLength = 3000;

    Request _Request;

    int recordViewWidth, recordViewHeight;//录制组件尺寸
    int screenWidth, screenHeight;//屏幕的尺寸

    public static final class Request extends SessionPageRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

        public Request(SessionClientFactory factory, Serializable data) {
            super(factory, data);
        }

        private transient Uri _ProjectUri;

        public Request setProjectUri(Uri uri) {
            _ProjectUri = uri;
            return this;
        }

        @Override
        protected void marshall(Intent intent) {
            super.marshall(intent);

            intent.setData(_ProjectUri);
        }

        @Override
        protected void unmarshall(Intent intent) {
            super.unmarshall(intent);

            _ProjectUri = intent.getData();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        _Request = PageRequest.from(this);

        initArgs();
        initView();
    }

    private void initArgs() {

        projectpath = getExternalFilesDir(null) + "/project";
        minTimeLength = (long) _Request.getVideoSessionClient(this).getProjectOptions().durationMin;
        maxTimeLength = (long) _Request.getVideoSessionClient(this).getProjectOptions().durationMax;
        rotationObserver = new RotationObserver(this);
        mRecordView = (RecordView) findViewById(R.id.record_view);
        mRecordView.selectAudioEncoder("fdk_aac");//设置音频编码器参数
        mRecordView.setDefaultFoucsImage(R.animator.focus_area_focus_qupai_start, R.drawable.qupai_camera_focus_area);//设置对焦的图片和动画
        mRecordView.setVideoSize(_Request.getVideoSessionClient(this).getProjectOptions().videoWidth, _Request.getVideoSessionClient(this).getProjectOptions().videoHeight);//输出大小
        mRecordView.setGop(1);//gop 关键帧间隔，默认125
        mRecordView.setBps(Integer.parseInt(_Request.getVideoSessionClient(this).getCreateInfo().getMovieExportOptions().getVideoEncoderOptions().get("maxrate")));//bps
        mRecordView.setFormat("mov");//设置缓存类型
        mRecordView.setManualFocuse(RecorderInterface.QupaiSwitch.OPEN);//设置手动对焦
        mRecordView.switchZoom(RecorderInterface.QupaiSwitch.OPEN);//缩放
        mRecordView.setDefualtCamera(RecorderInterface.CameraId.FRONTCAMERA);//前置摄像头
        mRecordView.setTempPath(projectpath);//设置中间文件的路径
        mRecordView.setCallback(recorderCallback);//设置回调函数
        isSupportSensor = rotationObserver.start();


    }

    private void initView() {
        //取消录制
        mBackspace = (ImageView) findViewById(R.id.ImageView_backspace);
        mBackspace.setOnClickListener(mViewMonitor);

        //下一步按钮
        mIvNextStep = (ImageView) findViewById(R.id.imageView_nextBtn);
        mIvNextStep.setOnClickListener(mViewMonitor);
        mIvNextStep.setVisibility(View.GONE);

        //录制按钮
        mIvRecord = (ImageView) findViewById(R.id.imageView_capture);
        mIvRecord.setOnClickListener(mViewMonitor);

        //回删按钮
        mIvDeleteClip = (ImageView) findViewById(R.id.ImageView_clipCanceller);
        mIvDeleteClip.setOnClickListener(mViewMonitor);
        mIvDeleteClip.setEnabled(false);

        //闪光灯
        mSwitchLight = (RotateImageView) findViewById(R.id.switch_light);
        mSwitchLight.setOnClickListener(mViewMonitor);

        //摄像头翻转
        mCameraSwitch = (RotateImageView) findViewById(R.id.ImageButton_cameraSwitch);
        mCameraSwitch.setOnClickListener(mViewMonitor);


        //倒计时拍摄
        mIvCountDown = (RotateImageView) findViewById(R.id.ImageButton_countdownSwitch);
        CountDownTips countdown_tips = new CountDownTips(
                mIvRecord,
                (TextView) findViewById(R.id.TextView_countdownTips),
                findViewById(R.id.LinearLayout_countdownTips),
                mCameraSwitch,
                mIvDeleteClip,
                this,
                onCountDownRecordListener);

        countDownSwitch = new CountDownSwitch(mIvCountDown, countdown_tips);//给IvCountDown 设置点击事件

        // 选择背景音乐
        mChooseBgMusic = (ImageView) findViewById(R.id.imageView_record_choose_music);
        mChooseBgMusic.setOnClickListener(mViewMonitor);

        //时间轴
        timeLayout = (TimelineTimeLayout) findViewById(R.id.time_layout);
        timeLayout.setChild((TextView) findViewById(R.id.time_text), (TimeProgress) findViewById(R.id.time_progress));
        timeLayout.setTime(minTimeLength, maxTimeLength);

        MySystemParams systemparams = MySystemParams.getInstance();
        screenWidth = systemparams.screenWidth;
        screenHeight = systemparams.screenHeight;
        recordViewWidth = screenWidth;
        recordViewHeight = (int) (
                (double) _Request.getVideoSessionClient(this).getProjectOptions().videoHeight
                        / (double) _Request.getVideoSessionClient(this).getProjectOptions().videoWidth
                        * recordViewWidth);

        int topHeight = screenHeight / 9;
        FrameLayout recordLayout = (FrameLayout) findViewById(R.id.record_view_layout);
        ViewGroup.LayoutParams layoutParams = recordLayout.getLayoutParams();
        layoutParams.width = recordViewWidth;
        layoutParams.height = recordViewHeight;
        recordLayout.setLayoutParams(layoutParams);
        //
//        int top = 0, bottom = 0;
//        //为了保证 浏览和录制生成的视频一致，并且浏览过程中视频不可以变形。所以需要使recordview 的比例和视频比例一致。
//        //策略是：尽量底部出现黑边，如果黑边超过顶部大小，就使得top变成黑色。用户可以自行调整策略，使得view和视频比例一致即可。
//        int leave = screenHeight - recordViewHeight;
//        if (recordViewHeight > screenHeight)
//            top = bottom = leave / 2;
//        else {
//            if (leave > topHeight) {
//                top = topHeight;
//                bottom = leave - top;
//            } else {
//                top = 0;
//                bottom = leave;
//            }
//
//        }
//        recordLayout.setPadding(0, top, 0, bottom);

        //默认如果是前置摄像头隐藏闪光灯
        if (mRecordView.isFrontCamera()) {
            mSwitchLight.setVisibility(View.GONE);
        } else {
            mSwitchLight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        RecorderInterface.ReturnCode ret = mRecordView.onResume();
        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal())
            isResume = true;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (isResume) {
            mRecordView.onPause();
            if (mIvCountDown.isActivated()) {
                mIvCountDown.callOnClick();

            }
            if (!mCameraSwitch.isEnabled()) {
                mCameraSwitch.setEnabled(true);
            }

            isResume = false;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecordView.onDestroy();
    }


    class ViewMonitor implements View.OnClickListener {
//        @Override
//        public boolean onTouch(View v, MotionEvent motionEvent) {//拍照按钮的 on Touch
//            int action = motionEvent.getActionMasked();
//            Log.e("qupai", "action is " + action + "recordState:" + recordState);
//            if (isPausing || !isPartCompleted) return false;
//            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                //如果进度条是0，则是初始化的情况
//                mChooseBgMusic.setVisibility(View.GONE);
//                mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_pressed);
//                startTime = System.currentTimeMillis();
//                if (recordState == State.STOP) {
//                    startRecord();
////                        mCameraSwitch.setActivated(false);
//                    mIvCountDown.setActivated(false);
//                    mSwitchLight.setActivated(false);
//                    mCameraSwitch.setEnabled(false);
//                } else if (recordState == State.PAUSE) {
//                    resumeRecord();
////                        mCameraSwitch.setActivated(true);
//                    mSwitchLight.setActivated(true);
//                    mCameraSwitch.setEnabled(true);
//                } else if (recordState == State.RESUME) {
//                    mIvCountDown.setActivated(false);
//                    mCameraSwitch.setEnabled(false);
//                    pauseRecord();
//                }
//
//            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP && recordState != State.STOP && recordState != State.PAUSE) {
//                isPausing = true;
//                mCameraSwitch.setEnabled(true);
//                mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_normal);
//                if (System.currentTimeMillis() - startTime > 500)
//                    pauseRecord();
//                else {
//                    Message msg = new Message();
//                    msg.what = 0x1111;
//                    handler.sendMessageDelayed(msg, 500);
//                }
//            }
//            return true;
//        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.imageView_capture:
                    detailRecord();
                    break;
                case R.id.ImageView_clipCanceller:
                    //回删
                    detailDelete();
                    break;
                case R.id.imageView_nextBtn:
                    finishRecord();
                    break;
                case R.id.ImageView_backspace:
                    onBackPressed();
                    break;
                case R.id.switch_light:
                    detailSwitchLight();
                    break;
                case R.id.ImageButton_cameraSwitch:
                    detailChangeCamera();
                    break;
                case R.id.imageView_record_choose_music:
                    //todo jump to choose bg Music
                    break;
            }
        }
    }

    /**
     * 拍摄按钮
     */
    private void detailRecord() {
        if (isPausing || !isPartCompleted) return;
        //如果进度条是0，则是初始化的情况

//        mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_pressed);
        if (recordState == State.STOP) {//如果是停止状态则开始录制
            if (mIvCountDown.isActivated()){
                countDownSwitch.startCountDown();
                return;
            }
            startTime = System.currentTimeMillis();
            mChooseBgMusic.setVisibility(View.GONE);
            startRecord();
            mIvCountDown.setActivated(false);
            mSwitchLight.setActivated(false);
            mCameraSwitch.setEnabled(false);
            mIvRecord.setActivated(true);
        } else if (recordState == State.PAUSE) {//如果是暂停状态 则继续录制
            if (mIvCountDown.isActivated()){
                countDownSwitch.startCountDown();
                return;
            }
            resumeRecord();
//                        mCameraSwitch.setActivated(true);
            mSwitchLight.setActivated(true);
            mCameraSwitch.setEnabled(true);
            mIvRecord.setActivated(true);
        } else if (recordState == State.RESUME) {//如果是录制状态，则
            mIvCountDown.setActivated(false);
            mCameraSwitch.setEnabled(false);
            mIvRecord.setActivated(false);
            pauseRecord();
        } else {//如果是 start 则暂停
            isPausing = true;
            mCameraSwitch.setEnabled(true);
            mIvRecord.setActivated(false);
            pauseRecord();
        }
    }


    private void detailDelete() {
        if (isDeleteActive)//判断状态，确认删除
        {
            ret = mRecordView.deletePart(mRecordView.getPartCount());
            if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal()) {
                timeLayout.deleteLast();
                mIvDeleteClip.setActivated(false);
                isDeleteActive = false;
                videoDuration = mRecordView.getDuration();

                if (mRecordView.getPartCount() == 0) {
                    // 没有part ，可以再显示背景音乐了
                    mChooseBgMusic.setVisibility(View.VISIBLE);
                    mIvDeleteClip.setEnabled(false);
                    mIvNextStep.setVisibility(View.GONE);
//                    mTvGallery.setVisibility(View.VISIBLE);
                }
            }
        } else//active状态
        {
            timeLayout.deleteLastPrepare();
            isDeleteActive = true;
            mIvDeleteClip.setActivated(true);
            ret = RecorderInterface.ReturnCode.SUCCESS;
        }
    }


    RecorderCallback recorderCallback = new RecorderCallback() {

        @Override
        public void onProgress(long timestamp) {
            Log.i("qupai", "onProgress" + timestamp);
            recorderProgressTime = timestamp;
            timeLayout.update(recorderProgressTime);//更新timeLayout
            if (recorderProgressTime >= maxTimeLength) {
                finishRecord();
            } else if (recorderProgressTime >= minTimeLength) {//时长超过了最短的限制，则会显示下一步
                mIvNextStep.setVisibility(View.VISIBLE);
                mIvNextStep.setEnabled(true);
            } else if (recorderProgressTime > 0)// 在minTimeLength之下
            {
                mIvNextStep.setVisibility(View.GONE);
            } else if (recorderProgressTime == 0) {
                //如果没有可以删除的，则变灰
                mIvDeleteClip.setActivated(false);
            }
        }

        @Override
        public void OnCompletion() {
            recordState = State.STOP;
            timeLayout.update(0);
            videoDuration = 0;
            timeLayout.clear();
            if (isDeleteActive) {
                mIvDeleteClip.setActivated(false);
            }
            mIvDeleteClip.setVisibility(View.GONE);

            new EditorActivity.Request(new VideoSessionClientFactoryImpl(), null)
                    .setProjectUri(Uri.parse(projectpath + "/project.json"))
                    .startForResult(RecordActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        }

        @Override
        public void onError(RecorderInterface.ReturnCode error) {

        }

        @Override
        public void onPartCompletion(long duration) {
            videoDuration += duration;
            isPartCompleted = true;
            mIvDeleteClip.setVisibility(View.VISIBLE);
            mIvDeleteClip.setEnabled(true);
            recordState = State.PAUSE;
            timeLayout.update(videoDuration);
            timeLayout.setPause(videoDuration);//总时长是onprogress的回调
        }

        @Override
        public void poorCpu() {
            //cpu性能过差，生成视频需要等待，测试情况下，没有出现过该情况，可以忽视
        }
    };

    private void detailChangeCamera() {
        if (recordState == State.RESUME) //首先暂停视频，recordview内部也会自动暂停录制
            pauseRecord();
        if (lightSwitch == RecorderInterface.QupaiSwitch.OPEN) {
            detailSwitchLight();
        }

        ret = mRecordView.changeCamera();
        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal())//判断是否成功
        {
            if (mRecordView.isFrontCamera()) {
                mSwitchLight.setVisibility(View.GONE);
            } else {
                mSwitchLight.setVisibility(View.VISIBLE);
            }
        }
    }

    private RecorderInterface.ReturnCode detailSwitchLight() {
        //确认是打开还是关闭
        RecorderInterface.QupaiSwitch ord;
        if (lightSwitch == RecorderInterface.QupaiSwitch.CLOSE)
            ord = RecorderInterface.QupaiSwitch.OPEN;
        else
            ord = RecorderInterface.QupaiSwitch.CLOSE;
        ret = mRecordView.switchLight(ord);//执行
        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal())//判断是否成功
        {
            lightSwitch = ord;
            if (lightSwitch == RecorderInterface.QupaiSwitch.OPEN)//更改状态记录
                mSwitchLight.setActivated(true);
            else
                mSwitchLight.setActivated(false);
        }
        return ret;
    }

    //开始录制
    private RecorderInterface.ReturnCode startRecord() {
        mRecordView.setRotation(recordRotate);

        ret = mRecordView.startRecord();
        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal()) {
            mIvDeleteClip.setEnabled(false);
            mIvNextStep.setVisibility(View.GONE);
            recordState = State.START;
        } else
            mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_normal);

        showToast(ret.toString());
        mChooseBgMusic.setVisibility(View.GONE);
        return ret;
    }

    private boolean isPartCompleted = true;

    //暂停录制
    private RecorderInterface.ReturnCode pauseRecord() {
        ret = mRecordView.pauseRecord();
        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal()) {
            isPartCompleted = false;
        } else
            showToast(ret.toString());
        isPausing = false;
        return ret;
    }

    //恢复录制
    private RecorderInterface.ReturnCode resumeRecord() {
        mChooseBgMusic.setVisibility(View.GONE);

        ret = mRecordView.resumeRecord();

        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal()) {
            mIvDeleteClip.setEnabled(false);
            mIvDeleteClip.setActivated(false);
            isDeleteActive = false;
            recordState = State.RESUME;
        } else
            mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_normal);
        showToast(ret.toString());
        return ret;
    }

    private long stopTime = 0l;

    //调用生成视频，异步生成，会在视频生成后有OnCompletion 回调
    private RecorderInterface.ReturnCode finishRecord() {

        mIvDeleteClip.setActivated(false);
        isDeleteActive = false;
        mIvDeleteClip.setVisibility(View.GONE);
        mIvNextStep.setVisibility(View.GONE);
        stopTime = System.currentTimeMillis();
        ret = mRecordView.stopRecordToJson();

        if (ret.ordinal() <= RecorderInterface.ReturnCode.WARNING_UNKNOWN.ordinal()) {
            recordRotate = defaultRotate;
        } else {
            showToast(ret.toString());
        }
        mIvRecord.setImageResource(R.drawable.btn_qupai_camera_capture_normal);
        return ret;
    }

    Toast toast;

    public void showToast(String text) {

        if (toast == null) {
            toast = Toast.makeText(RecordActivity.this, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }


    /**
     * 旋转角度的Observer
     */
    private class RotationObserver extends DisplayRotationObserver {

        public RotationObserver(Context context) {
            super(context);
        }

        @Override
        protected void onRotationChange(int rotation) {
            Log.e("rotation", "rotate1 change " + rotation);
            if (isSupportSensor) {
                recordRotate = rotation;

                float rotateRotation =- mCameraSwitch.getRotation();
                mCameraSwitch.setRotation(-rotation);
                mIvCountDown.setRotation(-rotation);
                mSwitchLight.setRotation(-rotation);
            } else {
                recordRotate = defaultRotate;
            }
        }
    }

    CountDownTips.OnCountDownRecordListener onCountDownRecordListener = new CountDownTips.OnCountDownRecordListener() {

        @Override
        public void onRecordStart() {
            if (recordState == State.STOP)
                startRecord();
            else if (recordState == State.PAUSE)
                resumeRecord();
        }

        @Override
        public void onRecordStop() {
            pauseRecord();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private final int RC_SETTINGS_SCREEN = 1002;
    private static final int RC_EXTERNAL_STORAGE = 124;

//    public void readExternalStorage(){
//        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE };
//        if (EasyPermissions.hasPermissions(this, perms)) {
//            // Have permissions, do the thing!
//            new ImportActivity.Request(new VideoSessionClientFactoryImpl(), null)
//                    .startForResult(RecordActivity2.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
//        } else {
//            // Ask for both permissions
//            EasyPermissions.requestPermissions(this, "readExternalStorage",
//                    RC_EXTERNAL_STORAGE, perms);
//        }
//    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.qupai_message_storge_acquisition_failure))
                    .setTitle(null)
                    .setPositiveButton(getString(R.string.qupai_camera_permission))
                    .setNegativeButton(getString(R.string.qupai_cancel), null /* click listener */)
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == RC_EXTERNAL_STORAGE) {
            new ImportActivity.Request(new VideoSessionClientFactoryImpl(), null)
                    .startForResult(RecordActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
        }
    }
}
