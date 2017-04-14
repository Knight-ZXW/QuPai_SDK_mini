package com.duanqu.qupaicustomuidemo.dao.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.duanqu.qupaicustomuidemo.dao.bean.ResourceCorrespondence;
import com.duanqu.qupaicustomuidemo.editor.VideoEditResources;
import com.duanqu.qupaicustomuidemo.provider.DIYOverlayCategory;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * ormLite的包装类
 */
public class SQLiteHelperOrm extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "qupai.db";
	private static final int DATABASE_VERSION = 1;

	public SQLiteHelperOrm(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			TableUtils.createTableIfNotExists(arg1, VideoEditResources.class);
			Log.d("SQLiteHelperOrm", "创建VideoEditResources表成功");
			TableUtils.createTableIfNotExists(arg1, DIYOverlayCategory.class);
			Log.d("SQLiteHelperOrm", "DIYOverlayCategory");

			TableUtils.createTableIfNotExists(arg1, ResourceCorrespondence.class);
			Log.d("SQLiteHelperOrm", "创建ResourceCorrespondence表成功");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource arg1, int oldV,
			int newV) {

	}

}
