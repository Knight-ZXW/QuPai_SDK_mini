package com.duanqu.qupaicustomuidemo.trim;

import android.content.DialogInterface;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.duanqu.qupai.dialog.ProgressDialogFragment;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.importsdk.QupaiImportListener;
import com.duanqu.qupai.importsdk.QupaiImportTask;
import com.duanqu.qupai.project.Clip;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.project.ProjectConnection;
import com.duanqu.qupai.project.ProjectConnection.OnChangeListener;
import com.duanqu.qupai.project.UIMode;
import com.duanqu.qupai.project.WorkspaceClient;
import com.duanqu.qupai.trim.ImportTask;
import com.duanqu.qupai.widget.android.app.ProgressDialog;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.EditorActivity;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;

public class ImportProgressDialogFragment extends ProgressDialogFragment
         {

    private static final String TAG = "ImportProgressDFragment";

    public static class Builder extends ProgressDialogFragment.Builder {

        private static final String KEY_TRIM = "TRIM";

        private static final String KEY_CONTENT_RECT = "CONTENT_RECT";

        private static final String KEY_INPUT_PATH = "INPUT_PATH";

        public Builder() {
            setMax(100);
            setMessage(R.string.qupai_transcode_in_progress);
            setProgressStyle(ProgressDialog.STYLE_SPINNER);
            setAnimatedRotateContent(R.drawable.progress_recorder_qupai_content);
        }

        public Builder setTrim(long from, long to) {
            _Bundle.putLongArray(KEY_TRIM, new long[]{from, to});
            return this;
        }

        public static long[] getTrim(Bundle args) {
            return args.getLongArray(KEY_TRIM);
        }

        public Builder setContentRect(int left, int top, int right, int bottom) {
            _Bundle.putIntArray(KEY_CONTENT_RECT, new int[]{left, top, right, bottom});
            return this;
        }

        public static int[] getContentRect(Bundle args) {
            return args.getIntArray(KEY_CONTENT_RECT);
        }

        public Builder setInputPath(String input_path) {
            _Bundle.putString(KEY_INPUT_PATH, input_path);
            return this;
        }

        public static String getInputPath(Bundle args) {
            return args.getString(KEY_INPUT_PATH);
        }

        @Override
        protected ImportProgressDialogFragment newInstance() {
            return new ImportProgressDialogFragment();
        }

    }

    private String outPutPath;

    private ProjectConnection _ClipManager;

    private static int task_count = 0;
    private WorkspaceClient _PMClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionPageRequest request = SessionPageRequest.from(getActivity());

        VideoSessionClient vs_client = request.getVideoSessionClient(getActivity());

        _PMClient = vs_client.createWorkspace(getActivity());

        //创建一个ProjectConnection 用于保存project到本地
        _ClipManager = new ProjectConnection(_PMClient);

        _ClipManager.addOnChangeListener(new OnChangeListener() {
            @Override
            public void onChange(ProjectConnection pc, Project project) {

                if (project == null) {
//                    requestTaskStart();
                    requestTrimTaskStart();
                }
            }
        });

        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Recorder);
    }

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        requestTrimTaskStart();
    }

    @Override
    public void onPause() {
        Log.d("qupai","dialog framegment pause");
        requestTrimTaskStop(true);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoTrimActivity activity = (VideoTrimActivity) getActivity();
        if (activity != null) {
            activity.onImportDialogDismiss();
        }
    }

    QupaiImportTask task;

    private void requestTrimTaskStart(){
        if (!isResumed()) {
            return;
        }

        if (_ClipManager.getProjectUri() != null) {
            _ClipManager.removeProject();
            return;
        }

        Bundle args = getArguments();

        //得到输入文件地址
        String input = Builder.getInputPath(args);
        Log.e("InputPath","InputPath" + input);
        //得到裁剪时间参数
        long[] trim = Builder.getTrim(args);
        //得到裁剪位置矩阵
        int[] content_rect = Builder.getContentRect(args);

        //宽=left-right
        //高=bottom-top
        int width = content_rect[2] - content_rect[0];
        int height = content_rect[3] - content_rect[1];

        //分辨率需要偶数
        width = (width % 2 == 1) ? width - 1 : width;
        height = (height % 2 == 1) ? height - 1 : height;

        _ClipManager.createNewProject(Project.TYPE_VIDEO,width,height);
        Log.e(TAG, "task count " + (++task_count));
        outPutPath = _ClipManager.newFilename(".mp4");

        task = new QupaiImportTask(getActivity());
        task.setInputPath(input);
        task.setOutputPath(outPutPath);
        task.setFps(25);
        task.setGop(25 * 10);
        task.setBps(600);
        //旋转
        int angle = task.getRotation();
        int rotateWidth = 0;//旋转之后的长宽
        int rotateHeight = 0;
        int [] size = task.getSize();
        task.setRotation(angle);
        long startms = trim[0];
        long endms = trim[1];
        task.setTrimPoint(startms,endms);//0 , 14000
        if(angle == 90|| angle == 270)//确定旋转之后的长宽
        {
            rotateHeight = size[0];
            rotateWidth = size[1];
        }
        else
        {
            rotateHeight = size[1];
            rotateWidth = size[0];
        }
        //缩放 裁剪
        float ratioW = (float)rotateWidth/(float)width;
        float ratioH = (float)rotateHeight/(float)height;
        float ratio = ratioW > ratioH?ratioH:ratioW;
        if(rotateWidth > width && rotateHeight > height) {//根据模式选择设定缩放和裁剪参数
            if(true)//isScaleCrop
            {

                int scaleWidth = (int)(rotateWidth/ratio);
                int scaleHeight = (int)(rotateHeight/ratio);
                if(scaleHeight%4!=0)
                    scaleHeight = scaleHeight/4*4+4;
                if(scaleWidth%4!=0)
                    scaleWidth = scaleWidth/4*4+4;
                task.setScale(scaleWidth, scaleHeight);
                int cropx = ((scaleWidth - width)/2 )%2 ==1 ?(scaleWidth - width)/2 -1 : (scaleWidth - width)/2;
                int cropy = ((scaleHeight-height)/2 )%2 ==1 ?(scaleHeight-height)/2 -1 : (scaleHeight-height)/2;
                task.setCropPoint(cropx,cropy);
            }
            else
            {
                int cropx = ((rotateWidth - width)/2 )%2 ==1 ?(rotateWidth - width)/2 -1 : (rotateWidth - width)/2;
                int cropy = ((rotateHeight-height)/2 )%2 ==1 ?(rotateHeight-height)/2 -1 : (rotateHeight-height)/2;
                task.setScale(rotateWidth, rotateHeight);
                task.setCropPoint(cropx,cropy);
            }

        }else//必须要缩放，所以无视模式选择
        {
            int scaleWidth = (int)(rotateWidth/ratio);
            int scaleHeight = (int)(rotateHeight/ratio);
            if(scaleHeight%4!=0)
                scaleHeight = scaleHeight/4*4+4;
            if(scaleWidth%4!=0)
                scaleWidth = scaleWidth/4*4+4;
            task.setScale(scaleWidth, scaleHeight);
            int cropx = ((scaleWidth - width)/2 )%2 ==1 ?(scaleWidth - width)/2 -1 : (scaleWidth - width)/2;
            int cropy = ((scaleHeight-height)/2 )%2 ==1 ?(scaleHeight-height)/2 -1 : (scaleHeight-height)/2;
            task.setCropPoint(cropx,cropy);
        }
        task.setSize(width,height);
        task.setListener(new QupaiImportListener() {
            @Override
            public void onCompletion() {
                requestTrimTaskStop(false);
                dismiss();
            }

            @Override
            public void onProgress(int percent) {
                setProgress((int) percent);
            }

            @Override
            public void onError(QupaiImportTask.ReturnCode code) {
                ToastUtil.showToast(getActivity(),code.toString());
                dismiss();
            }

        });
        QupaiImportTask.ReturnCode retrunCode = task.start();
        if(retrunCode == QupaiImportTask.ReturnCode.ERROR_LICENSE_SERVICE_NEEDBUY){
            ToastUtil.showToast(getActivity(),getString(R.string.qupai_license_needbug));
            dismiss();
            return;
        }
        if(retrunCode.ordinal() > QupaiImportTask.ReturnCode.WARNING_UNKNOWN.ordinal()) {
            ToastUtil.showToast(getActivity(),retrunCode.toString());
            dismiss();
            return;
        }
    }

    private void requestTrimTaskStop(boolean cancel){
        if(task ==null){
            return;
        }
        if(cancel){
            task.cancel();
        }

        if (cancel) {
            return;
        }

        int angle = 0;

        Clip bean = new Clip();
        bean.setPath(outPutPath);
        //判断旋转角度传递一个矩阵
        Matrix m = new Matrix();
        if (angle == 90 || angle == 270) {
            bean.width = _ClipManager.getProject().getCanvasHeight();
            bean.height = _ClipManager.getProject().getCanvasWidth();
            m.postRotate(angle, bean.width / 2, bean.height / 2);
            float tx = (float) (bean.height - bean.width) / 2;
            m.postTranslate(tx, -tx);
        } else {
            bean.width = _ClipManager.getProject().getCanvasWidth();
            bean.height = _ClipManager.getProject().getCanvasHeight();
            m.postRotate(angle, bean.width / 2, bean.height / 2);
        }

        Bundle args = getArguments();

        //得到裁剪时间参数
        long[] trim = Builder.getTrim(args);

        bean.setDurationMilli(trim[1] - trim[0]);
        bean.setDisplayMatrix(m);

        _ClipManager.addClip(bean);
        _ClipManager.saveProject(UIMode.EDITOR);

        new EditorActivity.Request(new VideoSessionClientFactoryImpl(), null)
                .setProjectUri(_ClipManager.getProject().getUri())
                .startForResult(getActivity(), RenderRequest.RENDER_MODE_EXPORT_VIDEO);

        getActivity().finish();
//        new RecordActivity.Request(new VideoSessionClientFactoryImpl(), null)
//                .setProjectUri(_ClipManager.getProject().getUri())
//                .startForResult(getActivity(), RenderRequest.RENDER_MODE_EXPORT_VIDEO);
    }
}
