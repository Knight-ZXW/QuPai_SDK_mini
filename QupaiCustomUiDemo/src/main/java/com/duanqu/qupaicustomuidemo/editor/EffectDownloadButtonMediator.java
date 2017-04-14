package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.duanqu.qupai.asset.AssetRepository.Kind;
import com.duanqu.qupaicustomuidemo.R;

public class EffectDownloadButtonMediator extends RecyclerView.ViewHolder implements OnClickListener {

    private EditorSession mEditorSession;
    private int  mRotation;
    public EffectDownloadButtonMediator(EditorSession session,ViewGroup list_view) {
        super(getItemView(list_view));

        mEditorSession = session;
        itemView.setTag(this);
        itemView.setOnClickListener(this);
    }

    public EffectDownloadButtonMediator(EditorSession session, ViewGroup list_view, int rotation) {
        super(getItemView(list_view));
        mRotation = rotation;
        mEditorSession = session;
        itemView.setTag(this);
        itemView.setOnClickListener(this);
    }

    private Kind _Category;

    public void setCategory(Kind category) {
        _Category = category;
    }

    @Override
    public void onClick(View v) {
        //showDownLoad
        mEditorSession.openDownloadPage(_Category, mRotation);
    }

    public View getView() { return itemView; }

    public void setTitle(int title) {
        TextView text = (TextView) itemView.findViewById(R.id.effect_chooser_item_text);
        text.setText(title);
    }

    private static View getItemView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(R.layout.item_header_qupai_editor_asset_more, parent, false);
    }

}
