package com.duanqu.qupaicustomuidemo.editor.mv;

import java.io.Serializable;

public class MusicItemForm implements Serializable {
	private static final long serialVersionUID = -1123409872296582012L;

	private long id;
	private String name;
	private String iconUrl;
	private String resourceUrl;
	private String description;
	private int isNewRecommend;
	private int isNew;
	private int typeId;
	private String musicUrl;

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

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getResourceUrl() {
		return resourceUrl;
	}

	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIsNewRecommend() {
		return isNewRecommend;
	}

	public void setIsNewRecommend(int isNewRecommend) {
		this.isNewRecommend = isNewRecommend;
	}

	public int getIsNew() {
		return isNew;
	}

	public void setIsNew(int isNew) {
		this.isNew = isNew;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getMusicUrl() {
		return musicUrl;
	}

	public void setMusicUrl(String musicUrl) {
		this.musicUrl = musicUrl;
	}


}
