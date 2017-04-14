package com.duanqu.qupaicustomuidemo.editor.mv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IMVItemForm2 {
	private static final long serialVersionUID = -1123409872296582012L;

	private long id;
	private String name;
	private String key;
	private int level;
	private String tag;
	private String cat;
	private String previewPic;
	private String previewMp4;
	private long duration;
	private int type;
	private List<AspectResource> aspectList;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getPreviewPic() {
		return previewPic;
	}

	public void setPreviewPic(String previewPic) {
		this.previewPic = previewPic;
	}

	public String getPreviewMp4() {
		return previewMp4;
	}

	public void setPreviewMp4(String previewMp4) {
		this.previewMp4 = previewMp4;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<AspectResource> getAspectList() {
		return aspectList;
	}

	public void setAspectList(List<AspectResource> aspectList) {
		this.aspectList = aspectList;
	}
}