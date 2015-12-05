package com.dean.phonesafe.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.dean.phonesafe.domain.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * 白名单数据库
 */
public class HappyDbDao {
    /**
     * 电话被拦截，拦截记录被更改
     */
    public static final Uri CONTENT_URI_ADD_CALL = Uri.parse("content://com.dean.phonesafe.db/happy/calls");
    /**
     * 黑名单号码更改
     */
    public static final Uri CONTENT_URI_BLACK_LIST = Uri.parse("content://com.dean.phonesafe.db/happy/black_list");
    /**
     * 白名单城市更改
     */
    public static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.dean.phonesafe.db/happy/white_list");
    private Context mContext;
    private static HappyDbDao mHappyDbDao;
    private SQLiteDatabase mDb;
    
    private HappyDbDao(Context context) {
        HappyDbHelper helper = new HappyDbHelper(context);
        mDb = helper.getWritableDatabase();
        mContext = context;
    }
    
    public synchronized static HappyDbDao getInstance(Context context) {
        if (mHappyDbDao == null) {
            mHappyDbDao = new HappyDbDao(context);
        }
        return mHappyDbDao;
    }
    
    /**
     * 添加允许的区域，区域名由省名+城市组成，如：湖南 湘潭
     *
     * @param areaName 被AreaNameUtil.getAvailAreaName()方法处理过的5个字符长度的城市名称
     * @return
     */
    public boolean addGrantArea(String areaName) {
        //已经存在该城市名称直接返回成功
        if (existsArea(areaName))
            return true;
        ContentValues contentValues = new ContentValues();
        contentValues.put("area_name", areaName);
        boolean retValue = mDb.insert(HappyDbHelper.TABLE_WHITE_LIST, null, contentValues) > 0;
        mContext.getContentResolver().notifyChange(CONTENT_URI_WHITE_LIST, null);
        return retValue;
    }
    
    /**
     * 判断白名单城市表中是否有记录
     *
     * @return
     */
    public boolean hasRecordInWhiteList() {
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_WHITE_LIST, new String[]{"area_name"}, null, null, null, null, null, "1");
        boolean has = cursor.getCount() > 0;
        cursor.close();
        return has;
    }
    
    /**
     * 判断数据是否已经存在该城市
     *
     * @param areaName 被AreaNameUtil.getAvailAreaName()方法处理过的5个字符长度的城市名称
     * @return
     */
    public boolean existsArea(String areaName) {
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_WHITE_LIST, null, "area_name=?", new String[]{areaName}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    /**
     * 判断数据是否已经存在该城市
     *
     * @param areaName 被AreaNameUtil.getAvailAreaName()方法处理过的5个字符长度的城市名称
     * @return
     */
    public boolean existsArea(List<String> areaName) {
        StringBuilder where = new StringBuilder();
        for (int i = 0; i < areaName.size(); i++) {
            if (i == 0) {
                where.append("area_name=?");
            } else {
                where.append(" or area_name=?");
            }
        }
        String[] args = new String[areaName.size()];
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_WHITE_LIST, null, where.toString(), areaName.toArray(args), null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    /**
     * 查询所有允许的城市名
     *
     * @return
     */
    public List<String> getAllGrantAreaName() {
        List<String> list = new ArrayList<>();
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_WHITE_LIST, new String[]{"area_name"}, null, null, null, null, "area_name");
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }
    
    /**
     * 从数据库删除一个城市名
     *
     * @param areaName 被AreaNameUtil.getAvailAreaName()方法处理过的5个字符长度的城市名称
     * @return
     */
    public boolean deleteAreaName(String areaName) {
        boolean retValue = mDb.delete(HappyDbHelper.TABLE_WHITE_LIST, "area_name=?", new String[]{areaName}) > 0;
        mContext.getContentResolver().notifyChange(CONTENT_URI_WHITE_LIST, null);
        return retValue;
    }
    
    /**
     * 查询所有拦截记录
     *
     * @return
     */
    public List<Call> getAllCall() {
        List<Call> list = new ArrayList<>();
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_CALL, new String[]{"_id", "number", "date", "area_name"}, null, null, null, null, "date desc");
        while (cursor.moveToNext()) {
            Call call = new Call();
            call.setId(cursor.getInt(0));
            call.setNumber(cursor.getString(1));
            call.setDate(cursor.getLong(2));
            call.setAreaName(cursor.getString(3));
            list.add(call);
        }
        cursor.close();
        return list;
    }
    
    /**
     * 添加一条拦截记录
     *
     * @param number
     * @return
     */
    public boolean addCall(String number, String totalAreaName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("number", number);
        contentValues.put("date", System.currentTimeMillis());
        contentValues.put("area_name", totalAreaName);
        boolean ok = mDb.insert(HappyDbHelper.TABLE_CALL, null, contentValues) > 0;
        mContext.getContentResolver().notifyChange(CONTENT_URI_ADD_CALL, null);
        
        return ok;
    }
    
    /**
     * 删除一条拦截记录
     *
     * @param id
     * @return
     */
    public boolean deleteCall(int id) {
        boolean retValue = mDb.delete(HappyDbHelper.TABLE_CALL, "_id=?", new String[]{String.valueOf(id)}) > 0;
        return retValue;
    }
    
    /**
     * 添加一个黑名单号码表达式
     *
     * @param number
     * @return
     */
    public boolean addBlackListNumber(String number) {
        if (existsBlackListNumber(number))
            return true;
        ContentValues contentValues = new ContentValues();
        contentValues.put("number_exp", number);
        boolean retValue = mDb.insert(HappyDbHelper.TABLE_BLACK_LIST, null, contentValues) > 0;
        mContext.getContentResolver().notifyChange(CONTENT_URI_BLACK_LIST, null);
        return retValue;
    }
    
    /**
     * 删除一个黑名单号码表达式
     *
     * @param number
     * @return
     */
    public boolean deleteBlackListNumber(String number) {
        boolean retValue = mDb.delete(HappyDbHelper.TABLE_BLACK_LIST, "number_exp=?", new String[]{number}) > 0;
        mContext.getContentResolver().notifyChange(CONTENT_URI_BLACK_LIST, null);
        return retValue;
    }
    
    /**
     * 查询一个黑名单号码表达式是否存在
     *
     * @param number
     * @return
     */
    public boolean existsBlackListNumber(String number) {
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_BLACK_LIST, new String[]{"number_exp"}, "number_exp=?", new String[]{number}, null, null, null, "1");
        boolean retValue = cursor.getCount() > 0;
        cursor.close();
        return retValue;
    }
    
    /**
     * 获取所有黑名单号码表达式
     *
     * @return
     */
    public List<String> getAllBlackListNumber() {
        List<String> list = new ArrayList<>();
        Cursor cursor = mDb.query(HappyDbHelper.TABLE_BLACK_LIST, new String[]{"number_exp"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }
    
}
