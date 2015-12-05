package com.dean.phonesafe.domain;

import android.test.AndroidTestCase;

/**
 * Created by Administrator on 2015/11/11.
 */
public class CallTest extends AndroidTestCase {
    
    public void testGetDateString() throws Exception {
        Call call=new Call();
        call.setDate(System.currentTimeMillis() - 60*1000);
//        DebugLog.d(call.getRelativeTimeSpanString());
    }
}