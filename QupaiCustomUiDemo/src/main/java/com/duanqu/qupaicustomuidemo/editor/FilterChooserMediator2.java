package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.AssetRepository.Kind;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupaicustomuidemo.R;
public class FilterChooserMediator2 extends EditParticipant
implements OnRenderChangeListener, AssetListAdapter.OnItemClickListener {
    public static final int TYPE_SMOOTH_TO_LEFT = 0;
    public static final int TYPE_SMOOTH_TO_RIGHT = 1;

    private final RecyclerView   _ListView;
    private final AssetListAdapter _FilterAdapter;

    private final EffectService effectService;

    public FilterChooserMediator2(RecyclerView view,  EffectService service,
                                  AssetRepository repo) {
        _ListView = view;
        _ListView.setItemAnimator(null);
        this.effectService = service;

        LinearLayoutManager _LayoutManager = new LinearLayoutManager(_ListView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        _ListView.setLayoutManager(_LayoutManager);

        _FilterAdapter = new AssetListAdapter(null,true);
        _FilterAdapter.setData(repo.find(Kind.FILTER));
        _FilterAdapter.setOnItemClickListener(this);
        _FilterAdapter.setNullTitle(R.string.qupai_ve_none);
        _FilterAdapter.set_NullImage(R.drawable.ic_qupai_yuanpian);

        _ListView.setAdapter(_FilterAdapter);

        AssetID asset_id = service.getActivedEffect();

        setCheckedItem(asset_id);
    }

    private void setCheckedItem(AssetID assset_id) {
        int position = _FilterAdapter.setActiveDataItem(assset_id);

        _ListView.scrollToPosition(position);
    }

    @Override
    public boolean onItemClick(AssetListAdapter adapter, int adapter_position) {
        AssetInfo item = _FilterAdapter.getItem(adapter_position);

        effectService.useEffect(item == null ? null : item.getAssetID());

        return true;
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        AssetID filter = effectService.getActivedEffect();
        setCheckedItem(filter);
    }

}
