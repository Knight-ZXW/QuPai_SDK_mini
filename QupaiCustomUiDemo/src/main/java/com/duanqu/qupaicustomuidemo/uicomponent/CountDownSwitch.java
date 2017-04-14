package com.duanqu.qupaicustomuidemo.uicomponent;

import android.view.View;

public class CountDownSwitch implements View.OnClickListener{

    interface OnTimerListener{
        void onTimerStart();
        void onTimerStop();
    }
    private OnTimerListener mOnTimerListener;
    private View view;

    public CountDownSwitch(View view, OnTimerListener timer) {
        this.view= view;
        view.setOnClickListener(this);
        mOnTimerListener = timer;
    }

    @Override
    public void onClick(View view) {
        if(view.isActivated()) {
            view.setActivated(false);
            mOnTimerListener.onTimerStop();
        } else {
            view.setActivated(true);
            mOnTimerListener.onTimerStart();
        }
    }

}
