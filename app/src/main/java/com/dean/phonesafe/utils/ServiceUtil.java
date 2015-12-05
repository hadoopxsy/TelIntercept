package com.dean.phonesafe.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ServiceUtil {
    public static boolean existsService(Context context,Class<?> clsName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : runningServices) {
            if(info.service.getClassName().equals(clsName.getCanonicalName()))
                return true;
        }
        return false;
    }
}
