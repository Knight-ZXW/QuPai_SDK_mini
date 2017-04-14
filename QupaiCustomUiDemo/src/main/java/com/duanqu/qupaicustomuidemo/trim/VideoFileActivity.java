package com.duanqu.qupaicustomuidemo.trim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.view.ImmersiveSupport;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.trim.drafts.VideoDirBean;
import com.duanqu.qupaicustomuidemo.trim.drafts.VideoInfoBean;

import java.util.ArrayList;

public class VideoFileActivity extends Activity {

    public static class Request extends SessionPageRequest {

        public Request(SessionPageRequest request) {
            super(request);
        }

    }

	public static final int REQUEST_GALLERY_CODE = 200;

    Request _Request;

	private LinearLayout emptyText;
	private ListView dirListView;

	private ArrayList<VideoInfoBean> dataList;
	private ArrayList<VideoDirBean> dirList;

	private View abView;
	private GalleryNameResolver galleryNameResolver;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("VideoFileList", "onCreate Time:" + System.currentTimeMillis());
        _Request = PageRequest.from(this);

        VideoSessionClient client = _Request.getVideoSessionClient(this);

		galleryNameResolver = new GalleryNameResolver(this, client.getUISettings().getGalleryDirTitles());
		FontUtil.applyFontByContentView(this, R.layout.activity_qupai_video_file);

        abView = findViewById(R.id.action_bar);

		dataList = (ArrayList<VideoInfoBean>) getIntent().
				getSerializableExtra("video_list");
		dirList = (ArrayList<VideoDirBean>) getIntent().
				getSerializableExtra("dir_list");

		initView();
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
		ImmersiveSupport.attachBaseContext(this, newBase);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		ImmersiveSupport.onWindowFocusChanged(this, hasFocus);
	}

	private void initView() {
		Log.d("VideoFileList", "initView Time:" + System.currentTimeMillis());
		ImageButton closeBtn = (ImageButton) abView.findViewById(R.id.draft_closeBtn);
		closeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		TextView abTitle = (TextView) abView.findViewById(R.id.draft_title);
		abTitle.setText(R.string.qupai_video_album_list);

		ImageView nextBtn = (ImageView) abView.findViewById(R.id.draft_nextBtn);
		nextBtn.setImageResource(R.drawable.btn_qupai_cancel_cross);
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		emptyText = (LinearLayout) findViewById(R.id.gallery_group_empty_view);
		dirListView = (ListView) findViewById(R.id.video_list);
		VideoFileAdapter adapter = new VideoFileAdapter(this, dataList, dirList, galleryNameResolver);
		dirListView.setAdapter(adapter);

		if(dirList.size() == 0) {
			emptyText.setVisibility(View.VISIBLE);
			dirListView.setVisibility(View.GONE);
		}else {
			emptyText.setVisibility(View.GONE);
			dirListView.setVisibility(View.VISIBLE);
		}
		Log.d("VideoFileList", "initView END Time:" + System.currentTimeMillis());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_GALLERY_CODE:
			if (resultCode == RESULT_OK) {
			    setResult(RESULT_OK, data);
				finish();
			} else if (resultCode == RESULT_FIRST_USER) {
			    finish();
			}
			break;
		default:
	        super.onActivityResult(requestCode, resultCode, data);
		    break;
		}
	}

}
