package com.duanqu.qupaicustomuidemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.duanqu.qupaicustomuidemo.R;

public class CircleProgressView extends ProgressBar{

	private RectF mOval;
	//private Paint mPaint;
	private Paint mBackGround;
	//private int mProgress;
	private int mStrokeWidth;
	private int mViewWidth;
	private int backgroundColor;

	public CircleProgressView(Context paramContext){
	    super(paramContext);
	    //instantiate(paramContext);
	}

	public CircleProgressView(Context paramContext, AttributeSet paramAttributeSet){
	    super(paramContext, paramAttributeSet);
	    //instantiate(paramContext);
	}

	public CircleProgressView(Context paramContext, AttributeSet paramAttributeSet, int paramInt){
	    super(paramContext, paramAttributeSet, paramInt);
	    //instantiate(paramContext);
	}

	public static int dip2px(Context paramContext, float paramFloat){
	    return (int)(0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density);
	}

	private void instantiate(Context paramContext, int width) {
	    //this.mProgress = 0;
	    this.mStrokeWidth = dip2px(paramContext, 2.0F);
	    this.mOval = new RectF(this.mStrokeWidth, this.mStrokeWidth, width - mStrokeWidth, width - mStrokeWidth);
	    //this.mPaint = new Paint();
	    //this.mPaint.setAntiAlias(true);
	    //this.mPaint.setStyle(Paint.Style.STROKE);
	    //this.mPaint.setStrokeWidth(this.mStrokeWidth);
	    this.mViewWidth = width;

	    this.mBackGround = new Paint();
	    this.mBackGround.setAntiAlias(true);
	    this.mBackGround.setStyle(Paint.Style.FILL);

	    //setBackgroundColor(getResources().getColor(R.color.halftransparent));
	}

	@Override
	public void setBackgroundColor(int color){
		backgroundColor = color;
	}

	  @Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		instantiate(getContext(), getMeasuredWidth());
	}

	@Override
	protected void onDraw(Canvas paramCanvas){
	    super.onDraw(paramCanvas);
	    this.mBackGround.setColor(getResources().getColor(backgroundColor == 0 ? R.color.halftransparent : backgroundColor));
	   // this.mPaint.setColor(getResources().getColor(R.color.bg_color_2));
	   // this.mPaint.setAlpha(160);
	    paramCanvas.drawArc(this.mOval, 0.0F, 360.0F, false, this.mBackGround);
//	    if (this.mProgress <= 100){
//	    	this.mPaint.setAlpha(0);
//	        this.mPaint.setColor(getResources().getColor(R.color.video_download_progress_white));
//	        paramCanvas.drawArc(this.mOval, 270.0F, - (360 * (100 - this.mProgress) / 100), false, this.mPaint);
//	    }
	}

	  @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
	    setMeasuredDimension(width, width);
	}

//	public void setProgress(int paramInt){
//	    this.mProgress = paramInt;
//	    invalidate();
//	}

}
