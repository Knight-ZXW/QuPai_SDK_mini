package com.duanqu.qupaicustomuidemo.editor;

import com.duanqu.qupai.asset.AssetBundle;
import com.duanqu.qupai.asset.AssetInfo;

/**
 * Created by qupai on 16-5-5.
 */
public class ColorEffectBean extends AssetInfo {
    private String mConfigPath;

    public void setConfigPath(String configPath) {
        mConfigPath = configPath;
    }

    @Override
    public long getUID() {
        return 0;
    }

    @Override
    public long getID() {
        return 0;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public String getIconURIString() {
        return null;
    }

    @Override
    public String getBannerURIString() {
        return null;
    }

    @Override
    public String getContentURIString() {
        return mConfigPath;
    }

    @Override
    public AssetBundle getContent() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public int getResourceStatus() {
        return 0;
    }

    @Override
    public String getResourceUrl() {
        return null;
    }

    @Override
    public int getResourceFrom() {
        return 0;
    }

    @Override
    public int getFlags() {
        return 0;
    }

}
