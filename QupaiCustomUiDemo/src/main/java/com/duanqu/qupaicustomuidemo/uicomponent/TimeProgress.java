package com.duanqu.qupaicustomuidemo.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.duanqu.qupaicustomuidemo.R;

import java.util.ArrayList;

/**
 * Created by dangyutao on 2016/7/28.
 */
public class TimeProgress extends View {

    Paint recorded ,paused,minTimePaint,deltePaint;
    int posWidth = 3;
    long maxTimePos = 0;
    long minTimePos = 0;
    boolean isDeleteLastPrepare = false;
    ArrayList<Long> pauseList = new ArrayList<Long>();
    long right;
    public TimeProgress(Context context) {
        super(context);
        initPaint();
    }

    public TimeProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public TimeProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint()
    {
        recorded = new Paint();
        recorded.setColor(getResources().getColor(R.color.orange_40));
        paused = new Paint();
        paused.setColor(Color.BLUE);
        minTimePaint = new Paint();
        minTimePaint.setColor(Color.WHITE);
        deltePaint = new Paint();
        deltePaint.setColor(Color.RED);

    }
    public void setTime(long min,long max)
    {
        minTimePos = min;
        maxTimePos = max;
    }

    public void deleteLastPrepare()
    {
        isDeleteLastPrepare = true;
        this.invalidate();
    }
    public void deleteLast()
    {
        if(pauseList.size() >= 1)
            pauseList.remove(pauseList.size()-1);
        if(pauseList.size() >0 )
            right = pauseList.get(pauseList.size() -1);
        else
            right = 0;
        this.invalidate();
        Log.e("pupai","delete length = "+pauseList.size());
        isDeleteLastPrepare =false;
    }
    public void updata(long length)
    {
        isDeleteLastPrepare = false;
        right = length;
        this.invalidate();
    }
    public void setPause(long length)
    {
        pauseList.add(length);

    }
    public void clear()
    {
        pauseList.clear();
        right = 0;
        invalidate();
    }


    @Override
    public void onDraw(Canvas canvas)
    {
        drawPos( canvas,minTimePos,minTimePaint);
        drawTime( canvas,0,right,recorded);
        for (int i = 0;i < pauseList.size();i++)
        {
            drawPos( canvas,(long)pauseList.get(i),paused);
        }
        int size = pauseList.size();
        if(isDeleteLastPrepare)
        {
            if(pauseList.size() == 1)
                drawTime(canvas,0,pauseList.get(size-1),deltePaint);
            else if(pauseList.size() > 1)
                drawTime(canvas,pauseList.get(size-2),pauseList.get(size-1),deltePaint);
        }
    }
    private void drawTime(Canvas canvas,long left,long right,Paint paint)
    {
        int total = getWidth();
        int bottom = getHeight();
        float leftRatio = (float)left/(float)maxTimePos;
        float rightRatio = (float)right/(float)maxTimePos;
        int l = (int)(total*leftRatio);
        int r = (int)(total*rightRatio);
        Log.e("qupai","l = "+l+"r = "+r+"total = "+total +"maxTimePos = "+maxTimePos+"mintimepos = "+minTimePos);
        canvas.drawRect(l,0,r,bottom,paint);
    }
    private void drawPos(Canvas canvas,long left,Paint paint)
    {
        int total = getWidth();
        int bottom = getHeight();
        float leftRatio = (float)left/(float)maxTimePos;
        int l = (int)(total*leftRatio);
        int r = (int)l+ posWidth;
        canvas.drawRect(l,0,r,bottom,paint);
    }


}
