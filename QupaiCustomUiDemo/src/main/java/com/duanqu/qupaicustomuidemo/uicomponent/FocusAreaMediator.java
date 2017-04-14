package com.duanqu.qupaicustomuidemo.uicomponent;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import com.duanqu.qupai.android.camera.AutoFocusCallback;
import com.duanqu.qupai.android.camera.CameraDevice;
import com.duanqu.qupaicustomuidemo.R;

public class FocusAreaMediator
        implements Animator.AnimatorListener,
        AutoFocusCallback {

    private static final int STATE_INACTIVE = 0;
    private static final int STATE_START_ANIMATING = 1;
    private static final int STATE_PROGRESS_ANIMATING = 2;
    private static final int STATE_STOP_ANIMATING = 3;

    private final View _View;

    private int _State = STATE_INACTIVE;

    public FocusAreaMediator(View view) {
        _View = view;
        _View.setVisibility(View.GONE);

        Context c = view.getContext();
        _StartAnimator = AnimatorInflater.loadAnimator(c, R.animator.focus_area_focus_qupai_start);
        _StartAnimator.setTarget(view);
        _StopAnimator = AnimatorInflater.loadAnimator(c, R.animator.focus_area_focus_qupai_stop);
        _StopAnimator.setTarget(view);
        _StopAnimator.addListener(this);
    }

    public void onAutoFocusStart(float touch_x, float touch_y) {
        MarginLayoutParams lp = (MarginLayoutParams) _View.getLayoutParams();
        lp.leftMargin = (int) touch_x - lp.width / 2;
        lp.topMargin = (int) touch_y - lp.height / 2;

        _View.setLayoutParams(lp);

        _View.setVisibility(View.VISIBLE);
        _StartAnimator.end();
        _StopAnimator.end();
        _StartAnimator.start();
        _State = STATE_START_ANIMATING;
    }

    public boolean isActive() {
        return _View.getVisibility() == View.VISIBLE;
    }

    private final Animator _StartAnimator;
    private final Animator _StopAnimator;

    public void onAutoFocus(boolean success) {
        switch (_State) {
        case STATE_START_ANIMATING:
            _StartAnimator.cancel();
            _StopAnimator.start();
            _State = STATE_STOP_ANIMATING;
            break;
        default:
            break;
        }
    }

    public void cancel() {
        _StartAnimator.cancel();
        _StopAnimator.cancel();
        toInactiveState();
    }

    private void onStartEnd() {
        switch (_State) {
        case STATE_START_ANIMATING:
            _State = STATE_STOP_ANIMATING;
            _StopAnimator.start();
            break;
        default:
            break;
        }
    }

    private void onStopEnd() {
        switch (_State) {
        case STATE_STOP_ANIMATING:
            toInactiveState();
            break;
        default:
            break;
        }
    }

    private void toInactiveState() {
        _State = STATE_INACTIVE;
        _View.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (animation == _StartAnimator) {
            onStartEnd();
        } else if (animation == _StopAnimator) {
            onStopEnd();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAutoFocus(boolean success, CameraDevice camera) {
        onAutoFocus(success);
    }

    @Override
    public void onAutoFocusMoving(boolean start, CameraDevice camera) {

    }
}
