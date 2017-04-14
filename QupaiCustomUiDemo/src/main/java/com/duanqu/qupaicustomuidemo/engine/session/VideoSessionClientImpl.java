package com.duanqu.qupaicustomuidemo.engine.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.engine.session.PageNavigator;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.license.LicenseImpl;
import com.duanqu.qupai.license.LicenseInterface;
import com.duanqu.qupai.minisdk.record.RecordWorkspace;
import com.duanqu.qupai.project.WorkspaceClient;
import com.duanqu.qupaicustomuidemo.editor.music.MusicDownloadActivity;
import com.duanqu.qupaicustomuidemo.editor.mv.DownLoadCaptionActivity;
import com.duanqu.qupaicustomuidemo.editor.mv.DownLoadFontActivity;
import com.duanqu.qupaicustomuidemo.editor.mv.DownLoadPasterActivity;
import com.duanqu.qupaicustomuidemo.editor.mv.IMVDownloadActivity;
import com.duanqu.qupaicustomuidemo.provider.PackageAssetRepository;
import com.duanqu.qupaicustomuidemo.provider.ProviderUris;

public class VideoSessionClientImpl extends VideoSessionClient {
    private final AssetRepository _AssetRepo;
    private Context context;
    private final JSONSupport _JSON;
    private ProjectOptions _ProjectOptions;
    private final ProviderUris _Uris;

    public VideoSessionClientImpl(Context app) {
        super(app);
        context = app;
        _Uris = new ProviderUris(context);
        _JSON = new JSONSupportImpl();
        _AssetRepo = new PackageAssetRepository(context,_Uris,_JSON);
    }

    @Override
    public AssetRepository getAssetRepository() {
        return  _AssetRepo;
    }

    @Override
    public JSONSupport getJSONSupport() {
        return _JSON;
    }

    @Override
    public ProjectOptions getProjectOptions() {
        return _ProjectOptions;
    }

    public void setProjectOptions(ProjectOptions _ProjectOptions) {
        this._ProjectOptions = _ProjectOptions;
    }

    private VideoSessionCreateInfo _CreateInfo;

    public void setCreateInfo(VideoSessionCreateInfo info) {
        _CreateInfo = info;
    }

    @Override
    public VideoSessionCreateInfo getCreateInfo() {
        return _CreateInfo;
    }

    @Override
    public WorkspaceClient createWorkspace(Context context) {
//        return new SimpleWorkspace(context, context.getString(R.string.qupai_simple_workspace_dir),
//                _JSON, _ProjectOptions);

        return new RecordWorkspace(context.getExternalFilesDir(null) + "/project");
    }

    private final PageNavigator _PageNavigator
            = new PageNavigator() {

        {
            addPage(PAGE_DOWNLOAD_MUSIC, PAGE_DOWNLOAD_FONT, PAGE_DOWNLOAD_MV, PAGE_DOWNLOAD_PASTER, PAGE_DOWNLOAD_CAPTION);
        }

        @Override
        public void openPage(int page,int rotation, Activity activity, int request_code) {
            Class<? extends Activity> activity_class;
            switch (page) {
                case PAGE_DOWNLOAD_MUSIC: activity_class = MusicDownloadActivity.class; break;
                case PAGE_DOWNLOAD_MV: activity_class = IMVDownloadActivity.class; break;
                case PAGE_DOWNLOAD_FONT: activity_class = DownLoadFontActivity.class; break;
                case PAGE_DOWNLOAD_PASTER: activity_class = DownLoadPasterActivity.class; break;
                case PAGE_DOWNLOAD_CAPTION: activity_class = DownLoadCaptionActivity.class; break;
                default: return;
            }
            Intent intent = new Intent(activity, activity_class);
            intent.putExtra("rotation",rotation);//传递rotation是为了旋转素材的选择
            activity.startActivityForResult(intent, request_code);
        }
    };

    @Override
    public PageNavigator getPageNavigator() {
        return _PageNavigator;
    }
}
