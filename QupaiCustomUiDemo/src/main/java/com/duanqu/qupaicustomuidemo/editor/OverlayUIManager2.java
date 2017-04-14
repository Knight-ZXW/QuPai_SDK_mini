package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import com.duanqu.qupai.asset.AssetRepository;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.duanqu.qupai.effect.*;
import com.duanqu.qupaicustomuidemo.R;

/**
 *
 * Created by Administrator on 2016/12/30.
 */
public class OverlayUIManager2 extends OverlayUIManager {


    OverlayUIManager2(Activity activity, AssetRepository repository,
                                EffectService overlayEffectService, Player player) {
        super(activity, repository, overlayEffectService, player);
    }

    @Override
    public void onOverlayAdded(OverlayUIController controller) {
        super.onOverlayAdded(controller);
        if(controller != editController){
            View v = controller.getEditOverlayView();
            final GestureDetector gesture = new GestureDetector(v.getContext(),
                    gestureListener);
            v.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            gesture.onTouchEvent(event);
                            return true;
                        }
                    }
            );
        }
    }

    private class MyGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        float mPosX;
        float mPosY;
        private OverlayUIController currentEdit;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d("MOVE", "onDoubleTapEvent");
            findCurrentOverlayForUser(e.getX(), e.getY());
            if(currentEdit != null){
                onOverlayTextEditing(currentEdit);
            }
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (currentEdit != null) {
                if (mPosX == 0 || mPosY == 0) {
                    mPosX = e1.getX();
                    mPosY = e1.getY();
                }
                float x = e2.getX();
                float y = e2.getY();

                currentEdit.moveContent(x - mPosX, y - mPosY);

                mPosX = x;
                mPosY = y;
            }

            Log.d("MOVE", "onScroll"
                    + " x : " + mPosX + " y : " + mPosY + " dx : "
                    + distanceX + " dy : " + distanceY);

            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("MOVE", "onDown" + " (" + e.getX() + " : " + e.getY()
                    + ")");
            findCurrentOverlayForUser(e.getX(), e.getY());

            mPosX = 0;
            mPosY = 0;
            return false;
        }

        private void findCurrentOverlayForUser(float x, float y){
            OverlayUIController current = getCurrentEditOverlay();
            boolean isCurrent = current != null && current.contentContains(x, y);
            if(!isCurrent){
                for(OverlayUIController uic : getOverlays()){
                    if(uic.isVisible() && uic.contentContains(x, y)){
                        current = uic;
                        break;
                    }
                }
            }

            currentEdit = current;
        }
    }

    private final MyGestureListener gestureListener = new MyGestureListener();

}
