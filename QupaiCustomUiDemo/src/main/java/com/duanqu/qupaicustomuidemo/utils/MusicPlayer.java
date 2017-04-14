package com.duanqu.qupaicustomuidemo.utils;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.duanqu.qupaicustomuidemo.widget.CircleProgressView;

import java.io.IOException;
import java.util.Map;

public class MusicPlayer {
	private static final String TAG = "AudioPlayer";
	private Context mContext;

	private Uri mUri;
	private Map<String, String> mHeaders;
	private int mDuration;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_SUSPEND = 6;

	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private MediaPlayer mMediaPlayer = null;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private int mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private ImageView stImage;
	private CircleProgressView mPbar;
	private ImageView mStBg;
	private ImageView mStAnim;
	private AnimationDrawable anim;
	private int curPosition = -1;
	private int curLine = -1;
	private String curUrl = null;

	private boolean looper=false;

	public MusicPlayer(Context context) {
		this.mContext = context;
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	public void setVideoURIWithImage(Uri uri, ImageView image,
			CircleProgressView pbar, ImageView stBg, ImageView jdText) {
		if(stImage != null && stImage != image) {
			stImage.setVisibility(View.VISIBLE);
		}
		stImage = image;

		if(mPbar != null && mPbar != pbar) {
			mPbar.setVisibility(View.GONE);
			mStBg.setVisibility(View.GONE);
			mStAnim.setVisibility(View.GONE);
		}
		mPbar = pbar;
		mStBg = stBg;
		mStAnim = jdText;
		anim = (AnimationDrawable) mStAnim.getBackground();

		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	private void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	public void setPlayerPosition(int position, int line, String url) {
		curPosition = position;
		curLine = line;
		curUrl = url;
	}

	public boolean isPlayerSameRes(int position, String url) {
		boolean isSame = false;

		if(position == curPosition && curUrl.equals(url)) {
			isSame = true;
		}

		return isSame;
	}

	public int getCurPosition() {
		return curPosition;
	}

	public int getCurLine() {
		return curLine;
	}

	private void openVideo() {
		if (mUri == null) {
			return;
		}

		// Tell the music playback service to pause
//		Intent i = new Intent("com.android.music.musicservicecommand");
//		i.putExtra("command", "pause");
//		mContext.sendBroadcast(i);
		// we shouldn&#39;t clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			//mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			// mMediaPlayer.setDataSource(mContext, mUri);
			if (Build.VERSION.SDK_INT<14) {
				mMediaPlayer.setDataSource(mContext, mUri);
			}else {
				mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			}

			// mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			//mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don&#39;t set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			mTargetState=STATE_PLAYING;

		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}catch (Exception e) {
			Log.w(TAG, "Unable to open content: " + mUri, e);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			mCurrentState = STATE_PREPARED;
			mCanPause = mCanSeekBack = mCanSeekForward = true;
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			int seekToPosition = mSeekWhenPrepared;
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if (mTargetState == STATE_PLAYING) {
				start();
			}
		}
	};



	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mPbar.setVisibility(View.GONE);
			mStBg.setVisibility(View.GONE);
			mStAnim.setVisibility(View.GONE);
			stImage.setVisibility(View.VISIBLE);
			if(anim.isRunning()) {
				anim.stop();
			}

			if(mCurrentState==STATE_ERROR && mTargetState == STATE_ERROR){
				stopPlayback();
				return;
			}
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}else if(looper){
				//mMediaPlayer.start();
				//mCurrentState = STATE_PLAYING;
				//mTargetState = STATE_PLAYING;
				start();
			}
		}
	};



	private OnErrorListener mErrorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, " mErrorListener Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err,
						impl_err)) {
					return true;
				}
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mPbar.setVisibility(View.GONE);
			mStAnim.setVisibility(View.VISIBLE);
			
			anim.setOneShot(false);
			if(anim.isRunning()) {
				anim.stop();
			}
			anim.start();
			
			mp.setOnBufferingUpdateListener(null);
			
			mCurrentBufferPercentage = percent;
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 *
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 *
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	public void setLooper(boolean loop) {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.setLooping(loop);
		}
		this.looper=loop;
	}

	public void setVolume(float leftVolume, float rightVolume) {
		if (mMediaPlayer != null) {// &&mMediaPlayer.isPlaying()
			mMediaPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 *
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void onDestroy() {
		if (mCurrentState != STATE_SUSPEND) {
			release(true);
		}
	}

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
				} else {
					start();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					&& mMediaPlayer.isPlaying()) {
				pause();
			}
		}
		return false;
	}

	public void start() {
		if (isInPlaybackState()) {
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;

		mPbar.setVisibility(View.GONE);
		mStBg.setVisibility(View.GONE);
		mStAnim.setVisibility(View.GONE);
		stImage.setVisibility(View.VISIBLE);
	}

	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}
}
