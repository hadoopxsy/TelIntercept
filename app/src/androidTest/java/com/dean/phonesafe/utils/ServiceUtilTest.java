package com.dean.phonesafe.utils;

import android.test.AndroidTestCase;

import com.dean.phonesafe.service.TelService;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ServiceUtilTest extends AndroidTestCase {
    
    public void testExistsService() throws Exception {
        ServiceUtil.existsService(getContext(), TelService.class);
    }
}