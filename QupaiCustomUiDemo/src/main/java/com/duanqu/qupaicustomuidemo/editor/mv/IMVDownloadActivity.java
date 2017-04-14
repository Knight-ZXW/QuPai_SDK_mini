package com.duanqu.qupaicustomuidemo.editor.mv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereNode;
import com.duanqu.qupaicustomuidemo.dao.local.database.DBHelper;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Mulberry on 2016/8/28 18:54
 */
public class IMVDownloadActivity extends FragmentActivity {
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentByTag("myFragmentTag");
        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new IMVDownloadFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("rotation", getIntent().getIntExtra("rotation", 0));
            fragment.setArguments(bundle);
            ft.add(android.R.id.content, fragment, "myFragmentTag");
            ft.commit();
        }
    }

    private List<VideoEditResources> idList = new ArrayList<>();
    private List<VideoEditResources> delList = new ArrayList<>();
    private int RESOURCE_RECOMMEND = 2;
    private int RESOURE_IS_DOWNLOAD = 1;
    private int RESOURE_TYPE_DOWNLOAD = 7;
    private QueryResIdTask queryTask;
    private boolean isQuery;

    private String[] columns = new String[]{
            VideoEditResources.ID,
    };

    private class QueryResIdTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DBHelper<VideoEditResources> db = new DBHelper<>(
                    getApplicationContext());

            WhereNode where = new WhereNode.WhereBuilder()
                    .ne(VideoEditResources.RECOMMEND, RESOURCE_RECOMMEND)
                    .eq(VideoEditResources.RESOURCETYPE, RESOURE_TYPE_DOWNLOAD)
                    .eq(VideoEditResources.ISLOCAL, RESOURE_IS_DOWNLOAD).build();
            idList = db.query(VideoEditResources.class, columns,
                    where, null, null, null, null, null, false);

            return null;
        }

        @SuppressLint("UseSparseArrays")
        @Override
        protected void onPostExecute(Void result) {
            if (queryTask.isCancelled()) {
                return;
            }
            isQuery = true;
            if (isQuery) {
                if (fragment != null) {
                    if (isFlushData) {
                        ((IMVDownloadFragment)fragment).setDeleteResList(delList);
                        ((IMVDownloadFragment)fragment).flushDownloadData(idList);
                    } else {
                        ((IMVDownloadFragment)fragment).setQueryData(idList);
                    }
                }

                isFlushData = false;
            }

            super.onPostExecute(result);
        }
    }

    private static final int REQUEST_RESOURCE_MANAGER_CODE = 0x1;
    private boolean isFlushData;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_RESOURCE_MANAGER_CODE) {
            if(resultCode == RESULT_OK) {
                isFlushData = true;

                delList = (List<VideoEditResources>)
                        data.getSerializableExtra("delResIdList");
//                clearDelDownId();
            }
        }
    }
}
