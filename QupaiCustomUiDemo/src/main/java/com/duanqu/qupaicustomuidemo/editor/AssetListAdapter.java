package com.duanqu.qupaicustomuidemo.editor;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupaicustomuidemo.R;

import java.util.Collections;
import java.util.List;

public class AssetListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {

    public interface OnItemClickListener {
        boolean onItemClick(AssetListAdapter adapter, int adapter_position);
    }

    private final EffectDownloadButtonMediator _DownloadItem;

    public AssetListAdapter(EffectDownloadButtonMediator download,boolean hasRaw) {
        this(download, -1, hasRaw);
    }

    public AssetListAdapter(EffectDownloadButtonMediator download, int layoutId, boolean hasRaw) {
        _DownloadItem = download;
        if(layoutId != -1){
            _ItemLayoutID = layoutId;
        }
        if (_DownloadItem != null && hasRaw) {
            _HeaderList = new int[] { VIEW_TYPE_DOWNLOAD, VIEW_TYPE_NULL };
        } else {
            if (hasRaw) {
                _HeaderList = new int[]{VIEW_TYPE_NULL};
            } else {
                if(_DownloadItem != null){
                    _HeaderList = new int[] {VIEW_TYPE_DOWNLOAD};
                }else{
                    _HeaderList = new int[0];
                }

            }
        }

        setHasStableIds(true);
        _ActiveAdapterPosition = _HeaderList.length - 1;

    }

    @Override
    public long getItemId(int position) {
        if (position < _HeaderList.length) {
            return RecyclerView.NO_ID;
        } else {
            return _List.get(position - _HeaderList.length).getID();
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean active = _ActiveAdapterPosition == position;
        if (position < _HeaderList.length) {
            switch (_HeaderList[position]) {
                case VIEW_TYPE_DOWNLOAD:
                    // stateless
                    break;
                case VIEW_TYPE_NULL:
                    ((AssetNullViewHolder) holder).onBind(active);
                    break;
            }
        } else {
            ((AssetItemViewMediator) holder).onBind(getItem(position), active);
        }
    }

    public AssetInfo getItem(int adapter_position) {
        int data_pos = adapter_position - _HeaderList.length;
        if (data_pos < 0) {
            return null;
        }
        return _List.get(data_pos);
    }

    private static final int VIEW_TYPE_ASSET = 0;
    private static final int VIEW_TYPE_NULL = 1;
    private static final int VIEW_TYPE_DOWNLOAD = 2;

    private static final int NULL_DATA_POSITION = -1;

    private int _ItemLayoutID = R.layout.item_qupai_editor_asset;

    private int _NullTitle;
    private int _NullImage;

    public void setNullTitle(int title) {
        _NullTitle = title;
    }

    public void set_NullImage(int image) {
        _NullImage = image;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_NULL:
                holder = new AssetNullViewHolder(parent, _NullTitle, _NullImage);
                holder.itemView.setOnClickListener(this);
                break;
            case VIEW_TYPE_ASSET:
                holder = new AssetItemViewMediator(parent, _ItemLayoutID);
                ((AssetItemViewMediator) holder).setTitleVisible(_TitleVisible);
                holder.itemView.setOnClickListener(this);
                break;
            case VIEW_TYPE_DOWNLOAD :
                return  _DownloadItem;
            default:
                return null;
        }

        return holder;
    }

    private List<? extends AssetInfo> _List = Collections.EMPTY_LIST;

    public void setData(List<? extends AssetInfo> list) {
        _List = list;
        notifyDataSetChanged();
    }

    public int findDataPosition(long uid) {

        for (int i = 0, count = _List.size(); i < count; i++) {
            AssetInfo item = _List.get(i);
            if (item.getUID() == uid) {
                return i;
            }
        }

        return NULL_DATA_POSITION;
    }

    public int findAdapterPosition(AssetID asset_id) {
        return findDataPosition(asset_id.getUID()) + _HeaderList.length;
    }

    public int setActiveDataItem(AssetID asset_id) {
        return setActiveDataItem(asset_id == null ? AssetInfo.INVALID_UID : asset_id.getUID());
    }

    public int setActiveDataItem(AssetInfo info) {
        return setActiveDataItem(info == null ? AssetInfo.INVALID_UID : info.getUID());
    }

    public int setActiveDataItem(long uid) {
        int data_pos = findDataPosition(uid);

        int adapter_pos = data_pos + _HeaderList.length;
        setActiveAdapterItem(adapter_pos);
        return adapter_pos;
    }

    private int _ActiveAdapterPosition = 0;

    int getActiveAdapterPosition() {
        return _ActiveAdapterPosition;
    }

    private void setActiveAdapterItem(int adapter_pos) {

        int old_adapter_pos = _ActiveAdapterPosition;
        if (old_adapter_pos == adapter_pos) {
            return;
        }

        _ActiveAdapterPosition = adapter_pos;
        notifyItemChanged(adapter_pos);
        notifyItemChanged(old_adapter_pos);
    }

    @Override
    public void onClick(View v) {

        RecyclerView.ViewHolder holder = (ViewHolder) v.getTag();
        int adapter_pos = holder.getAdapterPosition();

        AssetInfo asset = getItem(adapter_pos);

        if (_OnItemClickListener != null) {
            Log.d("active", "onItemClick");
            if (!_OnItemClickListener.onItemClick(this, adapter_pos)) {
                Log.d("active", "onItemClick1");
                return;
            }
        }

        setActiveAdapterItem(adapter_pos);


        Log.d("active", "adapter_pos:" + adapter_pos + "_HeaderList.length:" + _HeaderList.length);
//       long id =  getItem(adapter_pos).getID();
//       boolean isLocked = getItem(adapter_pos).isLocked();
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
