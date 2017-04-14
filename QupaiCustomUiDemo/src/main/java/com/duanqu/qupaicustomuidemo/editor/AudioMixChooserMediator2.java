package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.AssetRepository.Kind;
import com.duanqu.qupai.asset.AssetRepositoryClient;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupaicustomuidemo.R;

import javax.annotation.Nonnull;


public class AudioMixChooserMediator2 extends EditParticipant
        implements AssetListAdapter.OnItemClickListener, AssetRepositoryClient.Listener, OnRenderChangeListener {

    private final RecyclerView _ListView;
    private final AssetListAdapter _Adapter;

    private final View _WeightControlView;
    private EffectService effectService;
    LinearLayoutManager _LayoutManager;
    AssetRepositoryClient _RepoClient;

    public AudioMixChooserMediator2(RecyclerView list_view, View weight_control,
                                    EffectService effectService, AssetRepositoryClient repo,
                                    EditorSession session, int rotation) {

        _ListView = list_view;
        _ListView.setItemAnimator(null);

        _LayoutManager = new LinearLayoutManager(_ListView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        _ListView.setLayoutManager(_LayoutManager);

        _WeightControlView = weight_control;
        this.effectService = effectService;
        _RepoClient = repo;

        EffectDownloadButtonMediator download_item = null;
        download_item = new EffectDownloadButtonMediator(session, _ListView, rotation);
        download_item.setCategory(Kind.SOUND);
        download_item.setTitle(R.string.qupai_btn_text_download_music);

        _Adapter = new AssetListAdapter(download_item, true);
        _Adapter.setData(repo.find(Kind.SOUND));
        _Adapter.setOnItemClickListener(this);
        _Adapter.setNullTitle(R.string.qupai_sound_mixer_effect_none);
        _Adapter.set_NullImage(R.drawable.ic_qupai_null_music);

        _ListView.setAdapter(_Adapter);

        AssetID ai = effectService.getActivedEffect();
        _Adapter.setActiveDataItem(ai);

        _RepoClient.addListener(AssetRepository.Kind.SOUND, this);
    }

    @Override
    public boolean onItemClick(AssetListAdapter adapter, int adapter_position) {
        AssetInfo asset = _Adapter.getItem(adapter_position);
        return effectService.useEffect(asset) == EffectService.EFFECTUSESUCCESS;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        _WeightControlView.setVisibility(active ? View.VISIBLE : View.GONE);

        if (active) {
            _ListView.post(new Runnable() {
                @Override
                public void run() {
                    if (_ListView.getVisibility() == View.VISIBLE) {
                        onActive();
                    }
                }
            });
        }
    }

    private void onActive() {
        int active_pos = _Adapter.getActiveAdapterPosition();
        int first = _LayoutManager.findFirstCompletelyVisibleItemPosition();
        int last = _LayoutManager.findLastCompletelyVisibleItemPosition();
        if (active_pos < first || active_pos > last) {
            _LayoutManager.scrollToPosition(active_pos);
        }
    }

    @Override
    public void onDataChange(@Nonnull AssetRepositoryClient repo, @Nonnull Kind cat) {
        _Adapter.setData(repo.find(Kind.SOUND));
    }

    @Override
    public void scrollTo(AssetID asset_id) {
        int position = _Adapter.findAdapterPosition(asset_id);
        if (position >= 0) {
            _ListView.scrollToPosition(position);
        }
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        if(!service.isMusicSupport()){
            return ;
        }
        AssetID item = effectService.getActivedEffect();

        _Adapter.setActiveDataItem(item);
    }

}
