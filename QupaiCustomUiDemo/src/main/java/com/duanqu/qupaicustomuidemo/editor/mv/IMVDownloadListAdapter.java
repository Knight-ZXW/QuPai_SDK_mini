package com.duanqu.qupaicustomuidemo.editor.mv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.ResourceItem;
import com.duanqu.qupai.utils.ScaleTypeUtils;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.editor.EditorAction;
import com.duanqu.qupaicustomuidemo.editor.ScaleTypeFormat;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.download.IMVPreviewDialog;
import com.duanqu.qupaicustomuidemo.editor.download.PasterDownloadManager;
import com.duanqu.qupaicustomuidemo.editor.download.ResourceDownListener;
import com.duanqu.qupaicustomuidemo.utils.DownloadMvTask;
import com.duanqu.qupaicustomuidemo.utils.FileUtil;
import com.duanqu.qupaicustomuidemo.utils.ToastUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMVDownloadListAdapter extends RecyclerView.Adapter<IMVDownloadListAdapter.IMVViewHolder> {
	public final static int STATE_DOWNLOAD = 1;
	public final static int STATE_DOWNLOADING = 2;
	public final static int STATE_USED = 3;

	private IMVPreviewDialog imvDialog;
	private FragmentActivity mActivity;
	private List<IMVItemForm2> dataList;
	private Map<Long, Boolean> downList;
	private List<Long> downloadPoiList;
	private List<VideoEditResources> idList;
	private List<VideoEditResources> delIdList;
	private AbstractDownloadManager downloadManager;
	private List<Long> nearDownList;
	private float mScaleType;

	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options= new DisplayImageOptions.Builder()
			.bitmapConfig(Bitmap.Config.ARGB_8888)
			.showImageForEmptyUri(R.drawable.video_thumbnails_loading_126)
			.showImageOnFail(R.drawable.video_thumbnails_loading_126)
			.showImageOnLoading(R.drawable.video_thumbnails_loading_126)
			.cacheInMemory(true)
			.cacheOnDisk(true).build();

	public IMVDownloadListAdapter(FragmentActivity activity) {
		this.mActivity = activity;
		dataList = new ArrayList<>();
		downList = new HashMap<>();
		downloadPoiList = new ArrayList<>();
		idList = new ArrayList<>();
		delIdList = new ArrayList<>();
		nearDownList = new ArrayList<>();

		AssetRepository assetRepository = QupaiApplication.videoSessionClient.getAssetRepository();
		downloadManager = (AbstractDownloadManager)assetRepository.getDownloadManager();
	}

	public void setScaleType(float mScaleType) {
		this.mScaleType = mScaleType;
	}

	/**
	 * update data list
	 *
	 * @param dataList
	 */
	public void setDataList(List<IMVItemForm2> dataList) {
		this.dataList = dataList;
	}

	public void clearDownloadList() {
		downList.clear();
	}

	public void setNearDownList(List<Long> list) {
		nearDownList = list;
	}

	public void setIdList(List<VideoEditResources> list) {
		idList = list;
	}

	public void setDelIdList(List<VideoEditResources> list) {
		delIdList = list;

		if(downList == null || delIdList == null) {
			return;
		}

		if(!downList.isEmpty() && !delIdList.isEmpty()) {
			for(VideoEditResources res : delIdList) {
				if(downList.get(res.getId()) != null) {
					if(downList.get(res.getId())) {
						downList.remove(res.getId());
					}
				}
			}
		}
	}

	@Override
	public IMVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.download_imv_item, parent, false);
		IMVViewHolder holder = new IMVViewHolder(view);

		holder.imvLayout = (FrameLayout) view.findViewById(R.id.imv_download_item_layout);
		holder.imvThumb = (ImageView) view.findViewById(R.id.imv_download_item_image_thumb);
		holder.imvShade = (ImageView) view.findViewById(R.id.imv_download_item_image_shade);
		holder.imvNew = (ImageView) view.findViewById(R.id.imv_download_item_image_new);
		holder.imvName = (TextView) view.findViewById(R.id.imv_download_item_text_name);
		holder.imvDesc = (TextView) view.findViewById(R.id.imv_download_item_text_description);
		holder.imvInfoLayout = (LinearLayout) view.findViewById(R.id.imv_download_item_info_layout);
		holder.imvViewBtn = (Button) view.findViewById(R.id.imv_download_item_view_btn);
		holder.imvDownloadBtn = (Button) view.findViewById(R.id.imv_download_item_download_btn);
		holder.imvDownloadPb = (CircleProgressBar) view.findViewById(R.id.imv_download_item_download_pb);

		return holder;
	}

	@Override
	public void onBindViewHolder(final IMVViewHolder holder, int position) {
		final IMVItemForm2 imvForm = getItem(position);
		if (imvForm != null) {
			mImageLoader.displayImage(imvForm.getPreviewPic(), holder.imvThumb, options);

			holder.imvNew.setVisibility(View.GONE);

			holder.imvName.setText(imvForm.getName());
			holder.imvDesc.setVisibility(View.GONE);

			long resourceId = imvForm.getId();
			if(isJustDownLoad(resourceId)) {
				holder.btnState = STATE_USED;
				holder.imvDownloadBtn.setBackgroundResource(R.drawable.parser_used_btn_selector);
				holder.imvDownloadBtn.setText(R.string.qupai_mv_used);
			}else {
				holder.btnState = STATE_DOWNLOAD;
				holder.imvDownloadBtn.setBackgroundResource(R.drawable.bg_imv_download_rect);
				holder.imvDownloadBtn.setText("");
			}

			holder.imvViewBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previewIMVItem(holder, imvForm);
				}
			});

			holder.imvLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					previewIMVItem(holder, imvForm);
				}
			});

			holder.imvDownloadBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(holder.btnState == STATE_DOWNLOAD) {
							startDownloadTask(holder, imvForm);
					}else if(holder.btnState == STATE_USED){
						UseResourceFormChoose(imvForm);
					}
				}
			});
		}
	}

	private void UseResourceFormChoose(IMVItemForm2 imvForm) {
		if(imvForm != null) {
			Intent in = new Intent();
			in.setData(EditorAction.useMV(imvForm.getId()));
			mActivity.setResult(Activity.RESULT_OK, in);
			mActivity.finish();
		}
	}


	public boolean isJustDownLoad(long id) {
		boolean isDown = false;
		boolean isInIdList = false;

		if(!downList.isEmpty()) {
			if(downList.get(id) != null) {
				isDown = downList.get(id);
			}
		}

		if(idList == null) {
			return isDown;
		}

		for(int i=0; i<idList.size(); i++) {
			if(idList.get(i).getId() == id) {
				isInIdList = true;
				break;
			}
		}

		return isDown || isInIdList;
	}


	private void previewIMVItem(final IMVViewHolder holder, final IMVItemForm2 imvForm) {
		imvDialog = new IMVPreviewDialog().newInstance(imvForm.getPreviewMp4(),
				imvForm.getPreviewPic(), holder.btnState);
		imvDialog.setVideoPrepareListener(new IMVPreviewDialog.VideoPrepareListener() {

			@Override
			public void videoStopPlay() {
				if(imvDialog != null) {
					imvDialog.stopPlayVideo();
				}
			}

			@Override
			public void managerResource() {
				if(holder.btnState == STATE_DOWNLOAD) {
						startDownloadTask(holder, imvForm);
				}else if(holder.btnState == STATE_USED){
					if(imvDialog != null) {
						imvDialog.dismiss();
					}
					UseResourceFormChoose(imvForm);
				}
			}
		});

		imvDialog.show(mActivity.getSupportFragmentManager(), "dialog");
	}

	public void onStop() {
		mImageLoader.clearMemoryCache();
		mImageLoader.clearDiskCache();
	}

	public void cancelTask() {
		if(imvDialog != null) {
			imvDialog.cancelTask();
		}
	}

	private void startDownloadTask(final IMVViewHolder holder, final IMVItemForm2 imvForm) {
		ResourceItem item = new ResourceItem();
		item.setId(imvForm.getId());
		item.setIconUrl(imvForm.getPreviewPic());
		item.setName(imvForm.getName());
		item.setFontType(0);
		item.setBannerUrl("");
		item.setResourceType(AssetInfo.TYPE_SHADER_MV);
		item.setResourceUrl(getResourceUrlFromScaleType(mScaleType, imvForm));
		downloadManager.downloadResourcesItem(mActivity.getApplicationContext(), item, null,
				new AbstractDownloadManager.ResourceDownloadListener(){
					@Override
					public void onDownloadStart(ResourceItem id) {
						holder.btnState = STATE_DOWNLOADING;

						holder.imvDownloadBtn.setEnabled(false);
						holder.imvDownloadPb.setVisibility(View.VISIBLE);
						holder.imvDownloadBtn.setVisibility(View.GONE);

						if(imvDialog != null) {
							imvDialog.setDownloadBtnState(STATE_DOWNLOADING);
						}
					}

					@Override
					public void onDownloadFailed(ResourceItem id) {
						holder.btnState = STATE_DOWNLOAD;

						holder.imvDownloadPb.setVisibility(View.GONE);
						holder.imvDownloadBtn.setVisibility(View.VISIBLE);

						holder.imvDownloadBtn.setEnabled(true);
						holder.imvDownloadBtn.setBackgroundResource(R.drawable.bg_imv_download_rect);

						if(imvDialog != null) {
							imvDialog.setDownloadBtnState(STATE_DOWNLOAD);
						}
					}

					@Override
					public void onDownloadCompleted(ResourceItem id) {
						holder.btnState = STATE_USED;

						downloadPoiList.remove(imvForm.getId());
						downList.put(imvForm.getId(), true);

						holder.imvDownloadPb.setVisibility(View.GONE);
						holder.imvDownloadBtn.setVisibility(View.VISIBLE);

						holder.imvDownloadBtn.setEnabled(true);
						holder.imvDownloadBtn.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
						holder.imvDownloadBtn.setText(R.string.qupai_mv_used);

						nearDownList.add(id.getID());

						SharedPreferences sp = mActivity.getSharedPreferences("AppGlobalSetting", 0);
						boolean first = sp.getBoolean("first_download_completed_edit_tip_imv", true);
						if(first) {
							sp.edit().putBoolean("first_download_completed_edit_tip_imv", false).commit();
							ToastUtil.showToast(mActivity, R.string.qupai_paster_download_first_success);
						}else {
							ToastUtil.showToast(mActivity, R.string.qupai_paster_download_success);
						}

						if(imvDialog != null) {
							imvDialog.setDownloadBtnState(STATE_USED);
						}
					}
				});

		downloadManager.registResourceDownloadProgressListener((int)item.getID(),
				new AbstractDownloadManager.ProgressListener(){
					@Override
					public void onProgressUpdate(int id, int progress) {
						holder.imvDownloadPb.setProgress(progress);

						if(imvDialog != null) {
							imvDialog.setProgress(progress);
						}
					}
				});

	}

	private String getResourceUrlFromScaleType(float mScaleType,IMVItemForm2 imvItemForm2){
		int scale = ScaleTypeUtils.switchAspect(mScaleType);
		List<AspectResource> resourceList = imvItemForm2.getAspectList();
		for (AspectResource resource:resourceList){
			if(scale == resource.getAspect()){
				return resource.getDownload();
			}
		}
		return null;
	}

	@Override
	public int getItemCount() {
		return dataList == null ? 0 : dataList.size();
	}


	private IMVItemForm2 getItem(int position) {
		if (dataList != null
				&& position >= 0
				&& position < dataList.size()) {
			return dataList.get(position);
		}
		return null;
	}

	public static class IMVViewHolder extends RecyclerView.ViewHolder {
		public FrameLayout imvLayout;
		public ImageView imvThumb;
		public ImageView imvShade;
		public ImageView imvNew;
		public TextView imvName;
		public TextView imvDesc;
		public LinearLayout imvInfoLayout;
		public Button imvViewBtn;
		public Button imvDownloadBtn;
		public CircleProgressBar imvDownloadPb;
		public int btnState;

		public IMVViewHolder(View itemView) {
			super(itemView);
		}
	}

	public interface DownloadListener {
		void downloadState(int state);
		void downloadProgress(int progress);
	}
}
