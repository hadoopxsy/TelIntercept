package com.dean.phonesafe.activity;

import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;


/**
 * Created by Administrator on 2015/11/27.
 */
public class CounterActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
