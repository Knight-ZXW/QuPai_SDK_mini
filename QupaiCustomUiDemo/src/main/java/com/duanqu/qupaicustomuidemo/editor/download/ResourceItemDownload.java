package com.duanqu.qupaicustomuidemo.editor.download;

import android.view.View;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.cache.core.DownloadFileOptions;
import com.duanqu.qupai.cache.core.VideoLoader;
import com.duanqu.qupai.cache.core.VideoLoadingListener;
import com.duanqu.qupai.cache.core.VideoLoadingProgressListener;
import com.duanqu.qupai.cache.core.assist.FailReason;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.ProgressListener;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.ResourceDecompressListener;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager.ResourceDownloadListener;
import com.duanqu.qupai.effect.asset.ResourceItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/7/9.
 */
public class ResourceItemDownload implements ResourceDecompressListener {

    private List<ResourceDownloadListener> listeners = new ArrayList<>();
    private ResourceItem item;
    private ZIPFileProcessor processor;
    private List<ProgressListener> progressListeners = new ArrayList<>();
    private List<ResourceDecompressListener> decompressListeners = new ArrayList<>();
    private AbstractDownloadManager.NotifyTaskFinish taskFinishListener;

    private final AssetRepository _Repo;

    public ResourceItemDownload(ResourceItem item, File target, AssetRepository repo){
        this.item = item;
        processor = new ZIPFileProcessor(target, item.getID());
        processor.setResourceDecompressListener(this);
        _Repo = repo;
    }

    public void setTaskFinishListener(AbstractDownloadManager.NotifyTaskFinish l){
        taskFinishListener = l;
    }

    @Override
    public void onResourceDecompressCompleted(long id) {
        for(ResourceDecompressListener l : decompressListeners){
            l.onResourceDecompressCompleted(id);
        }
    }

    @Override
    public void onResourceDecompressStart(long id) {
        for(ResourceDecompressListener l : decompressListeners){
            l.onResourceDecompressStart(id);
        }
    }

    @Override
    public void onResourceDecompressFailed(long id) {
        for(ResourceDecompressListener l : decompressListeners){
            l.onResourceDecompressFailed(id);
        }
    }

    public void addResourceDecompressListener(ResourceDecompressListener l){
        if(l == null){
            return ;
        }
        decompressListeners.add(l);
    }

    public void removeResourceDecompressListener(ResourceDecompressListener l){
        decompressListeners.remove(l);
    }

    public void addDownloadListener(ResourceDownloadListener l){
        if(l == null){
            return ;
        }
        listeners.add(l);
    }

    public void removeDownloadListener(ResourceDownloadListener l){
        listeners.remove(l);
    }

    public void removeDownloadProgressListener(ProgressListener l){
        progressListeners.remove(l);
    }

    public void addDownloadProgressListener(ProgressListener l){
        if(l == null){
            return ;
        }
        progressListeners.add(l);
    }

    private void fireDownloadStartEvent(){
        for(ResourceDownloadListener l : listeners){
            l.onDownloadStart(item);
        }
    }

    private void fireDownloadCompletedEvent(){
        for(ResourceDownloadListener l : listeners){
            l.onDownloadCompleted(item);
        }

        if(taskFinishListener != null){
            taskFinishListener.onDownloadTaskFinish((int)item.getID());
        }
    }

    private void fireDownloadFailedEvent(){
        for(ResourceDownloadListener l : listeners){
            l.onDownloadFailed(item);
        }

        if(taskFinishListener != null){
            taskFinishListener.onDownloadTaskFinish((int)item.getID());
        }
    }

    private void fireDownloadProgress(int id, int progress){
        for(ProgressListener p : progressListeners){
            p.onProgressUpdate(id, progress);
        }
    }

    public void download(View view){
        DownloadFileOptions options = new DownloadFileOptions.Builder()
                .postProcessor(processor).build();
        VideoLoadingListener vll = new VideoLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(item.getType() != AssetInfo.TYPE_SHADER_MV){
                    _Repo.updateAssetInfo(item);
                }

                item.setStatus(AbstractDownloadManager.DOWNLOAD_RUNNING);
                fireDownloadStartEvent();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                item.setStatus(AbstractDownloadManager.DOWNLOAD_NOT);
                if(item.getType() != AssetInfo.TYPE_SHADER_MV){
                    _Repo.updateAssetInfo(item);
                }
                fireDownloadFailedEvent();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, File loadedVideo) {
                if(loadedVideo == null){
                    if(item.getType() != AssetInfo.TYPE_SHADER_MV){
                        item.setStatus(AbstractDownloadManager.DOWNLOAD_NOT);
                        _Repo.updateAssetInfo(item);
                    }
                    fireDownloadFailedEvent();

                }else{
                    item.setStatus(AbstractDownloadManager.DOWNLOAD_COMPLETED);
                    if(item.getType() == AssetInfo.TYPE_SHADER_MV){
                        String mvUrl = null;
                        String iconUrl = null;
                        File[] files = loadedVideo.listFiles();
                        if (files.length > 0) {
                            mvUrl = "file://" + files[0].getAbsolutePath();
                            iconUrl = "file://" + files[0].getAbsolutePath();
                            item.setIconUrl(iconUrl);
                            item.setLocalPath(mvUrl);
                        }
                    }else{
                        item.setLocalPath(loadedVideo.getAbsolutePath());
                    }
                    _Repo.updateAssetInfo(item);
                    fireDownloadCompletedEvent();
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                item.setStatus(AbstractDownloadManager.DOWNLOAD_NOT);
                _Repo.updateAssetInfo(item);
                fireDownloadFailedEvent();
            }
        };
        VideoLoadingProgressListener vlpl = new VideoLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                fireDownloadProgress((int)item.getID(), current * 100 / total);
            }
        };
        if(view == null){
            VideoLoader.getInstance().displayVideo(item.getResourceUrl(), options, vll, vlpl);
        }else{
            VideoLoader.getInstance().displayVideo(item.getResourceUrl(), view, options, vll, vlpl);
        }
    }

}
