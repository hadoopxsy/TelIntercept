package com.dean.phonesafe.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询归属地数据库
 */
public class AddressDao {

    private static AddressDao mAddressDao;
    private final SQLiteDatabase mDb;
    
    private AddressDao(Context context) {
        File file = new File(context.getFilesDir(), "address.db");
        mDb = SQLiteDatabase.openDatabase(file.getPath(),  null, SQLiteDatabase.OPEN_READONLY);
    }
    
    public synchronized static AddressDao getInstance(Context context){
        if(mAddressDao==null){
            mAddressDao=new AddressDao(context);
        }
        return mAddressDao;
    }


    /**
     * 通过手机号码前7位查询归属地
     *
     * @param number
     * @return
     */
    public String getAreaNameByMobileNumber(String number) {
        String areaName = "";
        Cursor cursor = mDb.query("phone_view", new String[]{"area_name"}, "phone_number=?", new String[]{number}, null, null, null);
        if (cursor.moveToFirst()) {
            areaName = cursor.getString(0);
        }
        cursor.close();
        return areaName;
    }

    /**
     * 通过固定号码查询归属地
     *
     * @param number
     * @return
     */
    public List<String> getAreaNameByTelNumber(String number) {
        List<String> retValue = new ArrayList<>();
        String prefix = "";
        //截取前4位区号查询
        prefix = number.substring(0, 4);
        retValue = getAreaNameByAreaNumber(prefix);
        if (retValue.size() == 0) {
            //截取前3位区号查询
            prefix = number.substring(0, 3);
            retValue = getAreaNameByAreaNumber(prefix);
        }
        return retValue;
    }

    /**
     * 通过区号查询归属地
     *
     * @param number
     * @return
     */
    public List<String> getAreaNameByAreaNumber(String number) {
        List<String> list = new ArrayList<>();
        Cursor cursor = mDb.query("area", new String[]{"area_name"}, "area_number=?", new String[]{number}, null, null, null);
        while (cursor.moveToNext()) {
            String areaName = cursor.getString(0);
            list.add(areaName);
        }
        cursor.close();
        return list;
    }
}
