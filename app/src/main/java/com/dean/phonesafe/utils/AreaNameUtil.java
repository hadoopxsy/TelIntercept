package com.dean.phonesafe.utils;

import android.text.TextUtils;

/**
 * Created by Administrator on 2015/11/11.
 */
public class AreaNameUtil {
    /**
     * 传入以空格间隔的省份名+城市名，返回如“湖南 湘潭”的5个字符长度的字符串
     *
     * @param areaName
     * @return 返回包含空格在内的5个长度的字符串，如：湖南 湘潭，否则返回""
     */
    public static String getAvailAreaName(String areaName) {
        if (TextUtils.isEmpty(areaName)) {
            return "";
        }
        String[] arrArea = areaName.split(" ");
        String resultStr = areaName;
        if (arrArea.length == 2) {
            if (arrArea.length == 2) {
                String province = arrArea[0];
                String city = arrArea[1];
                //只保存城市名的前2个字
                if (city.length() > 2)
                    city = city.substring(0, 2);
                //只保存省份名的前2个字
                if (province.length() > 2)
                    province = province.substring(0, 2);
                resultStr = province + " " + city;
            }
        }
        return resultStr;
    }



    /**
     * 通过传入的省份和城市名，生成一个有效的城市名称
     * @param province
     * @param city
     * @return
     */
    public static String getAvailAreaName(String province,String city){
        return getAvailAreaName(province+" "+city);
    }

    /**
     * 比较2个地区名是否相等
     * @param areaName1
     * @param areaName2
     * @return
     */
    public static boolean equals(String areaName1,String areaName2){
        return getAvailAreaName(areaName1).equals(getAvailAreaName(areaName1));
    }
}
