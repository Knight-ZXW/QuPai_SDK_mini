package com.duanqu.qupaicustomuidemo.trim.drafts;

/**
 * Created by Administrator on 2015/12/24.
 */
public interface ImportResPresenter {

    void onResume();

    void setUserVisibleHint(boolean isVisibleToUser);

    void onStop();

    void delete();

    boolean isCurrentListEmpty();

    long getLastModifiedTimestamp();

    void dispatchOnSelect();

    void dispatchTouchEvent();

}
