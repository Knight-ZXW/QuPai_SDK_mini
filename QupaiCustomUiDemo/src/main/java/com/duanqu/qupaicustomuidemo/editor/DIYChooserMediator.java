package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.AssetRepositoryClient;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupai.project.UIEditorPage;
import com.duanqu.qupai.project.UIEditorPageProxy;
import com.duanqu.qupaicustomuidemo.R;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DIYChooserMediator extends EditParticipant
implements OverlayListAdapter.OnItemClickListener, OnRenderChangeListener,
        AssetRepositoryClient.Listener {

    private final RecyclerView _ListView;
    private final OverlayListAdapter _DIYAdapter;

    AssetRepositoryClient _Repo;
    private final EffectService effectService;

    public DIYChooserMediator(RecyclerView view, EffectService service,
                              AssetRepositoryClient repo, EditorSession session) {
        _ListView = view;
        _ListView.setItemAnimator(null);
        this.effectService = service;
        _Repo = repo;
        _Repo.addListener(AssetRepository.Kind.DIY, this);
        LinearLayoutManager lm = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        _ListView.setLayoutManager(lm);

        EffectDownloadButtonMediator download_item = null;

        if (session.hasDownloadPage(AssetRepository.Kind.DIY)) {
            download_item = new EffectDownloadButtonMediator(session, _ListView, 0);
            download_item.setCategory(AssetRepository.Kind.DIY);
            download_item.setTitle(R.string.qupai_btn_text_download_overlay);
        }

        _DIYAdapter = new OverlayListAdapter(download_item, false);

        List<? extends AssetGroup> list = repo.getRepository().findDIYCategory();
        List<AssetGroup> available = new ArrayList<>();
        for(AssetGroup g : list){
            if(g.isAvailable() && g.getType() == AssetInfo.TYPE_DIYOVERLAY){
                available.add(g);
            }
        }
        _DIYAdapter.setData(available);
        _DIYAdapter.setOnItemClickListener(this);

        _ListView.setAdapter(_DIYAdapter);

    }

    @Override
    public boolean onItemClick(OverlayListAdapter adapter, int adapter_position) {
        final AssetGroup value = adapter.getItem(adapter_position);

        if(value == null){
            return false;
        }

        OverlaySelectDialog dialog = OverlaySelectDialog.newInstance(value.getGroupId());
        dialog.setEffectService(effectService);
        dialog.setRepository(_Repo.getRepository());
        dialog.setCancelable(true);
        dialog.show(((Activity)_ListView.getContext()).getFragmentManager(), "overlay");
        return true;
    }

    @Override
    public void onDataChange(@Nonnull AssetRepositoryClient repo, @Nonnull AssetRepository.Kind cat) {
        List<? extends AssetGroup> list = repo.getRepository().findDIYCategory();
        List<AssetGroup> available = new ArrayList<>();
        for(AssetGroup g : list){
            if(g.isAvailable() && g.getType() == AssetInfo.TYPE_DIYOVERLAY){
                available.add(g);
            }
        }
        _DIYAdapter.setData(available);
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        UIEditorPage page = service.getActiveRenderMode();
        boolean isUIRender = page != null && UIEditorPageProxy.isOverlayPage(page);
        effectService.changeEffectRenderMode(isUIRender);
    }

}
