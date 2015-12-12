package com.dean.phonesafe.utils;

import android.test.AndroidTestCase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ServiceUtilTest extends AndroidTestCase {
    
    public void testExistsService() throws Exception {
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        Log.d("ServiceUtilTest", dateFormat.format(new Date()));
    }
}