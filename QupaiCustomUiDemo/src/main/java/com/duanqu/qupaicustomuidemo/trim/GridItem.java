package com.duanqu.qupaicustomuidemo.trim;

public class GridItem {
	private String videoPath;
	private String addTime;
	private int duration;
	private int thumbnailId;
	private int section;
	private int type;

	public GridItem(String path, int dura, int id, int type, String time) {
		super();
		this.videoPath = path;
		this.duration = dura;
		this.thumbnailId = id;
		this.addTime = time;
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public String getPath() {
		return videoPath;
	}
	public void setPath(String path) {
		this.videoPath = path;
	}

	public int getDuration() {
		return duration;
	}
	public void setDuration(int dura) {
		this.duration = dura;
	}

	public int getId() {
		return thumbnailId;
	}
	public void setId(int id) {
		this.thumbnailId = id;
	}

	public String getTime() {
		return addTime;
	}
	public void setTime(String time) {
		this.addTime = time;
	}

	public int getSection() {
		return section;
	}

	public void setSection(int section) {
		this.section = section;
	}

}
