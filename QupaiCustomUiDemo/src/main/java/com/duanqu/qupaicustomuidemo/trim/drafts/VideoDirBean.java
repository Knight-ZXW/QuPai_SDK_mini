package com.duanqu.qupaicustomuidemo.trim.drafts;

import java.io.Serializable;

public class VideoDirBean implements Serializable{
	private static final long serialVersionUID = 1457689589322245L;

	private int type;
	private String filePath;
	private String dirName;
	private String VideoDirPath;
	private int thumbnailId;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setFilePath(String fPath) {
		this.filePath = fPath;
	}

	public void setDirName(String dName) {
		this.dirName = dName;
	}

	public void setVideoDirPath(String videoDPath) {
		this.VideoDirPath = videoDPath;
	}

	public void setThumbnailId(int thumbnailid) {
		this.thumbnailId = thumbnailid;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDirName() {
		return dirName;
	}

	public String getVideoDirPath() {
		return VideoDirPath;
	}

	public int getThumbnailId() {
		return thumbnailId;
	}
}