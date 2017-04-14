package com.duanqu.qupaicustomuidemo.provider;

import android.content.Context;
import android.net.Uri;
import com.duanqu.qupai.effect.asset.Scheme;

public class ProviderUris {

    public static String getAuthority(Context context) {
        return context.getPackageName() + ".provider";
    }

    public ProviderUris(Context context) {
        AUTHORITY = getAuthority(context);

        AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

        DIY_CATEGORY = getPathUri(PATH_DIY_CATEGORY);
        DIY_CATEGORY_ID = getPathUri(PATH_DIY_CATEGORY_ID);
        DIY = getPathUri(PATH_DIY);
        DIY_ID = getPathUri(PATH_DIY_ID);
        FONT_ID = getPathUri(PATH_FONT_ID);
        DIY_DOWNLOAD = getPathUri(PATH_DIY_DOWNLOAD);
        DIY_CONTENT = getPathUri(PATH_DIY_CATEGORY_CONTENT);
        DIY_CATEGORY_ALL = getPathUri(PATH_DIY_CATEGORY_ALL);
        DIY_CATEGORY_DOWNLOAD = getPathUri(PATH_DIY_CATEGORY_DOWNLOAD);

        MV = getPathUri(PATH_MV);
        MV_ID = getPathUri(PATH_MV_ID);

        FILTER_LOCAL = getPathUri(PATH_FILTER_LOCAL);

        FONT = getPathUri(PATH_FONT);
        MUSIC = getPathUri(PATH_MUSIC);
        MUSIC_ID = getPathUri(PATH_MUSIC_ID);

        RESOURCE = getPathUri(PATH_RESOURCE);
        EXPRESSION = getPathUri(PATH_EXPRESSION);
    }

    private Uri getPathUri(String path) {
        return Uri.parse("content://" + AUTHORITY + "/" + path);
    }

    public final String AUTHORITY;

    public final Uri AUTHORITY_URI;

    public static final String PATH_PASTER  = "paster";

    public static final String PATH_DIY                     = "diy";
    public static final String PATH_DIY_ID                  = "diy/#";
    public static final String PATH_DIY_LOCAL               = "diy/local";
    public static final String PATH_DIY_RECOMMEND           = "diy/recommend";
    public static final String PATH_DIY_DOWNLOAD            = "diy/download";
    public static final String PATH_DIY_CATEGORY_ID         = "diy/category/#";
    public static final String PATH_DIY_CATEGORY            = "diy/category";
    public static final String PATH_DIY_CATEGORY_DOWNLOAD   = "diy/category/download";
    public static final String PATH_DIY_CATEGORY_ALL        = "diy/category/all";
    public static final String PATH_DIY_CATEGORY_CONTENT    = "diy/category/content/#";

    public final Uri DIY_CATEGORY;
    public final Uri DIY_CATEGORY_ID;
    public final Uri DIY;
    public final Uri DIY_ID;
    public final Uri FONT_ID;
    public final Uri DIY_CONTENT;
    public final Uri DIY_CATEGORY_ALL;
    public final Uri DIY_CATEGORY_DOWNLOAD;

    public final Uri DIY_DOWNLOAD;

    public static final String PATH_MV              = "mv";
    public static final String PATH_MV_ID           = "mv/#";
    public static final String PATH_FONT_ID         = "font/#";
    public static final String PATH_MV_LOCAL        = "mv/local";

    public final Uri MV;
    public final Uri MV_ID;

    public static final String PATH_FILTER_LOCAL    = "filter/local";

    public final Uri FILTER_LOCAL;

    public static final String PATH_MUSIC = "music";
    public static final String PATH_MUSIC_ID = "music/#";

    public static final String PATH_FONT = "font";
    public final Uri FONT;

    public final Uri MUSIC;
    public final Uri MUSIC_ID;

    public static final String PATH_RESOURCE        = "resource";

    public final Uri RESOURCE;

    public static final String PATH_EXPRESSION = "expression";

    public final Uri EXPRESSION;

    public static final String MULI_SPLIT = ",";
    public static final String QUERY_IDS = "ids";
    public final Uri getDIYCategory(String[] ids){
        StringBuilder sb = new StringBuilder();
        for(String id : ids){
            sb.append(id).append(MULI_SPLIT);
        }
        sb.deleteCharAt(sb.length() - 1);
        String str = String.format("%s://%s?ids=%s", Scheme.PROVIDER, AUTHORITY + "/" + PATH_DIY_CATEGORY, sb.toString());
        return Uri.parse(str);
    }

    public long[] getQueryIds(Uri uri){
        String value = uri.getQueryParameter(QUERY_IDS);
        if(value == null){
            return null;
        }
        String[] ss = value.split(MULI_SPLIT);
        long[] r = new long[ss.length];
        for(int i = 0; i < ss.length; i++){
            r[i] = Long.parseLong(ss[i]);
        }
        return r;
    }

    public final Uri getPaster(long id) {
        return Uri.parse("content://" + AUTHORITY + "/diy/" + id);
    }
}
