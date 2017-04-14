package com.duanqu.qupaicustomuidemo.editor.mv;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.duanqu.qupai.effect.asset.AbstractDownloadManager;
import com.duanqu.qupai.effect.asset.ToastUtil;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaisdk.tools.DeviceUtils;

@SuppressLint("SetJavaScriptEnabled")
public class PasterPreviewDialog extends DialogFragment {

    private static final String KEY_URL = "url";
    private static final String KEY_NAME = "name";
    private static final String KEY_ID = "id";
    private static final String KEY_FLAG = "flag";

    private RegistUpdateDialog register;

    public void setUpdateDialogRegister(RegistUpdateDialog register) {
        this.register = register;
    }

    public static PasterPreviewDialog newInstance(String url, String name, int flag, int id){
        PasterPreviewDialog dialog = new PasterPreviewDialog();
        Bundle args=new Bundle();
        args.putString(KEY_URL, url);
        args.putString(KEY_NAME, name);
        args.putInt(KEY_ID, id);
        args.putInt(KEY_FLAG, flag);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onActivityCreated(arg0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.ResourcePreviewStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(true);
        d.setCancelable(true);
        return d;
    }

    TextView lockTxt;
    Button download;
    CircleProgressBar downloadPb;
    AbstractDownloadManager pasterCategoryDownloadManager;

    public void setPasterCategoryDownloadManager(AbstractDownloadManager pasterCategoryDownloadManager) {
        this.pasterCategoryDownloadManager = pasterCategoryDownloadManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = FontUtil.applyFontByInflate(getActivity(),
                R.layout.layout_paster_preview, container, false);

        WebView webView = (WebView) view.findViewById(R.id.webview);
        View close = view.findViewById(R.id.close);
        download = (Button) view.findViewById(R.id.download);
        downloadPb = (CircleProgressBar) view.findViewById(R.id.pb_progress);
        lockTxt = (TextView) view.findViewById(R.id.parser_locked_text);

        webView.setBackgroundColor(getResources().getColor(android.R.color.black));
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(getArguments().getString(KEY_URL));

        int flag = getArguments().getInt(KEY_FLAG);
        registerCallbackListener();
        if(flag == AbstractDownloadManager.DOWNLOAD_NOT){
        }else if(flag == AbstractDownloadManager.DOWNLOAD_UNCOMPLETED){
            download.setBackgroundResource(R.drawable.bg_parser_preview_unlock_rect);
            download.setText(R.string.qupai_download_goon);
        }else if(flag == AbstractDownloadManager.DOWNLOAD_RUNNING){
            if(pasterCategoryDownloadManager.isCategoryDownloading(getArguments().getInt(KEY_ID))){
            }else{
                download.setBackgroundResource(R.drawable.bg_parser_preview_unlock_rect);
                download.setText(R.string.qupai_download_goon);
            }
        }else{
            download.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
            download.setText(R.string.music_used);
        }

        download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int flag = getArguments().getInt(KEY_FLAG);
                if(flag == AbstractDownloadManager.DOWNLOAD_NOT){
                    if(!DeviceUtils.isOnline(v.getContext())){
                        ToastUtil.showToast(getActivity(), R.string.qupai_slow_network_check);
                        return ;
                    }
                    v.setEnabled(false);
                    register.download(getArguments().getInt(KEY_ID));
                }else if(flag == AbstractDownloadManager.DOWNLOAD_UNCOMPLETED){

                    if(!DeviceUtils.isOnline(v.getContext())){
                        ToastUtil.showToast(getActivity(), R.string.qupai_slow_network_check);
                        return ;
                    }
                    v.setEnabled(false);
                    register.download(getArguments().getInt(KEY_ID));
                }else if(flag == AbstractDownloadManager.DOWNLOAD_RUNNING){
                    if(!pasterCategoryDownloadManager.isCategoryDownloading(getArguments().getInt(KEY_ID))) {
                        register.download(getArguments().getInt(KEY_ID));
                    }
                }else{
                    register.useCategoryPaster(getArguments().getInt(KEY_ID));
                    dismiss();
                }
            }
        });

        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setCancelable(true);

        return view;
    }

    private void registerCallbackListener(){
        register.registerDialogListener(getArguments().getInt(KEY_ID),
                new UpdateDialogStatus() {

            @Override
            public void onProgress(int progress) {
                download.setVisibility(View.GONE);
                downloadPb.setVisibility(View.VISIBLE);

                download.setBackgroundResource(R.drawable.bg_parser_preview_unlock_rect);
                downloadPb.setProgress(progress);
            }

            @Override
            public void onFailed() {
                download.setVisibility(View.VISIBLE);
                downloadPb.setVisibility(View.GONE);

                download.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
                download.setText(R.string.qupai_download_immediately);
                download.setEnabled(true);
            }

            @Override
            public void onCompleted(boolean success) {
                download.setVisibility(View.VISIBLE);
                downloadPb.setVisibility(View.GONE);
                download.setEnabled(true);
                if(success){
                    getArguments().putInt(KEY_FLAG, AbstractDownloadManager.DOWNLOAD_COMPLETED);
                    download.setBackgroundResource(R.drawable.bg_parser_preview_used_rect);
                    download.setText(R.string.music_used);
                    register.unregisterDialogListener(getArguments().getInt(KEY_ID));
                }else{
                    getArguments().putInt(KEY_FLAG, AbstractDownloadManager.DOWNLOAD_UNCOMPLETED);
                    download.setBackgroundResource(R.drawable.bg_parser_preview_unlock_rect);
                    download.setText(R.string.qupai_download_goon);
                }
            }

        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        register.unregisterDialogListener(getArguments().getInt(KEY_ID));
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        super.onResume();
    }

}
