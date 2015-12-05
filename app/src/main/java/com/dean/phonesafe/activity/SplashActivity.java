package com.dean.phonesafe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.dean.phonesafe.BuildConfig;
import com.dean.phonesafe.R;
import com.dean.phonesafe.db.HappyDbDao;
import com.umeng.onlineconfig.OnlineConfigAgent;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SplashView;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/11/23.
 */
public class SplashActivity extends CounterActivity {
    SplashView mSplashView;
    Context mContext;
    View mSplash;
    RelativeLayout mSplashLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        SQLiteDatabase.loadLibs(this);
        mContext =this;
        //复制文件
        initFile();
        //显示开屏广告
        showSplashAds();
        OnlineConfigAgent.getInstance().updateOnlineConfig(this);
    }

    /**
     * 显示开屏广告
     */
    private void showSplashAds() {
        AdManager.getInstance(this).init(getString(R.string.youmi_appId), getString(R.string.youmi_appSecret), BuildConfig.DEBUG);
        // 第二个参数传入目标activity，或者传入null，改为setIntent传入跳转的intent
        mSplashView = new SplashView(mContext, null);
        // 设置是否显示倒数
        mSplashView.setShowReciprocal(true);
        // 隐藏关闭按钮
        mSplashView.hideCloseBtn(true);

        Intent intent = new Intent(mContext, CallsActivity.class);
        mSplashView.setIntent(intent);
        mSplashView.setIsJumpTargetWhenFail(true);

        mSplash = mSplashView.getSplashView();
        setContentView(R.layout.activity_splash);
        mSplashLayout = ((RelativeLayout) findViewById(R.id.splashview));
        mSplashLayout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
        params.addRule(RelativeLayout.ABOVE, R.id.cutline);
        mSplashLayout.addView(mSplash, params);

        SpotManager.getInstance(mContext).showSplashSpotAds(mContext, mSplashView,
                new SpotDialogListener() {

                    @Override
                    public void onShowSuccess() {
                        mSplashLayout.setVisibility(View.VISIBLE);
                        mSplashLayout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.pic_enter_anim_alpha));
//                        DebugLog.d("展示成功");
                    }

                    @Override
                    public void onShowFailed() {
//                        DebugLog.d("展示失败");
                    }

                    @Override
                    public void onSpotClosed() {
//                        DebugLog.d("展示关闭");
                    }

                    @Override
                    public void onSpotClick(boolean isWebPath) {
//                       DebugLog.d("插屏点击");
                    }
                });
    }

    private void initFile() {
        //复制文件到getFileDir()目录中
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                //创建数据库
                HappyDbDao.getInstance(SplashActivity.this);
                for (String fileName : params) {
                    File file = new File(getFilesDir(), fileName);
                    if (!file.exists()) {
                        copyFile(fileName);
                    }
                }
                return null;
            }
        }.execute("address.db", "data.db");
    }

    /**
     * 将资产目录中的fileName文件写到getFileDir()目录
     *
     * @param fileName
     */
    private void copyFile(final String fileName) {
        try {
            InputStream inputStream = getAssets().open(fileName);
            OutputStream outputStream = new FileOutputStream(new File(getFilesDir(), fileName));
            int len;
            byte[] buffer = new byte[1024 * 4];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
