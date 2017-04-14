package com.duanqu.qupaicustomuidemo.editor;

import android.content.UriMatcher;
import android.net.Uri;

import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.project.UIEditorPage;
import com.duanqu.qupai.utils.UriUtil;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public final class EditorAction {

    static final String AUTHORITY = "com.duanqu.qupai";
    static final String SCHEME = "action";

    static final String ROOT = SCHEME + "://" + AUTHORITY;

    static final UriMatcher ACTION_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static final String ACTION_NAME_USE_MUSIC = "use-music";
    static final String ACTION_NAME_USE_MV = "use-mv";
    static final String ACTION_NAME_USE_DIY = "use-diy";
    static final String ACTION_NAME_USE_FONT = "use-font";
    static final String ACTION_NAME_DOWNLOAD_DIY = "download-to-diy";
    static final String ACTION_NAME_SCROLL_TO_MUSIC = "scroll-to-music";
    static final String ACTION_NAME_SCROLL_TO_MV = "scroll-to-mv";
    static final String ACTION_NAME_SCROLL_TO_DIY_OVERLAY_GROUP = "scroll-to-diy-overlay-group";
    static final String ACTION_NAME_SWITCH_PAGE = "switch-page";
    static final String ACTION_NAME_START_DUBBING = "start-dubbing";
    static final String ACTION_NAME_USE_ASSET = "use-asset";

    static final int ACTION_USE_MUSIC = 0;
    static final int ACTION_USE_MV = 1;
    static final int ACTION_SCROLL_TO_MUSIC = 2;
    static final int ACTION_SCROLL_TO_MV = 3;
    static final int ACTION_USE_DIY = 4;
    static final int ACTION_USE_FONT = 10;
    static final int ACTION_DOWNLOAD_DIY = 5;
    static final int ACTION_SCROLL_TO_DIY_OVERLAY_GROUP = 6;
    static final int ACTION_SWITCH_PAGE = 7;
    static final int ACTION_START_DUBBING = 8;
    static final int ACTION_USE_ASSET = 9;

    static {
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_USE_MUSIC, ACTION_USE_MUSIC);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_USE_MV, ACTION_USE_MV);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_SCROLL_TO_MUSIC, ACTION_SCROLL_TO_MUSIC);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_SCROLL_TO_MV, ACTION_SCROLL_TO_MV);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_USE_DIY, ACTION_USE_DIY);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_USE_FONT, ACTION_USE_FONT);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_DOWNLOAD_DIY, ACTION_DOWNLOAD_DIY);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_SCROLL_TO_DIY_OVERLAY_GROUP, ACTION_SCROLL_TO_DIY_OVERLAY_GROUP);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_SWITCH_PAGE, ACTION_SWITCH_PAGE);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_START_DUBBING, ACTION_START_DUBBING);
        ACTION_MATCHER.addURI(AUTHORITY, ACTION_NAME_USE_ASSET, ACTION_USE_ASSET);
    }

    public static Uri useAsset(AssetID asset_id, boolean confirmed) {
        int id = asset_id.id;
        int type = asset_id.type;

        return Uri.parse(ROOT + "/" + ACTION_NAME_USE_ASSET + "?id=" + id + "&type=" + type + "&confirmed=" + confirmed);
    }

    public static Uri useDIY(long id){
        return Uri.parse(ROOT + "/" + ACTION_NAME_USE_DIY + "?id=" + id);
    }

    public static Uri useFont(long id){
        return Uri.parse(ROOT + "/" + ACTION_NAME_USE_FONT + "?id=" + id);
    }

    public static String useMVString(long id) { return ROOT + "/" + ACTION_NAME_USE_MV + "?id=" + id; }

    public static Uri useMV(long id) { return Uri.parse(useMVString(id)); }

    public static Uri useMusic(long id) {
        return Uri.parse(ROOT + "/" + ACTION_NAME_USE_MUSIC + "?id=" + id);
    }

    public static String scrollToMVString(long id) { return ROOT + "/" + ACTION_NAME_SCROLL_TO_MV + "?id=" + id; }

    public static Uri scrollToMV(long id) { return Uri.parse(scrollToMVString(id)); }

    public static Uri scrollToMusic(long id) {
        return Uri.parse(ROOT + "/" + ACTION_NAME_SCROLL_TO_MUSIC + "?id=" + id);
    }

    public static Uri downloadToDIY(long id) {
        return Uri.parse(ROOT + "/" + ACTION_NAME_DOWNLOAD_DIY + "?id=" + id);
    }

    public static String scrollToDIYOverlayGroupString(int group_id) {
        return ROOT + "/" + ACTION_NAME_SCROLL_TO_DIY_OVERLAY_GROUP + "?id=" + group_id;
    }

    public static String switchPageString(@Nonnull UIEditorPage page) {
        return ROOT + "/" + ACTION_NAME_SWITCH_PAGE + "?id=" + page;
    }

    @Nonnull
    public static UIEditorPage getSwitchPageID(@Nonnull Uri uri) {
        String id = uri.getQueryParameter("id");
        if (id == null) {
            return null;
        }
        return UIEditorPage.valueOf(id);
    }

    public static Uri startDubbing(boolean confirmed) {
        return Uri.parse(ROOT + "/" + ACTION_NAME_START_DUBBING + "?confirmed=" + confirmed);
    }

    public static boolean getConfirmed(Uri uri) {
        String value = uri.getQueryParameter("confirmed");
        return Boolean.parseBoolean(value);
    }


    private final Executor _Executor;

    public EditorAction(Executor exec) {
        _Executor = exec;
    }

    private final ArrayList<Uri> _PendingActionList = new ArrayList<>();

    public void executeAction(Uri uri) {

        if (!_Resumed) {
            _PendingActionList.add(uri);
            return;
        }

        dispatchAction(uri);
    }

    public void executeAction(@Nonnull String[] action_list) {
        for (String action : action_list) {
            Uri uri = Uri.parse(action);
            if (uri == null) {
                continue;
            }
            executeAction(uri);
        }
    }

    private void dispatchScrollTo(int type, int id) {
        _Executor.doScrollTo(new AssetID(type, id));
    }

    private void dispatchAction(Uri uri) {

        switch (EditorAction.ACTION_MATCHER.match(uri)) {
        case EditorAction.ACTION_USE_MV: {
            int id = UriUtil.getQueryI(uri, "id", -1);
            dispatchScrollTo(AssetInfo.TYPE_SHADER_MV, id);
            _Executor.doUseAsset(AssetInfo.TYPE_SHADER_MV, id, false);
        }   break;
        case EditorAction.ACTION_USE_MUSIC: {
            int id = UriUtil.getQueryI(uri, "id", -1);
            dispatchScrollTo(AssetInfo.TYPE_MUSIC, id);
            _Executor.doUseAsset(AssetInfo.TYPE_MUSIC, id, false);
        }   break;
        case EditorAction.ACTION_SCROLL_TO_MUSIC:
            dispatchScrollTo(AssetInfo.TYPE_MUSIC, UriUtil.getQueryI(uri, "id", -1));
            break;
        case EditorAction.ACTION_SCROLL_TO_MV:
            dispatchScrollTo(AssetInfo.TYPE_SHADER_MV, UriUtil.getQueryI(uri, "id", -1));
            break;
        case EditorAction.ACTION_USE_DIY:
            _Executor.doUseAsset(AssetInfo.TYPE_DIYOVERLAY, UriUtil.getQueryI(uri, "id", -1), false);
            break;
        case EditorAction.ACTION_USE_FONT:
            _Executor.doUseAsset(AssetInfo.TYPE_FONT, UriUtil.getQueryI(uri, "id", -1), false);
            break;
        case EditorAction.ACTION_DOWNLOAD_DIY:
            _Executor.doDownload(AssetInfo.TYPE_DIYOVERLAY, UriUtil.getQueryI(uri, "id", -1));
            break;
        case EditorAction.ACTION_SCROLL_TO_DIY_OVERLAY_GROUP:
            _Executor.doScrollToGroup(AssetInfo.TYPE_DIYOVERLAY, UriUtil.getQueryI(uri, "id", -1));
            break;
        case EditorAction.ACTION_SWITCH_PAGE: {
            UIEditorPage page_id = EditorAction.getSwitchPageID(uri);
            _Executor.doSwitchPage(page_id);
        }   break;
        case EditorAction.ACTION_START_DUBBING:
            _Executor.doStartDubbing(EditorAction.getConfirmed(uri));
            break;
        case EditorAction.ACTION_USE_ASSET: {
            boolean confirmed = EditorAction.getConfirmed(uri);
            int id = UriUtil.getQueryI(uri, "id", 0);
            int type = UriUtil.getQueryI(uri, "type", 0);
            _Executor.doUseAsset(type, id, confirmed);
        }   break;
        default:
            break;
        }
    }

    private boolean _Resumed = false;

    public void onResume() {
        _Resumed = true;

        for (Uri uri : _PendingActionList) {
            dispatchAction(uri);
        }
        _PendingActionList.clear();

    }

    public void onPause() {
        _Resumed = false;
    }

    interface Executor {

        void doUseAsset(int type, int id, boolean confirmed);

        void doScrollTo(AssetID asset_id);

        void doDownload(int type, int id);

        void doScrollToGroup(int type, int id);

        void doSwitchPage(@Nonnull UIEditorPage page);

        void doStartDubbing(boolean confirmed);
    }

}
