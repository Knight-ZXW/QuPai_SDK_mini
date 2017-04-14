package com.duanqu.qupaicustomuidemo.trim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.media.FrameExtractor10;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.view.*;
import com.duanqu.qupai.view.HorizontalListView.OnDownCallBack;
import com.duanqu.qupai.view.HorizontalListView.OnScrollCallBack;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.VideoScaleHelper;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;

public class TrimActivity extends Activity implements TextureView.SurfaceTextureListener,
        VideoSliceSeekBar.SeekBarChangeListener, OnCompletionListener,
        SizeChangedNotifier.Listener, OnVideoSizeChangedListener,
        OnScrollCallBack, VideoTrimFrameLayout.OnVideoScrollCallBack,
        OnDownCallBack, OnPreparedListener, OnSeekCompleteListener {

    public static class Request extends SessionPageRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }
        public Request(SessionClientFactory factory, Serializable data) {
            super(factory, data);
        }

        private String _Path;

        String getPath() {
            return _Path;
        }

        public Request setPath(String path) {
            _Path = path;
            return this;
        }

        private int _Duration;

        int getDuration() {
            return _Duration;
        }

        public Request setDuration(int duration) {
            _Duration = duration;
            return this;
        }

    }


    private static final String TAG = "VideoTrim";

    private static final int VIDEO_PLAY_POSITION = 1002;
    private HorizontalListView listView;
    public static final int REQUEST_CODE_RENDER_THUMBNAIL = 2;

    private VideoTrimFrameLayout frame;
    private TextureView textureview;
    private Surface mSurface;
    private View _PausePlayButton;
    private View videoScaleToggle;
    private MediaPlayer mPlayer;
    private ImageView nextBtn;

    private VideoTrimAdapter adapter;

    private FrameExtractor10 kFrame;
    private VideoSliceSeekBar mSeekBar;
    private int playTime = 0;//播放的时间，真实时间
    private int curLeftPro;//关键截取的左边的时间--毫秒
    private int curRightPro;//关键截取的右边的时间--毫秒
    private int curPosition;
    private int playLeftPoi;
    private boolean isTouchByUser;
    private boolean userPause;
    private int scrolledLeftInt;

    private int sceenWidth;
    private int sceenHeight;
    private int videoWidth;
    private int videoHeight;
    private int outVideoWidth;
    private int outVideoHeight;
    private TextView startTimeTxt;//开始时间
    private TextView realTimeTxt;//真实需要截取的时间
    private TextView totalTimeTxt;//总时长

    //private float realTimeSet;
    private long Listoffset;//滑动list的位置，一个位置一秒
    private int scrolledOffset;
    //private int seekOffset;
    private int startPosition;
    private int endPosition;
    private int secTime;//设置的最大时长
    private int realTime;
    private int curListCount;

    private float videoScale;
    private int mScrollX = 0, mScrollY = 0;

    private boolean isPressDown = false;
    private boolean isFirstIn = true;
    private boolean isScaleSquare;

    private View abView;

    private int duration;//传递过来的时长。真实的时长
    private double durationLimit = 15;//设置的最大时长，该时长目前是写死的，建议传递或者使用ProjectOption参数
    private int durationValue;//设置的最大时长

    private String path;
    private Request _Request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _Request = PageRequest.from(this);
        FontUtil.applyFontByContentView(this, R.layout.activity_qupai_video_trim);

        WindowManager wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        sceenWidth = wm.getDefaultDisplay().getWidth();
        sceenHeight = wm.getDefaultDisplay().getHeight();

        abView = findViewById(R.id.action_bar);

        isTouchByUser = false;

        if (mHandler != null) {
            mHandler.removeMessages(VIDEO_PLAY_POSITION);
        }
        outVideoWidth = _Request.getVideoSessionClient(this).getProjectOptions().videoWidth;
        outVideoHeight = _Request.getVideoSessionClient(this).getProjectOptions().videoHeight;
        durationLimit = _Request.getVideoSessionClient(this).getProjectOptions().durationMax;
        durationValue = (int) durationLimit;
        path = _Request.getPath();
        duration = _Request.getDuration();

        init();
        initSurface();
        initData();

        kFrame = new FrameExtractor10();
        kFrame.setDataSource(path);

        listView = (HorizontalListView) findViewById(R.id.video_tailor_image_list);
        listView.setOnScrollCallBack(this);
        listView.setOnDownCallBack(this);
        mSeekBar.setHorizontalListView(listView);

        if (!android.os.Build.MODEL.equals("MT887")) {
            adapter = new VideoTrimAdapter(this, duration, durationValue, kFrame, mSeekBar);
            listView.setAdapter(adapter);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        ImmersiveSupport.attachBaseContext(this, newBase);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ImmersiveSupport.onWindowFocusChanged(this, hasFocus);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

//    protected Request getPageRequest(){
//        return _Request;
//    }

    public void init() {
        ImageButton cancelBtn = (ImageButton) abView.findViewById(R.id.draft_closeBtn);
        cancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView title = (TextView) abView.findViewById(R.id.draft_title);
        title.setText(R.string.qupai_video_trim);

        nextBtn = (ImageView) abView.findViewById(R.id.draft_nextBtn);
        nextBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                nextBtn.setEnabled(false);

                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        _PausePlayButton.setVisibility(View.VISIBLE);
                        if (videoHeight != videoWidth) {
//                            videoScaleToggle.setVisibility(View.VISIBLE);
                        }
                        mPlayer.pause();
                    }
                    isTouchByUser = true;
                    userPause = false;
                    mSeekBar.removeVideoStatusThumb();
                }

                long videoFrom = (curLeftPro + scrolledOffset) * 1000;
                long videoTo = (curRightPro + scrolledOffset) * 1000;

                LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
                int displayWidth = lp.width;
                int displayHeight = lp.height;
                int left = 0, right = 0, top = 0, bottom = 0;
                if (displayWidth > sceenWidth || displayHeight > sceenHeight) {
                    float sx = (float) videoWidth / (float) displayWidth;
                    float sy = (float) videoHeight / (float) displayHeight;
                    Matrix transform = new Matrix();
                    transform.postScale(sx, sy);

                    if (displayWidth > sceenWidth) {
                        int scaleDis = (displayWidth - sceenWidth) / 2;
                        left = scaleDis + mScrollX;
                        right = sceenWidth + left;
                    } else {
                        right = displayWidth;
                    }
                    if (displayHeight > sceenHeight) {
                        int scaleDis = (displayHeight - sceenHeight) / 2;
                        top = scaleDis + mScrollY;
                        bottom = top + sceenHeight;
                    } else {
                        bottom = displayHeight;
                    }
                    RectF r = new RectF(left, top, right, bottom);
                    transform.mapRect(r);
                    left = (int) r.left;
                    top = (int) r.top;
                    right = (int) r.right;
                    bottom = (int) r.bottom;
                } else {
                    right = videoWidth;
                    bottom = videoHeight;
                }

                new ImportProgressDialogFragment.Builder()
                        .setInputPath(path)
                        .setTrim(videoFrom, videoTo)
                        .setContentRect(left, top, _Request.getVideoSessionClient(TrimActivity.this).getProjectOptions().videoWidth,
                                _Request.getVideoSessionClient(TrimActivity.this).getProjectOptions().videoHeight)
                        .get()
                        .show(getFragmentManager(), null);

            }
        });

        videoScaleToggle = findViewById(R.id.btn_scale);
        videoScaleToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoScaleHelper helper = new VideoScaleHelper()
                        .setScreenWidthAndHeight(sceenWidth, sceenHeight)
                        .setVideoWidthAndHeight(videoWidth, videoHeight);
                LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
                if (isScaleSquare) {
                    helper.generateDisplayLayoutParams(lp);
                    isScaleSquare = false;
                } else {
                    helper.generateSquareVideoLayout(lp);
                    isScaleSquare = true;
                }
                v.setActivated(isScaleSquare);
                lp.setMargins(0, 0, 0, 0);
                mScrollY = 0;
                mScrollX = 0;
                textureview.setLayoutParams(lp);
            }
        });

        mSeekBar = (VideoSliceSeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setSeekBarChangeListener(this);
        mSeekBar.setProgressMinDiff(Math.round(2 * 100 / (float) durationLimit));

        realTimeTxt = (TextView) findViewById(R.id.video_tailor_time_real);
        startTimeTxt = (TextView) findViewById(R.id.video_tailor_time_start);
        totalTimeTxt = (TextView) findViewById(R.id.video_tailor_time_total);

        if (!android.os.Build.MODEL.equals("MT887")) {
            //真实的时长大于定义的时长，将总时长和realTime,playTime置为定义的最大时长
            if (duration > durationValue * 1000) {
                realTimeTxt.setText(String.valueOf(durationLimit));
                secTime = durationValue;
                playTime = durationValue * 1000;

                curLeftPro = 0;
                curRightPro = secTime * 1000;
            } else {
                int second = Math.round(duration / 100f);
                float realTime = second / 10.0f;
                realTimeTxt.setText(String.valueOf(realTime));
                playTime = duration;

                curLeftPro = 0;
                curRightPro = (int) (realTime * 1000);
            }

            int endTime = Math.round(playTime / 100f);
            float rightTime = endTime / 10.0f;
            endTime = endTime / 10;
            int point = (int) (rightTime * 10 % 10);
            if (point > 4 && rightTime < durationValue) {
                endTime += 1;
            }

            startTimeTxt.setText("00:00");
            String totalTimeStr = "00:0" + endTime;
            totalTimeTxt.setText(String.valueOf(totalTimeStr));

            Log.e("totle", "totleTime111 = " + totalTimeStr);
        }
    }

    public void initSurface() {
        frame = (VideoTrimFrameLayout) findViewById(R.id.video_surfaceLayout);
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);

        textureview = (TextureView) findViewById(R.id.video_textureview);
        _PausePlayButton = findViewById(R.id.btn_playback);
        _PausePlayButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlayer != null && !mPlayer.isPlaying()) {
                    setMediaPlayerParam();
                    mPlayer.start();
                } else {
                    if (mPlayer == null) {
                        mPlayer = new MediaPlayer();
                    }
                    playVideo();
                    setMediaPlayerParam();
                }
            }
        });

        textureview.setSurfaceTextureListener(this);
        textureview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlayer != null && mPlayer.isPlaying()) {
                    isTouchByUser = true;
                    userPause = true;
                    _PausePlayButton.setVisibility(View.VISIBLE);
                    if (videoHeight != videoWidth) {
//                        videoScaleToggle.setVisibility(View.VISIBLE);
                    }
                    mPlayer.pause();
                }else {
                    setMediaPlayerParam();
                    mPlayer.start();
                }
            }
        });
    }

    private void initData() {
        curPosition = 0;
        Listoffset = 0;
    }

    private void setMediaPlayerParam() {
        isTouchByUser = false;
        userPause = false;
        mHandler.sendEmptyMessage(VIDEO_PLAY_POSITION);
        _PausePlayButton.setVisibility(View.GONE);
        videoScaleToggle.setVisibility(View.GONE);

        if (playFromStart) {
            mPlayer.seekTo((int) (curLeftPro + Listoffset * 1000));
            playFromStart = false;
        }
    }

    private void playVideo() {
        try {
            if (_PausePlayButton.getVisibility() == View.VISIBLE) {
                _PausePlayButton.setVisibility(View.GONE);
                videoScaleToggle.setVisibility(View.GONE);
            }

            File file = new File(path);
            mPlayer.setSurface(null);
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setSurface(mSurface);
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnSeekCompleteListener(this);
            mPlayer.setOnVideoSizeChangedListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setLooping(false);
            mPlayer.prepareAsync();


            if (android.os.Build.MODEL.equals("MT887")) {
                setVideoTime();
            }

//            mPlayer.start();
//            mPlayer.seekTo(0);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this, R.string.qupai_trim_video_failed, Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    }

    private void setVideoTime() {
        duration = mPlayer.getDuration();

        if (duration > durationValue * 1000) {
            realTimeTxt.setText(String.valueOf(durationLimit));
            secTime = durationValue;
            playTime = durationValue * 1000;

            curLeftPro = 0;
            curRightPro = secTime * 1000;
        } else {
            int second = Math.round(duration / 100f);
            float realTime = (float) (second / 10.0);
            realTimeTxt.setText(String.valueOf(realTime));
            playTime = duration;

            curLeftPro = 0;
            curRightPro = (int) (realTime * 1000);
        }

        int endTime = playTime / 100;
        float rightTime = (float) (endTime / 10.0);
        endTime = endTime / 10;
        int point = (int) (rightTime * 10 % 10);
        if (point > 4 && rightTime < durationValue) {
            endTime += 1;
        }

        startTimeTxt.setText("00:00");
        String totalTimeStr = "00:0" + endTime;
        totalTimeTxt.setText(String.valueOf(totalTimeStr));

        adapter = new VideoTrimAdapter(this, duration, durationValue, kFrame, mSeekBar);
        listView.setAdapter(adapter);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        isFirstIn = true;

        surface.setDefaultBufferSize(480, 480);
        mSurface = new Surface(surface);

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            playVideo();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onStop() {
        isFirstIn = true;

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.setSurface(null);
            mPlayer.release();
            mPlayer = null;
        }

        super.onStop();
    }

    @Override
    protected void onResume() {
        if (textureview.getSurfaceTexture() != null) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                playVideo();
            }
        }

//        _OverlayManager.onResume();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        _OverlayManager.onPause();

        isTouchByUser = true;
        playFromStart = true;

        if (mPlayer != null && mPlayer.isPlaying()) {
            _PausePlayButton.setVisibility(View.VISIBLE);
            if (videoHeight != videoWidth) {
//                videoScaleToggle.setVisibility(View.VISIBLE);
            }
            mPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {

        if (kFrame != null) {
            kFrame.release();
        }

        if (mHandler != null) {
            mHandler.removeMessages(VIDEO_PLAY_POSITION);
        }

        super.onDestroy();
    }

    private int startTimeInt;
    private int totleTimeInt;

    private float seekBarLeftPoi;
    private float seekBarRightPoi;

    private boolean playFromStart = false;

    @Override
    public void SeekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                _PausePlayButton.setVisibility(View.VISIBLE);
                if (videoHeight != videoWidth) {
//                    videoScaleToggle.setVisibility(View.VISIBLE);
                }
                mPlayer.pause();
            }
            isTouchByUser = true;
            userPause = false;
            mSeekBar.removeVideoStatusThumb();
        }
        float result;
        if (duration > durationValue * 1000) {
            duration = durationValue * 1000;
            result = ((duration / 1000) * (rightThumb - leftThumb)) / 100;
        } else {
            result = (rightThumb - leftThumb) * durationValue / 100;
        }

        seekBarLeftPoi = leftThumb;
        seekBarRightPoi = rightThumb;

        curLeftPro = Math.round(leftThumb * durationValue * 10);
        curRightPro = Math.round(rightThumb * durationValue * 10);

        float setTime = (result * durationValue) / 100;

        BigDecimal bd = new BigDecimal(result);
        result = bd.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        int resultMin = ((int) result) / 60;
        int resultSec = ((int) result) % 60;
        realTimeTxt.setText(String.format(String.format("%1d:%02d", resultMin, resultSec)));
        //realTimeSet = setTime;
        playTime = (int) (setTime * 1000);

        float perSecond = adapter.getPerItemOfSecond();
        float paddingSec = unvisiableWd * perSecond / adapter.getItemWidth();

        startTimeInt = (Math.round(leftThumb * durationValue / 100 + paddingSec));
        totleTimeInt = (Math.round(rightThumb * durationValue / 100 + paddingSec));

        int videoOffSet;
        int offsetSec = Math.round(Listoffset * perSecond);

        if (whitchSide == 0) {
            if (mPlayer == null) {
                return;
            }
            videoOffSet = (startTimeInt + offsetSec) * 1000;
            curPosition = Math.round(leftThumb);
            playLeftPoi = Math.round(leftThumb);
            mPlayer.seekTo(videoOffSet);
            Log.d(TAG, "curLeftPro = " + curLeftPro + " videoOffSet = " + videoOffSet + " total = " + (curLeftPro + videoOffSet));

            int startPosition = startTimeInt + offsetSec;
            this.startPosition = startPosition;
            int startMin = startPosition / 60;
            int startSec = startPosition % 60;
            startTimeTxt.setText(String.format(String.format("%1d:%02d", startMin, startSec)));
            Log.d("SeekBarValueChanged", "leftThumb = " + leftThumb + " rightThumb = " + rightThumb
                    + " startTimeInt = " + startTimeInt + " offsetSec = " + offsetSec);
            int endPosition = totleTimeInt + offsetSec;
            this.endPosition = endPosition;
            int endMin = endPosition / 60;
            int endSec = endPosition % 60;
            totalTimeTxt.setText(String.format(String.format("%1d:%02d", endMin, endSec)));
        } else if (whitchSide == 1) {
            if (mPlayer == null) {
                return;
            }
            videoOffSet = (totleTimeInt + offsetSec) * 1000;
            mPlayer.seekTo(videoOffSet);
            Log.d(TAG, "curRightPro = " + curRightPro + " videoOffSet = " + videoOffSet + " total = " + (curRightPro + videoOffSet));
            playFromStart = true;

            int startPosition = startTimeInt + offsetSec;
            this.startPosition = startPosition;
            int startMin = startPosition / 60;
            int startSec = startPosition % 60;
            startTimeTxt.setText(String.format(String.format("%1d:%02d", startMin, startSec)));
            Log.d("SeekBarValueChanged", "leftThumb = " + leftThumb + " rightThumb = " + rightThumb
                    + " startTimeInt = " + startTimeInt + " offsetSec = " + offsetSec);
            int endPosition = totleTimeInt + offsetSec;
            this.endPosition = endPosition;
            int endMin = endPosition / 60;
            int endSec = endPosition % 60;
            totalTimeTxt.setText(String.format(String.format("%1d:%02d", endMin, endSec)));
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (isFirstIn) {
            isFirstIn = false;
            mPlayer.pause();
            isTouchByUser = true;
            userPause = false;
            _PausePlayButton.setVisibility(View.VISIBLE);
            if (videoHeight != videoWidth) {
//                videoScaleToggle.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.seekTo((int) (curLeftPro + Listoffset * 1000));

        isTouchByUser = true;
        userPause = false;
        mSeekBar.removeVideoStatusThumb();
        _PausePlayButton.setVisibility(View.VISIBLE);
        if (videoHeight != videoWidth) {
//            videoScaleToggle.setVisibility(View.VISIBLE);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case VIDEO_PLAY_POSITION: {
                    if (!isTouchByUser) {
                        if (mPlayer == null) {
                            return;
                        }

                        int playStartPoi = startPosition * 1000;
                        int playStopPoi = endPosition * 1000;
                        int curDuration = mPlayer.getCurrentPosition();
                        int durationLen = curDuration - playStartPoi;
                        int playStation = (curLeftPro + durationLen) * 100 / (durationValue * 1000);

                        if (curDuration < playStopPoi) {
                            int delayTime = Math.min(playStopPoi - curDuration, 50);
                            mHandler.sendEmptyMessageDelayed(VIDEO_PLAY_POSITION, delayTime);
                        } else {
                            Log.e("mylog", " curDuration = " + curDuration);
                            curPosition = playLeftPoi;
                            mSeekBar.removeVideoStatusThumb();

                            if (mPlayer != null && mPlayer.isPlaying()) {
                                _PausePlayButton.setVisibility(View.VISIBLE);
                                if (videoHeight != videoWidth) {
//                                    videoScaleToggle.setVisibility(View.VISIBLE);
                                }
                                mPlayer.pause();

                                mPlayer.seekTo(startPosition * 1000);
                            }

                            return;
                        }

                        Log.e("mylog", " curDuration = " + curDuration + " playStopPoi = " + playStopPoi
                                + " playStartPoi = " + playStartPoi + " playStation = " + playStation);
                        if (curDuration <= playStopPoi) {
                            mSeekBar.videoPlayingProgress(playStation);
                        }

                    } else {
                        if (!userPause) {
                            mSeekBar.removeVideoStatusThumb();
                        }
                        curPosition = playLeftPoi;
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RENDER_THUMBNAIL:
                if (resultCode == RESULT_OK) {
                    int activity_flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;

//                    new VideoActivity.Request(_Request)
//                            .setProjectUri(data.getData())
//                            .setIsFromDraft(true)
//                            .setNextIntent(_Request.getIntent())
//                            .startWithFlags(this, activity_flags);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        sceenWidth = frame.getWidth();
        sceenHeight = frame.getHeight();

        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();

        LayoutParams lp = (LayoutParams) textureview.getLayoutParams();

        VideoScaleHelper helper = new VideoScaleHelper()
                .setScreenWidthAndHeight(sceenWidth, sceenHeight)
                .setVideoWidthAndHeight(videoWidth, videoHeight);
        if (isScaleSquare) {
            helper.generateSquareVideoLayout(lp);
        } else {
            helper.generateDisplayLayoutParams(lp);//generateLayoutScaleFixed
        }

        lp.setMargins(0, 0, 0, 0);

        textureview.setLayoutParams(lp);
    }

    private float unvisiableWd;

    @Override
    public void onScrollDistance(Long position, int distanceX) {
        Log.e("scroll", "position = " + position.intValue() + " dx = " + distanceX);
        float perSecond = adapter.getPerItemOfSecond();
        float itemWidth = adapter.getItemWidth();

        unvisiableWd = distanceX - itemWidth * position.intValue();
        float paddingSec = unvisiableWd * perSecond / itemWidth;
        int leftTimeInt ;
        int posOffset;
        if(seekBarLeftPoi == 0) {
            leftTimeInt = Math.round(paddingSec);
        }else {
            leftTimeInt = Math.round(seekBarLeftPoi * durationValue / 100 + paddingSec);
        }
        if( Listoffset != position || scrolledLeftInt != leftTimeInt) {
            scrolledLeftInt = leftTimeInt;
            posOffset = Math.round(position * perSecond);
            startPosition = posOffset+leftTimeInt;
            int videoOffSet = startPosition * 1000;
            Log.d(TAG,"scroll leftTimeInt = "+leftTimeInt + " scroll offset = "+posOffset );
            if(mPlayer != null) {
                mSeekBar.removeVideoStatusThumb();
                mPlayer.seekTo(videoOffSet);
                Log.d(TAG,"curLeftPro = "+curLeftPro+" videoOffSet = "+videoOffSet+" total = "+(curLeftPro + videoOffSet));
            }
        }

        Listoffset = position;
        scrolledOffset = (int) (position * 1000 + distanceX*10);


        setTailorTime(paddingSec, perSecond);
    }

    private void setTailorTime(float padding, float perSec) {
        int leftTimeInt;
        int rightTimeInt;
        if (seekBarLeftPoi == 0) {
            leftTimeInt = Math.round(padding);
        } else {
            leftTimeInt = Math.round(seekBarLeftPoi * durationValue / 100 + padding);
        }

        if (seekBarRightPoi == 0) {
            rightTimeInt = Math.round(playTime / 100f + padding);
            rightTimeInt = Math.round(rightTimeInt / 10f + padding);
        } else {
            rightTimeInt = Math.round(seekBarRightPoi * durationValue / 100 + padding);
        }

        int offsetSec = Math.round(Listoffset * perSec);
        int leftPoi = offsetSec + leftTimeInt;
        startPosition = leftPoi;
        int leftMin = leftPoi / 60;
        int leftSec = leftPoi % 60;
        Log.d(TAG, "set text leftTimeInt = " + leftTimeInt + " set text offset = " + offsetSec);
        startTimeTxt.setText(String.format(String.format("%1d:%02d", leftMin, leftSec)));

        int rightPoi = offsetSec + rightTimeInt;
        endPosition = rightPoi;
        int rightMin = rightPoi / 60;
        int rightSec = rightPoi % 60;
        totalTimeTxt.setText(String.format(String.format("%1d:%02d", rightMin, rightSec)));
        Log.e("totle", "totleTime333 = " + String.format(String.format("%1d:%02d", rightMin, rightSec)));
    }

    @Override
    public void onVideoScroll(float distanceX, float distanceY) {
        LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
        int width = lp.width;
        int height = lp.height;

        if (width > sceenWidth || height > sceenHeight) {
            int maxHorizontalScroll = width - sceenWidth;
            int maxVerticalScroll = height - sceenHeight;
            if (maxHorizontalScroll > 0) {
                maxHorizontalScroll = maxHorizontalScroll / 2;
                mScrollX += distanceX;
                if (mScrollX > maxHorizontalScroll) {
                    mScrollX = maxHorizontalScroll;
                }
                if (mScrollX < -maxHorizontalScroll) {
                    mScrollX = -maxHorizontalScroll;
                }
            }
            if (maxVerticalScroll > 0) {
                maxVerticalScroll = maxVerticalScroll / 2;
                mScrollY += distanceY;
                if (mScrollY > maxVerticalScroll) {
                    mScrollY = maxVerticalScroll;
                }
                if (mScrollY < -maxVerticalScroll) {
                    mScrollY = -maxVerticalScroll;
                }
            }
            lp.setMargins(0, 0, mScrollX, mScrollY);
        }

        textureview.setLayoutParams(lp);
    }

    @Override
    public void onBackPressed() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        finish();
    }

    @Override
    public void onIsDown(boolean isDown) {
        isPressDown = isDown;

        if (mPlayer != null && isPressDown) {
            if (mPlayer.isPlaying()) {
                _PausePlayButton.setVisibility(View.VISIBLE);
                if (videoHeight != videoWidth) {
//                    videoScaleToggle.setVisibility(View.VISIBLE);
                }
                mPlayer.pause();
            }
            isTouchByUser = true;
            userPause = false;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mp.seekTo(0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public void onImportDialogDismiss() {
        nextBtn.setEnabled(true);
    }
}
