package com.duanqu.qupaicustomuidemo.editor.mv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupaicustomuidemo.editor.download.PasterDownloadManager;
import com.duanqu.qupai.effect.asset.ResourceItem;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.editor.EditorAction;
import com.duanqu.qupaicustomuidemo.editor.api.Api;
import com.duanqu.qupaicustomuidemo.editor.manager.EffectManageActivity;
import com.duanqu.qupaiokhttp.HttpRequest;
import com.duanqu.qupaiokhttp.StringHttpRequestCallback;
import com.duanqu.qupaisdk.tools.SingnatureUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import okhttp3.Headers;

import java.util.ArrayList;
import java.util.List;

public class DownLoadFontActivity extends FragmentActivity {

	public static void show(Context context) {
		Intent intent = new Intent(context, DownLoadFontActivity.class);
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

    private List<CaptionForm> list = new ArrayList<>();
    private MyAdapter adapter;
    private ListView mListView;
    private TextView title;
    PasterDownloadManager pasterCategoryDownloadManager;

    private AssetRepository _AssetRepo;

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
    }

    private void updateCategoryStatus(final List<CaptionForm> dss){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                for(CaptionForm specie : dss){
                    AssetID assetID = new AssetID(AssetInfo.TYPE_FONT, specie.id);
                    AssetInfo info = _AssetRepo.resolveAsset(assetID);
                    if(info != null && info.isAvailable()){
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
                in.setClass(DownLoadFontActivity.this, EffectManageActivity.class);
                in.putExtra("kind", AssetRepository.Kind.FONT);
                in.putExtra("showCurrentFragment", CURRENT_FRAGMENT_PASTER);
                startActivityForResult(in, REQUEST_RESOURCE_MANAGER_CODE);
            }
        });
        mListView = (ListView) findViewById(R.id.channelSingleListView);

        mListView.setDrawingCacheEnabled(false);
        title = (TextView)findViewById(R.id.download_title);
        title.setText(R.string.qupai_btn_text_download_font);

    }

    private void initData() {
        //TODO 整个MV下载的接口获取是没有经过包装的.
        HttpRequest.get(Api.getInstance().getApiUrl(Api.FONT_RESOURCE_CATEGORY) + "?packageName=" + getPackageName()
                + "&signature=" + SingnatureUtils.getSingInfo(this) +
                "&cursor=0" + "&type=1", new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(Headers headers, String s) {
                super.onSuccess(headers, s);
                try {
                    list = new JSONSupportImpl().readListValue(s, new TypeReference<List<CaptionForm>>() {
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
        public CaptionForm getItem(int position) {
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
        private CaptionForm specie;

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
            }
        };

        public void setData(CaptionForm data){
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
                pasterCategoryDownloadManager.unRegistResourceDownloadProgressListener(data.id, pl);
            }else if(isLocal == AbstractDownloadManager.DOWNLOAD_UNCOMPLETED){
                download.setEnabled(true);
                download.setVisibility(View.VISIBLE);
                download.setText(R.string.qupai_download_goon);
                use.setVisibility(View.GONE);
                downloadPb.setVisibility(View.GONE);
                pasterCategoryDownloadManager.unRegistResourceDownloadProgressListener(data.id, pl);
            }else if(isLocal == AbstractDownloadManager.DOWNLOAD_RUNNING){
                download.setVisibility(View.GONE);
                use.setVisibility(View.GONE);
                downloadPb.setVisibility(View.VISIBLE);
                if(pasterCategoryDownloadManager.isResourceDownloading(data.id)){
                    download.setEnabled(false);
                }else{
                    downloadPb.setProgress(0);
                    download(specie);
                }
                pasterCategoryDownloadManager.registResourceDownloadProgressListener(data.id, pl);
            }else{
                pasterCategoryDownloadManager.unRegistResourceDownloadProgressListener(data.id, pl);
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
        in.setData(EditorAction.useFont(id));
        setResult(Activity.RESULT_OK, in);
        finish();
    }

    public void download(CaptionForm specie){
        ResourceItem item = new ResourceItem();
        item.setId(specie.id);
        item.setIconUrl(specie.icon);
        item.setName(specie.name);
        item.setFontType(specie.category);
        item.setBannerUrl(specie.banner);
        item.setResourceType(AssetInfo.TYPE_FONT);
        item.setResourceUrl(specie.url);
        pasterCategoryDownloadManager.downloadResourcesItem(this, item, null, listener);
    }

    private final AbstractDownloadManager.ResourceDownloadListener listener =
            new AbstractDownloadManager.ResourceDownloadListener() {
                @Override
                public void onDownloadStart(ResourceItem id) {
                    for(CaptionForm cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_RUNNING;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onDownloadFailed(ResourceItem id) {
                    for(CaptionForm cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_NOT;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onDownloadCompleted(ResourceItem id) {
                    for(CaptionForm cf : list){
                        if(id.getID() == cf.id){
                            cf.isLocal = AbstractDownloadManager.DOWNLOAD_COMPLETED;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            };

}
