package com.duanqu.qupaicustomuidemo.trim;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.view.ImmersiveSupport;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupaicustomuidemo.trim.drafts.VideoInfoBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import com.duanqu.qupaicustomuidemo.widget.astickyheader.SimpleSectionedGridAdapter;
import com.duanqu.qupaicustomuidemo.widget.astickyheader.SimpleSectionedGridAdapter.Section;
import com.duanqu.qupaicustomuidemo.widget.astickyheader.ui.SquareImageView;

public class VideoGalleryActivity extends CacheActivity {

    public static class Request extends SessionPageRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

    }

	private static final int REQUEST_TRIM_CODE = 300;

	private GridView grid;
	private ImageAdapter mAdapter;
	private ArrayList<VideoInfoBean> videoList = new ArrayList<VideoInfoBean>();
	private List<GridItem> mGirdList = new ArrayList<GridItem>();
	private int section = 0;
	private ArrayList<Section> sections = new ArrayList<Section>();
	private Map<String, Integer> sectionMap = new HashMap<String, Integer>();

	private List<String> mHeaderNames = new ArrayList<String>();
    private List<Integer> mHeaderPositions = new ArrayList<Integer>();

    private Point mPoint = new Point(0, 0);//用来封装ImageView的宽和高的对象

    private String dirName;
    private View abView;

    private Request _Request;

	private GalleryNameResolver galleryNameResolver;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(newBase);
		ImmersiveSupport.attachBaseContext(this, newBase);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        _Request = PageRequest.from(this);

        VideoSessionClient client = _Request.getVideoSessionClient(this);

        FontUtil.applyFontByContentView(this, R.layout.activity_qupai_video_gallery);

        abView = findViewById(R.id.action_bar);

		dirName = getIntent().getStringExtra("dirName");

		ArrayList<VideoInfoBean> dataList = (ArrayList<VideoInfoBean>)
				getIntent().getSerializableExtra("dataList");

		if(dataList != null && dataList.size() > 0) {
			SortVideoListByName(dirName, dataList);
		}

		galleryNameResolver = new GalleryNameResolver(this, client.getUISettings().getGalleryDirTitles());

		initView();
		initControls();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		ImmersiveSupport.onWindowFocusChanged(this, hasFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		ImageButton preBtn = (ImageButton) abView.findViewById(R.id.draft_closeBtn);
		preBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		TextView title = (TextView) abView.findViewById(R.id.draft_title);

		title.setText(galleryNameResolver.resolve(dirName));

		ImageView nextBtn = (ImageView) abView.findViewById(R.id.draft_nextBtn);
		nextBtn.setBackgroundResource(R.drawable.btn_qupai_cancel_cross);
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_FIRST_USER);
				finish();
			}
		});
	}

	private void initControls() {
		grid = (GridView)findViewById(R.id.grid);

		for (int i=0; i<videoList.size(); i++) {
			String path = videoList.get(i).getFilePath();
			int duration = videoList.get(i).getDuration();
			int thumbnailId = videoList.get(i).getOrigId();
			long times = videoList.get(i).getAddTime();
			int type = videoList.get(i).getType();
			GridItem mGridItem = new GridItem(path, duration, thumbnailId, type, paserTimeToYM(times));
			mGirdList.add(mGridItem);

		}
		//Collections.sort(mGirdList, new YMComparator());

		for(ListIterator<GridItem> it = mGirdList.listIterator(); it.hasNext();){
			GridItem mGridItem = it.next();
			String ym = mGridItem.getTime();
			if(!sectionMap.containsKey(ym)){
				mHeaderNames.add(ym);
				mHeaderPositions.add(section);
				sectionMap.put(ym, section);
			}
			section ++;
		}

		mAdapter = new ImageAdapter();
		for (int i = 0; i < mHeaderPositions.size(); i++) {
			sections.add(new Section(mHeaderPositions.get(i), mHeaderNames.get(i)));
		}
		SimpleSectionedGridAdapter simpleSectionedGridAdapter = new SimpleSectionedGridAdapter(VideoGalleryActivity.this, mAdapter,
				R.layout.item_header_qupai_gallery_video, R.id.header_layout, R.id.header);
		simpleSectionedGridAdapter.setGridView(grid);
		simpleSectionedGridAdapter.setSections(sections.toArray(new Section[0]));
		grid.setAdapter(simpleSectionedGridAdapter);
		grid.setOnItemClickListener(gridItemListener);

	}

	private OnItemClickListener gridItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			GridItem item = mGirdList.get((int) id);
			if(item.getType() == Project.TYPE_VIDEO){
//				new TrimActivity.Request(new VideoSessionClientFactoryImpl(),null)
//						.setPath(item.getPath())
//						.setDuration(item.getDuration())
//						.startForResult(VideoGalleryActivity.this, REQUEST_TRIM_CODE);
				new VideoTrimActivity.Request(new VideoSessionClientFactoryImpl(),null)
						.setPath(item.getPath())
						.setDuration(item.getDuration())
						.startForResult(VideoGalleryActivity.this,REQUEST_TRIM_CODE);
			}else{
				String path = item.getPath();
				Uri uri = Uri.parse("file://" + path);
				Intent in = new Intent();
				in.setData(uri);
				boolean isSquare = isImageSquared(path);
				in.putExtra("isSquare", isSquare);
				setResult(RESULT_OK, in);
				finish();
			}

		}
	};

	private boolean isImageSquared(String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		return options.outWidth == options.outHeight;
	}

	public static String paserTimeToYM(long time) {
		System.setProperty("user.timezone", "Asia/Shanghai");
		TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
		TimeZone.setDefault(tz);
		SimpleDateFormat format = new SimpleDateFormat("MM月dd日 , yyyy");
		return format.format(new Date(time * 1000L));
	}

	private void SortVideoListByName(String dirName, ArrayList<VideoInfoBean> list) {
		if(list != null && list.size() > 0) {
			for(int i=0; i<list.size(); i++) {
				VideoInfoBean vb = list.get(i);
				String filePath = vb.getFilePath();
				String[] dir = filePath.split("/");
                String curDir = dir[dir.length - 2];
                if(curDir.equals(dirName)) {
                	videoList.add(vb);
                }
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	    case REQUEST_TRIM_CODE:
            if(resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
		}
	}

	@Override
	public void onBackPressed() {
		VideoGalleryActivity.this.finish();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private class ImageAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mGirdList.size();
		}

		@Override
		public Object getItem(int position) {
			return mGirdList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SquareImageView image;

			if (convertView == null) {
				convertView = FontUtil.applyFontByInflate(
						parent.getContext(), R.layout.item_qupai_gallery, parent, false);
			}

			int type = mGirdList.get(position).getType();
			TextView duraTxt = ViewHolder.get(convertView, R.id.video_duration);
			if(type == Project.TYPE_VIDEO){
				int sec = Math.round((float) mGirdList.get(position).getDuration() / 1000);
				int min = sec / 60;
				sec %= 60;
				duraTxt.setText(String.format(String.format("%d:%02d", min, sec)));
			}else{
				duraTxt.setVisibility(View.GONE);
			}

			image = ViewHolder.get(convertView, R.id.gallery_image);
			//用来监听ImageView的宽和高
			image.setOnMeasureListener(new SquareImageView.OnMeasureListener() {
				@Override
				public void onMeasureSize(int width, int height) {
					mPoint.set(width, height);
				}
			});
			loadBitmap(mGirdList.get(position).getPath(),
					mGirdList.get(position).getId(), mGirdList.get(position).getType(), image, mPoint);
			return convertView;
		}

	}

	public static class ViewHolder {
		@SuppressWarnings("unchecked")
		public static <T extends View> T get(View view, int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}
}
