package com.duanqu.qupaicustomuidemo.widget.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.duanqu.qupaicustomuidemo.editor.download.IMVideoPlayControl;

import java.io.IOException;
import java.util.Map;


public class VideoTextureViewMediaPlayer extends TextureView
implements IVideoPlayer, TextureView.SurfaceTextureListener,
MediaPlayer.OnSeekCompleteListener,
MediaPlayer.OnPreparedListener,
MediaPlayer.OnVideoSizeChangedListener,
View.OnClickListener{

	private static final String MEDIA = "media";
	public static final String TAG = "TextureViewMediaPlayer";
	public static final String VIDEO_CACHE_PATH = Environment.getExternalStorageDirectory() + "/DCIM/WeiKan/";

	private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPEND            = 6;
    private static final int STATE_RESUME             = 7;
    private boolean isAvailableCalled = false;
	public int mCurrentState;
	private boolean mIsVideoReadyToBePlayed = false;
	private boolean mIsVideoSizeKnown = false;
	private final int[] mLock;
	public MediaPlayer mMediaPlayer;
	private Uri mUri;
	private RepeatPlayHelper stRepeatPlayHelper = null;
	private int to_start = 0;
	private IMVideoPlayControl.VideoPrepareListener mPrepareListener;
	private MediaPlayer.OnCompletionListener onCompletionListener;
	private MediaPlayer.OnPreparedListener onPreparedListener;

	public VideoTextureViewMediaPlayer(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}

	public VideoTextureViewMediaPlayer(Context context,AttributeSet attrs){
		this(context,attrs,null);
	}

	public VideoTextureViewMediaPlayer(Context context,AttributeSet attrs,Uri paramUri){
		super(context, attrs);
	    this.mUri = paramUri;
	    this.mMediaPlayer = null;
	    this.mCurrentState = STATE_IDLE;
	    this.mLock = new int[0];
	    setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
	    		FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
	    setSurfaceTextureListener(this);
	}

	public void setPrepareListener(IMVideoPlayControl.VideoPrepareListener listener) {
		mPrepareListener = listener;
	}

	public void removePrepareListener() {
		mPrepareListener = null;
	}

	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener onCompletionListener) {
		this.onCompletionListener = onCompletionListener;
	}

	public void setOnPrepareListener(
			MediaPlayer.OnPreparedListener onPrepareListener) {
		this.onPreparedListener = onPrepareListener;
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

//	private void doCleanUp(){
//	    this.mIsVideoReadyToBePlayed = false;
//	    this.mIsVideoSizeKnown = false;
//	}

	private void openVideo(){
        setScaleX(1.0001f);
        setScaleY(1.0001f);

		SurfaceTexture texture = getSurfaceTexture();
		if(texture == null){
			return;
		}
		release();
		mMediaPlayer = new MediaPlayer();
		Surface surface = new Surface(texture);
	    if (mUri != null){
	    	try{
		        Log.d("AUTOPLAY", "onSurfaceTextureAvailable play init uri" + mUri.toString());
		        mMediaPlayer.setDataSource(mUri.toString());
		        mMediaPlayer.setSurface(surface);
		        mMediaPlayer.setOnSeekCompleteListener(VideoTextureViewMediaPlayer.this);
		        mMediaPlayer.setOnPreparedListener(VideoTextureViewMediaPlayer.this);
		        mMediaPlayer.setOnErrorListener(errorListener);
		        mMediaPlayer.setOnVideoSizeChangedListener(VideoTextureViewMediaPlayer.this);
		        mMediaPlayer.setAudioStreamType(3);
		        mMediaPlayer.setLooping(true);
		        mMediaPlayer.prepareAsync();
		      }catch (IOException localIOException){
		    	  localIOException.printStackTrace();
		      }catch (NullPointerException e){
		    	  e.printStackTrace();
		      }
	    }
	}

	  private void release(){
	    synchronized (this.mLock){
	      if (this.mMediaPlayer != null){
	    	if(mMediaPlayer.isPlaying()){
	    		mMediaPlayer.stop();
	    	}
	        this.mMediaPlayer.reset();
	        this.mMediaPlayer.release();
	        this.mMediaPlayer = null;
	        this.mCurrentState = STATE_IDLE;
	      }
	      return;
	    }
	  }

	  public void setVideoURI(Uri paramUri, Map paramMap){
	    this.mUri = paramUri;
	    Log.e("TextureViewMediaPlayer", "setVideoURI:   " + this.mUri.toString());
	    //openVideo();
	  }

	private void startVideoPlayback(){
	    Log.v("TextureViewMediaPlayer", "startVideoPlayback");
	    Log.d("AUTOPLAY", "VideoTextureViewMediaPlayer:play");
	    try{
	      this.mMediaPlayer.start();
	      this.mCurrentState = STATE_PLAYING;

	      if(mPrepareListener != null) {
	    	  mPrepareListener.videoPrepare();
	      }

		  if(onPreparedListener != null) {
			  onPreparedListener.onPrepared(mMediaPlayer);
		  }

	    }catch (Exception localException){
	    	localException.printStackTrace();
	    }
	}

	public void stopPlayback() {
		if ((this.mMediaPlayer != null) && (this.mCurrentState != -1)){
	      if (this.mMediaPlayer.isPlaying())
	        this.mMediaPlayer.stop();
	      this.mMediaPlayer.release();
	      this.mMediaPlayer = null;
	      this.mCurrentState = STATE_IDLE;
	    }
    }

	public void seekTo(int paramInt){
	    if (isInPlaybackState())
	      this.mMediaPlayer.seekTo(paramInt);
	}

	public void setRepeatPlayHelper(RepeatPlayHelper paramRepeatPlayHelper) {
	    this.stRepeatPlayHelper = paramRepeatPlayHelper;
	}

	@Override
	public View getShowView() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public boolean hasPlayer() {
		if (this.mMediaPlayer != null)
			return true;
		return false;
	}

	public boolean isInPlaybackState(){
		return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
	}

	public boolean hasStarted(){
	    return isInPlaybackState();
	}

	public boolean isPaused(){
	    if (this.mCurrentState == STATE_PAUSED)
	    	return true;
	    return false;
	}

	@Override
	public boolean isPlaying() {
		if ((isInPlaybackState()) && (this.mMediaPlayer.isPlaying()))
			return true;
		return false;
	}

	@Override
	public void pause() {
		if ((this.mMediaPlayer != null) && (isInPlaybackState()) && (this.mMediaPlayer.isPlaying())){
	      this.mMediaPlayer.pause();
	      this.mCurrentState = STATE_PAUSED;
	    }
		this.to_start = 2;
	}

	@Override
	public int getCurrentPoi() {
		if(this.mMediaPlayer != null) {
			return this.mMediaPlayer.getCurrentPosition();
		}

		return 0;
	}

	@Override
	public void releaseMediaPlayer() {
		Log.e("TextureViewMediaPlayer", "releaseMediaPlayer");
		release();
	    this.mIsVideoReadyToBePlayed = false;
	    this.mCurrentState = STATE_IDLE;
	}

	@Override
	public void start() {
		if (getVisibility() != View.VISIBLE)
		      setVisibility(View.VISIBLE);
		this.to_start = 1;

		Log.d("AUTOPLAY", "VideoTextureViewMediaPlayer:start:"+mIsVideoReadyToBePlayed);
		if (this.mIsVideoReadyToBePlayed){
			startVideoPlayback();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		SurfaceTexture texture = getSurfaceTexture();
		Log.d("AUTOPLAY", "VideoTextureViewMediaPlayer : onAttachedToWindow" + (texture == null));
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.d("AUTOPLAY", "VideoTextureViewMediaPlayer : onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.e("TextureViewMediaPlayer", "onVideoSizeChanged called");
	    if ((width == 0) || (height == 0)){
	    	Log.e("TextureViewMediaPlayer",  "invalid video width(" + width + ") or height(" + height + ")");
	    }else{
	    	Log.e("TextureViewMediaPlayer",  "video width(" + width + ") or height(" + height + ")");
	 	    this.mIsVideoSizeKnown = true;
	 	    if (!this.mIsVideoReadyToBePlayed){

	 	    }
	    }
	}

	public void suspend(){
	    release();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.e("AUTOPLAY", "onPrepared called");
	    this.mIsVideoReadyToBePlayed = true;
	    this.mCurrentState=STATE_PREPARED;
	    if (this.to_start == 1){
	    	startVideoPlayback();
	    }
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		Log.d("AUTOPLAY", "SurfaceTextureAvailable");
		this.isAvailableCalled = true;
	    //Log.e("TextureViewMediaPlayer", CommonDefine.getLineInfo(new java.lang.Throwable().getStackTrace()[0]) + " onSurfaceTextureAvailable called");
//		Surface localSurface = new Surface(surface);
//		mSurfaceHolder = localSurface;
		openVideo();
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		isAvailableCalled=false;
		Log.d("AUTOPLAY", "SurfaceTextureDestroyed");
		release();
		return true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		Log.d("AUTOPLAY", "SurfaceTextureSizeChanged");
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	public static abstract interface RepeatPlayHelper{
	    public abstract void reStart();
	}

	private OnErrorListener errorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mCurrentState = STATE_ERROR;
			Log.e("TextureViewMediaPlayer", "onError called : what:" + what + ":extra:" + extra);
			if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
				Log.v(TAG, "MEDIA_ERROR_SERVER_DIED");
				//openVideo();
				releaseMediaPlayer();
			} else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
			    Log.v(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
			    releaseMediaPlayer();
			} else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			    Log.v(TAG, "MEDIA_ERROR_UNKNOWN");
			    openVideo();
			    //releaseMediaPlayer();
			} else {
				openVideo();
				//releaseMediaPlayer();
			}
			return false;
		}
	};

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		if(onCompletionListener != null){
			onCompletionListener.onCompletion(mp);
		}
	}

}
