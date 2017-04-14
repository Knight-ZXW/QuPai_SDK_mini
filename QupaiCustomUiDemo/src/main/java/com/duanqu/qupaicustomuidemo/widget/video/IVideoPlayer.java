package com.duanqu.qupaicustomuidemo.widget.video;

import android.view.View;

public interface IVideoPlayer {
	
	  public abstract View getShowView();

	  public abstract boolean hasPlayer();
	
	  public abstract boolean isPlaying();
	
	  public abstract void pause();
	
	  public abstract void releaseMediaPlayer();
	
	  public abstract void start();

	  public abstract int getCurrentPoi();
}
