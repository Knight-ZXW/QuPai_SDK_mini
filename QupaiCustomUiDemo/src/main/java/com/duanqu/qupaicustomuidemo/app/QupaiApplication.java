package com.duanqu.qupaicustomuidemo.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.amitshekhar.DebugDB;
import com.duanqu.qupai.cache.core.VideoLoader;
import com.duanqu.qupai.cache.core.VideoLoaderConfiguration;
import com.duanqu.qupai.cache.disc.naming.TempFileNameGenerator;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.ThumbnailExportOptions;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.httpfinal.QupaiHttpFinal;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.jni.ApplicationGlue;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.utils.SingnatureUtils;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientImpl;
import com.duanqu.qupaicustomuidemo.photocompose.loader.UILImageLoader;
import com.duanqu.qupaicustomuidemo.photocompose.loader.UILPauseOnScrollListener;
import com.duanqu.qupaicustomuidemo.service.CopyResourcesServices;
import com.duanqu.qupaicustomuidemo.utils.Constant;
import com.duanqu.qupaicustomuidemo.utils.MySystemParams;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ThemeConfig;

/**
 * 趣拍的初始化代码.建议在使用的时候将趣拍的application独立.然后继承QupaiApplication
 */
public class QupaiApplication extends Application {

    public static VideoSessionClientImpl videoSessionClient;

    @Override
    public void onCreate() {
        super.onCreate();

        for (String str : new String[]{"gnustl_shared", "qupai-media-thirdparty", "qupai-media-jni","QuTranscode"}) {
            System.loadLibrary(str);
        }

        ApplicationGlue.initialize(this);

        MySystemParams
                .getInstance().init(getApplicationContext());

        QupaiHttpFinal.getInstance().initOkHttpFinal();

        initVideoClientInfo(480,640);
        initImageLoader();
        initVideoCache();
        initFinalGallery();

        Intent in = new Intent(this, CopyResourcesServices.class);
        startService(in);

        Log.e("md5" , SingnatureUtils.getSingInfo(this));
        Log.e("Db","使用浏览器查看Database"+ DebugDB.getAddressLog());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void initVideoClientInfo(int w,int h) {
        if(w == 0){
            w = 480;
        }
        if (h == 0 ){
            h = 640;
        }
        ProjectOptions options = new ProjectOptions.Builder()
                //输出视频帧率
                .setVideoFrameRate(30)
                //关键帧间隔
                .setIFrameInterval(2)
                .setVideoSize(480 , 640)
                //时长区间.单位：毫秒
                .setDurationRange(1000, 17000)
                .get();

        MovieExportOptions movieExportOptions =
                new MovieExportOptions.Builder()
                .setVideoBitrate(800 * 1024)
                .setVideoPreset("faster")
                .setVideoRateCRF(6)
                .setOutputVideoLevel(30)
                .setOutputVideoTune("zerolatency")
                .setOutputVideoKeyInt(150)
                .configureMuxer("movflags", "+faststart")
                .build();

        ThumbnailExportOptions thumbnailExportOptions = new ThumbnailExportOptions
                .Builder()
                .setCount(1)
                .get();

        VideoSessionCreateInfo videoSessionCreateInfo = new VideoSessionCreateInfo.Builder()
                .setMovieExportOptions(movieExportOptions)
                .setThumbnailExportOptions(thumbnailExportOptions)
                .setWaterMarkPath(Constant.WATER_MARK_PATH) //水印
                .setWaterMarkPosition(1)
                .build();
        videoSessionClient = new VideoSessionClientImpl(this);
        videoSessionClient.setProjectOptions(options);
        videoSessionClient.setCreateInfo(videoSessionCreateInfo);

    }

    public static JSONSupport getJSONSupport(Context context) {
        return  new JSONSupportImpl();
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .threadPoolSize(3)
                .memoryCacheSize(10 * 1024 * 1024)
                .memoryCache(new LruMemoryCache(10 * 1024 * 1024))
                .diskCache(new UnlimitedDiskCache(new File(getExternalCacheDir(), "image"),
                        new File(getExternalCacheDir(), "image"),
                        new Md5FileNameGenerator()))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.FIFO).build();
        ImageLoader.getInstance().init(config);
    }

    private void initVideoCache() {
        VideoLoaderConfiguration config = new VideoLoaderConfiguration.Builder(
                getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(2)
                .discCache(new com.duanqu.qupai.cache.disc.impl.FileCountLimitedDiscCache(
                        new File(getExternalCacheDir(),"video"), 500))
                .tempDiscCache(new com.duanqu.qupai.cache.disc.impl.UnlimitedDiscCache(
                        new File(getExternalCacheDir(),"video"), new TempFileNameGenerator()))
                .tasksProcessingOrder(
                        com.duanqu.qupai.cache.core.assist.QueueProcessingType.FIFO).build();
        VideoLoader.getInstance().init(config);
    }

    //初始化FinalGallery github地址：https://github.com/pengjianbo/GalleryFinal
    private void initFinalGallery() {
        ThemeConfig theme = new ThemeConfig.Builder()
                .build();
        //配置功能
        FunctionConfig functionConfig = new FunctionConfig.Builder()
                .setEnableCamera(false)
                .setEnableEdit(false)
                .setEnableCrop(false)
                .setEnableRotate(true)
                .setCropSquare(true)
                .setEnablePreview(false)
                .setMutiSelectMaxSize(50)
                .build();
        CoreConfig coreConfig = new CoreConfig.Builder(this, new UILImageLoader(), theme)
                .setFunctionConfig(functionConfig)
                .setNoAnimcation(true)
                .setPauseOnScrollListener(new UILPauseOnScrollListener(false, true))
                .build();
        GalleryFinal.init(coreConfig);
    }

}
