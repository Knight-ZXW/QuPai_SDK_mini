package com.duanqu.qupaicustomuidemo.editor.manager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupaicustomuidemo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/22.
 */
public class OverlayEffectManageFragment extends Fragment implements MultiSelectedToCheck<AssetGroup> {

    private static final String KEY_KIND = "kind";

    private RecyclerView recyclerView;
    private View noDataLayout;
    private TextView noDataTxt;

    private List<AssetGroup> resources;
    private List<AssetGroup> selectList = new ArrayList<>();

    private MyAdapter adapter;
    private boolean isInEditMode;

    private EffectManageActivity getHostActivity(){
        return (EffectManageActivity)getActivity();
    }

    public static OverlayEffectManageFragment newInstance(int pos){
        OverlayEffectManageFragment fragment = new OverlayEffectManageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_KIND, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View managerView = View.inflate(
                getActivity(), R.layout.fragment_effect_manager, null);
        int position = getArguments().getInt(KEY_KIND);
        AssetRepository.Kind kind = getHostActivity().getKind(position);
        List<? extends AssetGroup> list = getHostActivity().getRepository().findDIYCategory();
        resources = new ArrayList<>();
        for(AssetGroup ai : list){
            if(ai.isAvailable() && ai.getResourceFrom() == AssetInfo.RECOMMEND_DOWNLOAD){
                if(kind == AssetRepository.Kind.CAPTION && ai.getType() == AssetInfo.TYPE_CAPTION){
                    resources.add(ai);
                }else if(kind == AssetRepository.Kind.DIY && ai.getType() == AssetInfo.TYPE_DIYOVERLAY){
                    resources.add(ai);
                }

            }
        }

        recyclerView = (RecyclerView) managerView.findViewById(R.id.manager_res_list);
        noDataLayout = managerView.findViewById(R.id.face_music_manager_no_data_layout);
        noDataTxt = (TextView)managerView.findViewById(R.id.face_music_manager_no_data_text);

        adapter = new MyAdapter();
        if(resources.size() == 0){
            recyclerView.setVisibility(View.GONE);
            noDataLayout.setVisibility(View.VISIBLE);
            if(kind == AssetRepository.Kind.CAPTION){
                noDataTxt.setText(R.string.caption_manager_no_data);
            }else{
                noDataTxt.setText(R.string.overlay_manager_no_data);
            }
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            noDataLayout.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }

        return managerView;
    }

    @Override
    public AssetGroup[] getSelectedItems() {
        return selectList.toArray(new AssetGroup[selectList.size()]);
    }

    @Override
    public void selectedAllItems() {
        selectList.clear();
        selectList.addAll(resources);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void unselectedAllItems() {
        selectList.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void selectedItem(AssetGroup item) {
        selectList.add(item);
        adapter.notifyItemChanged(resources.indexOf(item));
    }

    @Override
    public void unselectedItem(AssetGroup item) {
        selectList.remove(item);
        adapter.notifyItemChanged(resources.indexOf(item));
    }

    @Override
    public boolean isItemSelected(AssetGroup item) {
        return selectList.contains(item);
    }

    @Override
    public void toggleMultiCheckMode(boolean start) {
        isInEditMode = start;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeSelectedItem() {
        resources.removeAll(selectList);
        adapter.notifyDataSetChanged();
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(View.inflate(getContext(), R.layout.fragment_overlay_manager_item, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.setData(getItem(position));
        }

        @Override
        public int getItemCount() {
            return resources.size();
        }

        public AssetGroup getItem(int position){
            return resources.get(position);
        }

    }

    private ImageLoader mImageLoader=ImageLoader.getInstance();
    private DisplayImageOptions options= new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .showImageForEmptyUri(R.drawable.video_thumbnails_loading_126)
            .showImageOnFail(R.drawable.video_thumbnails_loading_126)
            .showImageOnLoading(R.drawable.video_thumbnails_loading_126)
            .cacheInMemory(true)
            .cacheOnDisk(true).build();

    class ItemHolder extends RecyclerView.ViewHolder {

        private final LinearLayout resLayout;
        private final ImageView resImage;
        private final CheckBox selectImage;
        private final TextView resName;
        private final TextView resDescription;
        public ItemHolder(View itemView) {
            super(itemView);
            resLayout = (LinearLayout) itemView.findViewById(R.id.manager_res_item_layout);
            resImage = (ImageView) itemView.findViewById(R.id.manager_res_item_image);
            selectImage = (CheckBox) itemView.findViewById(R.id.cb_manager_check);
            resName = (TextView) itemView.findViewById(R.id.manager_res_item_name);
            resDescription = (TextView) itemView.findViewById(R.id.manager_res_item_description);
        }

        public void setData(final AssetGroup data){
            String iconUrl = data.getIconUrl();
            mImageLoader.displayImage(iconUrl, resImage, options);

            resName.setText(data.getName());

//            if(srf.getDescription() != null) {
//                holder.resDescription.setVisibility(View.VISIBLE);
//                holder.resDescription.setText(srf.getDescription());
//            }else {
//                holder.resDescription.setVisibility(View.GONE);
//                holder.resDescription.setText("");
//            }

            if(isInEditMode) {
                selectImage.setVisibility(View.VISIBLE);
            }else {
                selectImage.setVisibility(View.GONE);
            }

            selectImage.setChecked(isItemSelected(data));

            resLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(isInEditMode) {
                        if(!selectImage.isChecked()){
                            selectImage.setChecked(true);
                            selectedItem(data);
                        }else {
                            selectImage.setChecked(false);
                            unselectedItem(data);
                        }
                    }
                }
            });
        }

    }

}
