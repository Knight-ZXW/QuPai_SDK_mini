package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.uil.UILOptions;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.List;

public class OverlayListAdapter extends RecyclerView.Adapter<ViewHolder>
        implements View.OnClickListener {

    public interface OnItemClickListener {
        boolean onItemClick(OverlayListAdapter adapter, int adapter_position);
    }

    private final EffectDownloadButtonMediator _DownloadItem;

    public OverlayListAdapter(EffectDownloadButtonMediator download, boolean hasFont) {
        _DownloadItem = download;
        if (_DownloadItem != null && hasFont) {
            _HeaderList = new int[] { VIEW_TYPE_DOWNLOAD, VIEW_TYPE_FONT };
        } else if(_DownloadItem != null){
            _HeaderList = new int[] {VIEW_TYPE_DOWNLOAD};
        }else if(hasFont){
            _HeaderList = new int[] {VIEW_TYPE_FONT};
        }else{
            _HeaderList = new int[0];
        }

        setHasStableIds(true);
    }

    private int fontTitle;
    private int fontImage;

    public void setFontTitle(int fontTitle) {
        this.fontTitle = fontTitle;
    }

    public void setFontImage(int fontImage) {
        this.fontImage = fontImage;
    }

    @Override
    public long getItemId(int position) {
        if (position < _HeaderList.length) {
            return RecyclerView.NO_ID;
        } else {
            return _List.get(position - _HeaderList.length).getGroupId();
        }
    }

    @Override
    public int getItemCount() {
        return _List.size() + _HeaderList.length;
    }

    private final int[] _HeaderList;

    @Override
    public int getItemViewType(int position) {
        if (position < _HeaderList.length) {
            return _HeaderList[position];
        } else {
            return VIEW_TYPE_ASSET;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < _HeaderList.length) {
            switch (_HeaderList[position]) {
                case VIEW_TYPE_DOWNLOAD:
                    // stateless
                    break;
            }
        } else {
            ((ItemHolder) holder).setData(getItem(position));
        }
    }

    public AssetGroup getItem(int adapter_position) {
        int data_pos = adapter_position - _HeaderList.length;
        if (data_pos < 0) {
            return null;
        }
        return _List.get(data_pos);
    }

    private static final int VIEW_TYPE_ASSET = 0;
    private static final int VIEW_TYPE_DOWNLOAD = 2;
    private static final int VIEW_TYPE_FONT = 3;

    private int _ItemLayoutID = R.layout.item_qupai_editor_asset_group_long;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_ASSET:
                holder = new ItemHolder(parent, _ItemLayoutID);
                ((ItemHolder) holder).setTitleVisible(_TitleVisible);
                holder.itemView.setOnClickListener(this);
                break;
            case VIEW_TYPE_DOWNLOAD :
                return  _DownloadItem;
            case VIEW_TYPE_FONT:
                holder = new AssetNullViewHolder(parent, fontTitle, fontImage);
                holder.itemView.setOnClickListener(this);
                break;
            default:
                return null;
        }

        return holder;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView text;
        private boolean titleVisble;
        public ItemHolder(ViewGroup list_view, int layout_id) {
            super(FontUtil.applyFontByInflate(
                    list_view.getContext(), layout_id, list_view, false));
            image = (ImageView) itemView.findViewById(R.id.effect_chooser_item_image);
            text = (TextView)itemView.findViewById(R.id.effect_chooser_item_text);

            itemView.setTag(this);
        }

        public void setData(AssetGroup group){
            ImageLoader.getInstance().displayImage(group.getIconUrl(), image, UILOptions.DISK);
            text.setVisibility(titleVisble ? View.VISIBLE : View.GONE);
            text.setText(group.getName());
        }

        public void setTitleVisible(boolean titleVisible){
            this.titleVisble = titleVisible;
        }
    }

    private List<? extends AssetGroup> _List = Collections.EMPTY_LIST;

    public void setData(List<? extends AssetGroup> list) {
        _List = list;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        ViewHolder holder = (ViewHolder) v.getTag();
        int adapter_pos = holder.getAdapterPosition();

        if (_OnItemClickListener != null) {
            Log.d("active", "onItemClick");
            if (!_OnItemClickListener.onItemClick(this, adapter_pos)) {
                Log.d("active", "onItemClick1");
                return;
            }
        }

        Log.d("active", "adapter_pos:" + adapter_pos + "_HeaderList.length:" + _HeaderList.length);
    }

    private boolean _TitleVisible = true;

    public void setTitleVisible(boolean visible) {
        _TitleVisible = visible;
    }

    private OnItemClickListener _OnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        _OnItemClickListener = listener;
    }

}
