package com.dean.phonesafe.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Created by Administrator on 2015/11/29.
 */
public class MobileUtil {
    /**
     * 未知提供商电话号码
     */
    public final static int PROVIDER_UNKNOWN = 0;
    /**
     * 中国电信电话号码
     */
    public final static int PROVIDER_CHINATELECOM = 1;
    /**
     * 中国移动电话号码
     */
    public final static int PROVIDER_CHINAMOBILE = 2;
    /**
     * 中国联通电话号码
     */
    public final static int PROVIDER_CHINAUNICOM = 3;

    /**
     * 获取当前手机号码运营商
     * @param context
     * @return
     */
    public static int getServiceProvider(Context context) {
        int provider = PROVIDER_UNKNOWN;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (!TextUtils.isEmpty(imsi)) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                provider = PROVIDER_CHINAMOBILE;
            } else if (imsi.startsWith("46001")) {
                //中国联通
                provider = PROVIDER_CHINAUNICOM;
            } else if (imsi.startsWith("46003")) {
                //中国电信
                provider = PROVIDER_CHINATELECOM;
            }
        }
        String operator = tm.getSimOperator();
        if (provider == PROVIDER_UNKNOWN && !TextUtils.isEmpty(operator)) {
            if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
                //中国移动
                provider = PROVIDER_CHINAMOBILE;
            } else if (operator.equals("46001")) {
                //中国联通
                provider = PROVIDER_CHINAUNICOM;
            } else if (operator.equals("46003")) {
                //中国电信
                provider = PROVIDER_CHINATELECOM;
            }
        }
        return provider;
    }


}
