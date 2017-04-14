package com.duanqu.qupaicustomuidemo.editor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import com.duanqu.qupai.asset.AssetBundle;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.FontResolver;
import com.duanqu.qupai.dialog.AlertDialogFragment;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.ResourceItem;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.editor.api.Api;
import com.duanqu.qupaicustomuidemo.editor.mv.CaptionForm;
import com.duanqu.qupaicustomuidemo.editor.mv.OverlayForm;
import com.duanqu.qupaiokhttp.HttpRequest;
import com.duanqu.qupaiokhttp.StringHttpRequestCallback;
import com.duanqu.qupaisdk.tools.SingnatureUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class OverlaySelectDialog extends DialogFragment {

    private AssetRepository repository;
    private EffectService effectService;
    private AssetInfo currentUseEffect;

    private final static String KEY_CATEGORY_ID = "group_id";

    public static OverlaySelectDialog newInstance(int categoryId) {
        OverlaySelectDialog dialog = new OverlaySelectDialog();
        Bundle args = new Bundle();
        args.putInt(KEY_CATEGORY_ID, categoryId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setRepository(AssetRepository repository) {
        this.repository = repository;
    }

    public void setEffectService(EffectService service){
        this.effectService = service;
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
                getActivity(), R.layout.qupai_overlay_select_list, null);
        RecyclerView overlayList = (RecyclerView) contentview.findViewById(R.id.overlay_list);

        List<? extends AssetInfo> list = repository.findDIYCategoryContent(getArguments().getInt(KEY_CATEGORY_ID));
        GridLayoutManager lm = new GridLayoutManager(getActivity(), 3);
        overlayList.setLayoutManager(lm);

        AssetListAdapter adapter = new AssetListAdapter(null,
                R.layout.item_qupai_editor_asset_overlay_long, false);
        adapter.setData(list);
        adapter.setOnItemClickListener(new AssetListAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(AssetListAdapter adapter, int adapter_position) {
                AssetInfo item = adapter.getItem(adapter_position);
                int id = item.getFontType();
                FontResolver fontResolver = repository.getFontResolver();
                if(!fontResolver.isFontExit(id)){
                    AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                            R.string.need_download_special_font,
                            R.string.qupai_download_immediately,
                            R.string.qupai_download_cancel);
                    dialog.setTargetFragment(OverlaySelectDialog.this, 7);
                    dialog.show(getFragmentManager(), "special_font");
                    currentUseEffect = item;
                }else{
                    int ret = effectService.useEffect(item);
                    if(ret == EffectService.EFFECTNOTPAY){
                        AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                R.string.qupai_license_needbug,
                                0,
                                R.string.qupai_dlg_button_confirm);
                        dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                        dialog.show(getFragmentManager(), "special_font");
                    }else
                        dismiss();

                }
                return true;
            }
        });
        overlayList.setAdapter(adapter);

        return contentview;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 9){
            dismiss();
        }else if(requestCode == 7){
            if(resultCode == DialogInterface.BUTTON_NEGATIVE){
               int ret = effectService.useEffect(currentUseEffect);
               if(ret == EffectService.EFFECTNOTPAY) {
                   AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                           R.string.qupai_license_needbug,
                           0,
                           R.string.qupai_dlg_button_confirm);
                   dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                   dialog.show(getFragmentManager(), "special_font");
               }else {
                   dismiss();
               }
            }else if(resultCode == DialogInterface.BUTTON_POSITIVE){
                AssetInfo ai = repository.find(AssetRepository.Kind.FONT, currentUseEffect.getFontType());
                showLoadSpecialFontDialog();
                if(ai != null){
                    downloadSpecialFontResource(ai);
                }else{
                    String api = Api.getInstance().getApiUrl(Api.FONT_SIGNAL_RESOURCE_CATEGORY) + "/" + currentUseEffect.getFontType();
                    HttpRequest.get(api + "?packageName=" + getActivity().getPackageName()
                            + "&signature=" + SingnatureUtils.getSingInfo(getActivity()),
                            new StringHttpRequestCallback(){
                        @Override
                        protected void onSuccess(String s) {
                            super.onSuccess(s);
                            CaptionForm font;
                            try{
                                font = new JSONSupportImpl().readValue(s, CaptionForm.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                font = null;
                            }

                            if(font != null){
                                final CaptionForm f = font;
                                downloadSpecialFontResource(new AssetInfo() {
                                    @Override
                                    public long getUID() {
                                        return f.id;
                                    }

                                    @Override
                                    public long getID() {
                                        return f.id;
                                    }

                                    @Override
                                    public int getType() {
                                        return TYPE_FONT;
                                    }

                                    @Override
                                    public String getTitle() {
                                        return f.name;
                                    }

                                    @Override
                                    public int getVersion() {
                                        return 0;
                                    }

                                    @Override
                                    public String getIconURIString() {
                                        return f.icon;
                                    }

                                    @Override
                                    public String getContentURIString() {
                                        return null;
                                    }

                                    @Override
                                    public AssetBundle getContent() {
                                        return null;
                                    }

                                    @Override
                                    public boolean isAvailable() {
                                        return false;
                                    }

                                    @Override
                                    public int getResourceStatus() {
                                        return 0;
                                    }

                                    @Override
                                    public String getResourceUrl() {
                                        return f.url;
                                    }

                                    @Override
                                    public int getFlags() {
                                        return 0;
                                    }

                                    @Override
                                    public String getBannerURIString() {
                                        return f.banner;
                                    }

                                    @Override
                                    public int getFontType() {
                                        return f.category;
                                    }

                                    @Override
                                    public int getResourceFrom() {
                                        return RECOMMEND_DOWNLOAD;
                                    }
                                });
                            }else{
                                loadingSpecialFontDialog.completedLoading();
                                int ret = effectService.useEffect(currentUseEffect);
                                if(ret == EffectService.EFFECTNOTPAY) {
                                    AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                            R.string.qupai_license_needbug,
                                            0,
                                            R.string.qupai_dlg_button_confirm);
                                    dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                                    dialog.show(getFragmentManager(), "special_font");
                                }else {
                                    dismiss();
                                }
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, String msg) {
                            super.onFailure(errorCode, msg);
                            loadingSpecialFontDialog.completedLoading();
                            int ret = effectService.useEffect(currentUseEffect);
                            if(ret == EffectService.EFFECTNOTPAY) {
                                AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                        R.string.qupai_license_needbug,
                                        0,
                                        R.string.qupai_dlg_button_confirm);
                                dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                                dialog.show(getFragmentManager(), "special_font");
                            }else {
                                dismiss();
                            }
                        }
                    });
                }

            }
        }
    }

    private void showLoadSpecialFontDialog(){
        LoadingSpecialFontDialog loading  = LoadingSpecialFontDialog.newInstance();
        loading.show(getFragmentManager(), "loading");
        loadingSpecialFontDialog = loading;
    }

    LoadingSpecialFontDialog loadingSpecialFontDialog;
    private void downloadSpecialFontResource(AssetInfo font){
        ResourceItem o = new ResourceItem();
        o.setName(font.getTitle());
        o.setResourceType(AssetInfo.TYPE_FONT);
        o.setId(font.getID());
        o.setBannerUrl(font.getBannerURIString());
        o.setIconUrl(font.getIconURIString());
        o.setFontType(font.getFontType());
        o.setResourceUrl(font.getResourceUrl());

        AbstractDownloadManager downloadManager = (AbstractDownloadManager) repository.getDownloadManager();
        downloadManager.downloadResourcesItem(getActivity(), o, null,
                downloadListener, null, decompressListener);
    }

    private final AbstractDownloadManager.ResourceDownloadListener downloadListener =
            new AbstractDownloadManager.ResourceDownloadListener() {
                @Override
                public void onDownloadStart(ResourceItem id) {
                    loadingSpecialFontDialog.showFontloading();
                }

                @Override
                public void onDownloadFailed(ResourceItem id) {
                    loadingSpecialFontDialog.completedLoading();
                    int ret = effectService.useEffect(currentUseEffect);
                    if(ret == EffectService.EFFECTNOTPAY) {
                        AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                R.string.qupai_license_needbug,
                                0,
                                R.string.qupai_dlg_button_confirm);
                        dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                        dialog.show(getFragmentManager(), "special_font");
                    }else {
                        dismiss();
                    }
                }

                @Override
                public void onDownloadCompleted(ResourceItem id) {
                    loadingSpecialFontDialog.completedLoading();
                    int ret = effectService.useEffect(currentUseEffect);
                    if(ret == EffectService.EFFECTNOTPAY) {
                        AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                                R.string.qupai_license_needbug,
                                0,
                                R.string.qupai_dlg_button_confirm);
                        dialog.setTargetFragment(OverlaySelectDialog.this, 9);
                        dialog.show(getFragmentManager(), "special_font");
                    }else {
                        dismiss();
                    }
                }
            };

    private final AbstractDownloadManager.ResourceDecompressListener decompressListener =
            new AbstractDownloadManager.ResourceDecompressListener() {
                @Override
                public void onResourceDecompressStart(long id) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingSpecialFontDialog.showFontDecompress();
                        }
                    });
                }

                @Override
                public void onResourceDecompressFailed(long id) {

                }

                @Override
                public void onResourceDecompressCompleted(long id) {

                }
            };

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

}
