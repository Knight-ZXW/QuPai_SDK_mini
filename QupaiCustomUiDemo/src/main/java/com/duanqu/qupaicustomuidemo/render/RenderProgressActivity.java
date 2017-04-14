package com.duanqu.qupaicustomuidemo.render;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.license.LicenseMessage;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.project.ProjectUtil;
import com.duanqu.qupai.render.RenderConf;
import com.duanqu.qupai.render.RenderConfImpl;
import com.duanqu.qupai.render.RenderTaskManager;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.EditorActivity;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class RenderProgressActivity extends Activity implements View.OnClickListener, RenderTaskManager.OnRenderTaskListener {

    public static class Request extends RenderRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

        @Override
        protected void marshall(Intent intent) {
            super.marshall(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }

    }

    private ProgressBar _RenderProgress;
    private TextView _RenderText;
    private Button mBtnGoEditor;
    private Project mProject;
    private RenderConf mRenderConf;

    Request request;
    VideoSessionClient videoSessionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_qupai_render_progress);

        _RenderProgress = (ProgressBar) findViewById(R.id.renderProgress);
        _RenderText = (TextView) findViewById(R.id.renderText);
        mBtnGoEditor = (Button) findViewById(R.id.btn_go_editor);
        mBtnGoEditor.setOnClickListener(this);

        initVideoSesionCreateInfo();
    }

    private void initVideoSesionCreateInfo() {
        request = PageRequest.from(this);
        videoSessionClient = request.getVideoSessionClient(this);

        //intent传递过来的project,这里是关键，录制完成之后save为一个json文件,这里读取json文件，并且设置project的值
        File projectFile = new File(request.getProjectUri().getPath());
        mProject = ProjectUtil.readProject(projectFile, videoSessionClient.getJSONSupport());
        if (mProject != null) {
            mProject.setProjectDir(projectFile.getParentFile(), projectFile);
        }

        if (mProject.getCanvasHeight() == 0 || mProject.getCanvasWidth() == 0) {
            mProject.setCanvasSize(videoSessionClient.getProjectOptions().videoWidth, videoSessionClient.getProjectOptions().videoHeight);
        }

        mRenderConf = new RenderConfImpl(this, videoSessionClient,mProject);
        //最大时长
        mRenderConf.setDurationLimit(TimeUnit.NANOSECONDS.toMillis(mProject.getDurationNano()));
        //帧率
        mRenderConf.setVideoFrameRate(videoSessionClient.getProjectOptions().videoFrameRate);
        mRenderConf.setOnRenderTaskListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //初始化完成之后组装需要合成的数据
        mRenderConf.enableExportThumbnailTask(0,20);
        mRenderConf.enableExportTask(20,80);
        mRenderConf.renderStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRenderConf.renderStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_editor:
                Intent intent = new Intent(this, EditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("project_file", mProject.getProjectFile().getAbsolutePath());
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRenderTaskError(LicenseMessage licenseMessage) {
        showAuthDialogFragment(this, licenseMessage);
    }

    @Override
    public void onRenderTaskProgress(int progress) {
        _RenderText.setText(progress + "%");
        Log.e("renderprogress","progress "+progress);
        _RenderProgress.setProgress(progress);
    }

    @Override
    public void onRenderTaskCompletion(long elapsed_time) {
        mProject.getDurationNano();//时长
        mRenderConf.getRenderOutputFilePath();//文件路径
        mRenderConf.getExportThumbnailPath();//缩略图

        int thumbnail_count = request.getVideoSessionClient(this).getCreateInfo().getThumbnailExportOptions().count;
        String[] thumb_list = null;
        if (mRenderConf.getExportThumbnailPath() != null) {
            thumb_list = new String[thumbnail_count];
            for (int i = 0; i < thumb_list.length; i++) {
                thumb_list[i] = String.format(mRenderConf.getExportThumbnailPath(), i + 1);
            }
        }

        finish();
    }



    @Override
    public void onBackPressed() {
        mRenderConf.renderStop();

        super.onBackPressed();
    }

    private void showAuthDialogFragment(Context context, LicenseMessage licenseMessage) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setMessage(licenseMessage.getMessage())
                .setNegativeButton(R.string.qupai_dlg_button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setCancelable(false);
        dialog.show();

    }

}
