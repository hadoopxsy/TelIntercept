package com.dean.phonesafe.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.dean.phonesafe.R;
import com.dean.phonesafe.receiver.AlarmReceiver;
import com.dean.phonesafe.service.TelService;
import com.dean.phonesafe.ui.SettingCheckView;
import com.dean.phonesafe.utils.MobileUtil;
import com.dean.phonesafe.utils.ServiceUtil;
import com.umeng.onlineconfig.OnlineConfigAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends CounterActivity implements SettingCheckView.OnCheckedChangeListener {
    
    
    @Bind(R.id.scv_enable_tel_service)
    SettingCheckView mScvEnableTelService;
    @Bind(R.id.scv_allow_contacts)
    SettingCheckView mScvAllowContacts;
    @Bind(R.id.scv_show_notify)
    SettingCheckView mScvShowNotify;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        //如果服务已经在运行，则显示停止拦截，否则显示开启拦截
        boolean serviceRunning = ServiceUtil.existsService(SettingActivity.this, TelService.class);
        mScvEnableTelService.setChecked(serviceRunning);
        mScvEnableTelService.setDescription(serviceRunning ? "拦截模式已开启" : "拦截模式已关闭");
        mScvEnableTelService.setOnCheckedChangeListener(this);
        //允许联系人拨入
        mSharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        mScvAllowContacts.setChecked(mSharedPreferences.getBoolean("allow_contacts", true));
        mScvAllowContacts.setOnCheckedChangeListener(this);

        mScvShowNotify.setChecked(mSharedPreferences.getBoolean("show_notify",true));
        mScvShowNotify.setOnCheckedChangeListener(this);
    }

    //点击标题中的返回
    @OnClick(R.id.iv_back)
    public void clickBack(View view) {
        onBackPressed();
    }

    //按下返回键
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.prev_in, R.anim.prev_out);
    }


    @Override
    public void onCheckedChanged(SettingCheckView view, boolean isChecked) {
        SharedPreferences.Editor edit=null;
        switch (view.getId()) {
            case R.id.scv_enable_tel_service:
                Intent intent = new Intent(SettingActivity.this, TelService.class);
                //根据用户设置的勾选状态来开启/停止拦截服务
                if (isChecked)
                    startService(intent);
                else {
                    stopService(intent);
                    //取消定时器
                    AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.cancel(PendingIntent.getBroadcast(this,0,new Intent(this, AlarmReceiver.class),0));
                }
                view.setDescription(isChecked ? "拦截模式已开启" : "拦截模式已关闭");
                break;
            case R.id.scv_allow_contacts:
                //保存是否允许联系人拨入
                edit = mSharedPreferences.edit();
                edit.putBoolean("allow_contacts", isChecked);
                edit.commit();
                break;
            case R.id.scv_show_notify:
                //显示通知栏
                edit = mSharedPreferences.edit();
                edit.putBoolean("show_notify", isChecked);
                edit.commit();
                break;
        }
    }


    //点击白名单
    @OnClick(R.id.sav_setting_white_list)
    public void clickSettingWhiteList(View view) {
        Intent intent = new Intent(this, WhiteListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }

    //点击黑名单
    @OnClick(R.id.sav_setting_black_list)
    public void clickSettingBlackList(View view) {
        Intent intent = new Intent(this, BlackListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }

    //点击关于
    @OnClick(R.id.sav_setting_about)
    public void clickSettingAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }

    @OnClick(R.id.sav_setting_set)
    public void clickSettingSet(View view) {
        //读取上次设置的挂断提示音选项
        final int last_tip_audio = mSharedPreferences.getInt("last_tip_audio", -1);
        //弹出单选对话框选择挂断提示音
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final DialogClickListener listener = new DialogClickListener(last_tip_audio);
        builder.setTitle("请选择提示音").setSingleChoiceItems(new String[]{"直接挂断", "提示停机", "提示空号", "提示关机", "提示号码有误"}, last_tip_audio, listener).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                which = listener.mWhich;
                if (which == last_tip_audio) {
                    //当前选中项与之前设置的没有变化，直接关闭对话框
                    return;
                }
                String tingji_number = OnlineConfigAgent.getInstance().getConfigParams(SettingActivity.this, "tingji_number");
                String guanji_number = OnlineConfigAgent.getInstance().getConfigParams(SettingActivity.this, "guanji_number");
                String konghao_number = OnlineConfigAgent.getInstance().getConfigParams(SettingActivity.this, "konghao_number");
                Intent intent = new Intent(Intent.ACTION_CALL);
                int provider = MobileUtil.getServiceProvider(SettingActivity.this);
                String telephone = "";
                Uri uri;
                //#=%23
                //*=%2A
                switch (which) {
                    case 0:
                        //直接挂断，取消来电转移
                        if (provider == MobileUtil.PROVIDER_CHINATELECOM)
                            telephone = "%2A900";
                        else
                            telephone = "%23%2367%23";
                        break;
                    case 1:
                        //停机
                        if (provider == MobileUtil.PROVIDER_CHINATELECOM)
                            telephone = "%2A90" + tingji_number;
                        else
                            telephone = "%2A%2A67%2A" + tingji_number + "%23";
                        break;
                    case 2:
                        //空号
                        if (provider == MobileUtil.PROVIDER_CHINATELECOM)
                            telephone = "%2A90" + konghao_number;
                        else
                            telephone = "%2A%2A67%2A" + konghao_number + "%23";
                        break;
                    case 3:
                        //关机
                        if (provider == MobileUtil.PROVIDER_CHINATELECOM)
                            telephone = "%2A90" + guanji_number;
                        else
                            telephone = "%2A%2A67%2A" + guanji_number + "%23";
                        break;
                    case 4:
                        //号码有误
                        if (provider == MobileUtil.PROVIDER_CHINATELECOM)
                            telephone = "%2A901380000000";
                        else
                            telephone = "%2A%2A67%2A1380000000%23";
                        break;
                }
                uri = Uri.parse("tel:" + telephone);
                intent.setData(uri);
                startActivity(intent);
                //保存挂断提示音选项
                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putInt("last_tip_audio", which);
                edit.commit();
            }
        }).setNegativeButton("取消", null).show();
    }

    //单选对话框点击事件
    class DialogClickListener implements DialogInterface.OnClickListener {
        /**
         * 初始化单选对话框点击事件
         *
         * @param which 最后选中的项
         */
        public DialogClickListener(int which) {
            mWhich = which;
        }

        /**
         * 保存最后选中的项
         */
        private int mWhich;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //保存当前选中的索引
            mWhich = which;
        }
    }
}
