package com.duanqu.qupaicustomuidemo.editor.download;

import android.util.Log;
import android.view.View;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.cache.core.DownloadFileOptions;
import com.duanqu.qupai.cache.core.VideoLoader;
import com.duanqu.qupai.cache.core.VideoLoadingListener;
import com.duanqu.qupai.cache.core.VideoLoadingProgressListener;
import com.duanqu.qupai.cache.core.assist.FailReason;
import com.duanqu.qupai.cache.videoaware.VideoNonViewAware;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.NotifyTaskFinish;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.OnItemDownloadCompleted;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.ProgressListener;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.ResourceDownloadListener;
import com.duanqu.qupai.effect.asset.ResourceItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPasterDownload {

    private List<ResourceDownloadListener> listener = new ArrayList<>();
    private NotifyTaskFinish notifyTaskFinish;
    private File dir;
    private List<String> downloadList = new ArrayList<String>();
    private List<ResourceItem> list = new ArrayList<>();
    private int size;
    private int id;
    private int success;
    private int current;
    private String currentUrl;
    private boolean stop;
    private ResourceItem specie;
    private List<ProgressListener> progressListener = new ArrayList<>();
    private List<OnItemDownloadCompleted> onItemDownloadCompletedListener = new ArrayList<>();
    private Map<String, Integer> mProgress = new HashMap<String, Integer>();

    private final AssetRepository _Repo;

    public MultiPasterDownload(File path, ResourceItem item, AssetRepository repo) {
        _Repo = repo;
        this.specie = item;
        id = (int)specie.getID();
        list.addAll(specie.getItemList());
        dir = path;
    }

    public void setTaskFinishListener(NotifyTaskFinish listener){
        notifyTaskFinish = listener;
    }

    public void stop(){
        stop = true;
        VideoLoader.getInstance().cancelDisplayTask(new VideoNonViewAware(currentUrl));
    }

    public void addDownloadListener(ResourceDownloadListener l){
        listener.add(l);
    }

    public void removeDownloadListener(ResourceDownloadListener l){
        listener.remove(l);
    }

    public void addDownloadProgressListener(ProgressListener l){
        progressListener.add(l);
    }

    public void removeDownloadProgressListener(ProgressListener l){
        progressListener.remove(l);
    }

    public void removeOnItemDownloadCompletedListener(OnItemDownloadCompleted l){
        onItemDownloadCompletedListener.remove(l);
    }

    public void addOnItemDownloadCompletedListener(OnItemDownloadCompleted l){
        onItemDownloadCompletedListener.add(l);
    }

    private void publishDownloadProgress(String key, int progress){
        mProgress.put(key, progress);
        int count = 0;
        for(Integer i : mProgress.values()){
            count += i;
        }
        int pos = count / size;
        fireDownloadProgress(pos);
    }

    private void fireDownloadProgress(int progress){
        for(ProgressListener p : progressListener){
            p.onProgressUpdate(id, progress);
        }
    }

    private void initDownloadList(List<ResourceItem> list){
        size = list.size();
        current = 0;
        for(ResourceItem overlay : list){
            overlay.setResourceType(VideoEditBean.TYPE_DIYOVERLAY);
            downloadList.add(overlay.getResourceUrl());
        }
    }

    private void fireItemDownloadCompletedEvent(ResourceItem overlay, boolean error){
        for(OnItemDownloadCompleted i : onItemDownloadCompletedListener){
            i.onItemDownloadCompleted(overlay, id, error);
        }
    }

    private void fireDownloadCompletedEvent(boolean completed){
        specie.setIsLocal(completed);
        for(ResourceDownloadListener l : listener){
            l.onDownloadCompleted(specie);
        }

        if(notifyTaskFinish != null){
            notifyTaskFinish.onDownloadTaskFinish((int)specie.getID());
        }
    }

    private void fireDownloadFailedEvent(){
        for(ResourceDownloadListener l : listener){
            l.onDownloadFailed(specie);
        }

        if(notifyTaskFinish != null){
            notifyTaskFinish.onDownloadTaskFinish((int) specie.getID());
        }
    }

    private void handleDownloadEvent(ResourceItem overlay, boolean error){
        current++;
        downloadList.remove(overlay.getResourceUrl());
        if(error){
            fireItemDownloadCompletedEvent(overlay, false);
        }else{
            success++;
            fireItemDownloadCompletedEvent(overlay, true);
        }
        if(downloadList.isEmpty()){
            boolean completed;
            if(success == size){
                completed = true;
            }else{
                completed = false;
            }

            list.clear();
            size = 0;
            mProgress.clear();
            if(listener != null){
                if(success == 0){
                    fireDownloadFailedEvent();
                }else{
                    fireDownloadCompletedEvent(completed);
                }
            }
        }else{
            if(stop){
                list.clear();
                downloadList.clear();
                mProgress.clear();
                size = 0;
                fireDownloadCompletedEvent(false);
                return;
            }
            download(list.get(current));
        }
    }

    public void downloadPasters(){
        if(list == null || list.size() == 0){
            if(listener != null){
                fireDownloadFailedEvent();
            }
            return;
        }

        if(stop){
            fireDownloadFailedEvent();
            return;
        }

        initDownloadList(list);
        download(list.get(current));
    }

    private void download(final ResourceItem overlay){

        AssetInfo info = _Repo.resolveAsset(overlay.getAssetID());
        if(info != null && info.isAvailable()){
            handleDownloadEvent(overlay, false);
            return;
        }

        if(stop){
            handleDownloadEvent(overlay, true);
            return;
        }

        File packageDir;
        packageDir = getAssetPackageDir(overlay.getID());
        ZIPFileProcessor processor = new ZIPFileProcessor(packageDir, overlay.getID());
        DownloadFileOptions options = new DownloadFileOptions.Builder()
            .postProcessor(processor).build();
        VideoLoadingListener listener = new VideoLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view,
                                        FailReason failReason) {
                Log.d("DOWNLOADER", "任务失败" + imageUri);
                handleDownloadEvent(overlay, true);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, File loadedVideo) {
                Log.d("DOWNLOADER", "任务完成" + imageUri);
                if(loadedVideo == null || !loadedVideo.isDirectory()){
                    handleDownloadEvent(overlay, true);
                    return;
                }
                overlay.setLocalPath("file://" + loadedVideo.getAbsolutePath());
                overlay.setIconUrl("file://" + loadedVideo.getAbsolutePath());
                overlay.setStatus(AbstractDownloadManager.DOWNLOAD_COMPLETED);
                _Repo.updateAssetInfo(overlay);
                handleDownloadEvent(overlay, false);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                Log.d("DOWNLOADER", "任务取消" + imageUri);
                handleDownloadEvent(overlay, true);
            }
        };
        VideoLoadingProgressListener progress = new VideoLoadingProgressListener() {

            @Override
            public void onProgressUpdate(String imageUri, View view, int current,
                                         int total) {
                Log.d("DOWNLOADER", "任务进度" + current);
                publishDownloadProgress(imageUri, current * 100 / total);
            }
        };
        currentUrl = overlay.getResourceUrl();
        VideoLoader.getInstance().loadVideo(currentUrl, options, listener, progress);
    }

    public File getAssetPackageDir(long id) {
        String prefix = "Shop_DIY_";

        return new File(dir, prefix + String.valueOf(id));
    }

}
