package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.StaticLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupai.effect.ThumbnailFetcher;
import com.duanqu.qupai.effect.VideoTimelineEditService;
import com.duanqu.qupai.effect.thumb.ShareableThumbnail;
import com.duanqu.qupaicustomuidemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class TimelineBar implements OnRenderChangeListener {

	private RecyclerView timeline;
	private View indicator;
	private ThumbnailFetcher thumbnailer;
	private ThumbnailAdapter adapter;
	private SimpleDateFormat format;

	private PopupWindow timeIndicator;

	private long mDuration;
	private int layoutWidth;

	private VideoTimelineEditService timelineEditService;

	private final long quantunDuration;

	TimelineBar(VideoTimelineEditService timeline, ThumbnailFetcher thumbnailer) {
		timelineEditService = timeline;
		this.thumbnailer = thumbnailer;
		format = new SimpleDateFormat("mm:ss", Locale.getDefault());

		quantunDuration = timeline.getTimelineQuantumDuration();
	}

	public void onCreateView(View view){
		timeline = (RecyclerView) view.findViewById(R.id.timeline_nav);
		indicator = view.findViewById(R.id.indicator);
		timeline.setLayoutManager(new LinearLayoutManager(view.getContext(),
				LinearLayoutManager.HORIZONTAL, false));

		timeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);

			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				daltSum += dx;

				long progress = timelineEditService.getTimelineProgress();

				if(daltSum == Math.round(computeWidthByDuration(progress))){
					return ;
				}

				long time = computeTimeByWidth(dx);
				progress += time;
				if(progress < 0){
					progress = 0;
				}

				timelineEditService.setTimelineProgress(progress);
			}
		});

		timelineEditService.setVideoProgressChangeListener(
				new VideoTimelineEditService.ProgressChangeListener() {
					@Override
					public void onProgressChangeListener(long progress) {
						if(mDuration == 0){
							return ;
						}
						onTimelineProgressChange(progress);
					}
				}
		);

		timelineEditService.setOnOverlayTimeEditChangeListener(
				new VideoTimelineEditService.OverlayTimeEditChange() {
					@Override
					public void onOverlayTimeEditChange(boolean changed) {
						onTimelineProgressChange(timelineEditService.getTimelineProgress());
					}
				}
		);

		timeline.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				timeline.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				layoutWidth = timeline.getWidth();
			}
		});

	}

	private float getMaxScrollDistance(){
		return getWidth() * (float)(adapter.getItemCount() - 8) / (float)8;
	}

	private float computeWidthByDuration(long duration){
		float sumDistance = getMaxScrollDistance();
		return sumDistance * ((float)duration / (float)getDuration());
	}

	private long computeTimeByWidth(float width){
		float sumDistance = getMaxScrollDistance();
		return (long)(getDuration() * (width / sumDistance));
	}

	private float daltSum;
	private void onTimelineProgressChange(long progress){
		Log.d("SERVICE", "daltSum : " + daltSum + " progress : " + progress);
		float distance = computeWidthByDuration(progress);
		float daltDx = distance - daltSum;

		timeline.scrollBy(Math.round(daltDx), 0);

		showTimeIndicator(progress);
	}

	private void showTimeIndicator(long progress){
		if(timeIndicator == null){
			initTimeIndicator();
		}

		TextView time = (TextView) timeIndicator.getContentView();
		String text = String.format("%s/%s", format.format(new Date(progress)),
				format.format(new Date(getDuration())));
		time.setText(text);

		if(!timeIndicator.isShowing()){
			int offset = (int)(StaticLayout.getDesiredWidth(text, time.getPaint()) / 2);

			timeIndicator.showAsDropDown(indicator, -offset, 0);
		}

		resetTimeIndicatorShowTime();
	}

	private void initTimeIndicator(){
		timeIndicator = new PopupWindow(timeline.getContext());
		timeIndicator.setOutsideTouchable(false);
		timeIndicator.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		timeIndicator.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		TextView time = new TextView(timeline.getContext());
		time.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8,
				timeline.getResources().getDisplayMetrics()));
		timeIndicator.setBackgroundDrawable(null);
		timeIndicator.setContentView(time);
	}

	private void resetTimeIndicatorShowTime(){
		timeline.removeCallbacks(dismissTimeIndicatorTask);
		timeline.postDelayed(dismissTimeIndicatorTask, 3000);
	}

	private void dismissTimeIndicator(){
		if(timeIndicator == null){
			return ;
		}

		if(timeIndicator.isShowing()){
			timeIndicator.dismiss();
		}

	}

	private final Runnable dismissTimeIndicatorTask = new Runnable() {
		@Override
		public void run() {
			dismissTimeIndicator();
		}
	};

	public void onDestory(){
		dismissTimeIndicator();
		timeline.removeCallbacks(dismissTimeIndicatorTask);
	}

	@Override
	public void onRenderChange(RenderEditService service) {
        if(!service.isRenderReady() || !service.isAllModeSupport()){
            return ;
        }

        if(mDuration == 0){
			mDuration = timelineEditService.getTimelineDuration();
			adapter = new ThumbnailAdapter();
			timeline.setAdapter(adapter);
			daltSum = 0;
		}

	}

	public long getDuration(){
		return mDuration;
	}

	public int getWidth(){
		return layoutWidth;
	}

	private class ThumbnailerItem extends RecyclerView.ViewHolder implements ThumbnailFetcher.OnThumbnailCompletion {

		private final ImageView image;
		private ShareableThumbnail sThumbnail;

		ThumbnailerItem(ImageView view) {
			super(view);
			image = view;
		}

		public void setData(float time) {
			if (requestTime == time) {
				return;
			}
			requestTime = time;
			image.setImageBitmap(null);

			if(sThumbnail != null){
				sThumbnail.release();
				sThumbnail = null;
			}

			if(requestTime < 0 || requestTime > getDuration() / 1000f){
				return ;
			}

			if (requestID >= 0) {
				thumbnailer.cancelThumbnailRequest(requestID);
			}
			requestID = thumbnailer.requestThumbnailImage(time, this);
		}

		private float requestTime = -1;
		private int requestID;

		@Override
		public void onThumbnailReady(ShareableThumbnail thumbnail, float time) {
			if (requestTime != time) {
				return;
			}
			image.setImageBitmap(thumbnail.getBitmap());
			sThumbnail = thumbnail;
			requestID = -1;
		}
	}

	private class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailerItem> {

		@Override
		public ThumbnailerItem onCreateViewHolder(ViewGroup parent, int viewType) {
			ImageView image = new ImageView(timeline.getContext());
			image.setScaleType(ScaleType.CENTER_CROP);
			image.setBackgroundResource(android.R.color.white);
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			int pw = getWidth();
			if(pw == 0){
				pw = parent.getMeasuredWidth();
				if(pw == 0){
					pw = parent.getResources().getDisplayMetrics().widthPixels;
				}
			}
			lp.width = pw / 8;
			lp.height = lp.width;
			image.setLayoutParams(lp);
			return new ThumbnailerItem(image);
		}

		@Override
		public void onBindViewHolder(ThumbnailerItem holder, int position) {
			int realPos = position - 4;
			float requestTime = -1;
			if(realPos >= 0 && realPos < getItemCount() - 8){
				requestTime = (float)((quantunDuration / 8) * realPos) / 1000f;
			}

			holder.setData(requestTime);
		}

		@Override
		public int getItemCount() {
			return (int)Math.ceil(8 * ((float)mDuration / (float)quantunDuration + 1));
		}
	}

}
