package com.duanqu.qupaicustomuidemo.uicomponent;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import com.duanqu.qupai.project.Clip;
import com.duanqu.qupai.recorder.ClipManager;
import com.duanqu.qupaicustomuidemo.R;

public class TimelineIndicator
implements ClipManager.OnClipChangeListener, ClipManager.Listener {

    private final TimelineTimeLayout _Layout;
    private final TextView _TimeText;

    public TimelineIndicator(TimelineTimeLayout layout, ClipManager clip_manager) {
        _Layout = layout;
        _TimeText = (TextView) layout.getChildAt(0);

        update(clip_manager);
    }

    private void update(ClipManager manager) {
        int duration = manager.getDuration();

        if (duration == 0) {
            _TimeText.setVisibility(View.GONE);
            return;
        }

        _TimeText.setVisibility(View.VISIBLE);

        float progress = (float) manager.getDuration() / manager.getMaxDuration();

        _Layout.setProgress(progress);

        float dur_sec = (float) duration / 1000;

        Resources res = _Layout.getResources();

        String time_str = res.getString(R.string.qupai_recorder_timeline_time_format, dur_sec);
        _TimeText.setText(time_str);
    }

    @Override
    public void onClipChange(ClipManager manager, Clip vb) {
        update(manager);
    }

    @Override
    public void onClipListChange(ClipManager manager, int event) {
        update(manager);
    }
}
