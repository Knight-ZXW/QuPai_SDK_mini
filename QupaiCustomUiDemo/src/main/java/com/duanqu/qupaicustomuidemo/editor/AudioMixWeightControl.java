package com.duanqu.qupaicustomuidemo.editor;

import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.Player;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupaicustomuidemo.R;

public class AudioMixWeightControl
        implements OnSeekBarChangeListener, OnRenderChangeListener {

    private final SeekBar mixMusicSeekBar;
    private final SeekBar mixPrimaryAudioSeekBar;
    private final Player mPlayer;
    private final RenderEditService renderEditService;


    public AudioMixWeightControl(View view, RenderEditService renderService, Player player) {
        mPlayer = player;
        renderEditService = renderService;

        mixMusicSeekBar = (SeekBar) view.findViewById(R.id.sb_audio_mix_weight);
        mixMusicSeekBar.setOnSeekBarChangeListener(this);

        mixPrimaryAudioSeekBar = (SeekBar) view.findViewById(R.id.sb_audio_primary_audio_weight);
        mixPrimaryAudioSeekBar.setOnSeekBarChangeListener(this);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() ==R.id.sb_audio_mix_weight){
            float weight = (float) progress / seekBar.getMax();

            mPlayer.setAudioMixWeight(weight);

        } else if (seekBar.getId() ==R.id.sb_audio_primary_audio_weight){
            if (!fromUser) {
                return;
            }
            float weight = (float) progress / seekBar.getMax();
            renderEditService.setRenderPrimaryAudioWeight(weight);
        }


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //TODO onstart
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //TODO onStop
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        float weight = service.getRenderPrimaryAudioWeight();
        mixPrimaryAudioSeekBar.setProgress((int)weight * mixPrimaryAudioSeekBar.getMax());
    }

}
