package com.duanqu.qupaicustomuidemo.editor;

import android.view.View;
import com.duanqu.qupai.asset.AssetID;

abstract class EditParticipant {

    public void onStart() { }

    public void onResume() { }

    public void onPause() { }

    public void onStop() { }

    public void setActive(boolean value) { }

    public void scrollTo(AssetID asset_id) { }

    public boolean requestExport() { return true; }

    public void onSave() { }

    public void onClick(View root) { }

}
