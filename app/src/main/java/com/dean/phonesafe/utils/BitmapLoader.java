package com.dean.phonesafe.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Administrator on 2015/10/6.
 */
public class BitmapLoader {
    private static final String TAG = "BitmapLoader";

    public static Bitmap Load(Resources resources, int image_resId, int reqWidth, int reqHeight) {
        //计算采样比例
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, image_resId, opts);
        opts.inSampleSize = calcSample(opts, reqWidth, reqHeight);

        //Log.d(TAG, "采样比例为：" + opts.inSampleSize);
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, image_resId, opts);
    }

    public static BitmapFactory.Options getWidthHeightOptions(Resources resources,int resId){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, opts);
        return opts;
    }


    private static int calcSample(BitmapFactory.Options opts, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        int width = opts.outWidth;
        int height = opts.outHeight;
//        Log.d(TAG, "图片宽度为：" + width);
//        Log.d(TAG, "图片高度为：" + height);

        if (width > reqWidth || height > reqHeight) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
}

