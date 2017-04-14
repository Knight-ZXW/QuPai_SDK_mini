package com.duanqu.qupaicustomuidemo.dao.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "resource_correspondence")
public class ResourceCorrespondence {

    public static final String CATEGORY_ID            = "category_id";
    public static final String RESOURCE_ID            = "resource_id";
    public static final String TYPE                   = "type";

    @DatabaseField(generatedId = true)
    public long _id;
    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = CATEGORY_ID)
    public int categoryId;
    @DatabaseField(uniqueCombo = true, columnName = RESOURCE_ID)
    public long resourceId;
    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = TYPE)
    public int resourceType;

}
