package com.duanqu.qupaicustomuidemo.editor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupaicustomuidemo.R;

public class EffectListMediator {

    public static
    View getNullItemView(ViewGroup vg, int text_res_id,int img_res_id) {

        View view = FontUtil.applyFontByInflate(
                vg.getContext(), R.layout.item_header_qupai_editor_asset_null, vg, false);

        ImageView img = (ImageView) view.findViewById(R.id.effect_chooser_item_img);
        TextView text = (TextView) view.findViewById(R.id.effect_chooser_item_text);
        text.setText(text_res_id);
        img.setImageResource(img_res_id);
        return view;
    }
}
