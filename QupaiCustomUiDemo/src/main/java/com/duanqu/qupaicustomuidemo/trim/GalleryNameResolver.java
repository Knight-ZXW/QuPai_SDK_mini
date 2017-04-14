package com.duanqu.qupaicustomuidemo.trim;

import android.content.Context;

import java.util.Map;

public class GalleryNameResolver {

    private final Context _Context;
    private final Map<String, Integer> _Map;

    public GalleryNameResolver(Context context, Map<String, Integer> map) {
        _Context = context;
        _Map = map;
    }

    public String resolve(String name) {
        Integer value = _Map.get(name);
        return value == null ? name : _Context.getString(value);
    }

}
