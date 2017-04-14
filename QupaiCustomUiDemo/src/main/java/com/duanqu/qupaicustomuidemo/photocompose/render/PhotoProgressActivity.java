package com.duanqu.qupaicustomuidemo.photocompose.render;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.license.LicenseMessage;
import com.duanqu.qupai.photo.PhotoComposeCallback;
import com.duanqu.qupai.photo.PhotoComposeTask;
import com.duanqu.qupai.photo.PhotoComposeTaskImpl;
import com.duanqu.qupai.project.Clip;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.project.ProjectConnection;
import com.duanqu.qupai.project.UIMode;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.EditorActivity;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupai.effect.SimpleWorkspace;

import java.io.Serializable;

/**
 * 照片合成视频
 */
public class PhotoProgressActivity extends Activity implements PhotoComposeCallback {


    public static class Request extends RenderRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

        public Request(SessionClientFactory factory, Serializable data) {
            super(factory, data);
        }

        @Override
        public SessionPageRequest setFactory(SessionClientFactory factory) {
            return super.setFactory(factory);
        }

        @Override
        protected void marshall(Intent intent) {
            super.marshall(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }

        @Override
        protected void unmarshall(Intent intent) {
            super.unmarshall(intent);
        }

    }

    private ProgressBar _RenderProgress;
    private TextView _RenderText;

    private ProjectConnection _ClipManager;
    Request request;
    VideoSessionClient videoSessionClient;
    VideoSessionCreateInfo videoSessionCreateInfo;

    PhotoComposeTask photoComposeTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = PageRequest.from(this);
        videoSessionClient = request.getVideoSessionClient(this);
        videoSessionCreateInfo = videoSessionClient.getCreateInfo();

        setContentView(R.layout.activity_qupai_render_progress);

        _RenderProgress = (ProgressBar) findViewById(R.id.renderProgress);
        _RenderText = (TextView) findViewById(R.id.renderText);

        _ClipManager = new ProjectConnection(new SimpleWorkspace(this,
                getString(R.string.qupai_simple_workspace_dir),
                videoSessionClient.getJSONSupport(), videoSessionClient.getProjectOptions()));
        _ClipManager.createNewProject(Project.TYPE_VIDEO, videoSessionClient.getProjectOptions().videoWidth,
                videoSessionClient.getProjectOptions().videoHeight);

        photoComposeTask = new PhotoComposeTaskImpl(this,videoSessionClient);
        outPutPath = _ClipManager.newFilename(".mov");
        photoComposeTask.setPhotoComposeCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        photoComposeTask.addInputPaths(request.photoList,videoSessionClient.getProjectOptions().videoWidth,
                videoSessionClient.getProjectOptions().videoHeight,outPutPath);
        PhotoComposeTask.ReturnCode returnCode= photoComposeTask.start();
        if(returnCode != PhotoComposeTask.ReturnCode.SUCCESS){
            if(returnCode == PhotoComposeTask.ReturnCode.ERROR_LICENSE_SERVICE_NEEDBUY){
                showAuthDialogFragment(this,getString(R.string.qupai_license_needbug));
            }else {
                showAuthDialogFragment(this,"LicenseCheck failed" + returnCode);
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        photoComposeTask.stop();
    }

    private String outPutPath;

    @Override
    public void onProgress(int progress) {
        _RenderText.setText(progress + "%");
        _RenderProgress.setProgress(progress);
    }

    @Override
    public void onComplete(long duration) {
        Clip bean = new Clip();
        bean.setPath(outPutPath);
        bean.setDurationMilli(request.getPhotoList().size() * 3000);
        bean.width = request.getVideoSessionClient(this).getProjectOptions().videoWidth;
        bean.height = request.getVideoSessionClient(this).getProjectOptions().videoHeight;
        _ClipManager.addClip(bean);
        _ClipManager.saveProject(UIMode.EDITOR);

        new EditorActivity.Request(new VideoSessionClientFactoryImpl(), null)
                .setProjectUri(_ClipManager.getProject().getUri())
                .startForResult(PhotoProgressActivity.this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);

        finish();
    }

    private void showAuthDialogFragment(Context context, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setMessage(message)
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
