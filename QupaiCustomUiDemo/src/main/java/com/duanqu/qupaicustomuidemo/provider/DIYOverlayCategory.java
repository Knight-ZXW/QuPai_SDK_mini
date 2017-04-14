package com.duanqu.qupaicustomuidemo.provider;

import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable(tableName = "diy_category")
public class DIYOverlayCategory {

	public static final String ID                 = "ID";
	public static final String NAME               = "name";
	public static final String ICON               = "iconUrl";
	public static final String PRIORITY           = "priority";
	public static final String DESCRIPTION        = "description";
	public static final String ISLOCAL            = "isLocal";
	public static final String RECOMMEND          = "recommend";
	public static final String TYPE               = "type";
	public static final String DOWNLOADTIME       = "downloadTime";

	@DatabaseField(id = true, columnName = ID)
	private int id;
	@DatabaseField(columnName = NAME)
	private String name;
	@DatabaseField(columnName = ICON)
	private String iconUrl;
	@DatabaseField(columnName = PRIORITY)
	private int priority;
	@DatabaseField(columnName = DESCRIPTION)
	private String description;

	@DatabaseField(columnName = ISLOCAL)
	private int isLocal;

	@DatabaseField(columnName = DOWNLOADTIME)
    private long downloadTime;
	@DatabaseField(columnName = RECOMMEND)
	private int recommend;
	@DatabaseField(columnName = TYPE)
	private int type;

	private List<VideoEditResources> pasterForms;

	public DIYOverlayCategory() {}
    public DIYOverlayCategory(int id, String name, int type,
							  String iconUrl, int priority, int recommend,
							  String description, int isLocal) {
        super();
        this.id = id;
        this.name = name;
		this.type = type;
        this.iconUrl = iconUrl;
        this.priority = priority;
        this.description = description;
        this.isLocal = isLocal;
		this.recommend = recommend;
    }
    public int getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(int isLocal) {
        this.isLocal = isLocal;
    }

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public List<VideoEditResources> getPasterForms() {
        return pasterForms;
    }

    public void setPasterForms(List<VideoEditResources> pasterForms) {
        this.pasterForms = pasterForms;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
