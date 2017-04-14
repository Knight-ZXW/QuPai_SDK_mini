package com.duanqu.qupaicustomuidemo.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.dao.bean.ResourceCorrespondence;
import com.duanqu.qupaicustomuidemo.dao.local.client.ConditionRelation;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereNode;
import com.duanqu.qupaicustomuidemo.dao.local.database.DBHelper;
import com.duanqu.qupaicustomuidemo.dao.local.database.SQLiteHelperOrm;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressLint("UseSparseArrays")
public class RecommendMusicProvider extends ContentProvider {

	private static final String MUSIC_MV = "imv_music";
	static final int MUSIC=1;//音乐

	static final int PASTER=3;//贴纸
    static final int PASTER_NEW = 5;
    static final int FONT = 36;
    static final int FONT_NEW = 38;

	static final int RESOURCE = 95;

	static final int RECOMMENDDIYOVERLAY = 16;//推荐diy动图
	static final int LOCALDIYOVERLAY = 18;//本地diy动图

	static final int MV = 20;//本地MV
	static final int LOCALMV = 19;//本地MV

	static final int LOCALFILTER = 24;//本地滤镜

	static final int DIY_CATEGORY = 330;
	static final int DIYOVERLAY = 200;//diy动图
	static final int DIYOVERLAY_CATEGORY = 201;//diy动图分类
	static final int DIYOVERLAY_CATEGORY_DOWNLOAD = 208;//diy动图分类
	static final int DIYOVERLAY_DOWNLOAD = 211;
//	private static final int DIYOVERLAY_GROUP = 202;//diy动图分组
	static final int DIYOVERLAY_CATEGORY_CONTENT = 203;//diy动图
    static final int DIYOVERLAY_CATEGORY_ID = 244;

	static final int MUSIC_ID = 505;
	static final int MV_ID = 506;
	static final int DIY_ID = 507;
    static final int FONT_ID = 508;

	private String[] contentColumes = new String[]{
			VideoEditResources.ID,
			VideoEditResources.RESOURCENAME,
			VideoEditResources.RESOURCETYPE,
			VideoEditResources.RESOURCEICON,
            VideoEditResources.RESOURCEBANNER,
			VideoEditResources.RESOURCEURL,
			VideoEditResources.RESOURCELOCALPATH,
			VideoEditResources.RECOMMEND,
            VideoEditResources.FONTTYPE,
			VideoEditResources.ISLOCAL,
			VideoEditResources.DESCRIPTION,
			VideoEditResources.DOWNLOADTIME
	};

	private String[] diyoverlayCategory = new String[]{
			DIYOverlayCategory.NAME,
			DIYOverlayCategory.ID,
			DIYOverlayCategory.TYPE,
			DIYOverlayCategory.DESCRIPTION,
			DIYOverlayCategory.ICON,
			DIYOverlayCategory.PRIORITY,
			DIYOverlayCategory.RECOMMEND,
			DIYOverlayCategory.ISLOCAL,
	};

	private DBHelper<VideoEditResources> dbhelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final int matcher=MATCHER.match(uri);
		WhereNode where;
		int result = -1;
		switch(matcher){
		case MUSIC:
			if(selection != null && selection.equals(MUSIC_MV)) {
                where = new WhereNode.WhereBuilder()
                        .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MV_MUSIC)
                        .in(VideoEditResources.ID, selectionArgs).build();
			}else {
                where = new WhereNode.WhereBuilder()
                        .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MUSIC)
                        .in(VideoEditResources.ID, selectionArgs).build();
			}
			result = dbhelper.delete(VideoEditResources.class, where);
			break;
		case MV:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MV_MUSIC)
                    .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_SHADER_MV)
                    .or().in(VideoEditResources.ID, selectionArgs).build();
			result = dbhelper.delete(VideoEditResources.class, where);
			break;
		case DIYOVERLAY:
			break;
		case DIYOVERLAY_CATEGORY:
			result = deleteDIYOverlay(selectionArgs);
			uri = _Uris.getDIYCategory(selectionArgs);
		    break;
        case FONT:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_FONT)
                    .in(VideoEditResources.ID, selectionArgs).build();
            result = dbhelper.delete(VideoEditResources.class, where);
            if(TextUtils.equals(selection, "notify")){
                return result;
            }
            break;
		}
		if(result > 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return result;
	}

	private int deleteDIYOverlay(Object[] ids){
		int result = 0;
		SQLiteHelperOrm db = new SQLiteHelperOrm(getContext());
		try{
			Dao<DIYOverlayCategory, Integer> categoryDao = db.getDao(DIYOverlayCategory.class);
			Dao<ResourceCorrespondence, Long> correspondenceDao = db.getDao(ResourceCorrespondence.class);
			Dao<VideoEditResources, Long> resourcesDao = db.getDao(VideoEditResources.class);
			DeleteBuilder<DIYOverlayCategory, Integer> deleteBuilder = categoryDao.deleteBuilder();
			deleteBuilder.where().in(DIYOverlayCategory.ID, ids);
			categoryDao.delete(deleteBuilder.prepare());
			List<Long> resourceIds = new ArrayList<>();
			QueryBuilder<ResourceCorrespondence, Long> queryBuilder = correspondenceDao.queryBuilder();
			queryBuilder.where().in(ResourceCorrespondence.CATEGORY_ID, ids);
			List<ResourceCorrespondence> resourceCorrespondences = correspondenceDao.query(queryBuilder.prepare());
			for(ResourceCorrespondence rc : resourceCorrespondences){
				resourceIds.add(rc.resourceId);
			}

			DeleteBuilder<ResourceCorrespondence, Long> delb = correspondenceDao.deleteBuilder();
			delb.where().in(ResourceCorrespondence.CATEGORY_ID, ids);
			correspondenceDao.delete(delb.prepare());

			DeleteBuilder<VideoEditResources, Long> delresb = resourcesDao.deleteBuilder();
			delresb.where().in(VideoEditResources.ID, resourceIds);
			resourcesDao.delete(delresb.prepare());
			result = 1;
		}catch (SQLException e){
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    final int matcher=MATCHER.match(uri);
	    int result;
	    if(matcher == DIYOVERLAY_CATEGORY){
	        result = insertDIYCategory(uri, values);
	    }else{
	        VideoEditResources srf = new VideoEditResources(
	                values.getAsLong(VideoEditResources.ID),
	                values.getAsString(VideoEditResources.RESOURCENAME),
	                values.getAsString(VideoEditResources.RESOURCEICON),
	                values.getAsString(VideoEditResources.RESOURCEURL),
	                values.getAsString(VideoEditResources.DESCRIPTION),
	                values.getAsString(VideoEditResources.RESOURCELOCALPATH),
	                values.getAsInteger(VideoEditResources.RESOURCETYPE),
					values.getAsInteger(VideoEditResources.FONTTYPE),
	                values.getAsInteger(VideoEditResources.ISLOCAL),
	                values.getAsInteger(VideoEditResources.RECOMMEND),
	                values.getAsLong(VideoEditResources.DOWNLOADTIME));
			srf.setBannerUrl(values.getAsString(VideoEditResources.RESOURCEBANNER));
	        result = dbhelper.createOrUpdate(srf);
	    }

		if(result > 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return uri;
	}

	private int insertDIYCategory(Uri uri, ContentValues values){
		long[] ids = _Uris.getQueryIds(uri);
		DIYOverlayCategory category = new DIYOverlayCategory(
				values.getAsInteger(DIYOverlayCategory.ID),
				values.getAsString(DIYOverlayCategory.NAME),
				values.getAsInteger(DIYOverlayCategory.TYPE),
				values.getAsString(DIYOverlayCategory.ICON),
				values.getAsInteger(DIYOverlayCategory.PRIORITY),
				values.getAsInteger(DIYOverlayCategory.RECOMMEND),
				values.getAsString(DIYOverlayCategory.DESCRIPTION),
				values.getAsInteger(DIYOverlayCategory.ISLOCAL));
		final List<ResourceCorrespondence> resourceCorrespondences = new ArrayList<>();
		for(long id : ids){
			ResourceCorrespondence correspondence = new ResourceCorrespondence();
			correspondence.categoryId = category.getId();
			correspondence.resourceId = id;
			correspondence.resourceType = AssetInfo.TYPE_DIYOVERLAY;
			resourceCorrespondences.add(correspondence);
		}

		int result = 0;
		SQLiteHelperOrm db = new SQLiteHelperOrm(getContext());
		try{
			final Dao<ResourceCorrespondence, Long> correspondenceDao = db.getDao(ResourceCorrespondence.class);
			Dao<DIYOverlayCategory, Integer> categoryDao = db.getDao(DIYOverlayCategory.class);
			categoryDao.deleteById(category.getId());
			result = categoryDao.create(category);
			correspondenceDao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for(ResourceCorrespondence rc : resourceCorrespondences){
						correspondenceDao.create(rc);
					}
					return null;
				}
			});
		}catch (SQLException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null) {
				db.close();
			}

		}
		return result;
	}

    ProviderUris   _Uris;

    static UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	@Override
	public boolean onCreate() {

	    _Uris = new ProviderUris(getContext());

        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_MUSIC, MUSIC);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_PASTER, PASTER);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY, DIYOVERLAY);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_FONT, FONT);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_LOCAL, LOCALDIYOVERLAY);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_RECOMMEND, RECOMMENDDIYOVERLAY);

        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_CATEGORY_DOWNLOAD, DIYOVERLAY_CATEGORY_DOWNLOAD);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_DOWNLOAD, DIYOVERLAY_DOWNLOAD);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_CATEGORY, DIYOVERLAY_CATEGORY);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_CATEGORY_ID, DIYOVERLAY_CATEGORY_ID);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_CATEGORY_ALL, DIY_CATEGORY);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_CATEGORY_CONTENT, DIYOVERLAY_CATEGORY_CONTENT);

        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_MV, MV);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_MV_LOCAL, LOCALMV);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_FILTER_LOCAL, LOCALFILTER);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_RESOURCE, RESOURCE);

        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_MUSIC_ID, MUSIC_ID);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_DIY_ID, DIY_ID);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_MV_ID, MV_ID);
        MATCHER.addURI(_Uris.AUTHORITY, ProviderUris.PATH_FONT_ID, FONT_ID);

		dbhelper=new DBHelper<>(getContext());
		return true;
	}

	private List<DIYOverlayCategory> getRecommendCategory(){
	    DBHelper<DIYOverlayCategory> dbhelper = new DBHelper<>(getContext());
	    WhereNode where = new WhereNode.WhereBuilder()
                .eq(DIYOverlayCategory.RECOMMEND, VideoEditBean.RECOMMEND_SERVER).build();
	    List<DIYOverlayCategory> list = dbhelper.query(DIYOverlayCategory.class, null, where, null, null,
                null, null, null, false);
        return list;
    }

   private List<DIYOverlayCategory> getLocalCategory(){
        DBHelper<DIYOverlayCategory> dbhelper = new DBHelper<>(getContext());
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(DIYOverlayCategory.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .ne(DIYOverlayCategory.ID, 200).build();
        List<DIYOverlayCategory> list = dbhelper.queryForFieldValues(DIYOverlayCategory.class, where);
        return list;
    }

    private List<DIYOverlayCategory> getDownloadCategory(){
        DBHelper<DIYOverlayCategory> dbhelper = new DBHelper<>(getContext());
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(DIYOverlayCategory.RECOMMEND, VideoEditBean.RECOMMEND_SERVER_DELETE)
                .eq(DIYOverlayCategory.RECOMMEND, VideoEditBean.RECOMMEND_DOWNLOAD)
                .build(ConditionRelation.OR);
        List<DIYOverlayCategory> list = dbhelper.query(DIYOverlayCategory.class, null, where, null, null,
                DIYOverlayCategory.DOWNLOADTIME, null, null, false);
        Collections.sort(list, new Comparator<DIYOverlayCategory>() {

            @Override
            public int compare(DIYOverlayCategory object1,
                    DIYOverlayCategory object2) {
                return object1.getPriority() - object2.getPriority();
            }
        });
        return list;
    }

    private List<DIYOverlayCategory> getRecommendCategoryForManage(){
        DBHelper<DIYOverlayCategory> dbhelper = new DBHelper<>(getContext());
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(DIYOverlayCategory.RECOMMEND, VideoEditBean.RECOMMEND_SERVER)
                .ne(DIYOverlayCategory.ISLOCAL, AbstractDownloadManager.DOWNLOAD_NOT)
                .ne(DIYOverlayCategory.ISLOCAL, AbstractDownloadManager.DOWNLOAD_RUNNING)
                .build();
        List<DIYOverlayCategory> list = dbhelper.query(DIYOverlayCategory.class, null, where, null, null,
                null, null, null, false);
        return list;
    }

    private List<DIYOverlayCategory> getDownloadCategoryForManage(){
        return new ArrayList<>();
    }

    private List<VideoEditResources> getRecommendResources(int type) {
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, type)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_SERVER)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null, true);
        checkResourcesExits(list);

        return list;
    }

    private List<VideoEditResources> getDownloadResources(int type) {
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, type)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_DOWNLOAD)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.DOWNLOADTIME, null, null, false);
        checkResourcesExits(list);

        return list;
    }

    private List<VideoEditResources> getLocalResources(int type) {
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, type)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.RECOMMEND, null, null, true);

        return list;
    }

    private List<VideoEditResources> getRecommendFonts(){
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_FONT).build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null, true);

        where = new WhereNode.WhereBuilder()
                .ne(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_FONT).build();
        list.addAll(dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.RECOMMEND, null, null, true));

        checkResourcesExits(list);

        return list;
    }

    private List<VideoEditResources> getDownloadMusic() {
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MV_MUSIC)
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MUSIC)
                .or()
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_DOWNLOAD)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.DOWNLOADTIME, null, null, false);
        checkResourcesExits(list);

        return list;
    }

    private List<VideoEditResources> getLocalMusic() {
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MV_MUSIC)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null, true);
        where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_MUSIC)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL)
                .build();
        list.addAll(dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null, true));
        return list;
    }

    private List<VideoEditResources> getDeleteResources(int type) {
    	WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, type)
                .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_SERVER_DELETE)
                .build();
        List<VideoEditResources> list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.DOWNLOADTIME, null, null, false);
        checkResourcesExits(list);
        return list;
    }

    private List<VideoEditResources> getResourcesById(Uri uri, int type){
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(VideoEditResources.RESOURCETYPE, type)
                .eq(VideoEditResources.ID, ContentUris.parseId(uri))
                .build();
        return dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null);
    }

	public List<VideoEditResources> findCategoryResource(long categoryId){
		SQLiteHelperOrm db = new SQLiteHelperOrm(getContext());
		List<ResourceCorrespondence> correspondences = null;
		try{
			Dao<ResourceCorrespondence, Long> correspondenceDao = db.getDao(ResourceCorrespondence.class);
			Dao<VideoEditResources, Long> resourceDao = db.getDao(VideoEditResources.class);
			QueryBuilder<ResourceCorrespondence, Long> qb = correspondenceDao.queryBuilder();
			qb.where().eq(ResourceCorrespondence.CATEGORY_ID, categoryId);
			correspondences = correspondenceDao.query(qb.prepare());
			List<Long> matterIds = new ArrayList<>();
			for(ResourceCorrespondence rc : correspondences){
				matterIds.add(rc.resourceId);
			}

			QueryBuilder<VideoEditResources, Long> qbR = resourceDao.queryBuilder();
			qbR.where().eq(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_DIYOVERLAY).and().in(VideoEditResources.ID, matterIds);

			return resourceDao.query(qbR.prepare());
		}catch(java.sql.SQLException e){
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}

		return new ArrayList<>();
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final int matcher=MATCHER.match(uri);
        List<VideoEditResources> list = null;
		WhereNode where;
		Cursor cursor = null;
		MatrixCursor c;
		String groupby = null;
		switch(matcher){
        case FONT:
            cursor = addData2Cursor(getRecommendFonts());
        break;
		case MUSIC: {
		    c = addData2Cursor(getRecommendResources(VideoEditBean.TYPE_MUSIC));
		    addData2Cursor(c, getDownloadMusic());
		    addData2Cursor(c, getLocalMusic());
		    cursor = addData2Cursor(c, getDeleteResources(VideoEditBean.TYPE_MUSIC));
		}
		break;
		case PASTER:

			break;
		case LOCALMV:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_SHADER_MV)
                    .eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL).build();
			list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, null, null, null, true);
			cursor = addData2Cursor(list);
			break;
		case MV:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, VideoEditBean.TYPE_SHADER_MV)
                    .ne(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_CLIENT_DELETE)
                    .build();
            list = dbhelper.query(VideoEditResources.class, contentColumes, where, null, null, VideoEditResources.RECOMMEND, null, null, true);
            cursor = addData2Cursor(list);
			break;
		case LOCALFILTER:
			cursor = addData2Cursor(getLocalResources(VideoEditBean.TYPE_SHADER_EFFECT));
			break;
		case MUSIC_ID:
			list = getResourcesById(uri, VideoEditBean.TYPE_MUSIC);
			cursor = addData2Cursor(list);
			break;
		case MV_ID:
			list = getResourcesById(uri, VideoEditBean.TYPE_SHADER_MV);
			cursor = addData2Cursor(list);
			break;
		case DIY_ID:
			list = getResourcesById(uri, VideoEditBean.TYPE_DIYOVERLAY);
			cursor = addData2Cursor(list);
			break;
        case FONT_ID:
            list = getResourcesById(uri, VideoEditBean.TYPE_FONT);
            cursor = addData2Cursor(list);
            break;
		case DIYOVERLAY_CATEGORY:
		    c = addCategoryData2Cursor(getRecommendCategory());
		    addCategoryData2Cursor(c, getLocalCategory());
		    cursor = addCategoryData2Cursor(c, getDownloadCategory());
			break;
		case DIYOVERLAY_CATEGORY_CONTENT:
		    long categoryId = ContentUris.parseId(uri);
			cursor = addData2Cursor(findCategoryResource(categoryId));
			break;
		case RESOURCE:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, selectionArgs[0])
                    .eq(VideoEditResources.ID, selectionArgs[1]).build();
			list = dbhelper.query(VideoEditResources.class, contentColumes, where, groupby, null, null, null, null);
			cursor = addData2Cursor(list);
			break;
		case DIYOVERLAY_CATEGORY_DOWNLOAD:
            cursor = addCategoryData2Cursor(getDownloadCategoryForManage());
		    break;
//		case DIYOVERLAY_DOWNLOAD:
//		    cursor = addData2Cursor(getDIYResourcesDownload());
//            break;
//		case DIY_CATEGORY:
//		    cursor = addData2Cursor(getDIYOverlayByCategory(selectionArgs[0]));
//		    break;
		}

		return cursor;
	}

	private MatrixCursor addCategoryData2Cursor(List<DIYOverlayCategory> list){
        MatrixCursor cursor = new MatrixCursor(diyoverlayCategory);
        for(DIYOverlayCategory category : list){
            cursor.addRow(new Object[]{
                    category.getName(),
                    category.getId(),
					category.getType(),
                    category.getDescription(),
                    category.getIconUrl(),
                    category.getPriority(),
					category.getRecommend(),
                    category.getIsLocal()});
        }
        return cursor;
    }

	private MatrixCursor addCategoryData2Cursor(MatrixCursor c, List<DIYOverlayCategory> list){
		for(DIYOverlayCategory category : list){
			c.addRow(new Object[]{
					category.getName(),
					category.getId(),
					category.getType(),
					category.getDescription(),
					category.getIconUrl(),
                    category.getPriority(),
					category.getRecommend(),
                    category.getIsLocal()});
		}
        return c;
	}

	private MatrixCursor addData2Cursor(List<VideoEditResources> list){
		MatrixCursor cursor = new MatrixCursor(contentColumes);
		for(VideoEditResources res : list){
			cursor.addRow(new Object[]{
					res.getId(),
					res.getName(),
					res.getType(),
					res.getIconUrl(),
                    res.getBannerUrl(),
					res.getResourceUrl(),
					res.getLocalPath(),
					res.getRecommend(),
                    res.getCategory(),
					res.getIsLocal(),
					res.getDescription(),
					res.getDownloadTime()
			});
		}
		return cursor;
	}

    private MatrixCursor addData2Cursor(MatrixCursor c, List<VideoEditResources> list) {
        for (VideoEditResources res : list) {
            c.addRow(new Object[]{
                    res.getId(),
                    res.getName(),
                    res.getType(),
                    res.getIconUrl(),
                    res.getBannerUrl(),
                    res.getResourceUrl(),
                    res.getLocalPath(),
                    res.getRecommend(),
                    res.getCategory(),
                    res.getIsLocal(),
                    res.getDescription(),
                    res.getDownloadTime()
            });
        }
        return c;
    }

	private void checkResourcesExits(List<VideoEditResources> list){
		if(list.size() == 0){
			return ;
		}
		Iterator<VideoEditResources> iterator = list.iterator();
		while(iterator.hasNext()){
			VideoEditResources srf = iterator.next();
			int recommend = srf.getRecommend();
			int local = srf.getIsLocal();
			if(recommend == VideoEditBean.RECOMMEND_LOCAL){
				continue;
			}
            if(local == AbstractDownloadManager.DOWNLOAD_COMPLETED){
                Uri uri = Uri.parse(srf.getLocalPath());
                File file = new File(uri.getPath());
                if(file.exists()){
                    continue;
                }
                if(recommend == VideoEditBean.RECOMMEND_DOWNLOAD){
                    dbhelper.remove(srf);
                    iterator.remove();
                }else{
                    srf.setIsLocal(0);
                    srf.setLocalPath(null);
                    dbhelper.update(srf);
                }
            }

		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int matcher=MATCHER.match(uri);
		int result = -1;
        WhereNode where;
		switch (matcher) {
		case DIYOVERLAY_CATEGORY:
			result = updateDIYCategory(values, selectionArgs[0]);
			if(result > 0 && !TextUtils.equals(selection, "notify")){
                getContext().getContentResolver().notifyChange(uri, null);
            }
			return result;
		case DIYOVERLAY_CATEGORY_ID:
			result = updateDIYCategory(values, String.valueOf(ContentUris.parseId(uri)));
			if(result > 0 && !TextUtils.equals(selection, "notify")){
				getContext().getContentResolver().notifyChange(uri, null);
			}
			return result;
        case DIY_ID:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_DIYOVERLAY)
                    .eq(VideoEditResources.ID, ContentUris.parseId(uri)).build();
            break;
		case FONT_ID:
			where = new WhereNode.WhereBuilder()
					.eq(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_FONT)
					.eq(VideoEditResources.ID, ContentUris.parseId(uri)).build();
			break;
		case MUSIC_ID:
			where = new WhereNode.WhereBuilder()
					.eq(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_MUSIC)
					.eq(VideoEditResources.ID, ContentUris.parseId(uri)).build();
			break;
		case MV_ID:
			where = new WhereNode.WhereBuilder()
					.eq(VideoEditResources.RESOURCETYPE, AssetInfo.TYPE_SHADER_MV)
					.eq(VideoEditResources.ID, ContentUris.parseId(uri)).build();
			break;
		default:
            where = new WhereNode.WhereBuilder()
                    .eq(VideoEditResources.RESOURCETYPE, selectionArgs[0])
                    .eq(VideoEditResources.ID, selectionArgs[1]).build();
			result = dbhelper.update(VideoEditResources.class, values, where);
			if(result > 0 && !TextUtils.equals(selection, "notify")){
				getContext().getContentResolver().notifyChange(uri, null);
			}
			return result;
		}

		result = dbhelper.update(VideoEditResources.class, values, where);
		if(result > 0 && !TextUtils.equals(selection, "notify")){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return result;
	}

	private int updateDIYCategory(ContentValues values, String id){
        WhereNode where = new WhereNode.WhereBuilder()
                .eq(DIYOverlayCategory.ID, id).build();
		DBHelper<DIYOverlayCategory> categoryHelper = new DBHelper<>(getContext());
		return categoryHelper.update(DIYOverlayCategory.class, values, where);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
	}

}
