package com.duanqu.qupaicustomuidemo.editor.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.astuetz.PagerSlidingTabStrip;
import com.duanqu.qupai.asset.AssetGroup;
import com.duanqu.qupai.asset.AssetInfo;
import com.duanqu.qupai.asset.AssetRepository;
import com.duanqu.qupaicustomuidemo.R;
import com.duanqu.qupaicustomuidemo.app.QupaiApplication;
import com.duanqu.qupaicustomuidemo.provider.ProviderUris;
import com.duanqu.qupaicustomuidemo.utils.FileUtil;

import java.util.*;

/**
 * Created by Administrator on 2016/11/22.
 */
public class EffectManageActivity extends FragmentActivity {

    public Map<AssetRepository.Kind, MultiSelectedToCheck> checks = new HashMap<>();
    private MultiSelectedToCheck currentCheck;
    private String[] CONTENT;
    private SparseArray<AssetRepository.Kind> pagerArray = new SparseArray<>();
    private PagerSlidingTabStrip indicator;
    private ViewPager pager;
    private AssetRepository repository;

    private int currentIndex;
    private TextView saveBtn;
    private View editLayout;
    private TextView selectBtn;
    private TextView delBtn;
    private ProviderUris URIS;

    public static void show(Context context, AssetRepository.Kind kind){
        Intent intent = new Intent(context, EffectManageActivity.class);
        intent.putExtra("kind", kind);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.effect_manage_activity);

        saveBtn = (TextView) findViewById(R.id.btn_edit);
        editLayout = findViewById(R.id.face_music_manager_delete_layout);
        selectBtn = (TextView) findViewById(R.id.face_music_manager_select_btn);
        delBtn = (TextView) findViewById(R.id.face_music_manager_delete_btn);

        findViewById(R.id.resource_manager_closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        indicator = (PagerSlidingTabStrip) findViewById(R.id.face_and_music_indicator);
        pager = (ViewPager) findViewById(R.id.face_and_music_manager_pager);

        indicator.setShouldExpand(true);
        saveBtn.setText(R.string.face_and_music_manager_edit);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean active = saveBtn.isActivated();
                if(active){
                    saveBtn.setText(R.string.face_and_music_manager_edit);
                }else{
                    saveBtn.setText(R.string.qupai_cancel);
                }
                active = !active;
                saveBtn.setActivated(active);
                if(currentCheck == null){
                    currentCheck = checks.get(getKind(pager.getCurrentItem()));
                }
                if(currentCheck !=  null){
                    currentCheck.toggleMultiCheckMode(active);
                }
                editLayout.setVisibility(active ? View.VISIBLE : View.GONE);
            }
        });

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean active = selectBtn.isActivated();
                if(active){
                    selectBtn.setText(R.string.all_select);
                }else{
                    selectBtn.setText(R.string.all_unselect);
                }
                active = !active;
                selectBtn.setActivated(active);
                if(currentCheck != null){
                    if(active){
                        currentCheck.selectedAllItems();
                    }else{
                        currentCheck.unselectedAllItems();
                    }
                }

            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentCheck == null){
                    return ;
                }

                Object[] selects = currentCheck.getSelectedItems();

                if(selects.length <= 0 ){
                    return;
                }

                currentCheck.removeSelectedItem();

                AssetRepository.Kind kind = getKind(currentIndex);
                Uri uri;
                if(kind == AssetRepository.Kind.DIY || kind == AssetRepository.Kind.CAPTION){
                    uri = URIS.DIY_CATEGORY;
                }else if(kind == AssetRepository.Kind.SOUND){
                    uri= URIS.MUSIC;
                }else if(kind == AssetRepository.Kind.MV){
                    uri= URIS.MV;
                }else if(kind == AssetRepository.Kind.FONT){
                    uri= URIS.FONT;
                }else{
                    throw new NoSuchElementException();
                }

                AssetInfo[] assetInfos = new AssetInfo[selects.length];
                AssetGroup[] assetGroups = new AssetGroup[selects.length];
                String[] args = new String[selects.length];
                for(int i = 0; i < selects.length; i++){
                    long id;
                    if(kind == AssetRepository.Kind.DIY || kind == AssetRepository.Kind.CAPTION){
                        AssetGroup ag = (AssetGroup) selects[i];
                        id = ag.getGroupId();
                        assetGroups[i] = ag;
                    }else{
                        AssetInfo ai = (AssetInfo)selects[i];
                        assetInfos[i] = ai;
                        id = ai.getID();
                    }
                    args[i] = String.valueOf(id);
                }
                getContentResolver().delete(uri, null, args);

                if(kind == AssetRepository.Kind.DIY || kind == AssetRepository.Kind.CAPTION){
                    new AsyncTask<AssetGroup, Void, Boolean>(){
                        @Override
                        protected Boolean doInBackground(AssetGroup... params) {
                            for(AssetGroup g : params){
                                List<? extends AssetInfo> list = repository.findDIYCategoryContent(g.getGroupId());
                                FileUtil.deleteSelectFiles(list.toArray(new AssetInfo[list.size()]), getApplicationContext());
                            }

                            return true;
                        }
                    }.execute(assetGroups);
                }else{
                    new AsyncTask<AssetInfo, Void, Boolean>(){
                        @Override
                        protected Boolean doInBackground(AssetInfo... params) {
                            return FileUtil.deleteSelectFiles(params, getApplicationContext());
                        }
                    }.execute(assetInfos);
                }


            }
        });

        URIS = new ProviderUris(this);
        repository = QupaiApplication.videoSessionClient.getAssetRepository();

        AssetRepository.Kind[] kinds = repository.findCategory();

        List<String> cs = new ArrayList<>();
        int j = 0;
        for(int i=0; i < kinds.length; i++) {
            if(kinds[i] == AssetRepository.Kind.FILTER){
                continue;
            }
            cs.add(getPageNameByKind(kinds[i]));
            pagerArray.put(j++, kinds[i]);
        }

        CONTENT = cs.toArray(new String[cs.size()]);

        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                currentCheck = checks.get(getKind(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                saveBtn.setText(R.string.face_and_music_manager_edit);
                selectBtn.setText(R.string.all_select);
                editLayout.setVisibility(View.GONE);

                if(currentCheck != null){
                    currentCheck.toggleMultiCheckMode(false);
                }
            }
        });

        pager.setAdapter(new EffectPageAdapter(getSupportFragmentManager()));

        indicator.setViewPager(pager);
        AssetRepository.Kind kind = (AssetRepository.Kind) getIntent().getSerializableExtra("kind");
        pager.setCurrentItem(pagerArray.indexOfValue(kind));

    }

    class EffectPageAdapter extends FragmentPagerAdapter {

        public EffectPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            int pos = position % CONTENT.length;
            checks.put(getKind(pos), (MultiSelectedToCheck) obj);
            return obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            int pos = position % CONTENT.length;
            checks.remove(getKind(pos));
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int pos = position % CONTENT.length;
            return CONTENT[pos];
        }

        @Override
        public long getItemId(int position) {
            return position % CONTENT.length;
        }

        @Override
        public Fragment getItem(int position) {
            return initFragment(position);
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    private Fragment initFragment(int pos){
        if(getKind(pos) == AssetRepository.Kind.DIY
                || getKind(pos) == AssetRepository.Kind.CAPTION){
            return OverlayEffectManageFragment.newInstance(pos);
        }else{
            return EffectManageFragment.newInstance(pos);
        }
    }

    public AssetRepository.Kind getKind(int pos){
        return pagerArray.get(pos);
    }

    private String getPageNameByKind(AssetRepository.Kind kind){
        if(kind == AssetRepository.Kind.DIY){
            return "动图";
        }else if(kind == AssetRepository.Kind.FILTER){
            return "滤镜";
        }else if(kind == AssetRepository.Kind.CAPTION){
            return "字幕";
        }else if(kind == AssetRepository.Kind.SOUND){
            return "音乐";
        }else if(kind == AssetRepository.Kind.MV){
            return "IMV";
        }else if(kind == AssetRepository.Kind.FONT) {
            return "字体";
        }else{
            return null;
        }
    }

    public AssetRepository getRepository(){
        return repository;
    }
}
