package com.duanqu.qupaicustomuidemo.editor.download;

import com.duanqu.qupai.asset.AssetBundle;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.bean.DynamicImage;
import junit.framework.Assert;

import java.io.File;
import java.io.Serializable;


public class VideoEditBean extends AssetInfo implements AssetBundle, Serializable {

    private static final long serialVersionUID = 6384992611872514089L;

	private final long videoId;

    @Override
    public long getID() {
        return videoId;
    }

	private String videoName;

	@Override
    public String getTitle() {
	    return videoName;
	}

    @Override
    public int getVersion() { return mvVersion; }

    private String videoEditSource;

	public int mvVersion;

	/**是否是推荐的资源*/
    public int recommend;

    /** 是否已经下载 */
    public boolean isLocal;

    @Override
    public boolean isAvailable() { return isLocal; }

    /**是否显示new*/
    public boolean isShow;

    /**资源状态 热门，普通，解锁*/
    public int status;

    /**是否显示下载*/
    public boolean _DownLoading;

    public boolean isAutoDownload;

    public boolean isCategoryCouldUse = true;

    public int specialFontStatus;

    public int fonttype;

    public boolean isDownloadable() { return !isLocal && !_DownLoading; }

    public boolean isDownloadMasked(){
        return !(isCategoryCouldUse || !isDownloadable());
    }

    private String _RemoteIconURL;

    private String _RemoteBannerURL;

    public void setRemoteIconURL(String url) {
        _RemoteIconURL = url;
    }

    public void setRemoteBannerURL(String url) {
        _RemoteBannerURL = url;
    }

	public String resourceUrl;//资源包路径

	public final int type;

	public VideoEditBean(int _type, long videoId, String videoName, String videoEditSource, int recommend, boolean isLocal) {
	    type = _type;

		this.videoId = videoId;
		this.videoName = videoName;
		this.recommend=recommend;
		this.isLocal=isLocal;

		setContentString(videoEditSource);
	}

	public void setTitle(String title){
		videoName = title;
	}

    public void setContentPath(File file) {
        videoEditSource = "file://" + file.getAbsolutePath();
    }

    public void setContentString(String str) {

        if (str == null || str.startsWith("assets://") || str.startsWith("file://")) {

        	videoEditSource = str;
            return;
        }

        switch (type) {
        case TYPE_MUSIC:
        case TYPE_DIYOVERLAY:
        case TYPE_SHADER_MV:
        case TYPE_MV_MUSIC:
        case TYPE_FONT:
            switch (recommend) {
            case RECOMMEND_LOCAL:
                videoEditSource = "assets://" + str;
                break;
            default:
                videoEditSource = "file://" + str;
                break;
            }
            break;
        default:
            Assert.fail();
        }
    }

    @Override
    public String getContentURIString() {
        return videoEditSource;
    }

    @Override
    public String getIconURIString() {
        if (!isLocal || (type != TYPE_FONT && recommend == 0)) {
        	if(_RemoteIconURL.startsWith("http://") || _RemoteIconURL.startsWith("https://")){
        		return _RemoteIconURL;
        	}
        }

        switch (type) {
        case TYPE_MV_MUSIC:
            return videoEditSource + "/icon_music.png";
        case TYPE_MUSIC:
            return videoEditSource + "/icon_without_name.png";
        case TYPE_FONT:
        default:
            return videoEditSource + "/icon.png";
        }
    }

    @Override
    public String getBannerURIString() {
        if (!isLocal || recommend == 0) {
            if(_RemoteBannerURL == null){
                return null;
            }
            if(_RemoteBannerURL.startsWith("http://") || _RemoteIconURL.startsWith("https://")){
                return _RemoteBannerURL;
            }
        }

        switch (type) {
            case TYPE_FONT:
                return videoEditSource + "/banner.png";
            default:
                return videoEditSource + "/icon.png";
        }
    }

    @Override
    public int getResourceFrom() {
        return recommend;
    }

    @Override
    public long getUID() {
        return ((long) type << 32) + videoId;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getMediaURIString() {
        switch (type) {
        case TYPE_MUSIC:
            return videoEditSource + "/audio.mp3";
        case TYPE_MV_MUSIC:
            return videoEditSource + "/music.mp3";
        case TYPE_DIYOVERLAY:
            return videoEditSource + "/content.mkv";
        case TYPE_FONT:
            return DynamicImage.TEXTONLYCONFIG + "/content.mkv";
        default:
            return null;
        }
    }

    @Override
    public String getMediaURIString(String name) {
        return videoEditSource + "/" + name;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof VideoEditBean){
            VideoEditBean veb = (VideoEditBean) o;
            return videoId == veb.videoId;
        }
        return false;
    }

    @Override
    public int getFontType() {
        return fonttype;
    }

    @Override
    public String getSceneURL() {
        return null;
    }

    @Override
    public AssetBundle getContent() { return this; }

    @Override
    public int getFlags() { return isShow ? FLAG_NEW : 0; }

    @Override
    public int getResourceStatus() {
        return status;
    }

    @Override
    public String getResourceUrl() {
        return resourceUrl;
    }
}
