package com.duanqu.qupaicustomuidemo.uicomponent;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.duanqu.qupai.minisdk.view.RecordView;
import com.duanqu.qupaicustomuidemo.R;

public class CountDownTips implements CountDownSwitch.OnTimerListener {

    public interface OnCountDownRecordListener{
        void onRecordStart();
        void onRecordStop();
    }

    private final Animator _Animator;
    private final TextView _CountDownText;
    private final View _CountDownTip;
    private final View _SwitchCamera;
    private final View _ViewDeleteClip;

    private MediaPlayer _Player;
    private Handler mHandler;

    private int mDelaySeconds;

    private final int mDelayInterval    = 1000;   /* 1000ms */
    private final int mTipsDelay        = 3000;   /* 3秒提示 */
    private final int mCountdownDelay   = 5000;   /* 5秒倒计时 */

    private OnCountDownRecordListener _RecordListener;

    public CountDownTips(TextView TextView_text, View view_tip,View switch_camera,View view_delete_clip, Context context, OnCountDownRecordListener recordListener) {

        _CountDownText = TextView_text;
        _CountDownTip  = view_tip;
        _SwitchCamera = switch_camera;
        _ViewDeleteClip = view_delete_clip;
        _RecordListener = recordListener;
        _CountDownTip.findViewById(R.id.photo_ic_self_timer_countdown).setActivated(true);

        _Animator = AnimatorInflater.loadAnimator(context, R.animator.qupai_self_timer_countdown);
        _Animator.setTarget(_CountDownText);

        mHandler = new Handler(CALLBACK);
    }

    @Override
    public void onTimerStart() {
        _CountDownTip.setVisibility(View.VISIBLE);

        mDelaySeconds = mCountdownDelay / 1000;

        Message msg = mHandler.obtainMessage(WHAT_COUNTDOWN_START, this);
        mHandler.sendMessageDelayed(msg, mTipsDelay);
    }

    @Override
    public void onTimerStop() {

        if(mDelaySeconds == 0) {
            _RecordListener.onRecordStop();
        }

        if(_Player != null) {
            _Player.release();
            _Player = null;

            _Animator.cancel();
            _CountDownText.setVisibility(View.GONE);
        } else {

            _CountDownTip.setVisibility(View.GONE);
            _CountDownTip.findViewById(R.id.photo_tip_self_timer_countdown_cancel).setVisibility(View.GONE);
        }

        mHandler.removeMessages(WHAT_COUNTDOWN_START);
        mHandler.removeMessages(WHAT_COUNTDOWN_ONGOING);
    }

    public void startCountDown() {
        _CountDownTip.setVisibility(View.GONE);
        _CountDownTip.findViewById(R.id.photo_tip_self_timer_countdown_cancel).setVisibility(View.GONE);

        _Player = MediaPlayer.create(_CountDownText.getContext(), R.raw.qupai_stop_timer_countdown);
        _Player.start();

        _Animator.start();
        _CountDownText.setVisibility(View.VISIBLE);

        _CountDownText.setText(Integer.toString(mDelaySeconds));
        _SwitchCamera.setEnabled(true);
        _ViewDeleteClip.setEnabled(true);
        Message msg = mHandler.obtainMessage(WHAT_COUNTDOWN_ONGOING, this);
        mHandler.sendMessageDelayed(msg, mDelayInterval);
    }

    public void countDown() {
        mDelaySeconds -= mDelayInterval / 1000;
        if(mDelaySeconds == 0) {
            _Player.release();
            _Player = null;

            _Animator.cancel();
            _CountDownText.setVisibility(View.GONE);
            _SwitchCamera.setEnabled(false);
            _ViewDeleteClip.setEnabled(false);
            _RecordListener.onRecordStart();
            return;
        }

        _Animator.cancel();
        _Animator.start();
        _CountDownText.setText(Integer.toString(mDelaySeconds));

        Message msg = mHandler.obtainMessage(WHAT_COUNTDOWN_ONGOING, this);
        mHandler.sendMessageDelayed(msg, mDelayInterval);
    }

    private final int WHAT_COUNTDOWN_START   = 0x00000001;
    private final int WHAT_COUNTDOWN_ONGOING = 0x00000002;
    private Handler.Callback CALLBACK = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            CountDownTips listener = (CountDownTips)message.obj;
            switch (message.what){
                case WHAT_COUNTDOWN_START:
                    listener.startCountDown();
                    break;
                case WHAT_COUNTDOWN_ONGOING:
                    listener.countDown();
                    break;
            }
            return true;
        }
    };
}
