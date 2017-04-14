package com.duanqu.qupaicustomuidemo.trim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.duanqu.qupai.utils.BitmapUtil;
import com.duanqu.qupai.view.HorizontalListView;
import com.duanqu.qupaicustomuidemo.R;

public class VideoSliceSeekBar extends ImageView {
    private static String TAG = "VideoSliceSeekBar";
    private static int MERGIN_PADDING = 20;

    enum SELECT_THUMB {
        SELECT_THUMB_NONE,
        SELECT_THUMB_LEFT,
        SELECT_THUMB_MORE_LEFT,
        SELECT_THUMB_RIGHT,
        SELECT_THUMB_MORE_RIGHT
    }

    enum SELECT_STATUS {
        SELECT_STATUS_NONE,
        SELECT_STATUS_MOVE
    }

    //params
    private Bitmap thumbSlice;
    private Bitmap thumbSliceRight;
    private Bitmap thumbCurrentVideoPosition = BitmapUtil.drawableToBitmap(getResources().getDrawable(R.drawable.theme_default_thumb_qupai_trim_editbar));
    private int progressMinDiff = 25; //percentage
    private int progressColor = getResources().getColor(R.color.qupai_black_opacity_30pct);
    private int greenColor;
    private int progressHalfHeight = 0;
    private int thumbPadding = getResources().getDimensionPixelOffset(R.dimen.qupai_seekbar_default_margin);
    private float maxValue = 100f;

    private int progressMinDiffPixels;
    private int thumbSliceLeftX, thumbSliceRightX, thumbCurrentVideoPositionX, thumbMaxSliceRightx,defalutToatalRightX;
    private float thumbSliceLeftValue, thumbSliceRightValue;
    private int thumbSliceY, thumbCurrentVideoPositionY;
    private Paint paint = new Paint();
    private Paint paintThumb = new Paint();
    private SELECT_THUMB selectedThumb;
    private SELECT_STATUS selectedStatus;
    private int thumbSliceHalfWidth, thumbCurrentVideoPositionHalfWidth;
    private SeekBarChangeListener scl;

    private HorizontalListView mHorizontalListView = null;

    private int progressTop;
    private int progressBottom;

    private boolean blocked;
    private boolean isVideoStatusDisplay;

    private boolean isTouch = false;
    private boolean isDefaultSeekTotal;
    private int prevX;
    private int downX;

    public VideoSliceSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initValue(context);
    }

    public VideoSliceSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue(context);
    }

    public VideoSliceSeekBar(Context context) {
        super(context);
        initValue(context);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        init();
    }


    private void initValue(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.qupaiSweepright, value, true);
        int leftSweepId = value.resourceId;
        thumbSlice = BitmapFactory.decodeResource(getResources(), R.drawable.theme_qupai_default_sweep_right);

        context.getTheme().resolveAttribute(R.attr.qupaiSweepLeft, value, true);
        int rightSweepId = value.resourceId;
        thumbSliceRight = BitmapFactory.decodeResource(getResources(), R.drawable.theme_qupai_default_sweep_left);

        context.getTheme().resolveAttribute(R.attr.qupaiTrimRecordColor, value, true);
        greenColor = value.data;
    }

    private void init() {
        if (thumbSlice.getHeight() > getHeight()) {
            getLayoutParams().height = thumbSlice.getHeight();
        }

        thumbSliceY = (getHeight() / 2) - (thumbSlice.getHeight() / 2);
        thumbCurrentVideoPositionY = getResources().getDimensionPixelOffset(R.dimen.qupai_seekbar_playbar_margin);

        thumbSliceHalfWidth = thumbSlice.getWidth() / 2;
        thumbCurrentVideoPositionHalfWidth = thumbCurrentVideoPosition.getWidth() / 2;
        if (thumbSliceLeftX == 0) {
            thumbSliceLeftX = thumbPadding;
        }
        progressMinDiffPixels = calculateCorrds(progressMinDiff) - 2 * thumbPadding;
        progressTop = 0;
//      progressBottom = getHeight() / 2 + progressHalfHeight;
        progressBottom = getResources().getDimensionPixelSize(R.dimen.fasthscroll_thumb_bg_height);

        selectedThumb = SELECT_THUMB.SELECT_THUMB_NONE;
        selectedStatus = SELECT_STATUS.SELECT_STATUS_NONE;
        invalidate();
    }

    public void setSeekBarChangeListener(SeekBarChangeListener scl) {
        this.scl = scl;
    }

    public void setHorizontalListView(HorizontalListView horizontalListView) {
        this.mHorizontalListView = horizontalListView;
    }

    private boolean adjustSliceXY(int mx) {

        boolean isNoneArea = false;
        int thumbSliceDistance = thumbSliceRightX - thumbSliceLeftX;
        if (thumbSliceDistance <= progressMinDiffPixels
                && selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_RIGHT
                && mx <= downX || thumbSliceDistance <= progressMinDiffPixels
                && selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_LEFT
                && mx >= downX) {
            isNoneArea = true;
        }

        if (thumbSliceDistance <= progressMinDiffPixels
                && selectedThumb == SELECT_THUMB.SELECT_THUMB_RIGHT
                && mx <= downX || thumbSliceDistance <= progressMinDiffPixels
                && selectedThumb == SELECT_THUMB.SELECT_THUMB_LEFT
                && mx >= downX) {

            isNoneArea = true;
        }

        if (isNoneArea) {
            if (selectedThumb == SELECT_THUMB.SELECT_THUMB_RIGHT || selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_RIGHT) {
                thumbSliceRightX = thumbSliceLeftX + progressMinDiffPixels;
            } else if (selectedThumb == SELECT_THUMB.SELECT_THUMB_LEFT || selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_LEFT) {
                thumbSliceLeftX = thumbSliceRightX - progressMinDiffPixels;
            }

            selectedStatus = SELECT_STATUS.SELECT_STATUS_NONE;
            return true;
        }

        if (mx > thumbMaxSliceRightx && (selectedThumb == SELECT_THUMB.SELECT_THUMB_RIGHT || selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_RIGHT)) {
            thumbSliceRightX = thumbMaxSliceRightx;
            selectedStatus = SELECT_STATUS.SELECT_STATUS_NONE;
            return true;
        }

        if (thumbSliceRightX >= getWidth() - MERGIN_PADDING) {
            thumbSliceRightX = getWidth();
        }

        if (thumbSliceLeftX < MERGIN_PADDING) {
            thumbSliceLeftX = 0;
        }

        return false;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect;
        //generate and draw progress
        int bottomHeight= getResources().getDimensionPixelSize(R.dimen.fasthscroll_thumb_height);
        paint.setColor(progressColor);
        rect = new Rect(thumbPadding, progressTop, thumbSliceLeftX, progressBottom + bottomHeight);
        canvas.drawRect(rect, paint);

        Log.d("thumbSliceRightX", "thumbSliceRightX:" + thumbSliceRightX + "thumbPadding" + thumbPadding);
        rect = new Rect(thumbSliceRightX, progressTop, defalutToatalRightX, progressBottom + bottomHeight);
        canvas.drawRect(rect, paint);

        int width = getResources().getDimensionPixelSize(R.dimen.fasthscroll_thumb_height);
        paint.setColor(greenColor);
        rect = new Rect(thumbSliceLeftX, progressTop, thumbSliceRightX, progressTop + width);
        canvas.drawRect(rect, paint);


        paint.setColor(greenColor);
        rect = new Rect(thumbSliceLeftX, progressBottom, thumbSliceRightX, progressBottom+bottomHeight);
        canvas.drawRect(rect, paint);

        //if (!blocked) {
        //generate and draw thumbs pointer
        canvas.drawBitmap(thumbSlice, thumbSliceLeftX, 0, paintThumb);
        canvas.drawBitmap(thumbSliceRight, thumbSliceRightX - thumbSliceHalfWidth * 2, 0, paintThumb);
        //}

        if (isVideoStatusDisplay) {
            //generate and draw video thump pointer
            canvas.drawBitmap(thumbCurrentVideoPosition, thumbCurrentVideoPositionX, thumbCurrentVideoPositionY, paintThumb);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (null != mHorizontalListView && mHorizontalListView.getItemContentHight() >= event.getY()) {
            return mHorizontalListView.onTouchEvent(event);
        }

        if (!blocked) {
            int mx = (int) event.getX();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mx <= thumbSliceLeftX + thumbSliceHalfWidth * 2) {
                        if (mx >= thumbSliceLeftX) {
                            selectedThumb = SELECT_THUMB.SELECT_THUMB_LEFT;
                        } else {
                            selectedThumb = SELECT_THUMB.SELECT_THUMB_MORE_LEFT;
                        }
                    } else if (mx >= thumbSliceRightX - thumbSliceHalfWidth * 2) {
                        if (mx <= thumbSliceRightX) {
                            selectedThumb = SELECT_THUMB.SELECT_THUMB_RIGHT;
                        } else {
                            selectedThumb = SELECT_THUMB.SELECT_THUMB_MORE_RIGHT;
                        }

                    }
                    selectedStatus = SELECT_STATUS.SELECT_STATUS_MOVE;
                    downX = mx;
                    prevX = mx;
                    break;
                case MotionEvent.ACTION_MOVE:

                    Log.i(TAG, "ACTION_MOVE" + " mx:" + mx + " lf:" + thumbSliceLeftX + " rf:" + thumbSliceRightX + " px:" + prevX + " dx:" + downX);
                    if (selectedThumb == SELECT_THUMB.SELECT_THUMB_LEFT) {
                        thumbSliceLeftX = mx;
                    } else if (selectedThumb == SELECT_THUMB.SELECT_THUMB_RIGHT) {
                        thumbSliceRightX = mx;
                    } else if (selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_RIGHT) {
                        int distance = mx - prevX;
                        thumbSliceRightX += distance;
                    } else if (selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_LEFT) {
                        int distance = mx - prevX;
                        thumbSliceLeftX += distance;
                    }

                    if (adjustSliceXY(mx)) {
                        break;
                    }

                    selectedStatus = SELECT_STATUS.SELECT_STATUS_MOVE;
                    prevX = mx;
                    break;
                case MotionEvent.ACTION_UP:
                    downX = mx;
                    adjustSliceXY(mx);
                    selectedThumb = SELECT_THUMB.SELECT_THUMB_NONE;
                    selectedStatus = SELECT_STATUS.SELECT_STATUS_NONE;
                    break;
            }

            if (mx != downX) {//&& selectedStatus!=SELECT_STATUS.SELECT_STATUS_NONE
                isTouch = true;
                notifySeekBarValueChanged();
            }
        }
        return true;
    }

    private void notifySeekBarValueChanged() {
        if (thumbSliceLeftX < thumbPadding)
            thumbSliceLeftX = thumbPadding;

        if (thumbSliceRightX < thumbPadding)
            thumbSliceRightX = thumbPadding;

        if (thumbSliceLeftX > getWidth() - thumbPadding)
            thumbSliceLeftX = getWidth() - thumbPadding;

        if (thumbSliceRightX > getWidth() - thumbPadding)
            thumbSliceRightX = getWidth() - thumbPadding;

        invalidate();
        if (scl != null) {
            calculateThumbValue();

            if (isTouch) {
                Log.d(TAG, "thumbSliceLeftValue" + thumbSliceLeftValue + "thumbSliceRightValue" + thumbSliceRightValue);
                if (selectedThumb == SELECT_THUMB.SELECT_THUMB_LEFT || selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_LEFT) {
                    scl.SeekBarValueChanged(thumbSliceLeftValue, thumbSliceRightValue, 0);
                } else if (selectedThumb == SELECT_THUMB.SELECT_THUMB_RIGHT || selectedThumb == SELECT_THUMB.SELECT_THUMB_MORE_RIGHT) {
                    scl.SeekBarValueChanged(thumbSliceLeftValue, thumbSliceRightValue, 1);
                } else {
                    scl.SeekBarValueChanged(thumbSliceLeftValue, thumbSliceRightValue, 2);
                }
            }
        }

        isTouch = false;
    }

    private void calculateThumbValue() {
        if (0 == getWidth()) {
            return;
        }

        thumbSliceLeftValue = maxValue * thumbSliceLeftX / getWidth();
        thumbSliceRightValue = maxValue * thumbSliceRightX / getWidth();
        Log.d(TAG, "thumbSliceRightValue" + thumbSliceRightValue + "getWidth()" + getWidth() + "thumbSliceRightX" + thumbSliceRightX);
    }


    private int calculateCorrds(int progress) {
        return (int) ((getWidth() / maxValue) * progress);
    }

    public void setLeftProgress(int progress) {
        if (progress <= thumbSliceRightValue - progressMinDiff) {
            thumbSliceLeftX = calculateCorrds(progress);
        }
        notifySeekBarValueChanged();
    }

    public void setRightProgress(int progress) {
        if (progress >= thumbSliceLeftValue + progressMinDiff) {
            thumbSliceRightX = calculateCorrds(progress);
            if(!isDefaultSeekTotal){
                isDefaultSeekTotal = true;
                defalutToatalRightX =  thumbSliceRightX;
            }
        }
        notifySeekBarValueChanged();
    }

    public float getLeftProgress() {
        return thumbSliceLeftValue;
    }

    public float getRightProgress() {
        return thumbSliceRightValue;
    }

    public void setProgress(int leftProgress, int rightProgress) {
        if (rightProgress - leftProgress >= progressMinDiff) {
            thumbSliceLeftX = calculateCorrds(leftProgress);
            thumbSliceRightX = calculateCorrds(rightProgress);
        }
        notifySeekBarValueChanged();
    }

    public void videoPlayingProgress(int progress) {
        isVideoStatusDisplay = true;
        thumbCurrentVideoPositionX = calculateCorrds(progress);
        invalidate();
    }

    public void removeVideoStatusThumb() {
        isVideoStatusDisplay = false;
        invalidate();
    }

    public void setSliceBlocked(boolean isBLock) {
        blocked = isBLock;
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setProgressMinDiff(int progressMinDiff) {
        this.progressMinDiff = progressMinDiff;
        progressMinDiffPixels = calculateCorrds(progressMinDiff);
    }

    public void setProgressHeight(int progressHeight) {
        this.progressHalfHeight = progressHalfHeight / 2;
        invalidate();
    }

    public void setThumbSlice(Bitmap thumbSlice) {
        this.thumbSlice = thumbSlice;
        init();
    }

    public void setThumbCurrentVideoPosition(Bitmap thumbCurrentVideoPosition) {
        this.thumbCurrentVideoPosition = thumbCurrentVideoPosition;
        init();
    }

    public void setThumbPadding(int thumbPadding) {
        this.thumbPadding = thumbPadding;
        invalidate();
    }

    public void setThumbMaxSliceRightx(int maxRightThumb) {
        this.thumbMaxSliceRightx = maxRightThumb;
    }

    public interface SeekBarChangeListener {
        void SeekBarValueChanged(float leftThumb, float rightThumb, int whitchSide);
    }

}
