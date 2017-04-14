package com.duanqu.qupaicustomuidemo.editor;

import android.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository.Kind;
import com.duanqu.qupai.asset.AssetRepositoryClient;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupaicustomuidemo.R;

import java.util.ArrayList;

public class MVChooserMediator2 extends EditParticipant
        implements AssetRepositoryClient.Listener, OnRenderChangeListener, AssetListAdapter.OnItemClickListener {

    private final RecyclerView _ListView;
    private final AssetListAdapter _OverlayAdatper;

    EditorSession _Session;
    AssetRepositoryClient provider;
    EffectService effectService;

    public MVChooserMediator2(RecyclerView view, EditorSession session,
                              EffectService effectService,
                              AssetRepositoryClient assetRepositoryClient, int rotation) {

        _ListView = view;

        _ListView.setItemAnimator(null);

        _Session = session;
        provider = assetRepositoryClient;
        this.effectService = effectService;

        LinearLayoutManager lm = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        _ListView.setLayoutManager(lm);

        EffectDownloadButtonMediator download_item = null;

        if (_Session.hasDownloadPage(Kind.MV)) {
            download_item = new EffectDownloadButtonMediator(_Session, _ListView, rotation);
            download_item.setCategory(Kind.MV);
            download_item.setTitle(R.string.qupai_btn_text_download_mv);
        }

//        AbstractDownloadManager downloadManager = (AbstractDownloadManager)comp.getDownloader();
        _OverlayAdatper = new AssetListAdapter(download_item, true);
        _OverlayAdatper.setOnItemClickListener(this);
        _OverlayAdatper.setNullTitle(R.string.qupai_ve_none);
        _OverlayAdatper.set_NullImage(R.drawable.ic_qupai_yuanpian);

        provider.addListener(Kind.MV, this);
        ArrayList<AssetInfo> list = new ArrayList<>();
        list.addAll(provider.find(Kind.MV));
        _OverlayAdatper.setData(list);

        _ListView.setAdapter(_OverlayAdatper);

        AssetID info = effectService.getActivedEffect();

        _OverlayAdatper.setActiveDataItem(info);
    }

    @Override
    public boolean onItemClick(AssetListAdapter adapter, int adapter_position) {
        AssetInfo item = _OverlayAdatper.getItem(adapter_position);
        int ret = effectService.useEffect(item);
        if(ret == EffectService.EFFECTNOTPAY){
                new AlertDialog.Builder(_ListView.getContext())
                        .setMessage(R.string.qupai_license_needbug)
                        .setPositiveButton(R.string.qupai_dlg_button_confirm,null)
                        .show();
        }else if(ret == EffectService.EFFECTNORESOURCE){
            new AlertDialog.Builder(_ListView.getContext())
                    .setMessage(R.string.qupai_effect_mv_nosuppport)
                    .setPositiveButton(R.string.qupai_dlg_button_confirm,null)
                    .show();
        }
        return ret == EffectService.EFFECTUSESUCCESS;
    }

    @Override
    public void onDataChange(AssetRepositoryClient repo, Kind cat) {
        _OverlayAdatper.setData(repo.find(Kind.MV));
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        if(!service.isMvSupport()){
            return ;
        }

        AssetID asset_id = effectService.getActivedEffect();
        _OverlayAdatper.setActiveDataItem(asset_id);
    }

    @Override
    public void scrollTo(AssetID asset_id) {
        int pos = _OverlayAdatper.findAdapterPosition(asset_id);
        _ListView.scrollToPosition(pos);
    }

}
