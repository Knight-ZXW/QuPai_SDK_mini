package com.duanqu.qupaicustomuidemo.trim;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.trim.drafts.VideoDirBean;
import com.duanqu.qupaicustomuidemo.trim.drafts.VideoInfoBean;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

class VideoFileAdapter extends BaseAdapter {
	private VideoFileActivity mActivity;
	private ArrayList<VideoInfoBean> dataList;
	private ArrayList<VideoDirBean> dirList;

	private GalleryNameResolver _NameResolver;
	private DisplayImageOptions displayOptions= new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.ARGB_8888)
			.cacheInMemory(true)
			.cacheOnDisk(true).build();

	public VideoFileAdapter(VideoFileActivity activity, ArrayList<VideoInfoBean> list,
							ArrayList<VideoDirBean> dList, GalleryNameResolver resolver) {
		mActivity = activity;
		dataList = list;
		dirList = dList;

		_NameResolver = resolver;
	}

	@Override
	public int getCount() {
		return dirList.size();
	}

	@Override
	public Object getItem(int position) {
		return dirList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();

			convertView = FontUtil.applyFontByInflate(mActivity, R.layout.item_qupai_gallery_dir, null, false);
			holder.sortVideoLayout = (LinearLayout) convertView.findViewById(R.id.sort_video_layout);
			holder.thumbImage = (ImageView) convertView.findViewById(R.id.thumb_image);
			holder.sortDirTxt = (TextView) convertView.findViewById(R.id.video_dir_name);
			holder.sortFileNum = (TextView) convertView.findViewById(R.id.video_file_count);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}

        VideoDirBean bean = dirList.get(position);

        String dir_name = _NameResolver.resolve(bean.getDirName());

        holder.sortDirTxt.setText(dir_name);

		final int videoNum = getDirFileCount(dirList.get(position).getDirName());
		holder.sortFileNum.setText(String.valueOf(videoNum));

		if(dirList.get(position).getFilePath() != null){
			BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap;
			if(bean.getType() == Project.TYPE_VIDEO){
				ImageLoader.getInstance().displayImage("content://media/external/video/media/"
						+dirList.get(position).getThumbnailId(), holder.thumbImage,
						displayOptions);
//				bitmap = MediaStore.Video.Thumbnails.getThumbnail(mActivity.getContentResolver(),
//						dirList.get(position).getThumbnailId(), Images.Thumbnails.MICRO_KIND, options);
			}else{
				ImageLoader.getInstance().displayImage("content://media/external/images/"
								+dirList.get(position).getThumbnailId(), holder.thumbImage,
						displayOptions);
//				bitmap = Images.Thumbnails.getThumbnail(mActivity.getContentResolver(),
//						dirList.get(position).getThumbnailId(), Images.Thumbnails.MICRO_KIND, options);
			}

//			holder.thumbImage.setImageBitmap(bitmap);

        }

		holder.sortVideoLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                // TODO move these to VideoFileActivity
                Intent in = new VideoGalleryActivity.Request(mActivity._Request)
                        .toIntent(mActivity);
				String dirPath = dirList.get(position).getFilePath().substring(0,
						dirList.get(position).getFilePath().lastIndexOf("/"));
				in.putExtra("dirPath", dirPath);
				in.putExtra("dataList", dataList);
				in.putExtra("dirName", dirList.get(position).getDirName());
				in.putExtra("videoCount", videoNum);
				mActivity.startActivityForResult(in, VideoFileActivity.REQUEST_GALLERY_CODE);
			}
		});

		return convertView;
	}

	class ViewHolder{
		public LinearLayout sortVideoLayout;
		public ImageView thumbImage;
		public TextView sortDirTxt;
		public TextView sortFileNum;
	}

	private int getDirFileCount(String dirName) {
		int count = 0;

		for(int i=0; i<dataList.size(); i++) {
			String tmpFile = dataList.get(i).getFilePath();
			String[] dir = tmpFile.split("/");
            String name = dir[dir.length - 2];
			if(dirName.equals(name)) {
				count++;
			}
		}

		return count;
	}

}
