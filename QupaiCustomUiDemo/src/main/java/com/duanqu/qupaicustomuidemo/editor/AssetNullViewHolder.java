package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

public class AssetNullViewHolder extends ViewHolder {

    public AssetNullViewHolder(ViewGroup parent, int text_res_id, int img_res_id) {
        super(EffectListMediator.getNullItemView(parent, text_res_id,img_res_id));
        itemView.setTag(this);
    }

    public void onBind(boolean active) {
        itemView.setActivated(active);
    }

}
