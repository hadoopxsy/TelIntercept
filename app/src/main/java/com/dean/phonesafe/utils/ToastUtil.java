package com.dean.phonesafe.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/11/9.
 */
public class ToastUtil {

    //单实例模式保证全局只有一个Toast
    private static Toast mToast;

    public static void show(final Activity activity, final String text) {
        checkToast(activity, text);
        if ("main".equals(Thread.currentThread().getName())) {
            mToast.show();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.show();
                }
            });
        }
    }

    /**
     * 保证只有一个Toast实例在运行，并且设置显示的文字
     * @param context
     * @param text
     */
    private synchronized static void checkToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
    }
}
