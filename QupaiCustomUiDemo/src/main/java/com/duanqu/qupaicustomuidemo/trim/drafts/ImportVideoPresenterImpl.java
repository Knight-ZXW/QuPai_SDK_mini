package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.duanqu.qupai.dialog.AlertDialogFragment;
import com.duanqu.qupai.engine.session.SessionPage;
import com.duanqu.qupai.utils.FileUtils;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.trim.VideoFileActivity;

import java.io.File;
import java.util.ArrayList;

public class ImportVideoPresenterImpl implements ImportResPresenter,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener {

    private TextureView textureview;
    private FrameLayout sFLayout;
    private Surface mSurface;
    private MediaPlayer mPlayer;
    private LinearLayout layout_no_video_data;

    private View _DeleteButton;
    public ImportEditor _importEditor;

    private LinearLayout systemCameraLayout;
    private FrameLayout toastLayout;

    private View mSurfaceView;
    private View root;
    private ImportVideoFragment mFragment;

    public ImportVideoPresenterImpl(ImportVideoFragment fragment, View root){
        mFragment = fragment;
        this.root = root;
        sFLayout = (FrameLayout) root.findViewById(R.id.import_layout);
        layout_no_video_data =(LinearLayout)root.findViewById(R.id.layout_no_video_data);

        InflaterTextureView();

        _importEditor = new ImportVideoEditor(root);
        _importEditor.setListener(importListener);
    }

    private void InflaterTextureView() {
        mSurfaceView = FontUtil.applyFontByInflate(
                mFragment.getActivity(), R.layout.import_video_texture_view, (ViewGroup) root, false);
        sFLayout.addView(mSurfaceView);

        systemCameraLayout = (LinearLayout) mSurfaceView.findViewById(R.id.drfat_system_camera_layout);
        toastLayout = (FrameLayout) mSurfaceView.findViewById(R.id.draft_toast_layout);

        _DeleteButton = mSurfaceView.findViewById(R.id.btn_delete);
        _DeleteButton.setOnClickListener(_Delete_OnClickListener);

        textureview = (TextureView) mSurfaceView.findViewById(R.id.import_surface);
        textureview.setSurfaceTextureListener(_surfaceTextureListener);
        textureview.setOnClickListener(_SurfaceOnClickListener);
    }

    private final View.OnClickListener _SurfaceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_importEditor.getCurrentList() == null) {
                return;
            }
            textureview.setClickable(false);

            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }

            ((ImportVideoFragment.VideoListener) mFragment.getActivity()).onVideoSelect(mFragment,
                    _importEditor.getCurrentList());
        }
    };

    @Override
    public void dispatchOnSelect() {
        _SurfaceOnClickListener.onClick(null);
    }

    public ImportEditor getVideoEditor() {
        return _importEditor;
    }

    @Override
    public boolean isCurrentListEmpty() {
        return getVideoEditor().getCurrentList() == null;
    }

    private void Resume() {
        if (textureview.getSurfaceTexture() != null) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
            }

            if (_importEditor.getCurrentPath() != null) {
                playVideo(_importEditor.getCurrentPath());
            }
        }
    }

    @Override
    public void onResume() {
        textureview.setClickable(true);

        Resume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (_importEditor.dataList != null && _importEditor.dataList.size() <= 0) {
            mSurfaceView.setVisibility(View.INVISIBLE);
        }else {
            mSurfaceView.setVisibility(View.VISIBLE);
        }
        if (isVisibleToUser) {
            Resume();
        } else {
            if (mPlayer != null) {
                mPlayer.pause();
            }
        }
    }

    @Override
    public void onStop() {
        if (_importEditor != null) {
            _importEditor.cancelTask();
        }

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.setSurface(null);
            mPlayer.release();
            mPlayer = null;
        }
    }


    private View.OnClickListener _Delete_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            AlertDialogFragment dlg = AlertDialogFragment.newInstance(
                    R.string.qupai_sure_delete_current_video,
                    R.string.qupai_dlg_button_yes,
                    R.string.qupai_dlg_button_no);
            dlg.setTargetFragment(mFragment, ImportVideoFragment.RC_DELETE_CONFIRMATION_DIALOG);
            mFragment.getFragmentManager().beginTransaction().add(dlg, null).commit();
        }
    };

    @Override
    public void delete() {
        deleteCurrentVideo();
    }

    private ImportVideoEditor.ImportListener importListener = new ImportVideoEditor.ImportListener() {

        @Override
        public void onCompelete(boolean hasVideo) {
            if (_importEditor.isTaskCancel()) {
                return;
            }

            if(!hasVideo){
                layout_no_video_data.setVisibility(View.VISIBLE);
            }

            ((ImportVideoFragment.VideoListener) mFragment.getActivity()).onSortComplete(mFragment);
        }

        @Override
        public void onPlayCurrent(String path) {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.setSurface(null);

                if (path == null) {
                    if (getVideoEditor().dataList != null
                            && getVideoEditor().dirList != null) {
                        Activity activity = mFragment.getActivity();
                        ArrayList<VideoInfoBean> videoList = getVideoEditor().dataList;
                        ArrayList<VideoDirBean> dirList = getVideoEditor().dirList;
                        Intent in = new VideoFileActivity.Request(((SessionPage) activity).getPageRequest())
                                .toIntent(activity);
                        in.putExtra("video_list", videoList);
                        in.putExtra("dir_list", dirList);

                        mFragment.getActivity().startActivityForResult(in, ImportActivity.REQUEST_CODE_PICK);
                        Log.d("VideoFileList", "startTime:" + System.currentTimeMillis());
                    }
                } else {
                    if (mSurfaceView != null) {
                        sFLayout.removeView(mSurfaceView);
                    }
                    InflaterTextureView();

                    playVideo(path);
                }
            }
        }

        @Override
        public void onVideoSort() {
            if (_importEditor.isTaskCancel()) {
                return;
            }

            ((ImportVideoFragment.VideoListener) mFragment.getActivity()).onSortStart(mFragment);

            if (textureview.getSurfaceTexture() != null) {
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                }
                playVideo(_importEditor.getCurrentPath());
            }
        }
    };

    private void playVideo(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                deleteCurrentItem(filePath);

                return;
            }
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.e("ImportVideo", "mSurface = " + mSurface);
            mPlayer.setSurface(mSurface);
            mPlayer.setLooping(true);
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnVideoSizeChangedListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCurrentItem(String path) {
        deleteCurrentVideo();
        FileUtils.scanFile(mFragment.getActivity(), new File(path));

        showUser(R.string.qupai_no_video_file);
    }

    private void showUser(int text_id) {
        Toast toast = Toast.makeText(mFragment.getActivity(),
                mFragment.getResources().getText(text_id), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();
    }

    private void deleteCurrentVideo() {
        VideoInfoBean bean = _importEditor.getCurrentList();
        if (bean == null) {
            return;
        }

        _importEditor.removeVideo(bean);

        if (_importEditor.getCurrentList() == null) {
            _importEditor.setCurrentPath(null);
            _importEditor.setCurrentDuration(-1);

            systemCameraLayout.setVisibility(View.GONE);
            _DeleteButton.setVisibility(View.GONE);

            textureview.setVisibility(View.GONE);
            layout_no_video_data.setVisibility(View.VISIBLE);

            if (mPlayer != null) {
                mPlayer.stop();
            }

            return;
        }

        sFLayout.removeView(mSurfaceView);

        InflaterTextureView();

        if (mPlayer != null) {
            playVideo(_importEditor.getCurrentPath());
        }
    }

    private TextureView.SurfaceTextureListener _surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                                int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.setSurface(null);
                mPlayer.release();
                mPlayer = null;
            }

            if (mSurface != null) {
                Log.e("123", "onSurfaceTextureDestroyed");
                mSurface.release();
                mSurface = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                              int height) {
            Log.e("ImportVideo", "onSurfaceTextureAvailable");
            mSurface = new Surface(surface);

            if (_importEditor.getCurrentPath() != null) {
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                }
                playVideo(_importEditor.getCurrentPath());
            }
        }
    };

    @Override
    public long getLastModifiedTimestamp() {
        return getVideoLastModifiedTimestamp();
    }

    private long getVideoLastModifiedTimestamp() {
        VideoInfoBean bean = _importEditor.getCurrentList();
        if (bean == null) {
            return -1;
        }

        return new File(bean.getFilePath()).lastModified();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {

        }
        return false;
    }

    public void dispatchTouchEvent() {
        if (toastLayout != null) {
            toastLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    private static final String IMPORT_FIRST_VALUE = "com.duanqu.qupai.ImportVideoFragment_FirstGuide";
    private void setToastLayout() {
        SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
        boolean isToastFirstIn = preferences1.getBoolean(IMPORT_FIRST_VALUE, true);

        if (isToastFirstIn && _importEditor.getCurrentList() != null) {
            preferences1.edit().putBoolean(IMPORT_FIRST_VALUE, false).commit();
            toastLayout.setVisibility(View.VISIBLE);
            toastLayout.removeAllViews();
            View startView = FontUtil.applyFontByInflate(
                    mFragment.getActivity(), R.layout.toast_qupai_import_duration_limit, null, false);
            toastLayout.addView(startView);
        }

        if (_importEditor.getCurrentList() != null) {
            _DeleteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setToastLayout();

        int video_width = 0;
        int video_height = 0;
        if (mp != null) {
            video_width = mp.getVideoWidth();
            video_height = mp.getVideoHeight();
        }

        int screenWidth = sFLayout.getWidth();
        int screenHeight = sFLayout.getHeight();

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        if (video_height > video_width && video_width >= screenWidth) {
            lp.gravity = Gravity.CENTER;
            lp.width = screenWidth * 2 / 3;
            lp.height = video_height * lp.width / video_width;
            lp.setMargins(0, 0, 0, 0);
        } else if (video_height < video_width) {
            lp.gravity = Gravity.CENTER;
            float scale_x = screenWidth / (float) video_width;
            float scale_y = screenHeight / (float) video_height;

            float scale = scale_x > scale_y ? scale_y : scale_x;

            lp.gravity = Gravity.CENTER;
            lp.width = (int) (video_width * scale);
            lp.height = (int) (video_height * scale);
            lp.setMargins(0, 0, 0, 0);
        } else if (video_width == video_height) {
            lp.gravity = Gravity.CENTER;
            lp.width = screenWidth > screenHeight ? screenHeight : screenWidth;
            lp.height = screenWidth > screenHeight ? screenHeight : screenWidth;
            lp.setMargins(0, 0, 0, 0);
        } else if (video_width < screenWidth && video_height < screenHeight) {
            float scale_x = screenWidth / (float) video_width;
            float scale_y = screenHeight / (float) video_height;
            float scale = scale_x > scale_y ? scale_y : scale_x;

            lp.gravity = Gravity.CENTER;
            lp.width = (int) (video_width * scale);
            lp.height = (int) (video_height * scale);
            lp.setMargins(0, 0, 0, 0);
        } else {
            lp.gravity = Gravity.CENTER;
            lp.width = video_width;
            lp.height = video_height;
            lp.setMargins(0, 0, 0, 0);
        }

        int sysPadding = mFragment.getResources().getDimensionPixelOffset(R.dimen.qupai_sys_camera_margin);

        if (toastLayout.getVisibility() == View.VISIBLE) {
            setToastGravity(mp, screenHeight, lp.height, sysPadding);
        }

        FrameLayout.LayoutParams delParam = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        delParam.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        if (video_width == video_height) {
            delParam.rightMargin = sysPadding;
            delParam.bottomMargin = (screenHeight - lp.height) / 2 + sysPadding;

            systemCameraLayout.setVisibility(View.GONE);
        } else {
            FrameLayout.LayoutParams sysParam = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            sysParam.gravity = Gravity.BOTTOM;

            if (video_height > video_width) {
                sysParam.bottomMargin = sysPadding;
                sysParam.leftMargin = (screenWidth - lp.width) / 2 + sysPadding;

                delParam.bottomMargin = sysPadding;
                delParam.rightMargin = (screenWidth - lp.width) / 2 + sysPadding;
            } else {
                sysParam.leftMargin = sysPadding;
                sysParam.bottomMargin = (screenHeight - lp.height) / 2 + sysPadding;

                delParam.rightMargin = sysPadding;
                delParam.bottomMargin = (screenHeight - lp.height) / 2 + sysPadding;
            }

            systemCameraLayout.setLayoutParams(sysParam);
            systemCameraLayout.setVisibility(View.VISIBLE);
        }

        _DeleteButton.setLayoutParams(delParam);

        if (android.os.Build.MODEL.equals("M045")
                || android.os.Build.MODEL.equals("MT887")
                || android.os.Build.MODEL.equals("MI 2")) {
            Matrix transform = new Matrix();
            textureview.getTransform(transform);
            transform.reset();

            transform.setScale(1, 1);
            textureview.setTransform(transform);
        }

        textureview.setLayoutParams(lp);
    }

    private void setToastGravity(MediaPlayer mp, int screenHeight, int layoutHeight, int padding) {
        if (mp == null) {
            return;
        }

        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();

        FrameLayout.LayoutParams toastParam = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        toastParam.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        if (videoHeight > videoWidth) {
            toastParam.bottomMargin = padding;
        } else {
            toastParam.bottomMargin = (screenHeight - layoutHeight) / 2 + padding;
        }

        toastLayout.setLayoutParams(toastParam);
    }
}
