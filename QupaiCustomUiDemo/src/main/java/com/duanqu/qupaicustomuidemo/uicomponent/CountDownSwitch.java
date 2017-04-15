package com.duanqu.qupaicustomuidemo.uicomponent;

import android.view.View;

public class CountDownSwitch implements View.OnClickListener{

    interface OnTimerListener{
        void onTimerStart();
        void onTimerStop();
    }
    private CountDownTips mCountDownTips;
    private View view;

    public CountDownSwitch(View view, CountDownTips countDownTips) {
        this.view= view;
        view.setOnClickListener(this);
        mCountDownTips = countDownTips;
    }

    @Override
    public void onClick(View view) {
        if(view.isActivated()) {
            view.setActivated(false);
//            mCountDownTips.onTimerStop();
        } else {
            view.setActivated(true);
//            mCountDownTips.onTimerStart();
        }
    }

    public void  startCountDown(){
        if (view.isActivated()){
            mCountDownTips.onTimerStart();
        }
    }

}
