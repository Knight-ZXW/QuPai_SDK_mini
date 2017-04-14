package com.duanqu.qupaicustomuidemo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.render.SceneFactoryClientImpl;
import com.duanqu.qupai.stage.resource.MVTemplate;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.editor.mv.IMVItemForm2;
import com.duanqu.qupaicustomuidemo.editor.mv.MusicItemForm;
import com.duanqu.qupaicustomuidemo.provider.ProviderUris;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnzipMusicTask extends AsyncTask<Void, Integer, Long> {
    private static final String TAG = "UnzipMusicTask";

    private Context mContext;
    private File mInputFile;
    private File mOutputFile;
    private String mInputPath;
    private String filename;
    private String mResName;
    private long id;
    private int resType;//resourceType; 1.音乐，2.表情，2.贴纸，4.滤镜，7.mv
    private long mCategoryId;
    private String mCategoryName;
    private MusicItemForm mSysForm;
    private IMVItemForm2 mImvForm;

    private boolean isRecommendFour;

    @Inject
    ProviderUris _Uris;

    private final JSONSupport mJSON;

    public UnzipMusicTask(long id, String filename, String input, String resName,
                          String output, int type, long categoryId, String categoryName,
                          MusicItemForm srf, IMVItemForm2 imvForm, Context context) {

//	    AssetStoreComponent.get(context).inject(this);

        mContext = context;
        _Uris = new ProviderUris(context);
        mJSON = new JSONSupportImpl();
        resType = type;
        mCategoryId = categoryId;
        mCategoryName = categoryName;
        mInputPath = input;
        mInputFile = new File(input + filename);
        mOutputFile = new File(output);
        mResName = resName;
        mSysForm = srf;
        mImvForm = imvForm;
        this.id = id;
        this.filename = filename.replace(".zip", "");
        if (!mOutputFile.exists()) {
            if (!mOutputFile.mkdirs()) {
                Log.e(TAG, "Failed to make directories:" + mOutputFile.getAbsolutePath());
            }
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        //解压成功后删除zip文件
        DeleteZipFile(mInputPath, filename);

        if (resType == VideoEditBean.TYPE_MUSIC
                || resType == VideoEditBean.TYPE_DIYOVERLAY) {
            handleMusicResource();
        } else if (resType == VideoEditBean.TYPE_SHADER_MV) {
            handleMvResource();
        }
        super.onPostExecute(result);
    }

    @Override
    protected Long doInBackground(Void... params) {
        try {
            return unZip(mInputFile.getPath(), mOutputFile.getPath(), new ZipFile(mInputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Long.valueOf(1);
    }

    @SuppressWarnings("unchecked")
    private long unzipMusic() {
        long extractedSize = 0L;
        Enumeration<ZipEntry> entries;
        ZipFile zip = null;
        try {
            zip = new ZipFile(mInputFile);
            long uncompressedSize = getOriginalSize(zip);
            publishProgress(0, (int) uncompressedSize);

            entries = (Enumeration<ZipEntry>) zip.entries();
            File destination = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                destination = new File(mOutputFile, entry.getName());
                if (!destination.getParentFile().exists()) {
                    Log.e(TAG, "make=" + destination.getParentFile().getAbsolutePath());
                    destination.getParentFile().mkdirs();
                }
                if (destination.exists() && mContext != null) {

                }
                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
                extractedSize += copy(zip.getInputStream(entry), outStream);
                outStream.close();
            }

            //修改文件夹名称
            File destinaFile = new File(destination.getParentFile().getAbsolutePath());
            File unzipFile = new File(mOutputFile, filename);
            destinaFile.renameTo(unzipFile);

        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return extractedSize;
    }

    private long unZip(String zipFileName, String outputDirectory, ZipFile zip) {
        long extractedSize = 0L;
        long uncompressedSize = getOriginalSize(zip);
        publishProgress(0, (int) uncompressedSize);

        try {
            String dirName = ZipUtils.unzipRDirName(zipFileName, outputDirectory);
            //修改文件夹名称
            File destinaFile = new File(mOutputFile, dirName);
            File unzipFile = new File(mOutputFile, filename);
            destinaFile.renameTo(unzipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractedSize;
    }

    @SuppressWarnings("unchecked")
    private long getOriginalSize(ZipFile file) {
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
        long originalSize = 0l;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getSize() >= 0) {
                originalSize += entry.getSize();
            }
        }
        return originalSize;
    }

    private int copy(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private final class ProgressReportingOutputStream extends FileOutputStream {

        public ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            super.write(buffer, byteOffset, byteCount);
        }

    }

    private void DeleteZipFile(String path, String file) {
        if (path == null && file == null) {
            return;
        }
        File directory = new File(path);
        if (directory.isDirectory()) {
            File[] mFile = directory.listFiles();
            for (File tmpFile : mFile) {
                if (tmpFile.getName().equals(file + ".zip")) {
                    tmpFile.delete();
                }
            }
        }
    }

    private boolean isRecordInDB(int resourceType, long id) {
        boolean result = false;

        List<String> selectionArgs = new ArrayList<String>();
        selectionArgs.add(String.valueOf(resourceType));
        selectionArgs.add(String.valueOf(id));

        Cursor cursor = mContext.getContentResolver().query(
                _Uris.RESOURCE, null, null, selectionArgs.toArray(new String[2]), null);

        if (cursor == null) {
            return result;
        }

        while (cursor.moveToNext()) {
            result = true;

            int recommend = cursor.getInt(cursor.getColumnIndexOrThrow(VideoEditResources.RECOMMEND));
            if (recommend == VideoEditBean.RECOMMEND_CLIENT_DELETE) {
                isRecommendFour = true;
            } else {
                isRecommendFour = false;
            }
        }

        cursor.close();

        return result;
    }

    private boolean isRecommendFour() {
        return isRecommendFour;
    }

    private void insertMvRecordInDB(ContentValues values, Uri uri,
                                    String mvUrl, String iconUrl, int recommend) {
        values.put(VideoEditResources.ID, id);
        values.put(VideoEditResources.RESOURCENAME, mResName);
        values.put(VideoEditResources.RECOMMEND, recommend);
        values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
        values.put(VideoEditResources.RESOURCEURL, mImvForm.getPreviewMp4());
        values.put(VideoEditResources.RESOURCELOCALPATH, mvUrl);
        values.put(VideoEditResources.RESOURCEICON, iconUrl);
        values.put(VideoEditResources.RESOURCETYPE, resType);
        values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
        values.put(VideoEditResources.FONTTYPE, 0);
        values.put(VideoEditResources.DESCRIPTION, "");
        values.put(VideoEditResources.RESOURCEBANNER, "");

        mContext.getContentResolver().insert(uri, values);
    }

    private void insertMusicRecordInDB(ContentValues values, Uri uri,
                                       String musicUrl, String iconUrl, int recommend) {
        values.put(VideoEditResources.ID, id);
        values.put(VideoEditResources.RESOURCENAME, mResName);
        values.put(VideoEditResources.RECOMMEND, AssetInfo.RECOMMEND_DOWNLOAD);
        values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
        values.put(VideoEditResources.DESCRIPTION, mSysForm.getDescription());
        values.put(VideoEditResources.RESOURCEURL, mSysForm.getMusicUrl());
        values.put(VideoEditResources.RESOURCELOCALPATH, musicUrl);
        values.put(VideoEditResources.RESOURCEICON, iconUrl);
        values.put(VideoEditResources.RESOURCETYPE, resType);
        values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
        values.put(VideoEditResources.FONTTYPE, 0);
        values.put(VideoEditResources.RESOURCEBANNER, "");

        mContext.getContentResolver().insert(uri, values);
    }

    private void handleMusicResource() {
        if (mSysForm == null) {
            return;
        }

        File file = new File(mOutputFile, filename);
        if (file.exists()) {
            String musicUrl = null;
            String iconUrl = null;
            File[] files = file.listFiles();
            File tmpFile = null;
            for (int i = 0; i < files.length; i++) {
                tmpFile = files[i];
                if (tmpFile.getName().endsWith(".mp3")) {
                    musicUrl = file.getAbsolutePath();
                } else if (tmpFile.getName().equals("icon_without_name.png")
                        && resType == VideoEditBean.TYPE_MUSIC) {
                    iconUrl = "file://" + file.getAbsolutePath();
                } else if (tmpFile.getName().equals("banner.png")
                        && resType == VideoEditBean.TYPE_DIYOVERLAY) {
                    iconUrl = "file://" + file.getAbsolutePath();
                } else if (tmpFile.getName().equals("big.png")
                        && resType == VideoEditBean.TYPE_DIYOVERLAY) {
                    musicUrl = "file://" + file.getAbsolutePath();
                }
            }

            Uri uri = null;
            if (resType == VideoEditBean.TYPE_MUSIC) {
                uri = _Uris.MUSIC;
            } else if (resType == VideoEditBean.TYPE_DIYOVERLAY) {
                uri = _Uris.EXPRESSION;
            }
            ContentValues values = new ContentValues();

            if (isRecordInDB(resType, id)) {
                if (isRecommendFour()) {
                    insertMusicRecordInDB(values, uri, musicUrl, iconUrl,
                            VideoEditBean.RECOMMEND_DOWNLOAD);
                } else {
                    values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
                    values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
                    values.put(VideoEditResources.RESOURCELOCALPATH, musicUrl);

                    List<String> selectionArgs = new ArrayList<String>();
                    selectionArgs.add(String.valueOf(resType));
                    selectionArgs.add(String.valueOf(id));

                    mContext.getContentResolver().update(
                            uri, values, null, selectionArgs.toArray(new String[2]));
                }
            } else {
                values.put(VideoEditResources.ID, id);
                values.put(VideoEditResources.RESOURCENAME, mResName);
                values.put(VideoEditResources.RECOMMEND, AssetInfo.RECOMMEND_DOWNLOAD);
                values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
                values.put(VideoEditResources.DESCRIPTION, mSysForm.getDescription());
                values.put(VideoEditResources.RESOURCEURL, mSysForm.getMusicUrl());
                values.put(VideoEditResources.RESOURCELOCALPATH, musicUrl);
                values.put(VideoEditResources.RESOURCEICON, iconUrl);
                values.put(VideoEditResources.RESOURCETYPE, resType);
                values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
                values.put(VideoEditResources.FONTTYPE, 0);
                values.put(VideoEditResources.RESOURCEBANNER, "");

                mContext.getContentResolver().insert(uri, values);
            }
        }
    }

    private void handleMvResource() {
        if (mImvForm == null) {
            return;
        }

        File file = new File(mOutputFile, filename);
        if (file.exists()) {
            String mvUrl = null;
            String iconUrl = null;
            File[] files = file.listFiles();
            if (files.length > 0) {
                mvUrl = "file://" + files[0].getAbsolutePath();
                iconUrl = "file://" + files[0].getAbsolutePath();
            }

            Uri uri = _Uris.MV;

            ContentValues values = new ContentValues();
            if (isRecordInDB(resType, id)) {
                if (isRecommendFour()) {
                    insertMvRecordInDB(values, uri, mvUrl, iconUrl,
                            VideoEditBean.RECOMMEND_DOWNLOAD);
                } else {
                    values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
                    values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
                    values.put(VideoEditResources.RESOURCELOCALPATH, mvUrl);

                    List<String> selectionArgs = new ArrayList<String>();
                    selectionArgs.add(String.valueOf(resType));
                    selectionArgs.add(String.valueOf(id));

                    mContext.getContentResolver().update(
                            uri, values, null, selectionArgs.toArray(new String[2]));
                }
            } else {
                insertMvRecordInDB(values, uri, mvUrl, iconUrl,
                        VideoEditBean.RECOMMEND_DOWNLOAD);
            }

            Uri MusicUri = _Uris.MUSIC;
            values.clear();

            if (isRecordInDB(VideoEditBean.TYPE_MV_MUSIC, id)) {
                return;
            }

            SceneFactoryClientImpl scene_client = new SceneFactoryClientImpl(mContext, mJSON);
            MVTemplate mvJosn = scene_client.readShaderMV(iconUrl);

            if (mvJosn == null) {
                return;
            }

            values.put(VideoEditResources.ID, id);
            values.put(VideoEditResources.RESOURCENAME, mvJosn.musicName);
            values.put(VideoEditResources.RECOMMEND, AssetInfo.RECOMMEND_DOWNLOAD);
            values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
            values.put(VideoEditResources.DESCRIPTION, "");
            values.put(VideoEditResources.RESOURCEURL, mImvForm.getPreviewMp4());
            values.put(VideoEditResources.RESOURCELOCALPATH, mvUrl);
            values.put(VideoEditResources.RESOURCEICON, iconUrl);
            values.put(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MV_MUSIC);
            values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
            values.put(VideoEditResources.FONTTYPE, 0);
            values.put(VideoEditResources.RESOURCEBANNER, "");

            mContext.getContentResolver().insert(MusicUri, values);
        }
    }
}
