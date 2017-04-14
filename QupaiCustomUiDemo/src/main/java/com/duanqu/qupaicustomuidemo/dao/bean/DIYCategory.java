package com.duanqu.qupaicustomuidemo.dao.bean;

import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;

public class DIYCategory extends AssetGroup {

    public int id;//类别id，后续下载和预览需要这个id
    public int type;//子分类type
    public int priority;//优先级别，数字越小越在前面
    public String name;//动图类别名字
    public String description;//动图描述
    public String iconUrl;//动图类别图标
	public int isLocal;
    public int recommend;

    @Override
    public boolean equals(Object o) {
        if(o instanceof DIYCategory){
            DIYCategory category = (DIYCategory) o;
            return id == category.id;
        }
        
        return false;
    }

    @Override
    public String getIconUrl() {
        if(iconUrl.startsWith("http://") || iconUrl.startsWith("https://")){
            return iconUrl;
        }
        return "assets://" + iconUrl;
    }

    @Override
    public boolean isAvailable() {
        return isLocal == AbstractDownloadManager.DOWNLOAD_COMPLETED;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getGroupId() {
        return id;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getResourceFrom() {
        return recommend;
    }

}
