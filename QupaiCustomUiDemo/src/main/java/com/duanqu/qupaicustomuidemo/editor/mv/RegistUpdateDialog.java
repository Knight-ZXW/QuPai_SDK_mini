package com.duanqu.qupaicustomuidemo.editor.mv;

/**
 * Created by Administrator on 2016/12/12.
 */
public interface RegistUpdateDialog {

    void registerDialogListener(int id, UpdateDialogStatus listener);

    void unregisterDialogListener(int id);

    void download(int id);

    void useCategoryPaster(int id);

}
