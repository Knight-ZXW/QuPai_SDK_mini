package com.duanqu.qupaicustomuidemo.trim;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.util.LruCache;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

import com.duanqu.qupai.project.Project;
import com.duanqu.qupaicustomuidemo.R;

import java.lang.ref.WeakReference;

public class CacheActivity extends Activity implements OnScrollListener {

	private Bitmap mPlaceHolderBitmap;
	private LruCache<String, Bitmap> mMemoryCache;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    private Point mPoint = new Point(0, 0);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initCache();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setPauseWork(true);
	}

    @Override
    public void onPause() {
        super.onPause();
        setPauseWork(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMemoryCache.evictAll();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initCache() {
		mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_qupai_photo);
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    final int cacheSize = maxMemory / 2;

	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
	        @SuppressLint("NewApi") @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            if (Build.VERSION.SDK_INT >= 12) {
	                return bitmap.getByteCount()/ 1024;
	            }
	            else{
		            return (bitmap.getRowBytes() * bitmap.getHeight())/ 1024;
	            }
	        }
	    };
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null && bitmap != null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}

	public void loadBitmap(String path, int id, int type, ImageView imageView, Point point) {
		mPoint = point;
	    final Bitmap bitmap = getBitmapFromMemCache(path);
	    if (bitmap != null) {
	    	imageView.setImageBitmap(bitmap);
	    } else {
		    if (cancelPotentialWork(path, imageView)) {
		        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		        final AsyncDrawable asyncDrawable =
		                new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
		        imageView.setImageDrawable(asyncDrawable);
		        task.execute(path, String.valueOf(id), String.valueOf(type));
		    }
	    }
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private String data = null;
	    private int id = -1;

	    public BitmapWorkerTask(ImageView imageView) {
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    @Override
	    protected Bitmap doInBackground(String... params) {
	    	Bitmap bitmap = null;
	        data = params[0];
	        id = Integer.parseInt(params[1]);
			int type = Integer.parseInt(params[2]);
	        if(data != null) {
		        bitmap =  decodeThumbBitmapForFile(data, id, type, mPoint == null ? 0: mPoint.x, mPoint == null ? 0: mPoint.y);
		        addBitmapToMemoryCache(params[0], bitmap);
	        }
	        return bitmap;
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask =  getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }
	    }
	}

	public static boolean cancelPotentialWork(String data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String bitmapData = bitmapWorkerTask.data;
	        if (!data.equals(bitmapData)) {
	            bitmapWorkerTask.cancel(true);
	        } else {
	            return false;
	        }
	    }
	    return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
	public static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference =
	            new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
	/**
	 * @param path
	 * @param viewWidth
	 * @param viewHeight
	 * @return
	 */
	private Bitmap decodeThumbBitmapForFile(String path, int id, int type, int viewWidth, int viewHeight){
		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(path, options);
//		options.inSampleSize = computeScale(options, viewWidth, viewHeight);
//		options.inJustDecodeBounds = false;
		options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap;
		if(type == Project.TYPE_VIDEO){
			bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),
					id, Images.Thumbnails.MICRO_KIND, options);
		}else{
			bitmap = Images.Thumbnails.getThumbnail(getContentResolver(),
					id, Images.Thumbnails.MICRO_KIND, options);
		}
		return bitmap;
	}


	/**
	 * @param options
	 * @param viewWidth
	 * @param viewHeight
	 */
	private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight){
		int inSampleSize = 1;
		if(viewWidth == 0 || viewHeight == 0){
			return inSampleSize;
		}
		int bitmapWidth = options.outWidth;
		int bitmapHeight = options.outHeight;

		if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
			int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
			int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

			inSampleSize = widthScale < heightScale ? widthScale : heightScale;
		}
		return inSampleSize;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Pause fetcher to ensure smoother scrolling when flinging
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            // Before Honeycomb pause image loading on scroll to help with performance
            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
                setPauseWork(true);
            }
        } else {
        	setPauseWork(false);
        }
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }
}