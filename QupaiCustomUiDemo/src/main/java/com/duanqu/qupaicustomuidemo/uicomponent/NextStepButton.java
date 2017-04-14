package com.duanqu.qupaicustomuidemo.uicomponent;

import android.view.View;
import com.duanqu.qupai.recorder.ClipManager;

public class NextStepButton implements View.OnClickListener , ClipManager.Listener{

    private View mView;
    private ClipManager mClipManager;

    public NextStepButton(View view, ClipManager clip_manager) {
        mView = view;
        mClipManager = clip_manager;
        mView.setEnabled(false);
        mView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mClipManager.saveProject();
    }

    @Override
    public void onClipListChange(ClipManager manager, int event) {
        if(manager.getDuration() >= manager.getMinDuration()) {
            mView.setEnabled(true);
        } else {
            mView.setEnabled(false);
        }
    }
}
