package com.dean.phonesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dean.phonesafe.service.TelService;

/**
 * Created by Administrator on 2015/12/11.
 */
public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        //DebugLog.d("时间片刻已到，重新开启服务");
        Intent i = new Intent(context, TelService.class);
        context.startService(i);
    }
}
