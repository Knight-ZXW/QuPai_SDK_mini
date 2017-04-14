package com.duanqu.qupaicustomuidemo.editor.download;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.CommonUtil;
import com.duanqu.qupai.effect.asset.ResourceItem;

import java.io.File;
import java.io.FileNotFoundException;

import static com.duanqu.qupai.effect.asset.CommonUtil.getResourcesUnzipPath;

public class PasterDownloadManager extends AbstractDownloadManager {

    public PasterDownloadManager(AssetRepository repo) {
        _Repo = repo;
    }

    private final AssetRepository _Repo;
    private boolean isStopped;

    private SparseArray<MultiPasterDownload> downloads = new SparseArray<>();

    private SparseArray<ResourceItemDownload> downloadResources = new SparseArray<>();

    public boolean hasCategoryDownloading(){
        return downloads.size() != 0;
    }

    public int[] getDownloadCategoryIds(){
        int[] ids = new int[downloads.size()];
        for(int i = 0; i < ids.length; i++){
            ids[i] = downloads.keyAt(i);
        }
        return ids;
    }

    public boolean isCategoryDownloading(int categoryId){
        return downloads.indexOfKey(categoryId) >= 0;
    }

    public void registItemCompletedListener(int id, OnItemDownloadCompleted l){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.addOnItemDownloadCompletedListener(l);
        }
    }

    public void unRegistItemCompletedListener(int id, OnItemDownloadCompleted l){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.removeOnItemDownloadCompletedListener(l);
        }
    }

    public void registProgressListener(int id, ProgressListener progressListener){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.addDownloadProgressListener(progressListener);
        }
    }

    public void unRegistProgressListener(int id, ProgressListener progressListener){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.removeDownloadProgressListener(progressListener);
        }
    }

    public void stop(){
        isStopped = true;
        for(int i = 0; i < downloads.size(); i++){
            downloads.valueAt(i).stop();
        }
    }

    public boolean isStopped(){
        return isStopped;
    }

    public void addDownloadCategoryListener(int id, ResourceDownloadListener l){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.addDownloadListener(l);
        }
    }

    public void removeDownloadCategoryListener(int id, ResourceDownloadListener l){
        MultiPasterDownload d = downloads.get(id);
        if(d != null){
            d.removeDownloadListener(l);
        }
    }

    public void removeResourcesListener(int id, ResourceDownloadListener downloadListener){
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.removeDownloadListener(downloadListener);
        }
    }

    public void addResourcesListener(int id, ResourceDownloadListener downloadListener) {
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.addDownloadListener(downloadListener);
        }
    }

    public void downloadPasterCategory(ResourceItem item, Context context,
                                       ResourceDownloadListener downloadListener) {
        isStopped = false;
        download(context, item, downloadListener);
    }

    private void download(Context context, ResourceItem item,
                          ResourceDownloadListener downloadListener){
        if(!CommonUtil.hasNetwork(context)){
 //           Toast.makeText(context, R.string.qupai_slow_network_check, Toast.LENGTH_SHORT).show();
            downloadListener.onDownloadFailed(item);
            return;
        }
        File dir = getResourcesUnzipPath(context);

        if(dir == null){
            return ;
        }
        MultiPasterDownload downloader;
        if(!isCategoryDownloading((int)item.getID())){
            downloader = new MultiPasterDownload(dir, item, _Repo);
            downloads.put((int)item.getID(), downloader);
            downloader.setTaskFinishListener(new NotifyTaskFinish() {
                @Override
                public void onDownloadTaskFinish(int id) {
                    downloads.remove(id);
                }
            });
            downloader.addDownloadListener(downloadListener);
            downloadListener.onDownloadStart(item);
            downloader.downloadPasters();
        }else{
            downloader = downloads.get((int)item.getID());
            downloader.addDownloadListener(downloadListener);
        }

    }

    @Override
    public boolean isResourceDownloading(int resourceId) {
        return downloadResources.indexOfKey(resourceId) >= 0;
    }

    @Override
    public void registResourceDownloadProgressListener(int id, ProgressListener progressListener) {
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.addDownloadProgressListener(progressListener);
        }
    }

    @Override
    public void unRegistResourceDownloadProgressListener(int id, ProgressListener progressListener) {
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.removeDownloadProgressListener(progressListener);
        }
    }

    @Override
    public void registResourceDecompressListener(int id, ResourceDecompressListener progressListener) {
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.addResourceDecompressListener(progressListener);
        }
    }

    @Override
    public void unRegistResourceDecompressListener(int id, ResourceDecompressListener progressListener) {
        ResourceItemDownload d = downloadResources.get(id);
        if(d != null){
            d.removeResourceDecompressListener(progressListener);
        }
    }

    public void downloadResourcesItem(Context context, ResourceItem item, View view,
                                      ResourceDownloadListener listener){
        if(!CommonUtil.hasNetwork(context)){

            //       Toast.makeText(context, R.string.qupai_slow_network_check, Toast.LENGTH_SHORT).show();
            listener.onDownloadFailed(item);
            return;
        }

        File target = null;
        try{
            target = getAssetPackageDir(context, item);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            target = null;
        }

        if(target == null){
            return ;
        }

        ResourceItemDownload d;
        if(!isResourceDownloading((int)item.getID())){

            d = new ResourceItemDownload(item, target, _Repo);
            downloadResources.put((int)item.getID(), d);
            d.setTaskFinishListener(new NotifyTaskFinish() {
                @Override
                public void onDownloadTaskFinish(int id) {
                    downloadResources.remove(id);
                }
            });
            d.addDownloadListener(listener);
            d.download(view);
        }else{
            d = downloadResources.get((int)item.getID());
            d.addDownloadListener(listener);
        }
    }

    public void downloadResourcesItem(Context context, ResourceItem item, View view,
                                      ResourceDownloadListener listener,
                                      ProgressListener progressListener,
                                      ResourceDecompressListener decompressListener){
        if(!CommonUtil.hasNetwork(context)){

     //       Toast.makeText(context, R.string.qupai_slow_network_check, Toast.LENGTH_SHORT).show();
            listener.onDownloadFailed(item);
            return;
        }

        File target = null;
        try{
            target = getAssetPackageDir(context, item);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            target = null;
        }

        if(target == null){
            return ;
        }

        ResourceItemDownload d;
        if(!isResourceDownloading((int) item.getID())){
            d = new ResourceItemDownload(item, target, _Repo);
            d.setTaskFinishListener(new NotifyTaskFinish() {
                @Override
                public void onDownloadTaskFinish(int id) {
                    downloadResources.remove(id);
                }
            });
            downloadResources.put((int) item.getID(), d);
            d.addDownloadListener(listener);
            d.addDownloadProgressListener(progressListener);
            d.addResourceDecompressListener(decompressListener);
            d.download(view);
        }else {
            d = downloadResources.get((int) item.getID());
            d.addDownloadListener(listener);
            d.addDownloadProgressListener(progressListener);
            d.addResourceDecompressListener(decompressListener);
        }
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

}
