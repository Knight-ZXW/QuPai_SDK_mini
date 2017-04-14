package com.duanqu.qupaicustomuidemo.editor.mv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.ResourceItem;
import com.duanqu.qupai.effect.asset.ToastUtil;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.editor.EditorAction;
import com.duanqu.qupaicustomuidemo.editor.api.Api;
import com.duanqu.qupaicustomuidemo.editor.download.PasterDownloadManager;
import com.duanqu.qupaicustomuidemo.editor.manager.EffectManageActivity;
import com.duanqu.qupaicustomuidemo.provider.DIYOverlayCategory;
import com.duanqu.qupaicustomuidemo.provider.ProviderUris;
import com.duanqu.qupaiokhttp.HttpRequest;
import com.duanqu.qupaiokhttp.StringHttpRequestCallback;
import com.duanqu.qupaisdk.tools.DeviceUtils;
import com.duanqu.qupaisdk.tools.SingnatureUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import okhttp3.Headers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownLoadCaptionActivity extends FragmentActivity implements RegistUpdateDialog{

	public static void show(Context context) {
		Intent intent = new Intent(context, DownLoadCaptionActivity.class);
		context.startActivity(intent);
	}

	private DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true).cacheOnDisk(true)
            .showImageForEmptyUri(R.drawable.video_thumbnails_loading_126)
            .showImageOnFail(R.drawable.video_thumbnails_loading_126)
            .showImageOnLoading(R.drawable.video_thumbnails_loading_126)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.ARGB_8888).build();

	private static final int REQUEST_RESOURCE_MANAGER_CODE = 0x2;
    private static final int CURRENT_FRAGMENT_PASTER = 0;
    public static final int RESULT_RESOURCE_MANAGER_CODE = 0x3;

    public static final String GUIDE_OVERLAY_MANAGE = "com.duanqu.qupai.overlay_first_manage";

    private List<OverlayGroup> list = new ArrayList<>();
    private MyAdapter adapter;
    private ListView mListView;
    private TextView title;
    PasterDownloadManager pasterCategoryDownloadManager;

    private AssetRepository _AssetRepo;
    private ProviderUris _Uris;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, UpdateDialogStatus> statusListener = new HashMap<>();

    public void registerDialogListener(int id, UpdateDialogStatus listener){
        statusListener.put(id, listener);
    }

    public void unregisterDialogListener(int id){
        statusListener.remove(id);
    }

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        _AssetRepo = QupaiApplication.videoSessionClient.getAssetRepository();
        pasterCategoryDownloadManager = (PasterDownloadManager)_AssetRepo.getDownloadManager();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        FontUtil.applyFontByContentView(this,
                R.layout.activity_paster_download);

        initView();
        initData();
        adapter = new MyAdapter();
        mListView.setAdapter(adapter);
        _Uris = new ProviderUris(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (!DeviceUtils.isOnline(parent.getContext())) {
                    ToastUtil.showToast(parent.getContext(), R.string.qupai_slow_network_check);
                    return;
                }
                OverlayGroup specie = (OverlayGroup) parent.getItemAtPosition(position);
                if (specie == null) {
                    return;
                }

                PasterPreviewDialog dialog = PasterPreviewDialog.newInstance(specie.preview,
                        specie.name, specie.isLocal, specie.id);
                dialog.setUpdateDialogRegister(DownLoadCaptionActivity.this);
                dialog.setPasterCategoryDownloadManager(pasterCategoryDownloadManager);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    private void updateCategoryStatus(final List<OverlayGroup> dss){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                for(OverlayGroup specie : dss){
                    AssetGroup g = findOverlayGroupFromCache(specie);
                    if(g != null && g.isAvailable()){
                        specie.isLocal = AbstractDownloadManager.DOWNLOAD_COMPLETED;
                    }else{
                        specie.isLocal = AbstractDownloadManager.DOWNLOAD_NOT;
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                adapter.notifyDataSetChanged();
            }

        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initView() {
        ImageView backBtn = (ImageView) findViewById(R.id.paster_download_title_back_btn);
        backBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView managerBtn = (TextView) findViewById(R.id.paster_download_manager_btn);
        managerBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClass(DownLoadCaptionActivity.this, EffectManageActivity.class);
                in.putExtra("kind", AssetRepository.Kind.CAPTION);
                in.putExtra("showCurrentFragment", CURRENT_FRAGMENT_PASTER);
                startActivityForResult(in, REQUEST_RESOURCE_MANAGER_CODE);
            }
        });
        mListView = (ListView) findViewById(R.id.channelSingleListView);

        mListView.setDrawingCacheEnabled(false);

        title = (TextView)findViewById(R.id.download_title);
        title.setText(R.string.qupai_btn_text_download_caption);

    }

    private void initData() {
        //TODO 整个MV下载的接口获取是没有经过包装的.
        HttpRequest.get(Api.getInstance().getApiUrl(Api.CAPTION_RESOURCE_CATEGORY) + "?packageName=" + getPackageName()
                + "&signature=" + SingnatureUtils.getSingInfo(this) +
                "&cursor=0", new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(Headers headers, String s) {
                super.onSuccess(headers, s);
                try {
                    list = new JSONSupportImpl().readListValue(s, new TypeReference<List<OverlayGroup>>() {
                    });
                    if(list.size() > 0){
                        updateCategoryStatus(list);
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LOCK", "download paster activity onResume");
        if(list.size() > 0){
            updateCategoryStatus(list);
        }
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Intent in = new Intent();
        boolean isDownloading = pasterCategoryDownloadManager.hasCategoryDownloading();
        in.setData(EditorAction.downloadToDIY(isDownloading ? 1 : -1));
        setResult(Activity.RESULT_OK, in);
        finish();
        super.onBackPressed();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public OverlayGroup getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewMediator localViewHolder;
            if (convertView == null) {
            	localViewHolder = new ItemViewMediator(parent);
            	convertView = localViewHolder.getView();
            } else {
                localViewHolder = (ItemViewMediator) convertView.getTag();
            }

            localViewHolder.setData(list.get(position));

            return convertView;
        }

    }

    private final class ItemViewMediator {
        private final ImageView image;
        private final ImageView indiator;
        private final TextView name;
        private final TextView description;
        private final TextView download;
        private final TextView use;
        private final CircleProgressBar downloadPb;
        private final View root;
        private OverlayGroup specie;

        ItemViewMediator(ViewGroup parent){
        	root = FontUtil.applyFontByInflate(parent.getContext(),
                    R.layout.list_item_download_paster, parent, false);
        	root.setTag(this);
        	image = (ImageView) root.findViewById(R.id.image);
        	indiator = (ImageView) root.findViewById(R.id.indiator);
        	name = (TextView) root.findViewById(R.id.name);
        	description = (TextView) root.findViewById(R.id.description);
        	download = (TextView) root.findViewById(R.id.download_paster);
        	use = (TextView) root.findViewById(R.id.use_paster);
            downloadPb = (CircleProgressBar) root.findViewById(R.id.parser_download_pb);
        }

        public View getView() {
            return root;
        }

        private final AbstractDownloadManager.ProgressListener pl = new AbstractDownloadManager.ProgressListener() {

            @Override
            public void onProgressUpdate(int id, int progress) {
                if (id == specie.id) {
                    downloadPb.setProgress(progress);
                }

                UpdateDialogStatus l = statusListener.get(id);
                if (l != null) {
                    l.onProgress(progress);
                }
            }
        };

        public void setData(OverlayGroup data){
            specie = data;

            ImageLoader.getInstance().displayImage(data.icon, image, mOptions);
        	name.setText(data.name);

        	int isLocal = data.isLocal;
            if(isLocal == AbstractDownloadManager.DOWNLOAD_NOT){
                download.setEnabled(true);
                download.setText(R.string.music_download);
                download.setVisibility(View.VISIBLE);
                use.setVisibility(View.GONE);
                downloadPb.setVisibility(View.GONE);
                pasterCategoryDownloadManager.unRegistProgressListener(data.id, pl);
            }else if(isLocal == AbstractDownloadManager.DOWNLOAD_UNCOMPLETED){
                download.setEnabled(true);
                download.setVisibility(View.VISIBLE);
                download.setText(R.string.qupai_download_goon);
                use.setVisibility(View.GONE);
                downloadPb.setVisibility(View.GONE);
                pasterCategoryDownloadManager.unRegistProgressListener(data.id, pl);
            }else if(isLocal == AbstractDownloadManager.DOWNLOAD_RUNNING){
                download.setVisibility(View.GONE);
                use.setVisibility(View.GONE);
                downloadPb.setVisibility(View.VISIBLE);
                if(pasterCategoryDownloadManager.isCategoryDownloading(data.id)){
                    download.setEnabled(false);
                }else{
                    downloadPb.setProgress(0);
                    download(specie);
                }
                pasterCategoryDownloadManager.registProgressListener(data.id, pl);
            }else{
                pasterCategoryDownloadManager.unRegistProgressListener(data.id, pl);
                download.setVisibility(View.GONE);
                use.setVisibility(View.VISIBLE);
                downloadPb.setVisibility(View.GONE);
                use.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        useCategoryPaster(specie.id);
                    }
                });
            }

            download.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    download.setEnabled(false);
                    download.setVisibility(View.GONE);
                    downloadPb.setVisibility(View.VISIBLE);
                    downloadPb.setProgress(0);
                    download(specie);

                }
            });

        }

    }

    public void useCategoryPaster(int id){
        Intent in = new Intent();
        in.setData(EditorAction.useDIY(id));
        setResult(Activity.RESULT_OK, in);
        finish();
    }

    public void download(OverlayGroup specie){
        AssetGroup g = findOverlayGroupFromCache(specie);
        List<ResourceItem> list = generateDownloadList(specie, g);
        ResourceItem item = new ResourceItem();
        item.setId(specie.id);
        item.setIconUrl(specie.icon);
        item.setName(specie.name);
        item.setResourceType(AssetInfo.TYPE_DIYOVERLAY);
        item.setItemList(list);
        pasterCategoryDownloadManager.downloadPasterCategory(item, getApplicationContext(), listener);
    }

    @Override
    public void download(int id) {
        for(OverlayGroup s : list){
            if(id == s.id){
                download(s);
                break;
            }
        }
    }

    private List<ResourceItem> generateDownloadList(OverlayGroup specie, AssetGroup group){
        List<ResourceItem> downloads = new ArrayList<>();
        List<? extends AssetInfo> list = null;
        if(group != null){
            list = _AssetRepo.findDIYCategoryContent(group.getGroupId());
        }
        if(list != null){
            for(OverlayForm of : specie.pasterList){
                AssetInfo ai = findLocalItem(list, of);
                if(ai == null){
                    downloads.add(transform(of));
                }else{
                    if(ai.getResourceStatus() != AbstractDownloadManager.DOWNLOAD_COMPLETED){
                        downloads.add(transform(of));
                    }
                }
            }
        }else{
            List<String> ids = new ArrayList<>();
            for(OverlayForm of : specie.pasterList){
                downloads.add(transform(of));
                ids.add(String.valueOf(of.id));
            }

            if(ids.size() == 0){
                return downloads;
            }
            Uri dc = _Uris.getDIYCategory(ids.toArray(new String[ids.size()]));
            ContentValues values = new ContentValues();
            values.put(DIYOverlayCategory.ID, specie.id);
            values.put(DIYOverlayCategory.NAME, specie.name);
            values.put(DIYOverlayCategory.ICON, specie.icon);
            values.put(DIYOverlayCategory.PRIORITY, 0);
            values.put(DIYOverlayCategory.TYPE, AssetInfo.TYPE_CAPTION);
            values.put(DIYOverlayCategory.RECOMMEND, AssetInfo.RECOMMEND_DOWNLOAD);
            values.put(DIYOverlayCategory.DESCRIPTION, specie.description);
            values.put(DIYOverlayCategory.ISLOCAL, AbstractDownloadManager.DOWNLOAD_NOT);
            getContentResolver().insert(dc, values);
        }

        return downloads;
    }

    private ResourceItem transform(OverlayForm of){
        ResourceItem item = new ResourceItem();
        item.setId(of.id);
        item.setIconUrl(of.icon);
        item.setName(of.name);
        item.setResourceType(AssetInfo.TYPE_DIYOVERLAY);
        item.setResourceUrl(of.url);
        item.setFontType((int)of.fontId);
        return item;
    }

//    private ResourceItem transform(AssetInfo ai){
//        ResourceItem item = new ResourceItem();
//        item.setId(ai.getID());
//        item.setIconUrl(ai.getIconURIString());
//        item.setName(ai.getTitle());
//        item.setResourceType(AssetInfo.TYPE_DIYOVERLAY);
//        item.setResourceUrl(ai.getResourceUrl());
//        return item;
//    }

    private AssetInfo findLocalItem(List<? extends AssetInfo> list, OverlayForm overlay){
        for(AssetInfo ai : list){
            if(overlay.id == ai.getID()){
                return ai;
            }
        }
        return null;
    }

    private AssetGroup findOverlayGroupFromCache(OverlayGroup specie){
        List<? extends AssetGroup> groups = _AssetRepo.findDIYCategory();
        for(AssetGroup g : groups){
            if(g.getGroupId() == specie.id){
                return g;
            }
        }
        return null;
    }

    private final AbstractDownloadManager.ResourceDownloadListener listener =
            new AbstractDownloadManager.ResourceDownloadListener() {
                @Override
                public void onDownloadStart(ResourceItem id) {
                    for(OverlayGroup cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_RUNNING;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onDownloadFailed(ResourceItem id) {
                    for(OverlayGroup cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_NOT;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    UpdateDialogStatus l = statusListener.get((int)id.getID());
                    if(l != null){
                        l.onFailed();
                    }
                }

                @Override
                public void onDownloadCompleted(ResourceItem id) {
                    for(OverlayGroup cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_COMPLETED;
                            ContentValues values = new ContentValues();
                            values.put(DIYOverlayCategory.ISLOCAL, cf.isLocal);
                            Uri uri = ContentUris.withAppendedId(_Uris.DIY_CATEGORY_ID, cf.id);
                            getContentResolver().update(uri, values, null, null);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    UpdateDialogStatus l = statusListener.get((int)id.getID());
                    if(l != null){
                        l.onCompleted(true);
                    }
                }
            };

}
