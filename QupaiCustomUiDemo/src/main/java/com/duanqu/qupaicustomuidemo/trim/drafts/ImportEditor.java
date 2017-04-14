package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.duanqu.qupai.utils.FileUtils;
import com.duanqu.qupai.widget.android.widget.HListView;
import com.duanqu.qupaicustomuidemo.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/5.
 */
public abstract class ImportEditor {

    protected Context mContext;
    protected ImportListener _Listener;
    protected HListView videoList;
    public VideoSortAdapter _adapter;

    public ArrayList<VideoInfoBean> dataList;
    public ArrayList<VideoDirBean> dirList = new ArrayList<>();

    protected String curDir = "";

    protected String currentPath = null;

    protected boolean isUpdate;

    protected int currentDuation;

    private SortVideoTask sortTask;

    public ImportEditor(Context context){
        this.mContext = context;

        isUpdate = true;
        sortTask = new SortVideoTask(mContext);
        sortTask.execute();
    }

    public interface ImportListener {
        void onVideoSort();

        void onCompelete(boolean hasVideo);

        void onPlayCurrent(String path);
    }

    public void setListener(ImportListener listener) {
        _Listener = listener;
    }

    public void cancelTask() {
        if (sortTask != null) {
            sortTask.cancel(false);
        }
    }

    public boolean isTaskCancel() {
        return sortTask.isCancelled();
    }

    private class SortVideoTask extends AsyncTask<Void, VideoInfoBean, Void> {

        private final ContentResolver _Resolver;

        public SortVideoTask(Context context) {
            _Resolver = context.getContentResolver();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Cursor cursor = _Resolver.query(generateQueryUri(), generateProjection(),
                        generateSelection(), fillSelectionArgs(),
                        sortOrder());

                if (cursor == null) {
                    return null;
                }

                initCursorColume(cursor);

                if (cursor.moveToNext()) {
                    do {
                        VideoInfoBean info = fillInfoToBeanFromCursor(cursor);
                        if(info == null){
                            continue;
                        }else{
                            info.setType(getImportType());
                            String[] dir = info.getFilePath().split("/");
                            String dirName = dir[dir.length - 2];
//                            if (!curDir.equals(dirName) && !isDirInList(dirName)) {
                            if ( !isDirInList(dirName)) {
                                VideoDirBean dirInfo = new VideoDirBean();

                                dirInfo.setThumbnailId(info.getOrigId());
                                dirInfo.setDirName(dirName);
                                dirInfo.setFilePath(info.getFilePath());
                                String videoDirPath = info.getFilePath().substring(0,
                                        info.getFilePath().lastIndexOf("/"));
                                dirInfo.setVideoDirPath(videoDirPath);
                                dirInfo.setType(getImportType());
                                dirList.add(dirInfo);
                                curDir = dirName;
                            }

                            Cursor thumbCursor = _Resolver.query(generateThumbnailQueryUri(),
                                    generateThumbnailProjection(),
                                    generateThumbnailSelection(),
                                    new String[]{String.valueOf(info.getOrigId())}, null);

                            if (thumbCursor.moveToFirst()) {
                                String thumbPath = thumbCursor.getString(
                                        thumbCursor.getColumnIndexOrThrow(getThumbnailPathColumn()));
                                info.setThumbnailPath(thumbPath);
                            }
                            thumbCursor.close();
                            publishProgress(info);
                        }
                    }while (cursor.moveToNext());
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (_Listener != null) {
                _Listener.onCompelete(dataList.size() > 0 ? true : false);
            }
        }

        @Override
        protected void onProgressUpdate(VideoInfoBean... bean) {
            if (bean[0] != null) {
                dataList.add(bean[0]);
            }

            if (_adapter != null) {
                _adapter.notifyDataSetChanged();
            }

            if (_Listener != null && isUpdate) {
                isUpdate = false;

                videoList.setItemChecked(1, true);

                if (_adapter != null && _adapter.getItem(1) != null) {
                    currentPath = _adapter.getItem(1).getFilePath();
                    currentDuation = _adapter.getItem(1).getDuration();
                }

                _Listener.onVideoSort();
            }

            super.onProgressUpdate(bean);
        }
    }

    protected abstract int getImportType();

    protected abstract String generateSelection();

    protected abstract String[] fillSelectionArgs();

    protected abstract Uri generateQueryUri();

    protected abstract String[] generateProjection();

    protected abstract String sortOrder();

    protected abstract String generateThumbnailSelection();

    protected abstract Uri generateThumbnailQueryUri();

    protected abstract String[] generateThumbnailProjection();

    protected abstract String getThumbnailPathColumn();

    protected abstract void initCursorColume(Cursor cursor) throws IllegalArgumentException;

    protected abstract VideoInfoBean fillInfoToBeanFromCursor(Cursor cursor);

    private boolean isDirInList(String dir) {
        if (dirList != null) {
            for (int i = 0; i < dirList.size(); i++) {
                if (dir.equals(dirList.get(i).getDirName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public VideoInfoBean getCurrentList() {
        int pos = videoList.getCheckedItemPosition();
        if (pos < 1 || dataList == null || dataList.size() < pos) {
            return null;
        } else {
            return dataList.get(pos - 1);
        }
    }

    public void removeVideo(final VideoInfoBean bean) {
        if (bean != null) {
            boolean isSucc = false;
            File tmpFile = new File(bean.getFilePath());
            if (tmpFile != null) {
                if (tmpFile.isFile() && tmpFile.canWrite()) {
                    isSucc = tmpFile.delete();
                    Log.e("delete", "isSucc = " + isSucc);
                }
            }

            if (!isSucc) {
                String content = mContext.getResources().getString(R.string.qupai_file_can_not_delete);
                Toast toast = Toast.makeText(mContext, content, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }

            dataList.remove(bean);
            _adapter.notifyDataSetChanged();
            videoList.invalidate();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    deleteVideoFromDir(bean.getFilePath(), bean.getOrigId());
                }
            }).start();

            FileUtils.scanFile(mContext, tmpFile);

            int pos = -1;
            int count = _adapter.getCount();
            if (videoList.getCheckedItemPosition() > count) {
                videoList.setItemChecked(videoList.getCheckedItemPosition() - 1, true);
                pos = videoList.getCheckedItemPosition() - 1;
            } else {
                pos = videoList.getCheckedItemPosition();
            }

            if (pos >= 0 && _adapter.getItem(pos) != null) {
                String path = _adapter.getItem(pos).getFilePath();
                setCurrentPath(path);
            }
        }
    }

    private void deleteVideoFromDir(String filePath, int id) {
        String videoDirPath = filePath.substring(0,
                filePath.lastIndexOf("/"));
        int count = getDirFileCount(videoDirPath);

        int size = dirList.size();
        for (int i = 0; i < size; i++) {
            VideoDirBean dirBean = dirList.get(i);
            if (dirBean == null) {
                return;
            }

            if (dirBean.getVideoDirPath().equals(videoDirPath)
                    && dirList.get(i).getThumbnailId() == id) {
                if (count == 0) {
                    dirList.remove(dirList.get(i));
                } else {
                    VideoDirBean bean = getNewDirBean(videoDirPath);

                    dirList.remove(dirList.get(i));
                    dirList.add(i, bean);
                }

                break;
            }
        }
    }

    private VideoDirBean getNewDirBean(String path) {
        VideoDirBean bean = null;

        for (int i = 0; i < dataList.size(); i++) {
            String tmpFile = dataList.get(i).getFilePath();
            String tmpDir = tmpFile.substring(0, tmpFile.lastIndexOf("/"));
            if (path.equals(tmpDir)) {
                String[] dir = tmpFile.split("/");
                String dirName = dir[dir.length - 2];

                bean = new VideoDirBean();
                bean.setDirName(dirName);
                bean.setFilePath(tmpFile);
                bean.setVideoDirPath(path);
                bean.setThumbnailId(dataList.get(i).getOrigId());

                break;
            }
        }

        return bean;
    }

    private int getDirFileCount(String path) {
        int count = 0;

        for (int i = 0; i < dataList.size(); i++) {
            String tmpFile = dataList.get(i).getFilePath();
            String tmpDir = tmpFile.substring(0, tmpFile.lastIndexOf("/"));
            if (path.equals(tmpDir)) {
                count++;
            }
        }

        return count;
    }

    public void setCurrentPath(String path) {
        currentPath = path;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentDuration(int duration) {
        currentDuation = duration;
    }

    public int getCurrentDuation() {
        return currentDuation;
    }

}
