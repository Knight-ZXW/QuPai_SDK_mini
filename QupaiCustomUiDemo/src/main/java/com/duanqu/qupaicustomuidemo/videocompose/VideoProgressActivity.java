package com.duanqu.qupaicustomuidemo.videocompose;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.bean.PhotoModule;
import com.duanqu.qupai.compose.ComposeCallback;
import com.duanqu.qupai.compose.QupaiComposeTask;
import com.duanqu.qupai.compose.QupaiComposeTaskImpl;
import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.photo.PhotoComposeTask;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaisdk.tools.StorageUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 照片合成视频
 */
public class VideoProgressActivity extends Activity implements ComposeCallback {


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

    Request request;
    QupaiComposeTask qupaiCompose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = PageRequest.from(this);

        setContentView(R.layout.activity_qupai_render_progress);

        _RenderProgress = (ProgressBar) findViewById(R.id.renderProgress);
        _RenderText = (TextView) findViewById(R.id.renderText);
        outPath = StorageUtils.getCacheDirectory(this) + "/out.mp4";
        qupaiCompose = new QupaiComposeTaskImpl(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        enableExportThumComposeTask(request.getPhotoList());
        QupaiComposeTask.ReturnCode ret = qupaiCompose.start(width, height, outPath);
        if (ret == QupaiComposeTask.ReturnCode.SUCCESS){
            Toast.makeText(VideoProgressActivity.this, "Start Compose", Toast.LENGTH_LONG).show();
            qupaiCompose.setComposeCallback(this);
        } else{
            if(ret == QupaiComposeTask.ReturnCode.ERROR_LICENSE_SERVICE_NEEDBUY){
                showAuthDialogFragment(VideoProgressActivity.this,getResources().getString(R.string.qupai_license_needbug));
            }else
                showAuthDialogFragment(VideoProgressActivity.this,"Compose Failed:" + ret);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        qupaiCompose.cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    int width = 360;
    int height = 640;

    String outPath;

    private void enableExportThumComposeTask(List<PhotoModule> resultList) {
        String[] imputpaths = new String[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            imputpaths[i] = resultList.get(i).getVideoPath();
        }

        qupaiCompose.addInputPaths(imputpaths);

    }

    @Override
    public void onProgress(int progress) {
        Log.e("QupaiComposeTaskImpl", "progress" + progress);
        _RenderText.setText(progress + "%");
        _RenderProgress.setProgress(progress);
    }

    @Override
    public void onError(int code) {
        Log.e("QupaiComposeTaskImpl", "onError" + code);
        Toast.makeText(VideoProgressActivity.this, "onError:" + code, Toast.LENGTH_LONG).show();
        finish();

        //生成成功了视频的.

    }

    @Override
    public void onComplete(long duration) {
        Log.e("QupaiComposeTaskImpl", "onComplete" +  + duration / 1000 / 1000 + "s");
        Toast.makeText(VideoProgressActivity.this, "Success:" + duration / 1000 / 1000 + "s", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onPartComplete(int index) {
        //一段视频成功
        Log.e("QupaiComposeTaskImpl", "onPartComplete" + index);
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
