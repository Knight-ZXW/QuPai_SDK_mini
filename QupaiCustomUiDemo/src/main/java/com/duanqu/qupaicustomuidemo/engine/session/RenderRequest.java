package com.duanqu.qupaicustomuidemo.engine.session;

import android.net.Uri;

import com.duanqu.qupai.bean.PhotoModule;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;

import java.io.Serializable;
import java.util.List;

public class RenderRequest extends SessionPageRequest {

    public static final int RENDER_MODE_EXPORT = 0;
    public static final int RENDER_MODE_THUMBNAIL = 1;
    public static final int RENDER_MODE_EXPORT_VIDEO = 2;
    public static final int RENDER_MODE_EXPORT_THUMBNAIL = 3;
    public static final int RENDER_MODE_EXPORT_THUMBNAIL_COMPOSE = 4;
    public static final int RENDER_MODE_EXPORT_VIDEO_COMPOSE = 4;

    public RenderRequest(SessionPageRequest original) {
        super(original);
    }

    protected RenderRequest(SessionClientFactory factory, Serializable data) {
        super(factory, data);
    }

    String _OutputVideoPath;

    public String getOutputVideoPath() { return _OutputVideoPath; }

    public RenderRequest setOutputVideoPath(String path) {
        _OutputVideoPath = path;
        return this;
    }

    String _OutputThumbnailPath;
    int _OutputThumbnailWidth;
    int _OutputThumbnailHeight;

    public String getOutputThumbnailPath() { return _OutputThumbnailPath; }

    public RenderRequest setOutputThumbnailPath(String path) {
        _OutputThumbnailPath = path;
        return this;
    }

    public RenderRequest setOutputThumbnailSize(int w, int h) {
        _OutputThumbnailWidth = w;
        _OutputThumbnailHeight = h;
        return this;
    }

    public int getOutputThumbnailWidth() { return _OutputThumbnailWidth; }
    public int getOutputThumbnailHeight() { return _OutputThumbnailHeight; }

    private String _Uri;

    public Uri getProjectUri() {
        return Uri.parse(_Uri);
    }

    public RenderRequest setProject(String uri) {
        _Uri = uri;
        return this;
    }

    public RenderRequest setProject(Uri uri) {
        _Uri = uri.toString();
        return this;
    }

    public List<PhotoModule> photoList;

    public List<PhotoModule> getPhotoList() {
        return photoList;
    }

    public RenderRequest setPhotoList(List<PhotoModule> mPhotoList){
        photoList = mPhotoList;
        return this;
    }

    int _RenderMode;

    public int getRenderMode() { return _RenderMode; }

    public RenderRequest setRenderMode(int mode) {
        _RenderMode = mode;
        return this;
    }
}
