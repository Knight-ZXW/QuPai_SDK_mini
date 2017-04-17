package com.duanqu.qupaicustomuidemo.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.duanqu.qupai.android.widget.AspectRatioLayout;
import com.duanqu.qupai.asset.AssetID;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.asset.AssetRepositoryClient;
import com.duanqu.qupai.effect.EditorService;
import com.duanqu.qupai.effect.EditorServiceFactory;
import com.duanqu.qupai.effect.EditorUIConfig;
import com.duanqu.qupai.effect.EffectService;
import com.duanqu.qupai.effect.OnRenderChangeListener;
import com.duanqu.qupai.effect.Player;
import com.duanqu.qupai.effect.RenderEditService;
import com.duanqu.qupai.effect.VideoTimelineEditService;
import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.engine.session.VideoSessionClient;
import com.duanqu.qupai.project.UIEditorPage;
import com.duanqu.qupai.project.UIEditorPageProxy;
import com.duanqu.qupai.project.UIMode;
import com.duanqu.qupai.widget.control.TabGroup;
import com.duanqu.qupai.widget.control.TabbedViewStackBinding;
import com.duanqu.qupai.widget.control.ViewStack;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.engine.session.RenderRequest;
import com.duanqu.qupaicustomuidemo.render.RenderProgressActivity;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nonnull;

public class EditorActivity extends Activity implements View.OnClickListener, OnRenderChangeListener {

    public static final class Request extends SessionPageRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

        public Request(SessionClientFactory factory, Serializable data) {
            super(factory, data);
        }

        @Override
        public SessionPageRequest setFactory(SessionClientFactory factory) {
            return super.setFactory(factory);
        }

        private transient Uri _ProjectUri;

        public Request setProjectUri(Uri uri) {
            _ProjectUri = uri;
            return this;
        }

        @Override
        protected void marshall(Intent intent) {
            super.marshall(intent);

            intent.setData(_ProjectUri);
        }

        @Override
        protected void unmarshall(Intent intent) {
            super.unmarshall(intent);

            _ProjectUri = intent.getData();
        }
    }

    private static final String TAG = "VideoEditor";

    private EditorService editorService;
    private SurfaceView _DisplaySurface;
    private Player _Player;
    private EditorSession _EditorSession;
    private ImageView mBtnNext;
    private ImageView mBtnBack;
    private TabGroup tab_group;
    private TimelineBar timelineBar;

    private Request _Request;
    private VideoSessionClient _SessionClient;

    private AssetRepository dataProvider;

    AssetRepositoryClient _RepoClient;

    private EditorAction _ActionParser;
    private boolean isResume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _Request = PageRequest.from(this);

        _ActionParser = new EditorAction(_ActionExecutor);

        //创建一个资源仓库，提供滤镜和音乐资源
        _SessionClient = _Request.getVideoSessionClient(this);
        dataProvider = _SessionClient.getAssetRepository();
        _RepoClient= new AssetRepositoryClient(dataProvider);
        setContentView(R.layout.activity_editor);

        _EditorSession = new EditorSession(this, dataProvider, _SessionClient.getPageNavigator(), 5);

        FrameLayout timeContainer = (FrameLayout)findViewById(R.id.dynamic_timeline_layout);
        FrameLayout overlayContainer = (FrameLayout)findViewById(R.id.dynamic_overlay_layout);

        EditorUIConfig.EditorUIConfigBuilder builder = new EditorUIConfig.EditorUIConfigBuilder();
        builder.setActivity(this)
                .setAssetRepository(dataProvider)
                .setFontResolver(dataProvider.getFontResolver())
                .setOverlayLayout(overlayContainer)
                .setDurationOneshot(8000)
                .setShowTextLabel(false);

        EditorUIConfig config = builder.build();

        String projectFilePath = _Request._ProjectUri.getPath();
        editorService = EditorServiceFactory.getEditService(this, _SessionClient, config, projectFilePath);
        _Player = editorService.getPlayer();
        RenderEditService renderEditService = editorService.getRenderEditService();

        mBtnNext = (ImageView) findViewById(R.id.btn_next);
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        _DisplaySurface = (SurfaceView) findViewById(R.id.surface_view);

        //预览播放器显示模式
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) _DisplaySurface.getLayoutParams();
        VideoScaleHelper helper = new VideoScaleHelper();
        helper.setVideoWidthAndHeight(renderEditService.getRenderWidth(),
                renderEditService.getRenderHeight())
                .setScreenWidthAndHeight(screenWidth, screenWidth)
                .generateDisplayLayoutParams(lp);

        renderEditService.setDisplayWidth(lp.width);
        renderEditService.setDisplayHeight(lp.height);

        SurfaceHolder holder = _DisplaySurface.getHolder();
        _Player.setSurfaceHolder(holder);

        mBtnNext.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        AspectRatioLayout video_frame = (AspectRatioLayout) findViewById(R.id.video);
        video_frame.setOriginalSize(480, 480);

        ViewStack view_stack = new ViewStack(View.INVISIBLE);

        final View preview = findViewById(R.id.preview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_Player.isVideoPlayStopped()){
                    _Player.start();
                }else{
                    _Player.stop();
                }
            }
        });

        //静音功能
        findViewById(R.id.silence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _Player.setAudioSilence(!_Player.isAudioSilence());
            }
        });

        _Player.setOnPlayStateChangeListener(new Player.OnPlayStateChangeListener() {
            @Override
            public void onPlayStateChanged(boolean paused) {
                preview.setActivated(!paused);
            }
        });

        tab_group = new TabGroup();
        tab_group.addView(findViewById(R.id.tab_effect_filter));
        tab_group.addView(findViewById(R.id.tab_effect_caption));
        tab_group.addView(findViewById(R.id.tab_effect_diy_animation));
        tab_group.addView(findViewById(R.id.tab_effect_audio_mix));
        tab_group.addView(findViewById(R.id.tab_effect_mv));

        RecyclerView filter_list_view = (RecyclerView) findViewById(R.id.effect_list_filter);
        RecyclerView effect_list_audio_mix = (RecyclerView) findViewById(R.id.effect_list_audio_mix);
        RecyclerView effect_list_view_mv = (RecyclerView) findViewById(R.id.effect_list_mv);
        RecyclerView effect_list_view_caption = (RecyclerView) findViewById(R.id.effect_list_caption);
        RecyclerView effect_list_view_overlay = (RecyclerView) findViewById(R.id.effect_list_overlay);

        view_stack.addView(filter_list_view);
        view_stack.addView(effect_list_view_caption);
        view_stack.addView(effect_list_view_overlay);
        view_stack.addView(effect_list_audio_mix);
        view_stack.addView(effect_list_view_mv);
        //IMV选择
        MVChooserMediator2 mv_page = new MVChooserMediator2(effect_list_view_mv, _EditorSession,
                editorService.getEffectUseService(AssetInfo.TYPE_SHADER_MV), _RepoClient,
                renderEditService.getRenderRotation());
        editorService.addOnRenderChangeListener(mv_page);

        //滤镜选择
        FilterChooserMediator2 filterChooserMediator = new FilterChooserMediator2(filter_list_view,
                editorService.getEffectUseService(AssetInfo.TYPE_SHADER_EFFECT), dataProvider);
        editorService.addOnRenderChangeListener(filterChooserMediator);

        //字幕选择器
        EffectService overlayEffectService = editorService.getEffectUseService(AssetInfo.TYPE_DIYOVERLAY);
        OverlayUIManager overlayUIManager = new OverlayUIManager(this, dataProvider,
                overlayEffectService, _Player);
        overlayEffectService.setOverlayManager(overlayUIManager);
        CaptionChooserMediator captionChooser = new CaptionChooserMediator(
                effect_list_view_caption, overlayEffectService, _RepoClient, _EditorSession);
        editorService.addOnRenderChangeListener(captionChooser);

        //动图选择器
        DIYChooserMediator overlayChooser = new DIYChooserMediator(
                effect_list_view_overlay, overlayEffectService, _RepoClient, _EditorSession);
        editorService.addOnRenderChangeListener(overlayChooser);

        //音乐选择,音量调节
        View audio_mix_weight_control = findViewById(R.id.audio_mix_weight_control);
        AudioMixWeightControl mixWeightControl = new AudioMixWeightControl(audio_mix_weight_control, renderEditService, _Player);
        editorService.addOnRenderChangeListener(mixWeightControl);
        EffectService musicEffect = editorService.getEffectUseService(AssetInfo.TYPE_MUSIC);
        AudioMixChooserMediator2 audio_page = new AudioMixChooserMediator2(effect_list_audio_mix, audio_mix_weight_control,
                 musicEffect, _RepoClient, _EditorSession, renderEditService.getRenderRotation());
        editorService.addOnRenderChangeListener(audio_page);

        _EditorSession.setPart(0, filterChooserMediator);
        _EditorSession.setPart(1, captionChooser);
        _EditorSession.setPart(2, overlayChooser);
        _EditorSession.setPart(3, audio_page);
        _EditorSession.setPart(4, mv_page);

        EffectChooserModeBinding _ViewStackBinding = new EffectChooserModeBinding();
        _ViewStackBinding.setViewStack(view_stack);
        tab_group.setOnCheckedChangeListener(_ViewStackBinding);
        tab_group.setCheckedIndex(0);

        _Player.updatePlayer();
        _Player.start();

        timelineBar = new TimelineBar(editorService.getTimelineEditService(), editorService.getThumbnailFetcher());
        timelineBar.onCreateView(findViewById(R.id.timeline_nav_layout));
    }

    @Override
    protected void onStart() {
        super.onStart();
        editorService.addOnRenderChangeListenerUnique(timelineBar);
        editorService.addOnRenderChangeListenerUnique(this);
        editorService.onStart();

        _EditorSession.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
        _RepoClient.connect();
        editorService.onResume();
        onRenderChange(editorService.getRenderEditService());
        _EditorSession.onResume();
        _ActionParser.onResume();

    }

    @Override
    protected void onPause() {

        isResume = false;
        _EditorSession.onPause();
        editorService.onPause();
        _RepoClient.disconnect();
        _ActionParser.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        _EditorSession.onStop();
        editorService.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        editorService.onDestroy();
        timelineBar.onDestory();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_next:
                editorService.saveProject(UIMode.EDITOR);
                RenderRequest request = new RenderProgressActivity.Request(_Request)
                        .setProject(editorService.getProjectUri())
                        .setRenderMode(RenderRequest.RENDER_MODE_EXPORT_VIDEO);
                request.startForResult(this, RenderRequest.RENDER_MODE_EXPORT_VIDEO);
                break;
        }
    }

    @Override
    public void onRenderChange(RenderEditService service) {
        if(!service.isRenderReady()){
            return ;
        }

        UIEditorPage page = service.getActiveRenderMode();
        tab_group.setCheckedIndex(page.index());

        if (!isResume) {
            Log.w(TAG, "fragment is not resumed, ignoring project client change event");
            return;
        }

        _Player.updatePlayer();
        if (UIEditorPageProxy.isOverlayPage(page)) {
            if(!_Player.isVideoPlayStopped()){
                return ;
            }
            _Player.start();
            _DisplaySurface.postDelayed(new Runnable() {
                @Override
                public void run() {
                    VideoTimelineEditService timeline = editorService.getTimelineEditService();
                    float time = ((float) timeline.getTimelineProgress()) / (float) 1000;
                    _Player.stopAt(time);
                }
            }, 100);
        } else {
            _Player.startAt(0);
        }

    }

    private class EffectChooserModeBinding extends TabbedViewStackBinding {

        @Override
        public void onCheckedChanged(TabGroup group, int checkedIndex) {
            RenderEditService service = editorService.getRenderEditService();
            service.setActiveRenderMode(UIEditorPage.get(checkedIndex));
            super.onCheckedChanged(group, checkedIndex);

            int lastIndex = _EditorSession.getCurrentActiveIndex();
            if(checkedIndex == lastIndex){
                return ;
            }

            if(lastIndex == -1){
                _EditorSession.setCurrentActiveIndex(checkedIndex);
                _EditorSession.getCurentActivePart().setActive(true);
            }else{
                _EditorSession.getCurentActivePart().setActive(false);
                _EditorSession.setCurrentActiveIndex(checkedIndex);
                _EditorSession.getCurentActivePart().setActive(true);
            }
        }

        public int getActiveIndex() {
            return getViewStack().getActiveIndex();
        }
    }

    static final int REQUEST_CODE_PICK_MUSIC = 2;
    static final int REQUEST_CODE_CONFIRM_ACTION = 5;

    private ArrayList<Integer> _Deleted_mv_lists;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_MUSIC:
                if(data != null){
                    Uri action_uri = data.getData();
                    _Deleted_mv_lists = data.getIntegerArrayListExtra("DELETED_MV_LIST");
                    if (action_uri != null) {
                        _ActionParser.executeAction(action_uri);
                    }
                }
                // XXX temporary hack
                _RepoClient.onDataChange(null);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private final EditorAction.Executor _ActionExecutor = new EditorAction.Executor() {

        @Override
        public void doScrollToGroup(int type, int id) {

        }

        @Override
        public void doScrollTo(AssetID asset_id) {

            UIEditorPage page;
            switch (asset_id.type) {
                case AssetInfo.TYPE_SHADER_MV:
                    page = UIEditorPage.MV;
                    break;
                case AssetInfo.TYPE_MUSIC:
                    page = UIEditorPage.AUDIO_MIX;
                    break;
                default:
                    return;
            }

            EditParticipant part = _EditorSession.getPart(page.index());
            if (part != null) {
                part.scrollTo(asset_id);
            }

            if (_Deleted_mv_lists != null) {
                EffectService mvEffectService = editorService.getEffectUseService(AssetInfo.TYPE_SHADER_MV);

                AssetID mv = mvEffectService.getActivedEffect();
                if (mv != null) {
                    int currentId = mv.id;
                    if (_Deleted_mv_lists.contains(currentId)) {
                        AssetID ai = null;
                        mvEffectService.useEffect(ai);
                    }
                }

                _Deleted_mv_lists = null;
            }

        }

        @Override
        public void doUseAsset(int type, int id, boolean confirmed) {

            if (id < 0) {
                return;
            }

            AssetID asset_id = new AssetID(type, id);
            EffectService service;
            switch (type) {
                case AssetInfo.TYPE_SHADER_MV:
                    service = editorService.getEffectUseService(type);
                    int mvReturn = service.useEffect(asset_id, confirmed);
                    if(mvReturn == EffectService.EFFECTNOTPAY){
                        new AlertDialog.Builder(EditorActivity.this)
                                .setMessage(R.string.qupai_license_needbug)
                                .setPositiveButton(R.string.qupai_dlg_button_confirm,null)
                                .show();
                    }
                    break;
                case AssetInfo.TYPE_MUSIC:
                case AssetInfo.TYPE_MV_MUSIC:
                    service = editorService.getEffectUseService(AssetInfo.TYPE_MUSIC);
                    service.useEffect(asset_id, confirmed);
                    break;
                case AssetInfo.TYPE_FONT:
                    service = editorService.getEffectUseService(AssetInfo.TYPE_DIYOVERLAY);
                    int fontReturn  = service.useEffect(dataProvider.resolveAsset(asset_id));
                    if(fontReturn == EffectService.EFFECTNOTPAY){
                        new AlertDialog.Builder(EditorActivity.this)
                                .setMessage(R.string.qupai_license_needbug)
                                .setPositiveButton(R.string.qupai_dlg_button_confirm,null)
                                .show();
                    }
                    break;
            }
        }

        @Override
        public void doDownload(int type, int id) {

        }

        @Override
        public void doSwitchPage(@Nonnull UIEditorPage page) {
            tab_group.setCheckedIndex(page.index());
        }

        @Override
        public void doStartDubbing(boolean confirmed) {

        }
    };
}
