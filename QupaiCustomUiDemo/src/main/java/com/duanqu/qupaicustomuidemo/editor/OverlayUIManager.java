package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import android.text.TextUtils;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.effect.*;
import com.duanqu.qupaicustomuidemo.R;

/**
 * Created by Administrator on 2016/12/29.
 */
public class OverlayUIManager extends OverlayManageImpl {

    private Activity activity;
    private AssetRepository repository;
    private EffectService overlayEffectService;
    protected OverlayUIController editController;
    private boolean isShowTextEdit;
    private boolean closeOverlay;
    private TextDialog textDialog;
    OverlayUIManager(Activity activity, AssetRepository repository,
                               EffectService overlayEffectService, Player player) {
        super(player);
        this.activity = activity;
        this.repository = repository;
        this.overlayEffectService = overlayEffectService;
    }

    @Override
    public void onOverlayAdded(OverlayUIController controller) {
        if(isShowTextEdit){
            if(editController == null){
                editController = controller;
                textDialog.setOverlayController(controller);
            }
        }else{
            super.onOverlayAdded(controller);
            if(controller.isTextOnly() && TextUtils.isEmpty(controller.getText())){
                onOverlayTextEditing(controller);
            }
        }

    }

    @Override
    public void onOverlayRemoved(OverlayUIController controller) {
        if(editController != null && controller == editController){
            if(textDialog != null){
                editController = null;
                closeOverlay = true;
                textDialog.dismiss();
            }
        }else{
            super.onOverlayRemoved(controller);
        }


    }

    @Override
    public void onOverlayTextEditing(OverlayUIController controller) {
        super.onOverlayTextEditing(controller);
        controller.setVisibility(false);
        isShowTextEdit = true;
        textDialog = TextDialog.newInstance();
        textDialog.setAssetRepository(repository);
        textDialog.setFontManager(repository.getFontResolver());
        int layout;
        if(controller.isTextOnly()){
            layout = R.layout.qupai_edittext_only;
        }else{
            layout = R.layout.qupai_edittext_overlay;
        }
        OverlayUIConfig.OverlayUIConfigBuilder builder = new OverlayUIConfig.OverlayUIConfigBuilder();
        builder.setOverlayId(controller.getOverlayId())
                .setOverlayResourceUri(controller.getOverlayUri())
                .setTextOnly(controller.isTextOnly())
                .setOverlayFont(controller.getFontId())
                .setOverlayTextColor(controller.getTextColor())
                .setOverlayTextStrokeColor(controller.getTextStrokeColor())
                .setOverlayLayout(layout)
                .setOverlayText(controller.getText().toString())
                .setAttachToUser(true);
        overlayEffectService.useEffect(builder.get());

        textDialog.setOnStateChangeListener(listener);
        textDialog.show(activity.getFragmentManager(), "dialog");
    }

    private final TextDialog.OnStateChangeListener listener = new TextDialog.OnStateChangeListener() {
        @Override
        public void onSendButtonClick(String text, int textColor, int textStroke, long fontId) {
            OverlayUIController currentEdit = getCurrentEditOverlay();
            if (currentEdit != null) {
                currentEdit.setVisibility(true);
                if (TextUtils.isEmpty(text) && currentEdit.isTextOnly()) {
                    currentEdit.removeOverlay();
                } else {
                    currentEdit.setText(text);
                    currentEdit.setTextColor(textColor);
                    currentEdit.setTextStrokeColor(textStroke);
                    currentEdit.setFontId(fontId);
                }
            }
        }

        @Override
        public void onDismiss(String text, int textColor, int textStroke, long fontId) {
            isShowTextEdit = false;
            textDialog = null;
            if(editController != null){
                editController.removeOverlay();
                editController = null;
            }

            onSendButtonClick(text, textColor, textStroke, fontId);

            OverlayUIController currentEdit = getCurrentEditOverlay();
            if (currentEdit != null) {
                if(closeOverlay){
                    currentEdit.removeOverlay();
                    closeOverlay = false;
                }else{
                    currentEdit.getEditOverlayView().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    OverlayUIController currentEdit = getCurrentEditOverlay();
                                    if (currentEdit != null) {
                                        currentEdit.setTextEditCompleted(true);
                                    }
                                }
                            }, 500);
                }
                onOverlayTimeEditing(false);
            }
        }
    };

}
