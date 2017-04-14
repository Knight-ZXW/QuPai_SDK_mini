package com.duanqu.qupaicustomuidemo.editor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "videoEditResource")
@JsonIgnoreProperties(ignoreUnknown=true)
public class VideoEditResources implements Serializable {

	public static final String ID                 = "ID";
	public static final String RESOURCENAME       = "name";
	public static final String DESCRIPTION        = "description";
	public static final String RESOURCEURL        = "url";
	public static final String RESOURCEICON       = "iconUrl";
	public static final String RESOURCEBANNER     = "bannerUrl";
	public static final String RESOURCELOCALPATH  = "localPath";
	public static final String DOWNLOADTIME       = "downloadTime";
	public static final String RESOURCETYPE       = "resourceType";
	public static final String ISLOCAL            = "isLocal";
	public static final String RECOMMEND          = "recommend";
	public static final String FONTTYPE           = "font_type";

	private static final long serialVersionUID = -8605961695815638631L;
	@DatabaseField(generatedId = true)
	public long _id;
	@DatabaseField(uniqueCombo = true, canBeNull = false, index = true, columnName = ID)
	private long id;
	@DatabaseField(columnName = RESOURCENAME)
	private String name;
	@DatabaseField(columnName = RESOURCEICON)
	private String iconUrl;
	@DatabaseField(columnName = RESOURCEBANNER)
	private String bannerUrl;
	@DatabaseField(columnName = RESOURCEURL)
	private String resourceUrl;
	@DatabaseField(columnName = DESCRIPTION)
	private String description;

	@DatabaseField(columnName = RESOURCELOCALPATH)
	private String localPath;

	private int priority;
	@DatabaseField(uniqueCombo = true, canBeNull = false, index = true, columnName = RESOURCETYPE)
	private int type;
	@DatabaseField(columnName = ISLOCAL)
	private int isLocal;//是否已经下载到本地了
	@DatabaseField(uniqueCombo = true, columnName = RECOMMEND)
	private int recommend;//标识资源类型,0,服务器推荐资源，1，下载下来的资源，2，安装包自带资源
	@DatabaseField(columnName = DOWNLOADTIME)
	private long downloadTime;
	@DatabaseField(columnName = FONTTYPE)
	private int category;

	private long fontId;

	private ArrayList<Integer> musicId;

	public VideoEditResources(){

	}

	public VideoEditResources(long id, String name, String iconUrl,
							  String resourceUrl, String description,
							  String localPath, int type, int fontType,
							  int isLocal, int recommend, long downloadTime) {
		this.id = id;
		this.name = name;
		this.iconUrl = iconUrl;
		this.resourceUrl = resourceUrl;
		this.description = description;
		this.localPath = localPath;
		this.type = type;
		this.isLocal = isLocal;
		this.recommend = recommend;
		this.category = fontType;
		this.downloadTime = downloadTime;
	}

	public String getBannerUrl() {
		return bannerUrl;
	}

	public void setBannerUrl(String bannerUrl) {
		this.bannerUrl = bannerUrl;
	}

	public ArrayList<Integer> getMusicId() {
        return musicId;
    }

    public void setMusicId(ArrayList<Integer> musicId) {
        this.musicId = musicId;
    }

    public long getId() {
		return id;
	}

	public int getIsLocal() {
		return isLocal;
	}

	public void setIsLocal(int isLocal) {
		this.isLocal = isLocal;
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

	public long getFontId() {
		return fontId;
	}

	public void setFontId(long fontId) {
		this.fontId = fontId;
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

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public long getDownloadTime() {
		return downloadTime;
	}

	public void setDownloadTime(long downloadTime) {
		this.downloadTime = downloadTime;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "VideoEditResources [id=" + id + ", name=" + name + ", iconUrl="
				+ iconUrl + ", resourceUrl=" + resourceUrl + ", description="
				+ description
				+ ", localPath=" + localPath
				+ ", priority=" + priority + ", type=" + type + ", isLocal="
				+ isLocal + ", recommend=" + recommend + ", downloadTime="
				+ downloadTime
				+ ", categoryName="
				+ "]";
	}

}
