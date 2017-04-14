package com.duanqu.qupaicustomuidemo.uicomponent;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.duanqu.qupai.project.Clip;
import com.duanqu.qupai.recorder.ClipManager;
import com.duanqu.qupai.widget.ChartAdapter;
import com.duanqu.qupai.widget.HSegmentedBarChartDrawable;
import com.duanqu.qupaicustomuidemo.R;

public class TimelineSlider extends ChartAdapter.Stub implements
        ClipManager.OnClipChangeListener,
        ClipManager.Listener {

    private final View _ClipListView;

    private final LinearLayout _Underlay;
    private final View _MinDurationSpacer;

    private final HSegmentedBarChartDrawable _Drawable = new HSegmentedBarChartDrawable();

    private final ClipManager _ClipManager;

    private final Drawable _ItemDrawable;

    public TimelineSlider(View root, ClipManager cm) {
        View timeline = root.findViewById(R.id.record_timeline);

        _ClipManager = cm;

        _ClipListView = timeline.findViewById(R.id.clip_list);

        _Underlay = (LinearLayout) timeline.findViewById(R.id.timeline_underlay);
        _MinDurationSpacer = timeline.findViewById(R.id.min_capture_duration_spacer);

        _Drawable.setAdapter(this);

        _ClipListView.setBackgroundDrawable(_Drawable);

        TypedValue v = new TypedValue();

        root.getContext().getTheme().resolveAttribute(R.attr.qupaiRecorderTimelineClip, v, true);

        _ItemDrawable = root.getResources().getDrawable(v.resourceId);

        setupMinDurationIndicator();
        update();
    }

    void update() {
        _Drawable.invalidateSelf();
    }

    @Override
    public float getFloat(int id, int key) {

        Clip item = _ClipManager.getClip(id);
        switch (key) {
        case HSegmentedBarChartDrawable.KEY_WEIGHT:
            return item.getDurationMilli();
        default:
            return super.getFloat(id, key);
        }
    }

    private final int[] _DrawableStateList = new int[1];

    @Override
    public Drawable getDrawable(int id) {
        Clip vb = _ClipManager.getClip(id);

        // TODO use custom drawable state
        switch (vb.getState()) {
        case CAPTURING:
            _DrawableStateList[0] = android.R.attr.state_enabled;
            break;
        case SELECTED:
            _DrawableStateList[0] = android.R.attr.state_activated;
            break;
        case READY:
        case COMPLETED:
            _DrawableStateList[0] = 0;
            break;
        }

        _ItemDrawable.setState(null);
        _ItemDrawable.setState(_DrawableStateList);
        _ItemDrawable.invalidateSelf();

        return _ItemDrawable;
    }

    @Override
    public float getFloat(int key) {
        switch (key) {
        case HSegmentedBarChartDrawable.KEY_WEIGHT_SUM:
            return _ClipManager.getMaxDuration();
        default:
            return super.getFloat(key);
        }
    }

    @Override
    public int getCount() {
        return _ClipManager.getClipCount();
    }


    private void setupMinDurationIndicator() {
        if (_MinDurationSpacer == null) {
            return;
        }

        LayoutParams lp = (LayoutParams) _MinDurationSpacer.getLayoutParams();
        lp.weight = _ClipManager.getMinDuration();
        _MinDurationSpacer.setLayoutParams(lp);

        _Underlay.setWeightSum(_ClipManager.getMaxDuration());
    }

    @Override
    public void onClipChange(ClipManager manager, Clip vb) {
        update();
    }

    @Override
    public void onClipListChange(ClipManager manager, int event) {
        update();
    }
}
