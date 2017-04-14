package com.duanqu.qupaicustomuidemo.uicomponent;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duanqu.qupaicustomuidemo.R;

public class TimelineTimeLayout extends RelativeLayout {

    TextView child; // 当前实际
    TimeProgress progress; //
    long maxTimeLength = 0;
    long minTimeLength = 0;
    public TimelineTimeLayout(Context context) {
        this(context, null);
    }

    public TimelineTimeLayout(Context context, AttributeSet attrs) {
        super(context, attrs,   0);
    }

    public TimelineTimeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setChild(TextView text, TimeProgress p)
    {
        child = text;
        child.setVisibility(View.INVISIBLE);
        progress = p;
        p.setVisibility(View.VISIBLE);
    }


    public void setTime(long minTimeLength,long maxTimeLength)
    {
        this.minTimeLength = minTimeLength;
        this.maxTimeLength = maxTimeLength;
        progress.setTime(minTimeLength, maxTimeLength);
    }
    public  void deleteLastPrepare()
    {
        progress.deleteLastPrepare();
    }

    public void deleteLast()
    {
        progress.deleteLast();
    }

    public void update(long duration ) {

        if (duration == 0) {
            child.setVisibility(View.GONE);
            return;
        }

        child.setVisibility(View.VISIBLE);
        float p = (float) duration / maxTimeLength;
        setProgress(p);

        float dur_sec = (float) duration / 1000;

        Resources res = getResources();

        String time_str = res.getString(R.string.qupai_recorder_timeline_time_format, dur_sec);
        child.setText(time_str);
        if(progress!=null) {

            progress.updata(duration);
        }
    }
    public void setPause(long timestamp)
    {
        progress.setPause(timestamp);
    }
    public void clear()
    {
        update(0);
        progress.clear();
    }


    public void setProgress(float value) {
        int width = getWidth();
        float x = width * value;

        int child_width = child.getWidth();
        int child_bg;
        if(x+child_width < width)
        {
            child_bg = R.drawable.recorder_qupai_time_balloon_tip_bg_left;
            child.setBackgroundResource(child_bg);
            child.setX(x);
        }
        else
        {
            child_bg = R.drawable.recorder_qupai_time_balloon_tip_bg_right;
            child.setBackgroundResource(child_bg);
            child.setX(x-child_width);
        }

    }


}
