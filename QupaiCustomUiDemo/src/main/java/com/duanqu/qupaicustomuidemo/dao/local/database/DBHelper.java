package com.duanqu.qupaicustomuidemo.dao.local.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.duanqu.qupaicustomuidemo.dao.local.client.ConditionRelation;
import com.duanqu.qupaicustomuidemo.dao.local.client.Conditions;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereItem;
import com.duanqu.qupaicustomuidemo.dao.local.client.WhereNode;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 数据库帮助类
 * @param <T>
 */
public class DBHelper<T> {

	private Context context;
	public static final int UPDATEANDINSERT=1;
	public static final int DELETE=0;

	public DBHelper(Context context) {
		this.context = context;
	}

	// 判断该记录是否存在
		@SuppressWarnings("unchecked")
	public boolean exists(T po, Map<String, Object> where) {
			SQLiteHelperOrm db = new SQLiteHelperOrm(context);
			try {
				Dao dao = db.getDao(po.getClass());
				if (dao.queryForFieldValues(where).size() > 0) {
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			} finally {
				SQLiteDatabase.releaseMemory();
				if (db != null)
					db.close();
			}
			return false;
		}

		public  List<T> queryForAll(Class<T> c){
			SQLiteHelperOrm db = new SQLiteHelperOrm(context);
			try {
				Dao<T, String> dao = db.getDao(c);
				return dao.queryForAll();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			} finally {
				SQLiteDatabase.releaseMemory();
				if (db != null)
					db.close();
			}
			return new ArrayList<T>();
		}

		// 根据特定条件查询一条记录 OrmBean.class
		@SuppressWarnings("unchecked")
		public T queryForWhere(Class<T> c, Map<String, Object> where) {
			SQLiteHelperOrm db = new SQLiteHelperOrm(context);
			try {
				Dao dao = db.getDao(c);
				List<T> list = dao.queryForFieldValues(where);
				if (list != null && list.size() > 0) {
					return list.get(0);
				} else {
					return null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			} finally {
				SQLiteDatabase.releaseMemory();
				if (db != null)
					db.close();
			}
			return null;
		}

	public T queryForWhere(Class<T> c, WhereNode where) {
		List<T> list = queryForFieldValues(c, where);
		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

    public List<T> queryForFieldValues(Class<T> c, WhereNode where){
        SQLiteHelperOrm db = new SQLiteHelperOrm(context);
        try {
            Dao<T, String> dao = db.getDao(c);
            QueryBuilder<T, String> qb = dao.queryBuilder();
            fillWhere(qb.where(), where);
            PreparedQuery<T> prep = qb.prepare();
            return dao.query(prep);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        } finally {
            SQLiteDatabase.releaseMemory();
            if (db != null)
                db.close();
        }
        return new ArrayList<T>();
    }

	@SuppressWarnings("unchecked")
	public List<T> queryForFieldValues(Class<T> c, Map<String, Object> where){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(c);
			return dao.queryForFieldValues(where);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return new ArrayList<T>();
	}

	public int batchUpdateValues(Class<T> c, final ContentValues values, final List<WhereNode> wherelist){
        SQLiteHelperOrm db = new SQLiteHelperOrm(context);
        try {
            final Dao<T, Long> dao = db.getDao(c);
            dao.callBatchTasks(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    int s1 = values.size();
                    int s2 = wherelist.size();
                    if(s1 != s2){
                        return null;
                    }
                    for(int i = 0; i < s1; i++){
                        update(dao, values, wherelist.get(i));
                    }
                    return null;
                }
            });
            return 1;
        } catch (java.sql.SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            SQLiteDatabase.releaseMemory();
            if (db != null)
                db.close();
        }
        return -1;
    }

	public int batchUpdateByValues(Class<T> c, final List<ContentValues> values, final List<Map<String, Object>> wherelist){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			final Dao<T, Long> dao = db.getDao(c);
			dao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					int s1 = values.size();
					int s2 = wherelist.size();
					if(s1 != s2){
						return null;
					}
					for(int i = 0; i < s1; i++){
						update(dao, values.get(i), wherelist.get(i));
					}
					return null;
				}
			});
			return 1;
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	private int update(Dao<T, Long> dao, ContentValues values, Map<String, Object> where){
		try {
			UpdateBuilder<T, Long> updateBuilder = dao.updateBuilder();
			if (where != null && where.size() > 0) {
				Set<Entry<String, Object>> entrys = where.entrySet();
				Iterator<Entry<String, Object>> iterator = entrys.iterator();
				int len = entrys.size();
				int i = 0;
				Where<T,Long> w = updateBuilder.where();
				while (iterator.hasNext()) {
					Entry<String, Object> entry = iterator.next();
					if (i++ == len - 1) {
						w.eq(entry.getKey(), entry.getValue());
						break;
					}
					w.eq(entry.getKey(), entry.getValue()).and();
				}
				updateBuilder.setWhere(w);
			}
			for (Entry<String, Object> entry : values.valueSet()) {
				updateBuilder.updateColumnValue(entry.getKey(),
						entry.getValue());
			}
			return dao.update(updateBuilder.prepare());
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	private int update(Dao<T, Long> dao, ContentValues values, WhereNode where){
		try {
			UpdateBuilder<T, Long> updateBuilder = dao.updateBuilder();
			fillWhere(updateBuilder.where(), where);
			for (Entry<String, Object> entry : values.valueSet()) {
				updateBuilder.updateColumnValue(entry.getKey(),
						entry.getValue());
			}
			return updateBuilder.update();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 根据特定条件更新特定字段
	 *
	 * @param c
	 * @param values
	 * @return
	 */
	public int update(Class<T> c, ContentValues values, Map<String, Object> where) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao<T, Long> dao = db.getDao(c);
			return update(dao, values, where);
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	public int update(Class<T> c, ContentValues values, WhereNode where) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao<T, Long> dao = db.getDao(c);
			return update(dao, values, where);
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> query(Class<T> c, String[] columns,
			Map<String, Object> where, String orderBy, boolean ascending){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(c);
			QueryBuilder<T, String> qb = dao.queryBuilder();
			if (columns != null && columns.length > 0) {
				qb.selectColumns(columns);
			}
			if (where != null && where.size() > 0) {
				Set<Entry<String, Object>> entrys = where.entrySet();
				Iterator<Entry<String, Object>> iterator = entrys.iterator();
				int len = entrys.size();
				int i = 0;
				Where<T,String> w=qb.where();
				while (iterator.hasNext()) {
					Entry<String, Object> entry = iterator.next();
					if (i++ == len - 1) {
						w.eq(entry.getKey(), entry.getValue());
						break;
					}
					w.eq(entry.getKey(), entry.getValue()).and();
				}
				qb.setWhere(w);
			}
			if (orderBy != null) {
				qb.orderBy(orderBy, false);
			}
			PreparedQuery<T> prep = qb.prepare();
			return dao.query(prep);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return new ArrayList<T>();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public int batchDelete(Class<T> clazz, final List<String> ids, String colume){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			final Dao dao = db.getDao(clazz);
			DeleteBuilder builder = dao.deleteBuilder();
			Where where = builder.where();
			where.in(colume, ids.toArray(new Object[ids.size()]));
			builder.setWhere(where);
			return dao.delete(builder.prepare());
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            SQLiteDatabase.releaseMemory();
            if (db != null) {
                db.close();
            }

        }
		return -1;
	}

	/*
	 * 批量更新和插入的方法
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean batchUpdateAndInsertOrDelete(Class<T> clazz,
			final List<T> po, final int type) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);

		try {
			final Dao dao = db.getDao(clazz);
			dao.callBatchTasks(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (type == UPDATEANDINSERT) {// 如果数据库没有该数据就插入，有就更新数据
						for (T t : po) {
							try {
								dao.createOrUpdate(t);
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						}
					} else if (type == DELETE) {// 删除该数据
						for (T t : po) {
							dao.delete(t);
						}
					}
					return null;
				}
			});
			return true;
		} catch (SQLException e) {

			e.printStackTrace();

		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null) {
				db.close();
			}

		}
		return false;
	}

	/** 新增一条记录 */
	@SuppressWarnings("unchecked")
	public int update(final T po) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			final Dao dao = db.getDao(po.getClass());
			return dao.update(po);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int createOrUpdate(final T po){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			final Dao dao = db.getDao(po.getClass());
			CreateOrUpdateStatus cous = dao.createOrUpdate(po);
			if(cous.isCreated()){
				return 1;
			}else if(cous.isUpdated()){
				return 2;
			}else{
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	/** 新增一条记录 */
	@SuppressWarnings("unchecked")
	public int create(final T po) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		//int result=0;
		try {
			final Dao dao = db.getDao(po.getClass());
//			CreateOrUpdateStatus cous=dao.createOrUpdate(po);
//			if(cous.isCreated()){
//				result=1;
//			}else if(cous.isUpdated()){
//				result=2;
//			}
			return dao.create(po);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	/** 删除一条记录 dao.remove(b); */
	@SuppressWarnings("unchecked")
	public int remove(T po) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(po.getClass());
			return dao.delete(po);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}


	/**
	 * 删除表
	 * @param c
	 */
	public void cleanTable(Class<T> c){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			TableUtils.clearTable(db.getConnectionSource(), c);
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}

	}


	@SuppressWarnings("unchecked")
	public int delete(Class<T> c,Map<String,Object> where){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(c);
			DeleteBuilder<T, String> deleteBuilder = dao.deleteBuilder();
			if (where != null && where.size() > 0) {
				Set<Entry<String, Object>> entrys = where.entrySet();
				Iterator<Entry<String, Object>> iterator = entrys.iterator();
				int len = entrys.size();
				int i = 0;
				Where<T,String> w=deleteBuilder.where();
				while (iterator.hasNext()) {
					Entry<String, Object> entry = iterator.next();
					if (i++ == len - 1) {
						w.eq(entry.getKey(), entry.getValue());
						break;
					}
					w.eq(entry.getKey(), entry.getValue()).and();
					// //.println("----------------------------------------"+);
				}
				deleteBuilder.setWhere(w);
			}
			PreparedDelete<T> prep = deleteBuilder.prepare();
			int result = dao.delete(prep);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	/** 查询一条记录是否存在 */
	@SuppressWarnings("unchecked")
	public boolean queryForExist(Class<T> c, String fieldName, Object value) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(c);
			if (dao.queryForEq(fieldName, value).size() > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return false;
	}

	public List<T> queryRaw(Class<T> c, RawRowMapper<T> mapping, String[] columns,
			WhereNode where, String groupBy, String having,
			String orderBy, Integer offset, Integer limit){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		GenericRawResults<T> result = null;
		try {
			Dao<T,Integer> dao = db.getDao(c);
			QueryBuilder<T, Integer> qb = dao.queryBuilder();
			if (columns != null && columns.length > 0){
				qb.selectRaw(columns);
			}
			if (where != null) {
				Where<T,Integer> w = qb.where();
				fillWhere(w, where);
			}
			if (groupBy != null){
				qb.groupByRaw(groupBy);
			}
			if (having != null) {
				qb.having(having);
			}
			if (orderBy != null){
				qb.orderByRaw(orderBy);
			}
			if (offset != null){
				qb.offset(offset);
			}
			if (limit != null) {
				qb.limit(limit);
			}
			result = dao.queryRaw(qb.prepareStatementString(), mapping);

			return result.getResults();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
			if(result != null){
				try {
					result.close();
				} catch (java.sql.SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new ArrayList<T>();
	}

	public List<T> query(Class<T> c, String[] columns,
			WhereNode where, String groupBy, String having,
			String orderBy, Integer offset, Integer limit){
		return query(c, columns, where, groupBy, having, orderBy, offset, limit, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> query(Class<T> c, String[] columns,
			WhereNode where, String groupBy, String having,
			String orderBy, Integer offset, Integer limit, boolean ascending){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao dao = db.getDao(c);
			QueryBuilder<T, String> qb = dao.queryBuilder();
			if (columns != null && columns.length > 0) {
				qb.selectColumns(columns);
			}
			if (where != null) {
				Where<T,String> w = qb.where();
				fillWhere(w, where);
			}
			if (groupBy != null) {
				qb.groupBy(groupBy);
			}
			if (having != null) {
				qb.having(having);
			}
			if (orderBy != null) {
				qb.orderBy(orderBy, ascending);
			}
			if (offset != null) {
				qb.offset(offset);
			}
			if (limit != null) {
				qb.limit(limit);
			}
			PreparedQuery<T> prep = qb.prepare();
			return dao.query(prep);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return new ArrayList<T>();
	}

	public int delete(Class<T> c, WhereNode where){
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			Dao<T, String> dao = db.getDao(c);
			DeleteBuilder<T, String> deleteBuilder = dao.deleteBuilder();
			if (where != null) {
				Where<T,String> w = deleteBuilder.where();
				fillWhere(w, where);
			}
			PreparedDelete<T> prep = deleteBuilder.prepare();
			int result = dao.delete(prep);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return -1;
	}

	public static void fillWhere(Where where, WhereNode node) throws java.sql.SQLException{
		if(node.relation == ConditionRelation.NONE){
			WhereItem item = node.item;
			if(item.condition == Conditions.EQ){
				where.eq(item.colume, item.value);
			}else if(item.condition == Conditions.LIKE){
				where.like(item.colume, "%" + item.value + "%");
			}else if(item.condition == Conditions.IN){
				where.in(item.colume, (Object[])item.value);
			}else if(item.condition == Conditions.NOTIN){
				where.notIn(item.colume, (Object[])item.value);
			}else if(item.condition == Conditions.NE){
				where.ne(item.colume, item.value);
			}else if(item.condition == Conditions.GE){
				where.ge(item.colume, item.value);
			}else if(item.condition == Conditions.GT){
				where.gt(item.colume, item.value);
			}else if(item.condition == Conditions.LE){
				where.le(item.colume, item.value);
			}else if(item.condition == Conditions.LT){
				where.lt(item.colume, item.value);
			}
		}else if(node.relation == ConditionRelation.AND){
			fillWhere(where, node.right);
			fillWhere(where, node.left);
			where.and(2);
//			fillWhere(where, node.right);
//			fillWhere(where.and(), node.left);
		}else if(node.relation == ConditionRelation.OR){
			fillWhere(where, node.right);
			fillWhere(where, node.left);
			where.or(2);
//			fillWhere(where, node.right);
//			fillWhere(where.or(), node.left);
		}else {
			throw new SQLException("no relation exception");
		}
	}

	/**
	 * 查询某几列的数据返回 关闭数据库 (Cursor) query(String table, String[] columns, String
	 * selection, String[] selectionArgs, String groupBy, String having, String
	 * orderBy, String limit)
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<T> query(Class<T> c, String[] columns,
			Map<String, Object> where, String groupBy, String having,
			String orderBy, Integer offset, Integer limit, boolean isLike) {
		SQLiteHelperOrm db = new SQLiteHelperOrm(context);
		try {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Dao dao = db.getDao(c);
			QueryBuilder<T, String> qb = dao.queryBuilder();
			if (columns != null && columns.length > 0) {
				qb.selectColumns(columns);
			}
			if (where != null && where.size() > 0) {
				Set<Entry<String, Object>> entrys = where.entrySet();
				Iterator<Entry<String, Object>> iterator = entrys.iterator();
				int len = entrys.size();
				int i = 0;
				Where<T,String> w=qb.where();
				while (iterator.hasNext()) {
					Entry<String, Object> entry = iterator.next();
					if (i++ == len - 1) {
						if (isLike) {
							w.like(entry.getKey(), "%" + entry.getValue() + "%");
							break;
						}
						w.eq(entry.getKey(), entry.getValue());
						break;
					}
					w.eq(entry.getKey(), entry.getValue()).and();
					// //.println("----------------------------------------"+);
				}
				//qb.setWhere(w);
			}
			if (groupBy != null) {
				qb.groupBy(groupBy);
			}
			if (having != null) {
				qb.having(having);
			}
			if (orderBy != null) {
				qb.orderBy(orderBy, false);
			}
			if (offset != null) {
				qb.offset(offset);
			}
			if (limit != null) {
				qb.limit(limit);
			}
			PreparedQuery<T> prep = qb.prepare();
			return dao.query(prep);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (db != null)
				db.close();
		}
		return new ArrayList<T>();
	}

}
