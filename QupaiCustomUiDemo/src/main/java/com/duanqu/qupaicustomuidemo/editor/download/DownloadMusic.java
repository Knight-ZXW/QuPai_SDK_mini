package com.duanqu.qupaicustomuidemo.editor.download;

import android.os.AsyncTask;
import android.util.Log;

import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupaicustomuidemo.utils.ProgressReportingInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class DownloadMusic extends AsyncTask<Void, Integer, Boolean>
implements ProgressReportingInputStream.OnProgressListener {

    public interface Callback {
        void onProgress();
        void onComplete();
    }

    private static final String TAG = "DownloadMusicTask";

    private static final int FILE_MAX_LENGTH = 10 * 1024 * 1024;

    private final URL mUrl;
    private final File mPackageDir;

    protected File getAssetPackageDir() { return mPackageDir; }

    private final VideoEditBean _Info;
    private final AssetRepository _Repo;

    public DownloadMusic(URL url, File package_dir, VideoEditBean veb, AssetRepository repo) {
        mPackageDir = package_dir;
        mUrl = url;
        _Info = veb;
        _Repo = repo;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        URLConnection connection;
        try {
            connection = mUrl.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "download failed", e);
            return false;
        }


        int length = connection.getContentLength();

        if (length <= 0) {
            publishProgress(0, FILE_MAX_LENGTH);
        } else {
            publishProgress(0, length);
        }

        InputStream stream;
        try {
            stream = connection.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "download failed", e);
            return false;
        }

        InputStream progress_stream = new ProgressReportingInputStream(stream, this);

        AssetPackageStreamExtractor xtr = new AssetPackageStreamExtractor(progress_stream, mPackageDir);

        try {
            while (xtr.extractNext()) {

            }
        } catch (IOException e) {
            Log.e(TAG, "download failed", e);
            return false;
        } finally {
            try {
                xtr.close();
            } catch (IOException e) {
            }
        }

        return true;
    }

    @Override
    public void onProgress(long byte_count) { publishProgress((int) byte_count); }

    @Override
    final protected void onPostExecute(Boolean result) {

        Log.d("onPostExecute" , "result: " + result );
        if (result && addAsset()) {
            onDownloadSuccess();
        } else {
            onDownloadFailure();
        }
    }

    protected void onDownloadSuccess() { }

    protected void onDownloadFailure() { }

    private boolean addAsset() {
        Log.d("onPostExecute" , "dir: " + getAssetPackageDir() );
        File dir = getAssetPackageDir();
        if (!dir.isDirectory()) {
            return false;
        }

        _Info.setContentPath(dir);

        _Repo.updateAssetInfo(_Info);
        return true;
    }
}
