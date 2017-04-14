package com.duanqu.qupaicustomuidemo.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.render.SceneFactoryClientImpl;
import com.duanqu.qupaicustomuidemo.dao.bean.ResourceCorrespondence;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.stage.resource.MVTemplate;
import com.duanqu.qupai.utils.FileUtils;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.dao.bean.ResourcesBean;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereNode;
import com.duanqu.qupaicustomuidemo.dao.local.database.DBHelper;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.provider.DIYOverlayCategory;
import com.duanqu.qupaisdk.tools.AppGlobalSetting;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CopyResourcesServices extends Service {

	final String TAG = "CopyResourcesServices";

    private JSONSupport _JSON;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
        _JSON = QupaiApplication.getJSONSupport(this);
	}

	@SuppressWarnings("deprecation")
    @Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//int copy = intent.getIntExtra("copy", COPY_MUSIC);
		Log.d("CopyService","onStartCommand");
		asyncTask().execute();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private AsyncTask<Void, Void, Void> asyncTask(){
		AsyncTask<Void, Void, Void> task;
		task = new AsyncTask<Void, Void, Void>(){

			@SuppressLint("UseSparseArrays")
			@Override
			protected Void doInBackground(Void... params) {
				long start = System.currentTimeMillis();
				int version = FileUtils.getVersion(getApplicationContext());
				AppGlobalSetting sp = new AppGlobalSetting(getApplicationContext());
				int copy=sp.getIntGlobalItem("copy_resources_version", -1);
				Log.d(TAG, "是否拷贝:" + copy + " version " + version);
				if(copy != version){

					List<VideoEditResources> list=new ArrayList<>();

					ResourcesBean assets = getAssetsResources(getApplicationContext());

					if(assets == null){
						return null;
					}

					DBHelper<VideoEditResources> dbhelper = new DBHelper<>(getApplicationContext());

					if(assets.resources != null){
						list.addAll(assets.resources);
					}

					if(assets.mv != null){
						list.addAll(assets.mv);
					}

					if(assets.filter != null){
						list.addAll(assets.filter);
					}
					if(assets.mvMusic != null){
						list.addAll(assets.mvMusic);
					}

					if(assets.font != null){
						list.addAll(assets.font);
					}

					if(assets.music != null){
						list.addAll(assets.music);
					}
					WhereNode where = new WhereNode.WhereBuilder()
							.eq(VideoEditResources.RECOMMEND, VideoEditBean.RECOMMEND_LOCAL).build();
					dbhelper.delete(VideoEditResources.class, where);
					dbhelper.batchUpdateAndInsertOrDelete(VideoEditResources.class, list, 1);

                    DBHelper<DIYOverlayCategory> categoryHelper = new DBHelper<>(getApplicationContext());
                    categoryHelper.delete(DIYOverlayCategory.class, where);
                    categoryHelper.batchUpdateAndInsertOrDelete(DIYOverlayCategory.class, assets.paster, 1);
                    DBHelper<ResourceCorrespondence> corrHelper = new DBHelper<>(getApplicationContext());
                    corrHelper.batchUpdateAndInsertOrDelete(ResourceCorrespondence.class, assets.pasterCorrespondence, DBHelper.UPDATEANDINSERT);

					sp.saveGlobalConfigItem("copy_resources_version", version);

				}

				Log.d("COPY", "time : " + (System.currentTimeMillis() - start));
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				stopSelf();
				super.onPostExecute(result);
			}

			@Override
			protected void onCancelled(Void result) {
				stopSelf();
				super.onCancelled(result);
			}

		};

		return task;
	}

    public ResourcesBean getAssetsResources(Context context){
        String dir = "Qupai";
        AssetManager assets = context.getAssets();

        ResourcesBean resources = null;
        InputStream istream;
        ObjectMapper mapper = new ObjectMapper();

        try {
            istream = assets.open(dir + "/" + "resource.json");
            resources = mapper.readValue(istream, ResourcesBean.class);
        } catch (IOException e) {

            e.printStackTrace();
        }

        if(resources != null){
            initFilterResources(context, resources.filter);
            initMusicResources(resources.music);
            initMVResources(context, resources);
			initFontResources(resources.font);
            initDIYAnimationResources(resources);
        }
        return resources;

    }

    private void initFilterResources(Context context, List<VideoEditResources> filters){
        String dir = "Qupai/";
        SceneFactoryClientImpl scene_client = new SceneFactoryClientImpl(context, _JSON);
        for(VideoEditResources ves : filters){
            String res_dir = "assets://" + dir + ves.getResourceUrl();
            MVTemplate conf = scene_client.readShaderMV(res_dir);

            if(conf == null){
                continue;
            }
            ves.setIsLocal(1);
            ves.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
            ves.setLocalPath(res_dir);
            ves.setIconUrl(res_dir);
            ves.setType(VideoEditBean.TYPE_SHADER_EFFECT);
            ves.setName(conf.name);
            Log.d("Filter", conf.name);
        }
    }

	private void initFontResources(List<VideoEditResources> fonts){
		String dir = "Qupai/";
		for(VideoEditResources ves : fonts){
			String res_dir = "assets://" + dir + ves.getResourceUrl();
			ves.setIsLocal(1);
			ves.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
			ves.setLocalPath(res_dir);
			ves.setIconUrl(res_dir);
			ves.setType(VideoEditBean.TYPE_FONT);
			Log.d("DIYOVERLAY", ves.getName());
		}
	}

    private void initMusicResources(List<VideoEditResources> musics){
        String dir = "Qupai/";
        for(VideoEditResources ves : musics){
            String res_dir = "assets://" + dir + ves.getResourceUrl();
            ves.setIsLocal(1);
            ves.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
            ves.setLocalPath(res_dir);
            ves.setIconUrl(res_dir);
            ves.setType(VideoEditBean.TYPE_MUSIC);
            Log.d("Music", ves.getName());
        }
    }

    private void initMVResources(Context context, ResourcesBean bean){
        String dir = "Qupai/";
        List<VideoEditResources> mvMusics = new ArrayList<>();
        SceneFactoryClientImpl scene_client = new SceneFactoryClientImpl(context, _JSON);
        for(VideoEditResources ves : bean.mv){
            String res_dir = "assets://" + dir + ves.getResourceUrl();
            MVTemplate mv = scene_client.readShaderMV(res_dir);

            if(mv == null){
                continue;
            }

            ves.setIsLocal(1);
            ves.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
            ves.setLocalPath(res_dir);
            ves.setIconUrl(res_dir);
            //ves.setResourceUrl(res_dir);
            ves.setType(VideoEditBean.TYPE_SHADER_MV);
            ves.setName(mv.name);
            Log.d("MV", mv.name);

            VideoEditResources mm = new VideoEditResources();
            mm.setId(ves.getId());
            mm.setIsLocal(1);
            mm.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
            mm.setLocalPath(res_dir);
            mm.setResourceUrl(res_dir);
            mm.setIconUrl(res_dir);
            mm.setType(VideoEditBean.TYPE_MV_MUSIC);
            mm.setName(mv.musicName);
            mvMusics.add(mm);
            Log.d("MV", mv.musicName);
        }
        bean.mvMusic = mvMusics;
    }

    private void initDIYAnimationResources(ResourcesBean bean){
        String dir = "Qupai/";
        List<DIYOverlayCategory> categorys = bean.paster;
        List<VideoEditResources> assertRes=new ArrayList<>();
        for(DIYOverlayCategory category : categorys){
            category.setIsLocal(1);
            category.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
            category.setType(AssetInfo.TYPE_DIYOVERLAY);
			String icon = category.getIconUrl();
			category.setIconUrl(dir + icon);
            List<VideoEditResources> vers = category.getPasterForms();
            for(VideoEditResources res : vers){
				res.setCategory((int)res.getFontId());
                res.setLocalPath(dir + res.getResourceUrl());
                res.setIconUrl(dir + res.getResourceUrl() + "/icon.png");
                res.setRecommend(VideoEditBean.RECOMMEND_LOCAL);
                res.setIsLocal(1);
                res.setType(VideoEditBean.TYPE_DIYOVERLAY);
                assertRes.add(res);
                Log.d("INIT", "category name : " + category.getName() + " path : " +res.getResourceUrl());
            }
        }
        bean.resources = assertRes;
    }

}
