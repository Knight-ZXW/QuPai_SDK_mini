package com.duanqu.qupaicustomuidemo.editor;

import android.view.View;
import android.view.ViewGroup;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;

public class AssetItemViewMediator extends EffectChooserItemViewMediator {

    public AssetItemViewMediator(ViewGroup list_view, int layout_id) {
        super(list_view, layout_id);
    }

    private AssetInfo _Data;

    public void setData(AssetInfo asset) {

        _Data = asset;

        // TODO use a wrapper class to handle download states
        VideoEditBean bean = (VideoEditBean) asset;

        setTitle(asset.getTitle());

        setDownloadMask(bean.isDownloadMasked());
        setDownloadable(bean.isDownloadable());

        //Log.d("ITEM", bean.getIconURIString());
        setImageURI(asset.getIconURIString());

    }

    public AssetInfo getValue(){
    	return _Data;
    }

    public void setTitleVisible(boolean value) {
        _Text.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
    }

    public void onBind(AssetInfo info, boolean active) {
        setData(info);
        if(info.getType() == AssetInfo.TYPE_DIYOVERLAY
                || info.getType() == AssetInfo.TYPE_FONT){
            return ;
        }
        itemView.setActivated(active);
    }
}
