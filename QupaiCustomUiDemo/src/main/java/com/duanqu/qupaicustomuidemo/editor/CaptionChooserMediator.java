package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.duanqu.qupai.asset.*;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupai.project.UIEditorPage;
import com.duanqu.qupai.project.UIEditorPageProxy;
import com.duanqu.qupaicustomuidemo.R;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CaptionChooserMediator extends EditParticipant
implements OverlayListAdapter.OnItemClickListener, OnRenderChangeListener,
        AssetRepositoryClient.Listener {

    private final RecyclerView _ListView;
    private final OverlayListAdapter _CaptionAdapter;

    AssetRepositoryClient _Repo;
    private final EffectService effectService;
    private EditorSession session;

    public CaptionChooserMediator(RecyclerView view, EffectService service,
                                  AssetRepositoryClient repo, EditorSession session) {
        _ListView = view;
        _ListView.setItemAnimator(null);
        this.effectService = service;
        this.session = session;
        _Repo = repo;
        _Repo.addListener(AssetRepository.Kind.FONT, this);
        LinearLayoutManager lm = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        _ListView.setLayoutManager(lm);

        EffectDownloadButtonMediator download_item = null;

        if (session.hasDownloadPage(AssetRepository.Kind.CAPTION)) {
            download_item = new EffectDownloadButtonMediator(session, _ListView, 0);
            download_item.setCategory(AssetRepository.Kind.CAPTION);
            download_item.setTitle(R.string.qupai_btn_text_download_caption);
        }

        _CaptionAdapter = new OverlayListAdapter(download_item, true);

        List<? extends AssetGroup> list = repo.getRepository().findDIYCategory();
        List<AssetGroup> available = new ArrayList<>();
        for(AssetGroup g : list){
            if(g.isAvailable() && g.getType() == AssetInfo.TYPE_CAPTION){
                available.add(g);
            }
        }

        _CaptionAdapter.setFontImage(R.drawable.qupai_caption_font_list_icon);
        _CaptionAdapter.setFontTitle(R.string.qupai_diy_font_typeface);
        _CaptionAdapter.setData(available);
        _CaptionAdapter.setOnItemClickListener(this);

        _ListView.setAdapter(_CaptionAdapter);
    }

    @Override
    public boolean onItemClick(OverlayListAdapter adapter, int adapter_position) {
        final AssetGroup value = adapter.getItem(adapter_position);

        if(value == null){
            FontSelectDialog dialog = FontSelectDialog.newInstance();
            dialog.setEffectService(effectService);
            dialog.setRepository(_Repo.getRepository());
            dialog.setSession(session);
            dialog.setCancelable(true);
            dialog.show(((Activity)_ListView.getContext()).getFragmentManager(), "font");
            return true;
        }

        OverlaySelectDialog dialog = OverlaySelectDialog.newInstance(value.getGroupId());
        dialog.setEffectService(effectService);
        dialog.setRepository(_Repo.getRepository());
        dialog.setCancelable(true);
        dialog.show(((Activity)_ListView.getContext()).getFragmentManager(), "caption");
        return true;
    }

    @Override
    public void onDataChange(@Nonnull AssetRepositoryClient repo, @Nonnull AssetRepository.Kind cat) {
        List<? extends AssetGroup> list = repo.getRepository().findDIYCategory();
        List<AssetGroup> available = new ArrayList<>();
        for(AssetGroup g : list){
            if(g.isAvailable() && g.getType() == AssetInfo.TYPE_CAPTION){
                available.add(g);
            }
        }
        _CaptionAdapter.setData(available);
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        UIEditorPage page = service.getActiveRenderMode();
        boolean isUIRender = page != null && UIEditorPageProxy.isOverlayPage(page);
        effectService.changeEffectRenderMode(isUIRender);
    }

}
