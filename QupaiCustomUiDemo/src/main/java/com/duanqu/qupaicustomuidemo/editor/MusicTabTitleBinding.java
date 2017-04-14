package com.duanqu.qupaicustomuidemo.editor;

import android.util.Log;
import android.widget.TextView;

import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.editor.ProjectClient;
import com.duanqu.qupaicustomuidemo.R;

/**
 * 音乐变化显示
 */
public class MusicTabTitleBinding implements ProjectClient.OnChangeListener {

    private final TextView _TitleView;

    public MusicTabTitleBinding(TextView title) {
        _TitleView = title;
    }


    @Override
    public void onChange(ProjectClient client, int bits) {
        long s1 = System.currentTimeMillis();
        if ((bits & ProjectClient.CHANGE_BIT_AUDIO_MIX) == 0) {
            return;
        }

        if (!client.hasProject()) {
            _TitleView.setText(R.string.qupai_effect_audio_mix);
            _TitleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            return;
        }

        AssetInfo music = client.getResolvedAudioMix();

        if (music == null) {
            _TitleView.setText(R.string.qupai_effect_audio_mix);
            _TitleView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        } else {
            _TitleView.setText(music.getTitle());
            _TitleView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.theme_default_ic_edit_effect_audio_mix,0,0,0);
        }
        Log.d("TIMEEDIT", "notifyChange item MusicTabTitleBinding : "  + bits + " 耗时 ："+ (System.currentTimeMillis() - s1));

    }

}
