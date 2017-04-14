package com.duanqu.qupaicustomuidemo.editor.download;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.cache.core.VideoLoader;
import com.duanqu.qupai.cache.core.VideoLoadingListener;
import com.duanqu.qupai.cache.core.VideoLoadingProgressListener;
import com.duanqu.qupai.cache.core.assist.FailReason;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.utils.DensityUtil;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;
import com.duanqu.qupaicustomuidemo.widget.video.IVideoPlayer;
import com.duanqu.qupaicustomuidemo.widget.video.VideoTextureViewMediaPlayer;

import java.io.File;

public class IMVideoPlayControl {
	private Context mContext;
	private String mVideoUrl;
	private ViewGroup mView;
	private Button mManageBtn;

	private VideoPrepareListener mPrepareListener;
	private ProgressLayout mProgressLayout;
	private IVideoPlayer mPlayer;
	private ImageView deleteImg;
	private boolean isStop;

	public interface VideoPrepareListener {
		void videoPrepare();
		void videoStopPlay();
		void managerResource();
	}

	public void setVideoPrepareListener(VideoPrepareListener listener) {
		mPrepareListener = listener;
	}

	public IMVideoPlayControl(Context context, String videoUrl, ViewGroup view, Button btn) {
		mContext = context;
		mVideoUrl = videoUrl;
		mView = view;
		mManageBtn = btn;
		deleteImg = new ImageView(mContext);
		mProgressLayout = new ProgressLayout(mContext);
		mView.addView(mProgressLayout);
		mProgressLayout.setProgress(0);

		if(mManageBtn != null) {
			addDeleteView();

			mManageBtn.setOnClickListener(_managerClickListener);
			mManageBtn.setOnTouchListener(_managerTouchListener);
		}
	}

	private void addDeleteView() {
		deleteImg.setImageResource(R.drawable.btn_imv_download_delete_selector);
		LayoutParams deleteParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		deleteParam.gravity = Gravity.TOP | Gravity.RIGHT;
		deleteParam.topMargin = 40;
		deleteParam.rightMargin = 40;
		deleteImg.setLayoutParams(deleteParam);
		deleteImg.setOnClickListener(_deleteClickListener);
		deleteImg.setOnTouchListener(_deleteTouchListener);

		mView.addView(deleteImg);
	}

	public void startPlayVideo() {
		isStop = false;

		VideoLoader.getInstance().displayVideo(mVideoUrl, mView, new VideoLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				ToastUtil.showToast(mContext, mContext.getResources()
						.getString(R.string.slow_network), Toast.LENGTH_SHORT, Gravity.CENTER);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, File loadedVideo) {
				if(!isStop) {
					mProgressLayout.hide();
					playByFilePath(loadedVideo.getAbsolutePath());
				}
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
			}
		},

		new VideoLoadingProgressListener() {

			@Override
			public void onProgressUpdate(String imageUri, View view, int current,
					int total) {
				Log.e("videoLoade", " current = " + current + " total = " + total);
				int progress = current * 100 / total;
				mProgressLayout.setProgress(progress);
			}
		});
	}

	public void cancelTask() {
		VideoLoader.getInstance().cancelDisplayTask(mView);
	}

	public void stopPlayVideo() {
		isStop = true;

		mView.removeView(deleteImg);

		if(mProgressLayout != null) {
			mProgressLayout.hide();
		}

		if (mPlayer != null) {
			mPlayer.releaseMediaPlayer();
			((VideoTextureViewMediaPlayer) mPlayer).removePrepareListener();

			View paramView = mPlayer.getShowView();
			if (paramView != null) {
				mView.removeView(paramView);
			}else {
				View v = mView.findViewById(Math.abs(mVideoUrl.hashCode()));
				if (v != null) {
					mView.removeView(v);
				}
			}
		}
	}

	private void playByFilePath(String filePath) {
		if (mPlayer != null) {
			if (mPlayer.hasPlayer()) {
				mPlayer.releaseMediaPlayer();
			}
			mPlayer = null;
		}

		mPlayer = createPlayer(mContext, Uri.parse(filePath));

		if(mPlayer != null) {
			View videoview = mView.findViewById(Math.abs(mVideoUrl.hashCode()));
			if (videoview != null) {
				mView.removeView(videoview);
			}

			View paramView = mPlayer.getShowView();

			if (paramView != null) {
				ViewGroup vg = (ViewGroup) paramView.getParent();
				if (vg != null) {
					vg.removeView(paramView);
				}
				mView.addView(paramView, 0);
				paramView.setOnClickListener(_surfaceClickListener);
				paramView.setOnTouchListener(_surfaceTouchListener);

				paramView.setId(Math.abs(mVideoUrl.hashCode()));
			}

			mPlayer.start();

			if(mPrepareListener != null) {
				mPrepareListener.videoPrepare();
			}
		}
	}

	private IVideoPlayer createPlayer(Context context, Uri paramUri) {
		VideoTextureViewMediaPlayer textureViewMediaPlayer = new VideoTextureViewMediaPlayer(
				context, null, paramUri);
		return textureViewMediaPlayer;
	}

	private OnClickListener _surfaceClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mPrepareListener != null) {
				mPrepareListener.videoStopPlay();
			}

			VideoLoader.getInstance().cancelDisplayTask(mView);
		}
	};

	private OnTouchListener _surfaceTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.getParent().requestDisallowInterceptTouchEvent(true);

			return false;
		}
	};

	private OnClickListener _deleteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mPrepareListener != null) {
				mPrepareListener.videoStopPlay();
			}

			VideoLoader.getInstance().cancelDisplayTask(mView);
		}
	};

	private OnTouchListener _deleteTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			deleteImg.getParent().requestDisallowInterceptTouchEvent(true);

			return false;
		}
	};

	private OnClickListener _managerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mPrepareListener != null) {
				mPrepareListener.managerResource();
			}
		}
	};

	private OnTouchListener _managerTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mManageBtn.getParent().requestDisallowInterceptTouchEvent(true);

			return false;
		}
	};

	private class ProgressLayout extends FrameLayout {
		private ProgressBar mProgressBar;
		private TextView mProgressTextView;

		public ProgressLayout(Context localContext) {
			this(localContext, null);
		}

		public ProgressLayout(Context localContext, AttributeSet localAttributeSet) {
			this(localContext, localAttributeSet, 0);
		}

		public ProgressLayout(Context localContext,AttributeSet localAttributeSet, int i) {
			super(localContext, localAttributeSet, i);
			initView(localContext);
		}

		private void initView(Context context) {
			LayoutParams progressParams = new LayoutParams(
					DensityUtil.dip2px(69), DensityUtil.dip2px(69));
			progressParams.gravity = Gravity.CENTER;

			mProgressBar = (ProgressBar) FontUtil.applyFontByInflate(
					context, R.layout.layout_progress, null, false);
			mProgressBar.setVisibility(View.GONE);
			mProgressTextView = new TextView(context);
			mProgressTextView.setGravity(Gravity.CENTER);
			mProgressTextView.setTextColor(Color.parseColor("#FFFFFF"));
			mProgressTextView.setTextSize(17.0F);
			mProgressTextView.setTypeface(null, 1);

			addView(this.mProgressBar, progressParams);
			addView(this.mProgressTextView, progressParams);
		}

		public void setProgress(int paramInt) {
			if (paramInt >= 0) {
				showProgressBar();
				this.mProgressBar.setVisibility(View.VISIBLE);
				this.mProgressTextView.setVisibility(View.VISIBLE);
				this.mProgressBar.setProgress(paramInt);
				this.mProgressTextView.setText(paramInt + "%");
			} else {
				hide();
			}
		}

		public void showProgressBar() {
			bringToFront();
			this.mProgressBar.setVisibility(View.VISIBLE);
			this.mProgressBar.setProgress(0);
			this.mProgressTextView.setVisibility(View.VISIBLE);
			this.mProgressTextView.setText("0%");
		}

		private void hide() {
			mProgressBar.setVisibility(View.GONE);
			mProgressTextView.setVisibility(View.GONE);

			setVisibility(View.GONE);

			bringToFront();
		}
	}
}