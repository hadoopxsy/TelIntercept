package com.dean.phonesafe.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ToastUtil {
    public static void show(final Activity activity, final String text){
        if("main".equals(Thread.currentThread().getName())){
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
        }else{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
