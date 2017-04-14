package com.duanqu.qupaicustomuidemo.editor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;

public class LoadingSpecialFontDialog extends DialogFragment {

    TextView tip;

    public static LoadingSpecialFontDialog newInstance(){
        LoadingSpecialFontDialog dialog = new LoadingSpecialFontDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TextDlgStyle);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = FontUtil.applyFontByInflate(getActivity(),
                R.layout.qupai_layout_overlay_font_loading, container, false);
        tip = (TextView)view.findViewById(R.id.tip);

        return view;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 190,
                        getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150,
                        getResources().getDisplayMetrics()));
        super.onResume();
    }

    public void showFontloading() {
        if(tip!= null){
            tip.setText(R.string.qupai_loading_font_waiting);
        }

    }

    public void showFontDecompress() {
        if(tip!= null){
            tip.setText(R.string.qupai_decompress_waiting);
        }
    }

    public void completedLoading() {
        dismiss();
    }

}
