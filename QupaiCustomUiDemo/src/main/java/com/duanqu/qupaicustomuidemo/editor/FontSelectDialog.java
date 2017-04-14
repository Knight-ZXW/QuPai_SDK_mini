package com.duanqu.qupaicustomuidemo.editor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.dialog.AlertDialogFragment;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.uil.UILOptions;
import com.duanqu.qupaicustomuidemo.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class FontSelectDialog extends DialogFragment {

    private AssetRepository repository;
    private EffectService effectService;
    private EditorSession session;
    private GridView fontList;

    public static FontSelectDialog newInstance() {
        return new FontSelectDialog();
    }

    public void setRepository(AssetRepository repository) {
        this.repository = repository;
    }

    public void setEffectService(EffectService service){
        this.effectService = service;
    }

    public void setSession(EditorSession session) {
        this.session = session;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TextDlgStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentview = View.inflate(
                getActivity(), R.layout.qupai_font_select_list, null);
        fontList = (GridView) contentview.findViewById(R.id.font_list);

        List<AssetInfo> fonts = new ArrayList<>();
        for(AssetInfo font : repository.find(AssetRepository.Kind.FONT)){
            if(font.isAvailable()){
                fonts.add(font);
            }
        }
        fonts.add(null);
        FontAdapter fontAdapter = new FontAdapter();
        fontAdapter.setData(fonts);
        fontList.setAdapter(fontAdapter);
        fontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetInfo item = (AssetInfo)parent.getItemAtPosition(position);
                if(item == null){
                    session.openDownloadPage(AssetRepository.Kind.FONT, 0);
                }else{
                    int ret = effectService.useEffect(item);
                    if(ret == EffectService.EFFECTNOTPAY){
                        AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                R.string.qupai_license_needbug,
                                0,
                                R.string.qupai_dlg_button_confirm);
                        dialog.setTargetFragment(FontSelectDialog.this, 6);
                        dialog.show(getFragmentManager(), "font");
                    }else
                        dismiss();
                }

                dismiss();
            }
        });

        return contentview;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams localLayoutParams = getDialog().getWindow()
                .getAttributes();
        localLayoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class FontAdapter extends BaseAdapter {

        private List<AssetInfo> list = new ArrayList<>();

        public void setData(List<? extends AssetInfo> data){
            if(data == null || data.size() == 0){
                return;
            }
            list.addAll(data);
            notifyDataSetChanged();
        }

        public List<AssetInfo> getData(){
            return list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public AssetInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FontItemViewMediator localViewHolder;
            if (convertView == null) {
                localViewHolder = new FontItemViewMediator(parent);
                convertView = localViewHolder.getView();
                //Log.d("share_menu", "分享菜单的position：" + paramInt);
            } else {
                localViewHolder = (FontItemViewMediator) convertView.getTag();
            }
            final AssetInfo item = getItem(position);

            localViewHolder.setData(item);

            if(fontList.getCheckedItemPosition() == position){
                localViewHolder.setSelected(true);
            }else{
                localViewHolder.setSelected(false);
            }

            return convertView;
        }

    }

    class FontItemViewMediator {
        private ImageView image;
        private View select;
        private ImageView indiator;
        private TextView name;
        private View root;

        public FontItemViewMediator(ViewGroup parent){
            root = View.inflate(parent.getContext(), R.layout.item_qupai_font_effect, null);
            select = root.findViewById(R.id.selected);
            image = (ImageView)root.findViewById(R.id.font_item_image);
            indiator = (ImageView)root.findViewById(R.id.indiator);
            name = (TextView)root.findViewById(R.id.item_name);
            root.setTag(this);
        }

        public void setData(AssetInfo font){
            if(font != null){
                name.setText(font.getTitle());

                ImageLoader.getInstance().displayImage(font.getBannerURIString(), image, UILOptions.DISK);
            }else{
                image.setImageResource(R.drawable.qupai_font_more_banner);
            }

        }

        public void setSelected(boolean selected){
            select.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public View getView(){
            return root;
        }

    }

}
