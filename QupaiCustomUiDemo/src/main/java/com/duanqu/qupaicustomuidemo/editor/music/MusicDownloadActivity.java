package com.duanqu.qupaicustomuidemo.editor.music;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.dao.local.database.DBHelper;
import com.duanqu.qupaicustomuidemo.editor.EditorAction;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.download.ResourceDownListener;
import com.duanqu.qupaicustomuidemo.editor.mv.MusicItemForm;
import com.duanqu.qupaicustomuidemo.utils.DownloadMusicTask;
import com.duanqu.qupaicustomuidemo.utils.FileUtil;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：Mulberry on 2016/9/29 20:32
 */
public class MusicDownloadActivity extends FragmentActivity implements View.OnClickListener {

    private Button download_music_btn;
    private Button download_music_used_btn;
    private ProgressBar download_music_progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_music_activity);

        initView();
    }

    private void initView(){
        download_music_btn = (Button) findViewById(R.id.download_music_btn);
        download_music_used_btn = (Button)findViewById(R.id.download_music_used_btn);
        download_music_progress = (ProgressBar)findViewById(R.id.download_music_progress);

        download_music_btn.setOnClickListener(this);
        download_music_used_btn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_music_btn:
                downLoadMusic();
                break;
            case R.id.download_music_used_btn:
                musicUse();
                break;
        }
    }

    private Long musicId;

    private void downLoadMusic(){
        MusicItemForm srf =new MusicItemForm();
        srf.setId(1550);
        srf.setName("音乐测试");
        srf.setTypeId(6);
        srf.setIconUrl("http://system.test.qupai.me/resource/20160304/810477a0-e889-46dc-a4b4-ac14f10c20d2.png");
        srf.setMusicUrl("http://system.test.qupai.me/resource/20160304/a5c99345-7176-4407-864c-34ab569fda2e.mp3");
        srf.setResourceUrl("http://system.test.qupai.me/resource/20160304/23ee8e26-3916-44f2-9be5-f609dac6d420.zip");
        DownloadMusicTask downloadTask = new DownloadMusicTask(download_music_progress,
                download_music_btn, download_music_used_btn, srf, FileUtil.getDEFAULT_MUSIC_PATH(this),
                VideoEditBean.TYPE_MUSIC, 10, "推荐", this);
//        downloadTask.setDownList(downList);
//        downloadTask.setDownPoiList(downloadPoiList);
        downloadTask.setResourceDownListener(new ResourceDownListener() {

            @Override
            public void downLoadSuccess(long id) {
                musicId = id;
                ToastUtil.showToast(MusicDownloadActivity.this,"下载成功");
            }
        });
        downloadTask.execute();
    }


    private void musicUse(){
        DBHelper<VideoEditResources> db = new DBHelper<VideoEditResources>(this);
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("ID", musicId);
        VideoEditResources form = db.queryForWhere(VideoEditResources.class, where);

        if(form != null) {
            Intent in = new Intent();
            in.setData(EditorAction.useMusic(form.getId()));
            ((Activity)this).setResult(Activity.RESULT_OK, in);
            ((Activity)this).finish();
        }
    }
}

