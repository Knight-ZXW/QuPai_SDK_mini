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

    private final SeekBar _SeekBar;
    private final Player _Player;
    private final RenderEditService renderEditService;

    private final View _MusicTrackText;

    public AudioMixWeightControl(View view, RenderEditService renderService, Player player) {
        _Player = player;
        renderEditService = renderService;

        _SeekBar = (SeekBar) view.findViewById(R.id.sb_audio_mix_weight);
        _SeekBar.setOnSeekBarChangeListener(this);
        _SeekBar.setClickable(true);
        _SeekBar.setEnabled(true);

        _MusicTrackText = view.findViewById(R.id.txt_track_music);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float weight = (float) progress / seekBar.getMax();

        _Player.setAudioMixWeight(1 - weight);

        if (!fromUser) {
            return;
        }

        renderEditService.setRenderPrimaryAudioWeight(1 - weight);

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

        _SeekBar.setProgress((int) ((1 - weight) * _SeekBar.getMax()));
    }

}
