package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import android.util.Log;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.engine.session.PageNavigator;

/**
 * Created by qupai on 16-5-4.
 */
public class EditorSession {
    private static final String TAG = "Editor";
    private final PageNavigator _Navigator;
    private AssetRepository mAssetRepository;
    private Activity _Activity;
    private int currentActiveIndex = -1;

    public EditorSession(Activity activity, AssetRepository assetRepository, PageNavigator nav, int tabs) {
        this._Activity = activity;
        this.mAssetRepository = assetRepository;
        this._Navigator = nav;

        _ParticipantList = new EditParticipant[tabs];
    }

    public void onStart() {
        for (EditParticipant part : _ParticipantList) {
            if (part != null) {
                part.onStart();
            }
        }
    }

    public void onResume() {
        for (EditParticipant part : _ParticipantList) {
            if (part != null) {
                part.onResume();
            }
        }
    }

    public void onPause() {
        for (EditParticipant part : _ParticipantList) {
            if (part != null) {
                part.onPause();
            }
        }
    }

    public void onStop() {

        for (EditParticipant part : _ParticipantList) {
            if (part != null) {
                part.onStop();
            }
        }
    }

    private static int getDownloadPageID(AssetRepository.Kind kind) {
        switch (kind) {
            case FONT:
                return PageNavigator.PAGE_DOWNLOAD_FONT;
            case DIY:
                return PageNavigator.PAGE_DOWNLOAD_PASTER;
            case SOUND:
                return PageNavigator.PAGE_DOWNLOAD_MUSIC;
            case MV:
                return PageNavigator.PAGE_DOWNLOAD_MV;
            case CAPTION:
                return PageNavigator.PAGE_DOWNLOAD_CAPTION;
            default:
                Log.e(TAG, "downlod page not available: " + kind);
                return 0;
        }
    }

    boolean hasDownloadPage(AssetRepository.Kind kind) {
        return _Navigator != null && _Navigator.hasPage(getDownloadPageID(kind));
    }

    void openDownloadPage(AssetRepository.Kind kind,int  rotation) {

        if (_Navigator == null) {
            Log.e(TAG, "PageNavigator is not available");
            return;
        }

        int page = getDownloadPageID(kind);

        _Navigator.openPage(page, rotation , _Activity, EditorActivity.REQUEST_CODE_PICK_MUSIC);

    }

    private final EditParticipant[] _ParticipantList;

    public EditParticipant getPart(int index) {
        return _ParticipantList[index];
    }

    public void setPart(int index, EditParticipant part) {
        _ParticipantList[index] = part;
        part.setActive(false);
    }

    public EditParticipant getCurentActivePart(){
        return getPart(getCurrentActiveIndex());
    }

    public void setCurrentActiveIndex(int currentActiveIndex) {
        this.currentActiveIndex = currentActiveIndex;
    }

    public int getCurrentActiveIndex() {
        return currentActiveIndex;
    }
}
