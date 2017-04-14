package com.duanqu.qupaicustomuidemo.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.editor.download.ResourceDownListener;
import com.duanqu.qupaicustomuidemo.editor.mv.MusicItemForm;
import com.duanqu.qupaisdk.tools.DeviceUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadMusicTask extends AsyncTask<Void, Integer, Long> {
	private static final String TAG = "DownloadMusicTask";
	private static final String SHOP_MUSIC_NAME = "Shop_Music_";
	private static final String SHOP_OVERLAY_NAME = "Shop_Overlay_";

	private Context mContext;
	private int FileLength;
	private URL mUrl;
    private File mFile;
    private String filePath;
    private String mResourceUrl;
    private ProgressBar mPbar;
    private Button downloadBtn;
    private Button usedBtn;
    private int mProgress = 0;
    private String filename;
    private String mMusicName;
    private long id;
    private int result=0;
    private ProgressReportingOutputStream mOutputStream;
    private int resType;//resourceType; 1.音乐，2.表情，2.贴纸，4.滤镜
    private long mCategoryId;
    private String mCategoryName;
    private MusicItemForm mSysForm;

    private Map<Long, Boolean> downList;
    private List<Long> downPoiList;

    private ResourceDownListener mDownListener;

	public DownloadMusicTask(ProgressBar pbar, Button btn, Button btn2, MusicItemForm srf,
							 String outPath, int type, long categoryId,
							 String categoryName, Context context) {
		super();
		mContext = context;
		resType = type;
		mCategoryId = categoryId;
		mCategoryName = categoryName;
		mResourceUrl = srf.getResourceUrl();
		mSysForm = srf;
		downList = new HashMap<Long, Boolean>();
		downPoiList = new ArrayList<Long>();

		if(type == VideoEditBean.TYPE_MUSIC) {
			filename= SHOP_MUSIC_NAME + String.valueOf(srf.getId()) + ".zip";
		}else if(type == VideoEditBean.TYPE_DIYOVERLAY) {
			filename= SHOP_OVERLAY_NAME + String.valueOf(srf.getId()) + ".zip";
		}

		filePath = outPath;
		mMusicName = srf.getName();
		mPbar = pbar;
		downloadBtn = btn;
		usedBtn = btn2;
		this.id=srf.getId();
		try {
			mUrl = new URL(mResourceUrl);

			File dir = new File(outPath);
			if (!dir.exists()) {
				boolean success = dir.mkdirs();
				if (!success) {
					Log.e(TAG, "mkdirs failed");
					result=-1;
					return;
				}
			}

			mFile = new File(outPath + filename);
		    try {
		    	mFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				result=-1;
			}

			Log.d(TAG, "mUrl = " + mUrl + " mFile = " + mFile);
		} catch (Exception e) {
			e.printStackTrace();
			result=-1;
		}
	}

	public void setDownList(Map<Long, Boolean> map) {
		downList = map;
	}

	public void setDownPoiList(List<Long> downloadList) {
		downPoiList = downloadList;
	}

	public void setResourceDownListener(ResourceDownListener listener) {
		mDownListener = listener;
	}

	@Override
	protected void onPreExecute() {
		if(!DeviceUtils.isOnline(mContext)) {
			String content = mContext.getResources().getString(R.string.slow_network);
			ToastUtil.showToast(mContext, content);
			if(mFile.exists()) {
				mFile.delete();
			}

			cancel(false);
		}else {
			if(mPbar != null && downloadBtn != null) {
				mPbar.setVisibility(View.VISIBLE);
				downloadBtn.setVisibility(View.GONE);
			}
		}
		super.onPreExecute();
	}

	@Override
	protected Long doInBackground(Void... params) {
		if(result<0){
			return Long.valueOf(-1);
		}
		return downloadMusic();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if(mPbar == null) {
			return;
		}
		if(values.length > 1) {
			int fileLength = values[1];
			if(fileLength == -1) {
				mPbar.setIndeterminate(true);
			}else {
				mPbar.setMax(fileLength);
			}
		}else {
			mPbar.setProgress(values[0].intValue());
		}

		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Long result) {
		Log.e(TAG, "result = " + result + "FileLength = " + FileLength);
		if(result < 0 || FileLength < 0 || result < FileLength){
			String content = mContext.getResources().getString(R.string.music_download_failed);
			ToastUtil.showToast(mContext, content);

			if(mFile.exists()) {
				mFile.delete();
			}

			if(downloadBtn != null && mPbar != null) {
				mPbar.setProgress(0);
				mPbar.setVisibility(View.GONE);
				downloadBtn.setVisibility(View.VISIBLE);
			}
		}else{
			if(downloadBtn != null && usedBtn != null && mPbar != null) {
				downPoiList.remove(mSysForm.getId());
				usedBtn.setVisibility(View.VISIBLE);
				mPbar.setProgress(0);
				mPbar.setVisibility(View.GONE);
				downList.put(mSysForm.getId(), true);
			}

			UnzipMusicTask unzipTask = new UnzipMusicTask(id, filename, filePath, mMusicName,
					FileUtil.getUNZIP_MUSIC_PATH(mContext), resType, mCategoryId,
					mCategoryName, mSysForm, null, mContext);
			unzipTask.execute();

			if(mDownListener != null) {
				mDownListener.downLoadSuccess(mSysForm.getId());
			}
		}


		if(isCancelled())
			return;
		super.onPostExecute(result);
	}

	private long downloadMusic() {
		URLConnection connection = null;
		int bytesCopied = 0;

		try {
			connection = mUrl.openConnection();
			connection.setRequestProperty("RANGE", "bytes=0-");
			String range = connection.getHeaderField("Content-Range");
			if(TextUtils.isEmpty(range)){
				FileLength = connection.getContentLength();
			}else{
				String len = range.substring(range.lastIndexOf("/") + 1);
				FileLength = Integer.parseInt(len);
			}
            if(mFile.exists() && FileLength == mFile.length()){
                Log.d(TAG, "file "+mFile.getName()+" already exits!!");
                return 0;
            }
            mOutputStream = new ProgressReportingOutputStream(mFile);
            publishProgress(0, FileLength);
            bytesCopied =copy(connection.getInputStream(),mOutputStream);
            if(bytesCopied != FileLength && FileLength != -1){
                Log.e(TAG, "Download incomplete bytesCopied="+bytesCopied+", length" + FileLength);
            }
            mOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytesCopied;
	}

	private int copy(InputStream input, OutputStream output){
		//new 8K的缓存
        byte[] buffer = new byte[1024*8];
        BufferedInputStream in = new BufferedInputStream(input, 1024*8);
        BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
        int count =0,n=0;
        try {
            while((n=in.read(buffer, 0, 1024*8))!=-1){
                out.write(buffer, 0, n);
                count+=n;
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

	private final class ProgressReportingOutputStream extends FileOutputStream{

        public ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            publishProgress(mProgress);
        }
    }

}