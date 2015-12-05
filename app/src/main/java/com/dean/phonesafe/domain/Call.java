package com.dean.phonesafe.domain;

import android.text.format.DateUtils;

/**
 * Created by Administrator on 2015/11/11.
 */
public class Call {
    private int id;


    private String areaName;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }


    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    private String number;
    private long date;

    public String getRelativeTimeSpanString() {
        return DateUtils.getRelativeTimeSpanString(date, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
    }
}
