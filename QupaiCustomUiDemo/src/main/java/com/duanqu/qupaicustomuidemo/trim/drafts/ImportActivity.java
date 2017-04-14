package com.duanqu.qupaicustomuidemo.trim.drafts;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.duanqu.qupai.engine.session.PageRequest;
import com.duanqu.qupai.engine.session.SessionClientFactory;
import com.duanqu.qupai.engine.session.SessionPage;
import com.duanqu.qupai.engine.session.SessionPageRequest;
import com.duanqu.qupai.project.Project;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.engine.session.VideoSessionClientFactoryImpl;
import com.duanqu.qupaicustomuidemo.trim.VideoTrimActivity;

import java.io.Serializable;

public class ImportActivity extends FragmentActivity implements ImportVideoFragment.VideoListener, SessionPage {
    private ImageView nextBtn;
    private ImageView closeBtn;
    private ImportVideoFragment videoFragment;

    public static class Request extends SessionPageRequest {

        public Request(SessionPageRequest original) {
            super(original);
        }

        public Request(SessionClientFactory factory, Serializable data) {
            super(factory, data);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        _Request = PageRequest.from(this);
        View actionbar_view = findViewById(R.id.action_bar);
        initView(actionbar_view);
        addImporterFragment(Project.TYPE_VIDEO);
    }

    private void initView(View actionbar_view) {
        closeBtn = (ImageView) actionbar_view.findViewById(R.id.draft_closeBtn);
        closeBtn.setOnClickListener(_CloseButtonOnClicListener);
        nextBtn = (ImageView) actionbar_view.findViewById(R.id.draft_nextBtn);
        nextBtn.setOnClickListener(_NextButtonOnClickListener);
    }

    private void addImporterFragment(int type) {

        videoFragment = (ImportVideoFragment) getFragmentManager()
                .findFragmentById(R.id.video_tab);
        if (videoFragment == null) {
            videoFragment = ImportVideoFragment.create(type);
            getFragmentManager().beginTransaction()
                    .add(R.id.video_tab, videoFragment)
                    .commit();
        }
        videoFragment.setUserVisibleHint(false);

    }

    private final View.OnClickListener _CloseButtonOnClicListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(RESULT_CANCELED);

            onBackPressed();
        }
    };

    private final View.OnClickListener _NextButtonOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            videoFragment.dispatchOnSelect();
        }
    };

    public static final int REQUEST_CODE_PICK = 1;

    @Override
    public void onVideoSelect(ImportVideoFragment fragment, VideoInfoBean bean) {
//        new TrimActivity.Request(_Request)
//                .setPath(bean.getFilePath())
//                .setDuration(bean.getDuration())
//                .startForResult(this, REQUEST_CODE_IMPORT);
        new VideoTrimActivity.Request(new VideoSessionClientFactoryImpl(),null)
                .setPath(bean.getFilePath())
                .setDuration(bean.getDuration())
                .startForResult(this,REQUEST_CODE_IMPORT);
    }

    @Override
    public void onSortComplete(ImportVideoFragment fragment) {

    }

    @Override
    public void onSortStart(ImportVideoFragment fragment) {

    }

    Request _Request;

    @Override
    public SessionPageRequest getPageRequest() {
        return _Request;
    }

    private static final int REQUEST_CODE_IMPORT = 1;

}
