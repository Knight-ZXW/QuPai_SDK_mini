package com.duanqu.qupaicustomuidemo.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.duanqu.qupai.asset.AssetBundle;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.AssetRepositoryClient;
import com.duanqu.qupai.asset.DownloadManager;
import com.duanqu.qupai.asset.FontResolver;
import com.duanqu.qupai.asset.RepositoryEditor;
import com.duanqu.qupai.effect.FontManager;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.render.SceneFactoryClientImpl;
import com.duanqu.qupai.stage.resource.MVTemplate;
import com.duanqu.qupaicustomuidemo.dao.bean.DIYCategory;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.download.PasterDownloadManager;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Auth:Mulberry
 * 实现资源的基本管理.目前所有的资源都通过数据库管理.
 */
public class PackageAssetRepository extends AssetRepository implements RepositoryEditor {

    private static final String TAG = "asset-repo";
    MyContentObserver observer;
    private Kind[] categorys;
    private final Context context;
    private final ProviderUris _Uris;
    private final JSONSupport _JSON;
    private final FontManager fontManager;
    private AssetStoreCache assetCache;
    private final PasterDownloadManager downloadManager;

    public PackageAssetRepository(Context context, ProviderUris uris, JSONSupport json) {

        this.context = context;
        _Uris = uris;
        _JSON = json;

        categorys = new Kind[6];
        categorys[0] = Kind.FILTER;
        categorys[1] = Kind.CAPTION;
        categorys[2] = Kind.FONT;
        categorys[3] = Kind.DIY;
        categorys[4] = Kind.MV;
        categorys[5] = Kind.SOUND;

        fontManager = new FontManager(context, this);
        downloadManager = new PasterDownloadManager(this);
        assetCache = new AssetStoreCache();
        observer=new MyContentObserver();
        ContentResolver cr = context.getContentResolver();

        cr.registerContentObserver(_Uris.DIY, true, observer);
        cr.registerContentObserver(_Uris.FONT, true, observer);
        cr.registerContentObserver(_Uris.MV, true, observer);
        cr.registerContentObserver(_Uris.MUSIC, true, observer);
        cr.registerContentObserver(_Uris.DIY_ID, true, observer);
        cr.registerContentObserver(_Uris.DIY_CATEGORY, true, observer);
        cr.registerContentObserver(_Uris.MV_ID, true, observer);
        cr.registerContentObserver(_Uris.MUSIC_ID, true, observer);
        cr.registerContentObserver(_Uris.FONT_ID, true, observer);

    }

    @Override
    public void updateAsset(AssetInfo asset) {
        ContentResolver cr = context.getContentResolver();

        if(asset.getType() == AssetInfo.TYPE_SHADER_MV){
            updateMVMusic(cr, asset);
        }

        ContentValues values = new ContentValues();
        values.put(VideoEditResources.ID, asset.getID());
        values.put(VideoEditResources.RESOURCENAME, asset.getTitle());
        values.put(VideoEditResources.RESOURCEICON, asset.getContentURIString());
        values.put(VideoEditResources.RESOURCEBANNER, asset.getBannerURIString());
        values.put(VideoEditResources.RESOURCEURL, asset.getResourceUrl());
        values.put(VideoEditResources.DESCRIPTION, asset.getTitle());

        values.put(VideoEditResources.FONTTYPE, asset.getFontType());
        values.put(VideoEditResources.RESOURCELOCALPATH, asset.getContentURIString());
        values.put(VideoEditResources.RESOURCETYPE, asset.getType());
        values.put(VideoEditResources.ISLOCAL, asset.getResourceStatus());
        values.put(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_DOWNLOAD);
        values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
        cr.insert(_Uris.AUTHORITY_URI, values);

        Uri uri;
        String[] selectArgs = null;
        if(asset.getType() == AssetInfo.TYPE_FONT){
            uri = _Uris.FONT_ID;
        }else if(asset.getType() == AssetInfo.TYPE_DIYOVERLAY){
            uri = _Uris.DIY_ID;
        }else if(asset.getType() == AssetInfo.TYPE_MUSIC){
            uri = _Uris.MUSIC_ID;
        }else if(asset.getType() == AssetInfo.TYPE_SHADER_MV){
            uri = _Uris.MV_ID;
        }else{
            uri = _Uris.AUTHORITY_URI;
            selectArgs = new String[]{String.valueOf(asset.getType()),
                    String.valueOf(asset.getID())};
        }
        uri = ContentUris.withAppendedId(uri, asset.getID());
        context.getContentResolver().update(uri, values, null, selectArgs);
    }

    @Override
    public Kind[] findCategory() {
        return categorys;
    }

    @Override
    public FontResolver getFontResolver() {
        return fontManager;
    }

    @Override
    public AssetInfo resolveAsset(AssetID asset_id) {
        VideoEditBean found;
        long uid = asset_id.getUID();
        switch (asset_id.type) {
            case AssetInfo.TYPE_MUSIC:
            case AssetInfo.TYPE_MV_MUSIC:
                found = find(getMusicResources(), uid);
                break;
            case AssetInfo.TYPE_SHADER_EFFECT:
                found = find(getFilterList(), uid);
                break;
            case AssetInfo.TYPE_SHADER_MV:
                found = find(getMVList(), uid);
                break;
            case AssetInfo.TYPE_FONT:
                found = find(getFontResources(), uid);
                break;
            case AssetInfo.TYPE_DIYOVERLAY:
                found = assetCache.getDIYById(asset_id.id);
                break;
            default:
                return null;
        }

        return found;
    }

    private List<VideoEditBean> getFilterList() {
        List<VideoEditBean> filter = assetCache.getFliterEffects();
        if (filter != null) {
            return filter;
        }

        List<VideoEditBean> list = getDataFromCursor(_Uris.FILTER_LOCAL);

        assetCache.saveFliterEffects(list);

        return list;
    }

    @Override
    public List<? extends AssetInfo> find(Kind kind) {
        switch (kind) {
            case FILTER:
                return getFilterList();
            case MV:
                return getMVList();
            case SOUND:
                return getMusicResources();
            case FONT:
                return getFontResources();
        }
        return null;
    }

    private static AssetInfo findAssetInfo(List<AssetInfo> list, long uid) {
        for (AssetInfo bean : list) {
            if (bean.getUID() == uid) {
                return bean;
            }
        }

        return null;
    }

    private static VideoEditBean find(List<VideoEditBean> list, long uid) {
        for (VideoEditBean bean : list) {
            if (bean.getUID() == uid) {
                return bean;
            }
        }

        return null;
    }

    @Override
    public AssetBundle resolveAssetBundle(AssetID asset_id) {
        return (AssetBundle) resolveAsset(asset_id);
    }

    @Override
    public AssetInfo find(Kind diy, long id) {
        List<AssetInfo> list = new ArrayList<>();
        list.addAll(find(diy));
        return findAssetInfo(list, id);
    }

    @Override
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Override
    public RepositoryEditor getEditor() { return this; }

    @Override
    public List<? extends AssetGroup> findDIYCategory() {
        List<DIYCategory> cs = assetCache.getDIYCategorys();
        if(cs.size() > 0){
            return cs;
        }
        cs = getAllCategory();
        assetCache.saveDIYCategorys(cs);
        return cs;
    }

    @Override
    public List<? extends AssetInfo> findDIYCategoryContent(@Nonnull int subCategoryId) {
        List<VideoEditBean> ps = assetCache.getDIYCategoryContent(subCategoryId);//查看本地内存缓存是否有
        if(ps.size() > 0){
            return ps;
        }
        Uri uri = ContentUris.withAppendedId(_Uris.DIY_CONTENT, subCategoryId);
        ps = getDataFromCursor(uri);
        assetCache.saveDIYCategoryContent(subCategoryId, ps);
        return ps;
    }

    @Override
    public List<? extends AssetGroup> findDIYCategory(@Nonnull int subCategoryId) {
        return null;
    }

    private Map<String, Integer> columnIndexs = new HashMap<>();

    private List<VideoEditBean> getMVList() {
        List<VideoEditBean> MVList = assetCache.getMVEffects();
        if (MVList != null) {
            return MVList;
        }

        List<VideoEditBean> list = getDataFromCursor(_Uris.MV);

        assetCache.saveMVEffects(list);

        return list;
    }

    private List<VideoEditBean> getFontResources() {
        List<VideoEditBean> fontList = assetCache.getFontEffects();
        if (fontList != null) {
            return fontList;
        }

        List<VideoEditBean> list = getDataFromCursor(_Uris.FONT);

        assetCache.saveFontEffects(list);

        return list;
    }

    private List<VideoEditBean> getMusicResources() {
        List<VideoEditBean> musicList = assetCache.getMusicEffects();
        if (musicList != null) {
            return musicList;
        }

        List<VideoEditBean> list = getDataFromCursor(_Uris.MUSIC);

        assetCache.saveMusicEffects(list);

        return list;
    }

    private int getColumnIndex(Cursor cursor, String columnName, String table){
        String key = table + columnName;
        if(columnIndexs.containsKey(key)){
            return columnIndexs.get(key);
        }
        int columnIndex = cursor.getColumnIndex(columnName);
        columnIndexs.put(key, columnIndex);
        return columnIndex;
    }

    public void onDestroy(){
        context.getContentResolver().unregisterContentObserver(observer);
        columnIndexs.clear();
    }

    private List<DIYCategory> getCategoryFromDB(Uri uri){
        Cursor cursor;
        try{
            cursor = context.getContentResolver().query(uri, null, null, null, null);
        }catch (Throwable tr){
            Log.e("DataProvider", "getDataFromCursor", tr);
            cursor = null;
        }

        if(cursor != null){
            List<DIYCategory> list = new ArrayList<>();
            while(cursor.moveToNext()){
                int categoryId = cursor.getInt(getColumnIndex(cursor, DIYOverlayCategory.ID, "diy_category"));
                String categoryName = cursor.getString(getColumnIndex(cursor, DIYOverlayCategory.NAME, "diy_category"));
                String iconUrl = cursor.getString(getColumnIndex(cursor, DIYOverlayCategory.ICON, "diy_category"));
                int recommend= cursor.getInt(getColumnIndex(cursor, DIYOverlayCategory.RECOMMEND, "diy_category"));
                int isLocal = cursor.getInt(getColumnIndex(cursor, DIYOverlayCategory.ISLOCAL, "diy_category"));
                int type = cursor.getInt(getColumnIndex(cursor, DIYOverlayCategory.TYPE, "diy_category"));

                DIYCategory category = new DIYCategory();
                category.id = categoryId;
                category.type = type;
                category.name = categoryName;
                category.iconUrl = iconUrl;
                category.isLocal = isLocal;
                category.recommend = recommend;
                list.add(category);
            }
            cursor.close();
            return list;
        }
        return new ArrayList<>();
    }

    private List<DIYCategory> getAllCategory(){
        return getCategoryFromDB(_Uris.DIY_CATEGORY);
    }

    private List<VideoEditBean> getDataFromCursor(Uri uri){
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
        } catch (Throwable tr) {
            Log.e("DataProvider", "getDataFromCursor", tr);
            cursor = null;
        }
        if(cursor!=null){
            ArrayList<VideoEditBean> list = new ArrayList<>();
            while(cursor.moveToNext()){
                int id=(int) cursor.getLong(getColumnIndex(cursor, VideoEditResources.ID, "videoEditResource"));
                String name=cursor.getString(getColumnIndex(cursor, VideoEditResources.RESOURCENAME, "videoEditResource"));
                String resourceIconUrl=cursor.getString(getColumnIndex(cursor, VideoEditResources.RESOURCEICON, "videoEditResource"));
                String resourceBannerUrl=cursor.getString(getColumnIndex(cursor, VideoEditResources.RESOURCEBANNER, "videoEditResource"));
                String videoEditSource=cursor.getString(getColumnIndex(cursor, VideoEditResources.RESOURCELOCALPATH, "videoEditResource"));
                String resourceUrl=cursor.getString(getColumnIndex(cursor, VideoEditResources.RESOURCEURL, "videoEditResource"));

                int local=cursor.getInt(getColumnIndex(cursor, VideoEditResources.ISLOCAL, "videoEditResource"));
                boolean isLocal = local == AbstractDownloadManager.DOWNLOAD_COMPLETED;
                int recommend=cursor.getInt(getColumnIndex(cursor, VideoEditResources.RECOMMEND, "videoEditResource"));
                int type=cursor.getInt(getColumnIndex(cursor, VideoEditResources.RESOURCETYPE, "videoEditResource"));
                int fontType=cursor.getInt(getColumnIndex(cursor, VideoEditResources.FONTTYPE, "videoEditResource"));

                VideoEditBean veb=new VideoEditBean(type, id, name, videoEditSource, recommend, isLocal);
                veb._DownLoading = local == AbstractDownloadManager.DOWNLOAD_RUNNING
                        || local == AbstractDownloadManager.DOWNLOAD_UNLOCKED;
                veb.isAutoDownload = local == AbstractDownloadManager.DOWNLOAD_UNLOCKED;
                veb.setRemoteIconURL(resourceIconUrl);
                veb.setRemoteBannerURL(resourceBannerUrl);
                veb.resourceUrl = resourceUrl;
                veb.fonttype = fontType;
                veb.setContentString(videoEditSource);
                list.add(veb);
            }
            cursor.close();
            return list;
        }
        return new ArrayList<>();
    }

    class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if(Build.VERSION.SDK_INT > 15){
                return;
            }
            handleDataChange(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("CONTENT", selfChange+":"+uri);
            handleDataChange(uri);
            super.onChange(selfChange, uri);
        }

        private void handleDataChange(Uri uri){
            Kind cat;
            long id = -1;
            if(uri == null){
                cat = Kind.ALL;
            }else{
                UriMatcher matcher = RecommendMusicProvider.MATCHER;
                switch (matcher.match(uri)){
                    case RecommendMusicProvider.MV:
                    case RecommendMusicProvider.MV_ID:
                        cat = Kind.MV;
                        break;
                    case RecommendMusicProvider.MUSIC:
                    case RecommendMusicProvider.MUSIC_ID:
                        cat = Kind.SOUND;
                        break;
                    case RecommendMusicProvider.DIYOVERLAY_CATEGORY_ID:
                        id = ContentUris.parseId(uri);
                        List<DIYCategory> list = getCategoryFromDB(uri);
                        if (list.size() != 0) {
                            assetCache.removeDIYCategoryContentById((int)id);
                            assetCache.saveDIYCategory(list.get(0));

                        }
                        cat = Kind.DIY;

                        break;
                    case RecommendMusicProvider.DIYOVERLAY_CATEGORY:
                        long[] ids = _Uris.getQueryIds(uri);
                        for(long cid : ids){
                            assetCache.removeDIYCategoryContentById((int)cid);
                        }
                        assetCache.saveDIYCategorys(new ArrayList<DIYCategory>());
                        cat = Kind.DIY;
                        break;
                    case RecommendMusicProvider.DIYOVERLAY:
                        assetCache.saveDIYCategorys(new ArrayList<DIYCategory>());
                        cat = Kind.DIY;
                        break;
                    case RecommendMusicProvider.FONT:
                    case RecommendMusicProvider.FONT_ID:
                        cat = Kind.FONT;
                        break;
                    case RecommendMusicProvider.DIY_ID:
                        cat = Kind.DIY;
                        id = ContentUris.parseId(uri);
                        break;
                    default:
                        cat = Kind.ALL;
                        break;
                }

            }

            dispatchDataChange(cat, (int)id);
        }

    }

    private AssetRepositoryClient _Client;
    private Set<Kind> kinds = new HashSet<>();

    @Override
    public void attachClient(AssetRepositoryClient client) {
        _Client = client;
        if(kinds.size() > 0){
            for(Kind kind : kinds){
                dispatchDataChange(kind, -1);
            }
        }
        kinds.clear();
    }

    @Override
    public void detachClient(AssetRepositoryClient client) {
        _Client = null;
        kinds.clear();
    }

    private void dispatchDataChange(Kind kind, int id) {

        if (kind != null) {
            switch (kind) {
                case MV:
                    assetCache.saveMVEffects(null);
                    assetCache.saveMusicEffects(null);
                    break;
                case SOUND:
                    assetCache.saveMusicEffects(null);
                    break;
                case FONT:
                    assetCache.saveFontEffects(null);
                    break;
            }
        }
        if (_Client != null) {
            _Client.onDataChange(kind);
        }else{
            kinds.add(kind);
        }
    }

    private boolean updateMVMusic(ContentResolver cr, AssetInfo asset){
        SceneFactoryClientImpl scene_client = new SceneFactoryClientImpl(context, _JSON);
        MVTemplate mv = scene_client.readShaderMV(asset.getContentURIString());

        if (mv == null || TextUtils.isEmpty(mv.music)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(VideoEditResources.ID, asset.getID());
        values.put(VideoEditResources.RESOURCENAME, mv.musicName);
        values.put(VideoEditResources.RESOURCEICON, asset.getContentURIString());
        values.put(VideoEditResources.RESOURCEBANNER, asset.getBannerURIString());
        values.put(VideoEditResources.RESOURCEURL, asset.getResourceUrl());
        values.put(VideoEditResources.DESCRIPTION, mv.name);

        values.put(VideoEditResources.FONTTYPE, 0);
        values.put(VideoEditResources.RESOURCELOCALPATH, asset.getContentURIString());
        values.put(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_MV_MUSIC);
        values.put(VideoEditResources.ISLOCAL, AbstractDownloadManager.DOWNLOAD_COMPLETED);
        values.put(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_DOWNLOAD);
        values.put(VideoEditResources.DOWNLOADTIME, System.currentTimeMillis());
        cr.insert(_Uris.MUSIC, values);

        return true;
    }

    @Override
    public boolean onAssetUsed(AssetInfo info) {

        return true;
    }
}
