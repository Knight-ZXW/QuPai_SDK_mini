package com.duanqu.qupaicustomuidemo.editor.mv;

/**
 * Created by Administrator on 2016/12/12.
 */
public interface UpdateDialogStatus {

    void onProgress(int progress);

    void onCompleted(boolean success);

    void onFailed();

}
