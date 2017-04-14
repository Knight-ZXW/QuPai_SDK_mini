package com.duanqu.qupaicustomuidemo.editor;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.duanqu.qupai.utils.StringUtil;

/**
 * Created by Administrator on 2016/4/12.
 */
public class EffectTextViewAnimator {
    private static final int MESSAGE_EFFECT_TEXT_DISAPPEAR = 0x10;
    private static final int DELAY_EFFECT_TEXT_DISAPPEAR = 2300;

    private final TextView _EffectText;
    private final TextView _LeftEffectText;
    private final TextView _CenterEffectText;
    private final TextView _RightEffectText;

    private String _PreEffect;
    private String _CurrentEffect;
    private String _NextEffect;

    public EffectTextViewAnimator(TextView effectText, TextView leftText, TextView centerText, TextView rightText) {
        _EffectText = effectText;
        _LeftEffectText = leftText;
        _CenterEffectText = centerText;
        _RightEffectText = rightText;
    }

    public void setEffectTitle(String preEffect, String currentEffect, String nextEffect) {
        _PreEffect = preEffect;
        _CurrentEffect = currentEffect;
        _NextEffect = nextEffect;
    }

    public void startEffectAnimator(int type) {
        effectTextTranslateAnimator(type);
        mHandler.sendEmptyMessageDelayed(MESSAGE_EFFECT_TEXT_DISAPPEAR, DELAY_EFFECT_TEXT_DISAPPEAR);
    }

    public void removeEffectAnimator() {
        mHandler.removeMessages(MESSAGE_EFFECT_TEXT_DISAPPEAR);
    }

    private void effectTextTranslateAnimator(final int type) {
        float strWidth;
        PropertyValuesHolder pvh1;
        PropertyValuesHolder pvh2;
        ObjectAnimator animator1;
        ObjectAnimator animator2;

        int rootWidth = _EffectText.getRootView().getWidth();

        Paint paint = new Paint();
        paint.setTextSize(StringUtil.sp2px(_EffectText.getContext(), 20));
        float curWidth = paint.measureText(_CurrentEffect);

        if(type == FilterChooserMediator2.TYPE_SMOOTH_TO_LEFT) {
            _RightEffectText.setVisibility(View.VISIBLE);
            _RightEffectText.setText(_NextEffect);

            strWidth = paint.measureText(_NextEffect);

            pvh1 = PropertyValuesHolder.ofFloat("translationX", strWidth, strWidth / 2 - rootWidth / 2);
            animator1 = ObjectAnimator.ofPropertyValuesHolder(_RightEffectText, pvh1);

            pvh2 = PropertyValuesHolder.ofFloat("translationX", 0.0f, -curWidth / 2 - rootWidth / 2);
            animator2 = ObjectAnimator.ofPropertyValuesHolder(_CenterEffectText, pvh2);
        }else {
            _LeftEffectText.setVisibility(View.VISIBLE);
            _LeftEffectText.setText(_PreEffect);

            strWidth = paint.measureText(_PreEffect);

            pvh1 = PropertyValuesHolder.ofFloat("translationX", -strWidth, rootWidth / 2 - strWidth / 2);
            animator1 = ObjectAnimator.ofPropertyValuesHolder(_LeftEffectText, pvh1);

            pvh2 = PropertyValuesHolder.ofFloat("translationX", 0.0f, rootWidth / 2 + curWidth / 2);
            animator2 = ObjectAnimator.ofPropertyValuesHolder(_CenterEffectText, pvh2);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        if(_EffectText.getVisibility() == View.GONE) {
            animatorSet.play(animator1);
        }else {
            animatorSet.play(animator1).with(animator2);
        }

        if(_EffectText.getVisibility() != View.GONE) {
            _CenterEffectText.setVisibility(View.VISIBLE);
            _CenterEffectText.setText(_CurrentEffect);

            _EffectText.setVisibility(View.GONE);
        }

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                _EffectText.setVisibility(View.VISIBLE);
                _LeftEffectText.setVisibility(View.GONE);
                _CenterEffectText.setVisibility(View.GONE);
                _RightEffectText.setVisibility(View.GONE);

                if(type == FilterChooserMediator2.TYPE_SMOOTH_TO_LEFT) {
                    _EffectText.setText(_NextEffect);
                }else {
                    _EffectText.setText(_PreEffect);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private void effectTextAlphaAnimator() {
        PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(_EffectText, pvh);
        animator.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                _EffectText.setAlpha(1.0f);
                _EffectText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                _EffectText.setAlpha(1.0f);
                _EffectText.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_EFFECT_TEXT_DISAPPEAR:
                effectTextAlphaAnimator();

                break;
            }
            super.handleMessage(msg);
        }
    };
}
