package com.duanqu.qupaicustomuidemo.editor.download;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.cache.core.VideoLoader;
import com.duanqu.qupai.cache.core.VideoLoadingListener;
import com.duanqu.qupai.cache.core.VideoLoadingProgressListener;
import com.duanqu.qupai.cache.core.assist.FailReason;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.view.SquareFrameLayout;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.mv.IMVDownloadListAdapter;
import com.duanqu.qupaicustomuidemo.utils.DensityUtil;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;
import com.duanqu.qupaicustomuidemo.widget.video.IVideoPlayer;
import com.duanqu.qupaicustomuidemo.widget.video.VideoTextureViewMediaPlayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

public class IMVPreviewDialog extends DialogFragment {

    private static final String KEY_VIDEO_URL = "video_url";
    private static final String KEY_THUMB_URL = "thumb_url";
    private static final String KEY_BUTTON_STATE = "button_state";

    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options= new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .showImageForEmptyUri(R.drawable.video_thumbnails_loading_126)
            .showImageOnFail(R.drawable.video_thumbnails_loading_126)
            .showImageOnLoading(R.drawable.video_thumbnails_loading_126)
            .cacheInMemory(true)
            .cacheOnDisk(true).build();

    public interface VideoPrepareListener {
        void videoStopPlay();
        void managerResource();
    }

    private VideoPrepareListener mPrepareListener;

    public void setVideoPrepareListener(VideoPrepareListener listener) {
        mPrepareListener = listener;
    }

    public static IMVPreviewDialog newInstance(String videoUrl, String thumbUrl, int state){
        IMVPreviewDialog dialog = new IMVPreviewDialog();
        Bundle args=new Bundle();
        args.putString(KEY_VIDEO_URL, videoUrl);
        args.putString(KEY_THUMB_URL, thumbUrl);
        args.putInt(KEY_BUTTON_STATE, state);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ResourcePreviewStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
    }

    private Context mContext;
    private SquareFrameLayout mVideoView;
    private ImageView thumbView;
    private ImageView mCloseBtn;
    private Button mDownloadBtn;
    private CircleProgressBar mProgressBar;
    private int state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = FontUtil.applyFontByInflate(getActivity(),
                R.layout.imv_preview_dialog_layout, container, false);

        mContext = view.getContext();

        mVideoView = (SquareFrameLayout) view.findViewById(R.id.imv_video_view);
        mCloseBtn = (ImageView) view.findViewById(R.id.iv_imv_close_btn);
        mDownloadBtn = (Button) view.findViewById(R.id.iv_imv_download_btn);
        mProgressBar = (CircleProgressBar) view.findViewById(R.id.pb_imv_progress);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrepareListener.videoStopPlay();
                dismiss();
            }
        });

        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPrepareListener != null) {
                    if(state == IMVDownloadListAdapter.STATE_USED) {
                        cancelTask();
                        stopPlayVideo();
                    }
                    mPrepareListener.managerResource();
                }
            }
        });

        state = getArguments().getInt(KEY_BUTTON_STATE);
        if(state == IMVDownloadListAdapter.STATE_DOWNLOADING) {
            mDownloadBtn.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }else if(state == IMVDownloadListAdapter.STATE_USED) {
            mDownloadBtn.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            mDownloadBtn.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
            mDownloadBtn.setText(R.string.qupai_mv_used);
        }else {
            mDownloadBtn.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            mDownloadBtn.setBackgroundResource(R.drawable.bg_parser_preview_download_rect);
        }

        return view;
    }

    public void setProgress(int progress) {
        if(mProgressBar != null) {
            mProgressBar.setProgress(progress);
        }
    }

    public void setDownloadBtnState(int state) {
        if(state == IMVDownloadListAdapter.STATE_DOWNLOADING) {
            mDownloadBtn.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }else if(state == IMVDownloadListAdapter.STATE_USED) {
            mDownloadBtn.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            mDownloadBtn.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
            mDownloadBtn.setText(R.string.qupai_mv_used);
        }else {
            mDownloadBtn.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

            mDownloadBtn.setBackgroundResource(R.drawable.bg_parser_preview_download_rect);
        }
    }

    private boolean isStop;
    private String mVideoUrl;
    private IVideoPlayer mPlayer;
    private ProgressLayout mProgressLayout;

    private void initView() {
        mVideoUrl = getArguments().getString(KEY_VIDEO_URL);

        thumbView = new ImageView(mContext);
        mVideoView.addView(thumbView, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mImageLoader.displayImage(getArguments().getString(KEY_THUMB_URL), thumbView, options);
        mProgressLayout = new ProgressLayout(mContext);
        mVideoView.addView(mProgressLayout);

        startPlayVideo();
    }

    private void startPlayVideo() {
        isStop = false;

        VideoLoader.getInstance().displayVideo(mVideoUrl, mVideoView, new VideoLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view,
                                            FailReason failReason) {
                    Log.e("onLoadingFailed","onLoadingFailed" + imageUri);
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

    private void playByFilePath(String filePath) {
        if (mPlayer != null) {
            if (mPlayer.hasPlayer()) {
                mPlayer.releaseMediaPlayer();
            }
            mPlayer = null;
        }

        mPlayer = createPlayer(mContext, Uri.parse(filePath));

        if(mPlayer != null) {
            View videoView = mVideoView.findViewById(Math.abs(mVideoUrl.hashCode()));
            if (videoView != null) {
                mVideoView.removeView(videoView);
            }

            View paramView = mPlayer.getShowView();

            if (paramView != null) {
                ViewGroup vg = (ViewGroup) paramView.getParent();
                if (vg != null) {
                    vg.removeView(paramView);
                }
                mVideoView.addView(paramView, 0);
                paramView.setId(Math.abs(mVideoUrl.hashCode()));
            }

            mPlayer.start();

            thumbView.setVisibility(View.GONE);
        }
    }

    private IVideoPlayer createPlayer(Context context, Uri paramUri) {
        VideoTextureViewMediaPlayer textureViewMediaPlayer = new VideoTextureViewMediaPlayer(
                context, null, paramUri);
        return textureViewMediaPlayer;
    }

    public void cancelTask() {
        VideoLoader.getInstance().cancelDisplayTask(mVideoView);
    }

    public void stopPlayVideo() {
        isStop = true;

        if(mProgressLayout != null) {
            mProgressLayout.hide();
        }

        if (mPlayer != null) {
            mPlayer.releaseMediaPlayer();
            ((VideoTextureViewMediaPlayer) mPlayer).removePrepareListener();

            View paramView = mPlayer.getShowView();
            if (paramView != null) {
                mVideoView.removeView(paramView);
            }else {
                View v = mVideoView.findViewById(Math.abs(mVideoUrl.hashCode()));
                if (v != null) {
                    mVideoView.removeView(v);
                }
            }
        }
    }

    @Override
    public void onStop() {
        cancelTask();

        super.onStop();
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        initView();

        super.onResume();
    }

    private class ProgressLayout extends FrameLayout {
        private CircleProgressBar mProgressBar;
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
            FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                    DensityUtil.dip2px(69), DensityUtil.dip2px(69));
            progressParams.gravity = Gravity.CENTER;

            mProgressBar = (CircleProgressBar) FontUtil.applyFontByInflate(
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
                this.mProgressBar.setVisibility(View.GONE);
                this.mProgressTextView.setVisibility(View.VISIBLE);
                this.mProgressBar.setProgress(paramInt);
                this.mProgressTextView.setText(paramInt + "%");
            } else {
                hide();
            }
        }

        public void showProgressBar() {
            bringToFront();
            this.mProgressBar.setVisibility(View.GONE);
            this.mProgressBar.setProgress(View.VISIBLE);
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
