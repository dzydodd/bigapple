package com.winupon.andframe.bigapple.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.winupon.andframe.bigapple.db.callback.MapRowMapper;
import com.winupon.andframe.bigapple.db.callback.MultiRowMapper;
import com.winupon.andframe.bigapple.db.callback.SingleRowMapper;
import com.winupon.andframe.bigapple.db.helper.SqlUtils;
import com.winupon.andframe.bigapple.utils.log.LogUtils;

/**
 * 对原生数据库操作做了一层轻量级封装，主要屏蔽了显式的close操作，并且处理了多线程操作的问题，当然也可以使用原生的API
 * 
 * @author xuan
 */
public class BasicDao2 {
    public static boolean DEBUG = false;

    private final static ReentrantLock lock = new ReentrantLock();// 保证多线程访问数据库的安全，性能有所损失

    /**
     * 想使用安卓本身的数据操作方法可以获取这个对象，但操作完后要显示的Close掉
     * 
     * @return
     */
    public SQLiteDatabase getSQLiteDatabase() {
        return DBHelper.getInstance().getWritableDatabase();
    }

    /**
     * 使用完后请Close数据库连接，dbHelper的close其实内部就是sqliteDatabase的close
     */
    public void close() {
        DBHelper.getInstance().close();
    }

    // ///////////////////////////////////////////////android的sqlite原生api///////////////////////////////////////////
    /**
     * 插入
     * 
     * @param table
     * @param nullColumnHack
     *            当values是空时，指向该字段的设置成null，插入。
     * @param values
     * @return
     */
    protected long insert(String table, String nullColumnHack, ContentValues values) {
        long updateCount = 0;

        lock.lock();
        try {
            updateCount = getSQLiteDatabase().insert(table, nullColumnHack, values);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量插入
     * 
     * @param table
     * @param nullColumnHack
     * @param valuesList
     * @return
     */
    protected long insertBatch(String table, String nullColumnHack, List<ContentValues> valuesList) {
        long updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (ContentValues values : valuesList) {
                long temp = getSQLiteDatabase().insert(table, nullColumnHack, values);
                updateCount += temp;
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 更新
     * 
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    protected int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int updateCount = 0;

        lock.lock();
        try {
            updateCount = getSQLiteDatabase().update(table, values, whereClause, whereArgs);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量更新
     * 
     * @param table
     * @param valuesList
     * @param whereClauseList
     * @param whereArgsList
     * @return
     */
    protected int updateBatch(String table, List<ContentValues> valuesList, List<String> whereClauseList,
            List<String[]> whereArgsList) {
        int updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (int i = 0, n = valuesList.size(); i < n; i++) {
                int temp = sqliteDatabase
                        .update(table, valuesList.get(i), whereClauseList.get(i), whereArgsList.get(i));
                updateCount += temp;
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 删除
     * 
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
    protected int delete(String table, String whereClause, String[] whereArgs) {
        int updateCount = 0;

        lock.lock();
        try {
            updateCount = getSQLiteDatabase().delete(table, whereClause, whereArgs);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量删除
     * 
     * @param table
     * @param whereClauseList
     * @param whereArgsList
     * @return
     */
    protected int deleteBatch(String table, List<String> whereClauseList, List<String[]> whereArgsList) {
        int updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (int i = 0, n = whereClauseList.size(); i < n; i++) {
                int temp = sqliteDatabase.delete(table, whereClauseList.get(i), whereArgsList.get(i));
                updateCount += temp;
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * Sql语句执行方法
     * 
     * @param sql
     */
    protected void execSQL(String sql) {
        lock.lock();
        try {
            debugSql(sql, null);
            getSQLiteDatabase().execSQL(sql);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }
    }

    /**
     * 批量操作
     * 
     * @param sqlList
     */
    protected void execSQLBatch(List<String> sqlList) {
        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (String sql : sqlList) {
                debugSql(sql, null);
                sqliteDatabase.execSQL(sql);
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }
    }

    /**
     * Sql语句执行方法
     * 
     * @param sql
     * @param bindArgs
     */
    protected void execSQL(String sql, Object[] bindArgs) {
        lock.lock();
        try {
            debugSql(sql, null);
            getSQLiteDatabase().execSQL(sql, bindArgs);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }
    }

    /**
     * 批量操作
     * 
     * @param sqlList
     * @param bindArgsList
     */
    protected void execSQLBatch(List<String> sqlList, List<Object[]> bindArgsList) {
        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (int i = 0, n = sqlList.size(); i < n; i++) {
                String sql = sqlList.get(i);
                Object[] bindArgs = bindArgsList.get(i);
                debugSql(sql, bindArgs);
                sqliteDatabase.execSQL(sql, bindArgs);
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }
    }

    // ///////////////////////////////////////////////////以下是兼容keel的写法////////////////////////////////////////
    // ///////////////////////////////////////////////插入或者更新////////////////////////////////////////////////////
    /**
     * 插入或者更新
     * 
     * @param sql
     */
    protected void update(String sql) {
        lock.lock();
        try {
            debugSql(sql, null);
            getSQLiteDatabase().execSQL(sql);
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            close();
            lock.unlock();
        }
    }

    /**
     * 插入或者更新，带参
     * 
     * @param sql
     * @param args
     */
    protected void update(String sql, Object[] args) {
        if (null == args) {
            update(sql);
        }
        else {
            lock.lock();
            try {
                debugSql(sql, args);
                getSQLiteDatabase().execSQL(sql, args);
            }
            catch (Exception e) {
                LogUtils.e("", e);
            }
            finally {
                close();
                lock.unlock();
            }
        }
    }

    /**
     * 插入或者更新，批量
     * 
     * @param sql
     * @param argsList
     */
    protected void updateBatch(String sql, List<Object[]> argsList) {
        if (null == argsList) {
            return;
        }

        lock.lock();
        SQLiteDatabase sqliteDatabase = getSQLiteDatabase();
        try {
            sqliteDatabase.beginTransaction();
            for (Object[] args : argsList) {
                debugSql(sql, args);
                sqliteDatabase.execSQL(sql, args);
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            sqliteDatabase.endTransaction();
            close();
            lock.unlock();
        }
    }

    // ///////////////////////////////////////////////查询//////////////////////////////////////////////////////////////
    /**
     * 查询，返回多条记录
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> query(String sql, String[] args, MultiRowMapper<T> multiRowMapper) {
        List<T> ret = new ArrayList<T>();

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = getSQLiteDatabase().rawQuery(sql, args);
        try {
            int i = 0;
            while (cursor.moveToNext()) {
                T t = multiRowMapper.mapRow(cursor, i);
                ret.add(t);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            cursor.close();
            close();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询，返回单条记录
     * 
     * @param sql
     * @param args
     * @param singleRowMapper
     * @return
     */
    protected <T> T query(String sql, String[] args, SingleRowMapper<T> singleRowMapper) {
        T ret = null;

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = getSQLiteDatabase().rawQuery(sql, args);
        try {
            if (cursor.moveToNext()) {
                ret = singleRowMapper.mapRow(cursor);
            }
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            cursor.close();
            close();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询，返回MAP集合
     * 
     * @param sql
     * @param args
     * @param singleRowMapper
     * @return
     */
    protected <K, V> Map<K, V> query(String sql, String[] args, MapRowMapper<K, V> mapRowMapper) {
        Map<K, V> ret = new HashMap<K, V>();

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = getSQLiteDatabase().rawQuery(sql, args);
        try {
            int i = 0;
            while (cursor.moveToNext()) {
                K k = mapRowMapper.mapRowKey(cursor, i);
                V v = mapRowMapper.mapRowValue(cursor, i);
                ret.put(k, v);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e("", e);
        }
        finally {
            cursor.close();
            close();
            lock.unlock();
        }

        return ret;
    }

    /**
     * IN查询，返回LIST集合
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> queryForInSQL(String prefix, String[] prefixArgs, String[] inArgs, String postfix,
            MultiRowMapper<T> multiRowMapper) {
        if (null == prefixArgs) {
            prefixArgs = new String[0];
        }

        StringBuilder sql = new StringBuilder();
        sql.append(prefix).append(SqlUtils.getInSQL(inArgs.length));

        if (!TextUtils.isEmpty(postfix)) {
            sql.append(postfix);
        }

        String[] args = new String[inArgs.length + prefixArgs.length];

        System.arraycopy(prefixArgs, 0, args, 0, prefixArgs.length);
        System.arraycopy(inArgs, 0, args, prefixArgs.length, inArgs.length);

        return query(sql.toString(), args, multiRowMapper);
    }

    /**
     * IN查询，返回MAP集合
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <K, V> Map<K, V> queryForInSQL(String prefix, String[] prefixArgs, String[] inArgs, String postfix,
            MapRowMapper<K, V> mapRowMapper) {
        if (null == prefixArgs) {
            prefixArgs = new String[0];
        }

        StringBuilder sql = new StringBuilder();
        sql.append(prefix).append(SqlUtils.getInSQL(inArgs.length));

        if (!TextUtils.isEmpty(postfix)) {
            sql.append(postfix);
        }

        String[] args = new String[inArgs.length + prefixArgs.length];

        System.arraycopy(prefixArgs, 0, args, 0, prefixArgs.length);
        System.arraycopy(inArgs, 0, args, prefixArgs.length, inArgs.length);

        return query(sql.toString(), args, mapRowMapper);
    }

    private void debugSql(String sql, Object[] args) {
        if (DEBUG) {
            LogUtils.d(SqlUtils.getSQL(sql, args));
        }
    }

}