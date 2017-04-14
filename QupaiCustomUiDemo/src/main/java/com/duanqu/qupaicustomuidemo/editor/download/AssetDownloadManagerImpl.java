package com.duanqu.qupaicustomuidemo.editor.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.dagger.PerFragment;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.AssetItemViewMediator;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

@PerFragment
public class AssetDownloadManagerImpl {

    private static final String TAG = "Download";

    public static final class DownloadTask extends DownloadMusic {

        private final AssetItemViewMediator _Target;
        private final CircleProgressBar _progress_bar;
        private final VideoEditBean _bean;
        private DownloadStateListener _Listener;

        private final Context _Context;

        DownloadTask(AssetItemViewMediator target, URL url, File package_dir, AssetRepository repo) {
            super(url, package_dir, (VideoEditBean) target.getValue(), repo);
            _Target = target;
            _progress_bar = target.getProgressBar();
            _bean = (VideoEditBean) target.getValue();

            _Context = target.getContext().getApplicationContext();
        }

        public void setListener(DownloadStateListener listener) {
            _Listener = listener;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (_progress_bar == null) {
                return;
            }

            if (values.length > 1) {
                int fileLength = values[1];
                if (fileLength == -1) {
                    _progress_bar.setProgress(100/values.length);
                } else {
                    _progress_bar.setProgress(20);
                }
            } else {
                _progress_bar.setProgress(values[0].intValue());
            }
        }

        private void onDownloadFinish() {
            _Target.setDownloading(false, _Context);
            _bean._DownLoading = false;

            if(_Listener != null) {
                _Listener.getDownloading(_bean._DownLoading);
            }
        }

        @Override
        protected void onDownloadSuccess() {
            onDownloadFinish();

            _Target.setDownloadMask(false);
            _Target.setDownloadable(false);

            _Target.setTitle(_bean.getTitle());

            _bean.isLocal = true;
            _bean.isShow = false;
            _bean.isAutoDownload = false;
            _Target.setShowNewIndicator(false);

            SharedPreferences sp = _Context.getSharedPreferences("AppGlobalSetting", 0);
            boolean first;

            if(_bean.getType() == VideoEditBean.TYPE_MUSIC) {
                first = sp.getBoolean("first_download_completed_edit_tip_music", true);
            }else {
                first = sp.getBoolean("first_download_completed_edit_tip_imv", true);
            }

            if(first) {
                ToastUtil.showToast(_Context, R.string.qupai_paster_download_first_success);

                if(_bean.getType() == VideoEditBean.TYPE_MUSIC) {
                    sp.edit().putBoolean("first_download_completed_edit_tip_music", false).commit();
                }else {
                    sp.edit().putBoolean("first_download_completed_edit_tip_imv", false).commit();
                }
            }else {
                ToastUtil.showToast(_Context, R.string.qupai_paster_download_success);
            }
        }

        @Override
        protected void onDownloadFailure() {
            onDownloadFinish();

            _Target.setDownloadMask(false);
            _Target.setDownloadable(true);
            _bean.isAutoDownload = false;
            ToastUtil.showToast(_Context, R.string.qupai_download_failed_goon);
        }

    }

    public interface DownloadStateListener {
        void getDownloading(boolean isDownloading);
    }

    private boolean _IsDownloading;

    public boolean isDownloading() {
        return _IsDownloading;
    }

    private final AssetRepository _Repo;

    @Inject
    public AssetDownloadManagerImpl(AssetRepository repo) {
        _Repo = repo;
    }

    private static File getAssetPackageDir(Context context, AssetInfo data)
            throws FileNotFoundException, IllegalArgumentException {

        File asset_root_dir = getResourcesUnzipPath(context);

        if (asset_root_dir == null) {
            throw new FileNotFoundException();
        }

        String prefix;
        switch (data.getType()) {
            case AssetInfo.TYPE_MUSIC:
                prefix = "Shop_Music_";
                break;
            case AssetInfo.TYPE_SHADER_MV:
                prefix = "Shop_MV_";
                break;
            case AssetInfo.TYPE_DIYOVERLAY:
                prefix = "Shop_DIY_";
                break;
            case AssetInfo.TYPE_FONT:
                prefix = "Shop_Font_";
                break;
            default:
                throw new IllegalArgumentException("invalid asset type: " + data.getType());
        }

        return new File(asset_root_dir, prefix + String.valueOf(data.getID()));
    }

    public DownloadMusic newInstance(AssetItemViewMediator target) {

        final AssetInfo asset = target.getValue();

        VideoEditBean bean = (VideoEditBean) asset;

        URL url;
        try {
            url = new URL(bean.resourceUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "invalid asset download url", e);
            return null;
        }

        bean._DownLoading = true;
        _IsDownloading = bean._DownLoading;

        File asset_package_dir;
        try {
            asset_package_dir = getAssetPackageDir(target.getContext(), bean);
        } catch (FileNotFoundException | IllegalArgumentException e) {
            Log.e(TAG, "unable to find asset package dir", e);
            return null;
        }

        DownloadTask task = new DownloadTask(target, url, asset_package_dir, _Repo);
        task.setListener(new DownloadStateListener() {
            @Override
            public void getDownloading(boolean isDownloading) {
                _IsDownloading = isDownloading;
            }
        });

        return task;
    }

    public static File getResourcesUnzipPath(Context context) {
        File path = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (path != null && path.exists()) {
            return path;
        }
        return null;
    }

}
