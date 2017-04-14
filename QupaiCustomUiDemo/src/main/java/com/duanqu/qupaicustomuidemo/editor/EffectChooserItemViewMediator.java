package com.duanqu.qupaicustomuidemo.editor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duanqu.qupai.uil.UILOptions;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.widget.CircleProgressBar;
import com.duanqu.qupai.widget.CircularImageView;
import com.duanqu.qupai.widget.CircularImageViewAware;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.widget.FanProgressBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EffectChooserItemViewMediator extends RecyclerView.ViewHolder {

    protected final TextView _Text;
    protected final CircularImageView _Image;
    protected final ImageView _image_square;
    protected final ImageView _NewIndicator;
    protected final ImageView _LockIndicator;
    protected final ImageButton _Download;
    protected final CircleProgressBar _Bar;
//    protected final FanProgressBar _Bar;


    public EffectChooserItemViewMediator(ViewGroup list_view, int layout_id) {
        this(FontUtil.applyFontByInflate(
                list_view.getContext(), layout_id, list_view, false));
    }

    public EffectChooserItemViewMediator(View view) {
        super(view);

        _Text = (TextView) view.findViewById(R.id.effect_chooser_item_text);
        _Image = (CircularImageView) view.findViewById(R.id.effect_chooser_item_image);
        _image_square =(ImageView)view.findViewById(R.id.effect_chooser_item_image_square);
        _NewIndicator = (ImageView) view.findViewById(R.id.effect_chooser_item_new);
        _LockIndicator = (ImageView) view.findViewById(R.id.effect_chooser_item_lock);
        _Download = (ImageButton) view.findViewById(R.id.effect_chooser_item_download);
        _Bar = (CircleProgressBar) view.findViewById(R.id.effect_chooser_item_progress);

        view.setTag(this);

        _NewIndicator.setVisibility(View.GONE);
//        _Download.setVisibility(View.GONE);
        _Bar.setVisibility(View.GONE);
        _LockIndicator.setVisibility(View.GONE);

    }

    public CircleProgressBar getProgressBar() {
        return _Bar;
    }

    protected View getDownloadButton() {
        return  null;
    }

    public Context getContext() {
        return itemView.getContext();
    }

    public void setFontLocked(boolean isLocked){
//        if(isLocked){
//            _Download.setBackgroundResource(R.drawable.qupai_btn_unlock_asset_normal);
//        }else{
//            _Download.setBackgroundResource(R.drawable.qupai_btn_download_asset_normal);
//        }
    }

    public void setDownloadable(boolean download) {
        _Download.setVisibility(download ? View.VISIBLE : View.INVISIBLE);
        setDownloadVisibility(download);
    }

    public void setDownloadVisibility(boolean enable) {
        _Download.setEnabled(enable);
    }

    public void setDownloadMask(boolean mask) {
    }

    public void setShowFontIndicator(boolean value) {
    }


    public void setShowNewIndicator(boolean value) {
        _NewIndicator.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    public void displayManageLabel(String labelUrl){
        if(TextUtils.isEmpty(labelUrl)){
            return ;
        }
        ImageLoader.getInstance().displayImage(labelUrl, _NewIndicator, UILOptions.LOCAL);
    }

    public void setShowLockIndicator(boolean value){
    	_LockIndicator.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    public void displayLockIndicator(String lockUrl){
        if(TextUtils.isEmpty(lockUrl)){
            return ;
        }
        ImageLoader.getInstance().displayImage(lockUrl, _LockIndicator, UILOptions.LOCAL);
    }

    public void setOnClickListener(OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void setTitle(int title) {
        _Text.setText(title);
    }

    public void setTitle(String title) {
        _Text.setText(title);
    }

    public void setImage(int image_res) {
        if(image_res == -1){
            _Image.setImageDrawable(null);
        }else{
            _Image.setImageResource(image_res);
        }
        if(image_res == -1){
            _image_square.setImageDrawable(null);
        }else{
            _image_square.setImageResource(image_res);
        }

        _URI = null;
    }

    private String _URI;

    public void setImageURI(String uri) {
        if (uri == null) {
            _Image.setImageDrawable(null);
            _image_square.setImageDrawable(null);
        } else if (uri.equals(_URI)) {
            return;
        } else {
            _Image.setImageDrawable(null);
            _image_square.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(uri, new CircularImageViewAware(
                    _Image), UILOptions.DISK);
            ImageLoader.getInstance().displayImage(uri,
                    _image_square, UILOptions.DISK);
        }
        _URI = uri;
    }

    public void setDownloadOnClickListener(OnClickListener listener) {
        _Download.setOnClickListener(listener);
    }

    public void setDownloading(boolean downloading, Context _Context) {
      _Bar.setVisibility(downloading ? View.VISIBLE : View.GONE);
    }

    public View getView() {
        return itemView;
    }

    public View getImage() {
        return _Image;
    }

}
