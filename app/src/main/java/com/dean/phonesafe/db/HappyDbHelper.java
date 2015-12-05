package com.dean.phonesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2015/11/10.
 */
public class HappyDbHelper extends SQLiteOpenHelper {
    //允许拨入的城市表
    public static final String TABLE_WHITE_LIST = "white_list";
    //禁止拨入的号码，号码是一个正则表达式
    public static final String TABLE_BLACK_LIST = "black_list";
    public static final String TABLE_CALL = "calls";

    private static final String CREATE_TABLE_WHITE_LIST = "create table " + TABLE_WHITE_LIST + " (area_name varchar(20));";
    private static final String CREATE_TABLE_BLACK_LIST = "create table " + TABLE_BLACK_LIST + " (number_exp varchar(20));";
    private static final String CREATE_TABLE_CALL = "create table " + TABLE_CALL + " (_id integer primary key autoincrement,number varchar(20),date integer,area_name varchar(20))";

    private static final String DB_NAME = "happy.db";


    private HappyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public HappyDbHelper(Context context) {
        this(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WHITE_LIST);
        db.execSQL(CREATE_TABLE_BLACK_LIST);
        db.execSQL(CREATE_TABLE_CALL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
