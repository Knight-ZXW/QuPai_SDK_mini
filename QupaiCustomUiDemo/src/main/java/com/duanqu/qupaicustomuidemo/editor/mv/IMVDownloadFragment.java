package com.duanqu.qupaicustomuidemo.editor.mv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupai.jackson.JSONSupportImpl;
import com.duanqu.qupai.json.JSONSupport;
import com.duanqu.qupai.utils.FontUtil;
import com.duanqu.qupai.utils.ScaleTypeUtils;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.dao.local.client.ConditionRelation;
import com.duanqu.qupaicustomuidemo.dao.local.client.Conditions;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereItem;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereNode;
import com.duanqu.qupaicustomuidemo.dao.local.database.DBHelper;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.editor.api.Api;
import com.duanqu.qupaicustomuidemo.editor.manager.EffectManageActivity;
import com.duanqu.qupaiokhttp.HttpCycleContext;
import com.duanqu.qupaiokhttp.HttpRequest;
import com.duanqu.qupaiokhttp.StringHttpRequestCallback;
import com.duanqu.qupaisdk.tools.SingnatureUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Mulberry on 2016/8/29 11:14
 */
public class IMVDownloadFragment extends Fragment implements HttpCycleContext,View.OnClickListener {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private IMVDownloadListAdapter mIMVAdapter;
    private int _Rotation;
    private List<VideoEditResources> idList = new ArrayList<>();

    private ImageView mBtnBack;
    private TextView mDownLoadManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        _Rotation = getArguments().getInt("rotation");

        View rootView = FontUtil.applyFontByInflate(
                getActivity(), R.layout.download_imv_fragment, container, false);
        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.download_imv_list);
        mBtnBack = (ImageView) rootView.findViewById(R.id.imv_download_title_back_btn);
        mDownLoadManager = (TextView) rootView.findViewById(R.id.imv_download_manager_btn);
        mRefreshLayout.setEnabled(false);
        initView();
        initData();

        mBtnBack.setOnClickListener(this);
        mDownLoadManager.setOnClickListener(this);
        return rootView;
    }

    /**
     * init RecyclerView
     */
    private void initView() {
        mIMVAdapter = new IMVDownloadListAdapter(getActivity());
        float scale;
        if (_Rotation == 90 || _Rotation == 270) {
            scale = (float)QupaiApplication.videoSessionClient.getProjectOptions().videoHeight /
                    (float)QupaiApplication.videoSessionClient.getProjectOptions().videoWidth;
        } else {
            scale = (float)QupaiApplication.videoSessionClient.getProjectOptions().videoWidth /
                    (float)QupaiApplication.videoSessionClient.getProjectOptions().videoHeight;
        }

        mIMVAdapter.setScaleType(ScaleTypeUtils.getScaleType(scale));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setAdapter(mIMVAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    QueryResIdTask queryResIdTask;

    private JSONSupport jsonSupport = new JSONSupportImpl();

    private void initData() {
        //TODO 整个MV下载的接口获取是没有经过包装的.
        HttpRequest.get(Api.getInstance().getApiUrl(Api.MV_RESOURCE_CATEGORY) + "?packageName=" + getActivity().getPackageName()
                + "&signature=" + SingnatureUtils.getSingInfo(getActivity()) +
                "&cursor=0", new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(Headers headers, String s) {
                super.onSuccess(headers, s);
                List<IMVItemForm2> mIMVItemForm2 = null;
                try {
                    mIMVItemForm2 = jsonSupport.readListValue(s, new TypeReference<List<IMVItemForm2>>() {
                    });
//                    mIMVItemForm2= JSON.parseObject(s, new TypeReference<List<IMVItemForm2>>() {});

                    mIMVAdapter.setDataList(mIMVItemForm2);
                    mIMVAdapter.notifyDataSetChanged();

                    queryResIdTask =new QueryResIdTask();
                    queryResIdTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
            }
        });
    }

    @Override
    public String getHttpTaskKey() {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setDeleteResList(List<VideoEditResources> list) {
        if (list == null) {
            return;
        }

        if (mIMVAdapter != null) {
            mIMVAdapter.setDelIdList(list);
        }
    }

    public void flushDownloadData(List<VideoEditResources> list) {
        if (list == null) {
            return;
        }

        idList = list;

        if (mIMVAdapter != null) {
            mIMVAdapter.setIdList(idList);
            mIMVAdapter.notifyDataSetChanged();
        }
    }

    public void setQueryData(List<VideoEditResources> list) {
        if (list == null) {
            return;
        }

        idList = list;

        mIMVAdapter.setIdList(idList);
        mIMVAdapter.notifyDataSetChanged();
    }

    private static final int REQUEST_RESOURCE_MANAGER_CODE = 0x1;
    private static final int CURRENT_FRAGMENT_IMV = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imv_download_title_back_btn:
                getActivity().finish();
                break;
            case R.id.imv_download_manager_btn:
                Intent in = new Intent();
                in.setClass(v.getContext(), EffectManageActivity.class);
                in.putExtra("kind", AssetRepository.Kind.MV);
                in.putExtra("showCurrentFragment", CURRENT_FRAGMENT_IMV);
                startActivityForResult(in, REQUEST_RESOURCE_MANAGER_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_RESOURCE_MANAGER_CODE) {
            if (mIMVAdapter != null) {
                mIMVAdapter.clearDownloadList();
            }
            queryResIdTask = new QueryResIdTask();
            queryResIdTask.execute();
        }
    }

    private int RESOURCE_RECOMMEND = 2;
    private int RESOURE_IS_DOWNLOAD = 1;
    private int RESOURE_TYPE_DOWNLOAD = 7;

    private String[] columns = new String[]{
            VideoEditResources.ID,
    };

    private class QueryResIdTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DBHelper<VideoEditResources> db = new DBHelper<>(
                    getActivity());

            WhereNode where = new WhereNode.WhereBuilder()
                    .ne(VideoEditResources.RECOMMEND, RESOURCE_RECOMMEND)
                    .eq(VideoEditResources.RESOURCETYPE, RESOURE_TYPE_DOWNLOAD)
                    .eq(VideoEditResources.ISLOCAL, RESOURE_IS_DOWNLOAD)
                    .build();
            idList = db.query(VideoEditResources.class, columns, where, null, null, null, null, null, false);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            setQueryData(idList);
        }
    }

}
