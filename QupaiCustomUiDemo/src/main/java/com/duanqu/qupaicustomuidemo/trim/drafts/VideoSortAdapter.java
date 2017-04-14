package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.duanqu.qupai.project.Project;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.widget.RoundSquareView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoSortAdapter extends BaseAdapter{
	private static final String TAG = "VideoSortAdapter";

	private List<VideoInfoBean> dataList = new ArrayList<VideoInfoBean>();

	private DisplayImageOptions displayOptions= new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.ARGB_8888)
	.cacheInMemory(true)
	.cacheOnDisk(true).build();

	public VideoSortAdapter(List<VideoInfoBean> data) {
		dataList = data;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public VideoInfoBean getItem(int position) {
		if(dataList.size() > 0 && position > 0) {
			return dataList.get(position - 1);
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();

			convertView = FontUtil.applyFontByInflate(context, R.layout.item_qupai_video_draft, parent, false);
			holder.thumbImage = (RoundSquareView) convertView.findViewById(R.id.draft_thumbnail);
			holder.duration = (TextView) convertView.findViewById(R.id.draft_duration);
			holder.durationLayoput = convertView.findViewById(R.id.duration_layoput);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}

		if(dataList.get(position).getThumbnailPath() != null
				&& onCheckFileExsitence(dataList.get(position).getThumbnailPath())) {
			String uri = "file://" + dataList.get(position).getThumbnailPath();
			ImageLoader.getInstance().displayImage(uri, holder.thumbImage, displayOptions);
		}else {
			BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inDither = false;
	        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			int type = dataList.get(position).getType();
			Bitmap bitmap;
			if(type == Project.TYPE_PHOTO){
				bitmap = Images.Thumbnails.getThumbnail(context.getContentResolver(),
						dataList.get(position).getOrigId(), Images.Thumbnails.MICRO_KIND, options);
			}else{
				bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
						dataList.get(position).getOrigId(), Images.Thumbnails.MICRO_KIND, options);
			}
			holder.thumbImage.setImageBitmap(bitmap);
		}

		int duration = dataList.get(position).getDuration();
		if(duration == 0){
			holder.durationLayoput.setVisibility(View.GONE);
		}else{
			onMetaDataUpdate(holder.duration, duration);
		}

		return convertView;
	}

	class ViewHolder{
		public RoundSquareView thumbImage;
		public TextView duration;
		public View durationLayoput;
	}

	private boolean onCheckFileExsitence(String path) {
		Boolean res = false;
		if(path == null) {
			return res;
		}

		File file = new File(path);
		if(file.exists()) {
			res = true;
		}

		return res;
	}

	private void onMetaDataUpdate(TextView view, int duration) {
        if (duration == 0) {
            return;
        }

        int sec = Math.round((float) duration / 1000);
        int min = sec / 60;
        sec %= 60;

        view.setText(String.format(String.format("%d:%02d", min, sec)));
    }
}
