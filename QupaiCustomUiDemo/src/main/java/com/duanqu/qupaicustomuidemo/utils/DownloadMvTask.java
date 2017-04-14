package com.duanqu.qupaicustomuidemo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.editor.download.ResourceDownListener;
import com.duanqu.qupaicustomuidemo.editor.mv.IMVDownloadListAdapter;
import com.duanqu.qupaicustomuidemo.editor.mv.IMVItemForm2;
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

public class DownloadMvTask extends AsyncTask<Void, Integer, Long> {
	private static final String TAG = "DownloadMvTask";
	private static final String SHOP_MV_NAME = "Shop_Mv_";

	private Context mContext;
	private int FileLength;
	private URL mUrl;
    private File mFile;
    private String filePath;
    private String mResourceUrl;

    private int mProgress = 0;
    private String filename;
    private String mMvName;
    private long id;
    private int result=0;
    private ProgressReportingOutputStream mOutputStream;
    private long mCategoryId;
    private String mCategoryName;
	private float mScaleType;
    private IMVItemForm2 mImvForm;

    private ResourceDownListener mResourceListener;
	private IMVDownloadListAdapter.DownloadListener mDownloadListener;

    @SuppressLint("UseSparseArrays")
	public DownloadMvTask(Context context, String outPath, long categoryId,
						  String categoryName, IMVItemForm2 imvForm , String resourceUrl,float scaleType) {
    	super();

    	mContext = context;
    	mCategoryId = categoryId;
		mCategoryName = categoryName;
		mImvForm = imvForm;
		filePath = outPath;
		mMvName = imvForm.getName();
		id = imvForm.getId();
		mResourceUrl = resourceUrl;
		mScaleType = scaleType;

    	filename= SHOP_MV_NAME + String.valueOf(imvForm.getId()) + ".zip";

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

	public void setResourceDownListener(ResourceDownListener listener) {
        mResourceListener = listener;
	}

    public void setDownloadListener(IMVDownloadListAdapter.DownloadListener listener) {
        mDownloadListener = listener;
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
            if(mDownloadListener != null) {
                mDownloadListener.downloadState(IMVDownloadListAdapter.STATE_DOWNLOADING);
            }
		}

		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = values[0].intValue() * 100 / FileLength;
        if(mDownloadListener != null) {
            mDownloadListener.downloadProgress(progress);
        }

		super.onProgressUpdate(values);
	}

	@Override
	protected Long doInBackground(Void... params) {
		if(result < 0) {
			return Long.valueOf(-1);
		}

		return downloadMv();
	}

	@Override
	protected void onPostExecute(Long result) {
		Log.e(TAG, "result = " + result + "FileLength = " + FileLength);
		if(result < 0 || FileLength < 0 || result < FileLength){
			String content = mContext.getResources().getString(R.string.music_download_failed);
			ToastUtil.showToast(mContext, content);

			if(mFile != null && mFile.exists()) {
				mFile.delete();
			}

            if(mDownloadListener != null) {
                mDownloadListener.downloadState(IMVDownloadListAdapter.STATE_DOWNLOAD);
            }
		}else{
            if(mDownloadListener != null) {
                mDownloadListener.downloadState(IMVDownloadListAdapter.STATE_USED);
            }

			UnzipMusicTask unzipTask = new UnzipMusicTask(id, filename, filePath, mMvName,
					FileUtil.getUNZIP_MUSIC_PATH(mContext), VideoEditBean.TYPE_SHADER_MV, mCategoryId,
					mCategoryName, null, mImvForm, mContext);
			unzipTask.execute();

			if(mResourceListener != null) {
                mResourceListener.downLoadSuccess(mImvForm.getId());
			}
		}


		if(isCancelled())
			return;

		super.onPostExecute(result);
	}

	private long downloadMv() {
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