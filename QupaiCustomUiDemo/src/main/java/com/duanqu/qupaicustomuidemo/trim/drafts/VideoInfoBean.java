package com.duanqu.qupaicustomuidemo.trim.drafts;

import java.io.Serializable;

public class VideoInfoBean implements Serializable{
	private static final long serialVersionUID = 1457689854322245L;
	private String filePath;
	private String thumbPath;
    private String mimeType;
    private String title;
    private int duration;
    private int origId;
    private long addTime;
    private boolean isSquare;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setFilePath(String filePath) {
    	this.filePath = filePath;
    }


    public void setThumbnailPath(String thumbPath) {
    	this.thumbPath = thumbPath;
    }

    public void setMimeType(String mimeType) {
    	this.mimeType = mimeType;
    }

    public void setTitle(String title) {
    	this.title = title;
    }

    public void setDuration(int duration) {
    	this.duration = duration;
    }

    public void setOrigId(int id) {
    	this.origId = id;
    }

    public void setAddTime(long time) {
    	this.addTime = time;
    }

    public String getFilePath() {
    	return filePath;
    }

    public String getThumbnailPath() {
    	return thumbPath;
    }

    public String getMimeType() {
    	return mimeType;
    }

    public String getTitle() {
    	return title;
    }

    public int getDuration() {
    	return duration;
    }

    public int getOrigId() {
    	return origId;
    }

    public long getAddTime() {
    	return addTime;
    }

    public boolean isSquare() {
        return isSquare;
    }

    public void setIsSquare(boolean isSquare) {
        this.isSquare = isSquare;
    }
}