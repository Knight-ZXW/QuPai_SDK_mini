package com.duanqu.qupaicustomuidemo.provider;

import android.util.SparseArray;
import android.util.SparseIntArray;
import com.duanqu.qupaicustomuidemo.editor.download.VideoEditBean;
import com.duanqu.qupaicustomuidemo.dao.bean.DIYCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/20.
 */
public class AssetStoreCache  {

    private List<VideoEditBean> fliterEffects; //滤镜
    private List<VideoEditBean> fontEffects; //字体
    private List<VideoEditBean> mvEffects; //mv
    private List<VideoEditBean> musicEffects; //音乐
    private SparseArray<DIYCategory> categoryArray = new SparseArray<>(); //
    private SparseArray<VideoEditBean> overlays = new SparseArray<>();
    private SparseIntArray overlayToCategory = new SparseIntArray();
    private SparseArray<List<Integer>> categoryToOverlay = new SparseArray<>();

    public List<VideoEditBean> getFliterEffects(){
        return fliterEffects;
    }

    public void saveFliterEffects(List<VideoEditBean> fliters){
        fliterEffects = fliters;
    }

    public List<VideoEditBean> getMVEffects(){
        return mvEffects;
    }

    public void saveMVEffects(List<VideoEditBean> mv){
        mvEffects = mv;
    }

    public List<VideoEditBean> getMusicEffects(){
        return musicEffects;
    }

    public void saveMusicEffects(List<VideoEditBean> music){
        musicEffects = music;
    }

    public List<VideoEditBean> getFontEffects(){
        return fontEffects;
    }

    public void saveFontEffects(List<VideoEditBean> music){
        fontEffects = music;
    }

    public void saveDIYCategorys(List<DIYCategory> categories){
        categoryArray.clear();
        for(DIYCategory ec : categories){
            categoryArray.put(ec.getGroupId(), ec);
        }
    }

    public List<DIYCategory> getDIYCategorys(){
        List<DIYCategory> list = new ArrayList<>();
        for(int i = 0; i < categoryArray.size(); i++){
            DIYCategory ec = categoryArray.valueAt(i);
            list.add(ec);
        }
        return list;
    }

    public void saveDIYCategoryContent(int categoryId, List<VideoEditBean> list){
        if(list.size() == 0){
            return ;
        }
        List<Integer> ids = new ArrayList<>();
        for(VideoEditBean v : list){
            int id = (int)v.getID();
            ids.add(id);
            saveDIY(v);
            overlayToCategory.put(id, categoryId);
        }
        categoryToOverlay.put(categoryId, ids);
    }

    public List<VideoEditBean> getDIYCategoryContent(int categoryId){
        List<Integer> ids = categoryToOverlay.get(categoryId);
        if(ids == null){
            return new ArrayList<>();
        }
        List<VideoEditBean> list = new ArrayList<>();
        for(Integer id : ids){
            VideoEditBean v = overlays.get(id);
            if(v != null){
                list.add(v);
            }
        }
        return list;
    }

    public void removeDIYCategoryContentById(int categoryId){
        List<Integer> ids = categoryToOverlay.get(categoryId);
        categoryToOverlay.remove(categoryId);
        if(ids == null){
            return ;
        }
        for(Integer id : ids){
            overlayToCategory.delete(id);
            overlays.remove(id);
        }

    }


    public void saveDIY(VideoEditBean effect){
        int id = (int)effect.getID();
        int index = overlays.indexOfKey(id);
        if(index >= 0){
            overlays.remove(id);
        }
        overlays.put(id, effect);
    }

    public VideoEditBean getDIYById(int id){
        return overlays.get(id, null);
    }

    public void saveDIYCategory(DIYCategory category){
        int id = category.id;
        removeDIYCategoryById(id);
        removeDIYCategoryContentById(id);
        categoryArray.put(id, category);

    }

    public void removeDIYCategoryById(int categoryId){
        int index = categoryArray.indexOfKey(categoryId);
        if(index >= 0){
            categoryArray.remove(categoryId);
        }
    }

    public DIYCategory getDIYCategoryById(int id){
        return categoryArray.get(id, null);
    }

    public void clearCache(){
        categoryArray.clear();
        categoryToOverlay.clear();
        overlayToCategory.clear();
        overlays.clear();

        fliterEffects = null;
        mvEffects = null;
        musicEffects = null;
        fontEffects = null;

    }

}
