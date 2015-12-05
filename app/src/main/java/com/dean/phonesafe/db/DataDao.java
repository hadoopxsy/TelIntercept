package com.dean.phonesafe.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询省市数据库
 */
public class DataDao {

    private static DataDao mDataDao;
    private SQLiteDatabase mDb;

    private DataDao(Context context) {
        File file = new File(context.getFilesDir(), "data.db");
        mDb = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    public synchronized static DataDao getInstance(Context context) {
        if (mDataDao == null) {
            mDataDao = new DataDao(context);
        }
        return mDataDao;
    }

    /**
     * 查询所有省份
     *
     * @return
     */
    public List<String> getProvinces() {
        List<String> list = new ArrayList<>();
        Cursor cursor = mDb.query("province", new String[]{"province_name"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    /**
     * 通过省份名查询所有城市
     *
     * @return
     */
    public List<String> getCities(String provinceName) {
        long tick = System.currentTimeMillis();

        List<String> list = new ArrayList<>();
        Cursor cursor = mDb.query("data_view", new String[]{"city_name"}, "province_name=?", new String[]{provinceName}, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    /**
     * 传入一个白名单简写的城市名得到一个完整的城市名，返回一个由：XXX省 XXX市组成的字符串
     *
     * @param areaNameInWhiteList
     * @return
     */
    public String getTotalAreaName(String areaNameInWhiteList) {
        String resultStr = areaNameInWhiteList;
        String[] arrAreaName = areaNameInWhiteList.split(" ");
        if (arrAreaName.length == 2) {
            String where = " where province_name like '" + arrAreaName[0] + "%' and city_name like '" + arrAreaName[1] + "%'";
            Cursor cursor = mDb.rawQuery("select province_name,city_name from data_view " + where, null);
            if (cursor.moveToFirst()) {
                resultStr = cursor.getString(0) + " " + cursor.getString(1);
            }
            cursor.close();
        }
        return resultStr;
    }
}
