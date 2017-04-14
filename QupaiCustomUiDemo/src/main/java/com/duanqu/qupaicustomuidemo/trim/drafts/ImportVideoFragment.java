package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;

/**
 * 导入视频的界面
 */
public class ImportVideoFragment extends Fragment {

    private static final String KEY_PROJECT_TYPE = "PROJECT_TYPE";

    public static ImportVideoFragment create(int type) {
        ImportVideoFragment fragment = new ImportVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_PROJECT_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    ImportResPresenter importResPresenter;

    public interface VideoListener {
        void onVideoSelect(ImportVideoFragment fragment, VideoInfoBean bean);

        void onSortComplete(ImportVideoFragment fragment);

        void onSortStart(ImportVideoFragment fragment);
    }

    public boolean isCurrentListEmpty() {
        return importResPresenter.isCurrentListEmpty();
    }

    public long getLastModifiedTimestamp() {
        return importResPresenter.getLastModifiedTimestamp();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public ImportActivity getHostActivity(){
        return (ImportActivity)getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = FontUtil.applyFontByInflate(
                getActivity(), R.layout.fragment_qupai_import_video, container, false);

         importResPresenter = new ImportVideoPresenterImpl(this, root);

        return root;
    }

    public void dispatchOnSelect() {
        importResPresenter.dispatchOnSelect();
    }

    @Override
    public void onResume() {
        importResPresenter.onResume();
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isResumed()) {
            importResPresenter.setUserVisibleHint(isVisibleToUser);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        importResPresenter.onStop();

        super.onStop();
    }

    static final int RC_DELETE_CONFIRMATION_DIALOG = 2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_DELETE_CONFIRMATION_DIALOG:
                if (resultCode == AlertDialog.BUTTON_POSITIVE) {
                    importResPresenter.delete();
                }
                break;
        }
    }

    public void dispatchTouchEvent() {
        importResPresenter.dispatchTouchEvent();
    }

}
