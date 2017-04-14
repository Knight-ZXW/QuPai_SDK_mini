package com.duanqu.qupaicustomuidemo.editor.mv;

import java.io.Serializable;

public class AspectResource implements Serializable {

    private int aspect;
    private String download;
    private String md5;

    public int getAspect() {
        return aspect;
    }

    public void setAspect(int aspect) {
        this.aspect = aspect;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
