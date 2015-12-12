package com.dean.phonesafe.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.android.internal.telephony.ITelephony;
import com.dean.phonesafe.R;
import com.dean.phonesafe.activity.CallsActivity;
import com.dean.phonesafe.db.AddressDao;
import com.dean.phonesafe.db.DataDao;
import com.dean.phonesafe.db.HappyDbDao;
import com.dean.phonesafe.receiver.AlarmReceiver;
import com.dean.phonesafe.utils.AreaNameUtil;
import com.dean.phonesafe.utils.ContactUtil;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/9.
 */
public class TelService extends Service {

    private TelephonyManager mTelephonyManager;
    private TelListener mTelListener;
    private DataDao mDataDao;
    private HappyDbDao mHappyDbDao;
    private AddressDao mAddressDao;
    private SharedPreferences mSharedPreferences;
    private List<ContactUtil.Contact> mContactList;
    private ContactObserver mContactObserver;
    private List<String> mNumberRegexList;
    private BlackListObserver mBlackListObserver;
    private List<String> mWhiteListAreaNameList;
    private WhiteListObserver mWhiteListObserver;
    private CallsContentObserver mCallsContentObserver;
    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        DebugLog.d("onCreate");
        initService();
    }

    //初始化各个监听器，读取联系人
    private void initService() {
        mDataDao = DataDao.getInstance(this);
        mHappyDbDao = HappyDbDao.getInstance(this);
        mAddressDao = AddressDao.getInstance(this);
        //注册来电监听
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelListener = new TelListener();
        mTelephonyManager.listen(mTelListener, PhoneStateListener.LISTEN_CALL_STATE);
        mSharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        //加载联系人和黑名单号码表达式以及白名单城市
        loadData();
        //监视联系人表的变更
        mContactObserver = new ContactObserver(new Handler());
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, false, mContactObserver);
        //监视黑名单表的变更
        mBlackListObserver = new BlackListObserver(new Handler());
        getContentResolver().registerContentObserver(HappyDbDao.CONTENT_URI_BLACK_LIST, false, mBlackListObserver);
        //监视白名单表的变更
        mWhiteListObserver = new WhiteListObserver(new Handler());
        getContentResolver().registerContentObserver(HappyDbDao.CONTENT_URI_WHITE_LIST, false, mWhiteListObserver);
        //监视来电记录的变更
        mCallsContentObserver = new CallsContentObserver(new Handler());
        //友盟启动服务计数
        MobclickAgent.onEvent(this, "startInterceptService");
        //请求权限
        //挂断电话
        requestEndCall();
        //尝试修改通话记录
        getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?", new String[]{""});
        //DebugLog.d("main_id:" + Thread.currentThread().getId());
        //点击通知栏面板广播
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    //将拦截的来电显示到通知栏
    private void showNotify(String number, String areaName) {
        //创建通知栏，只有拦截到来电时才显示它
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notify_custom);
        Intent intent = new Intent(this, CallsActivity.class);
        remoteViews.setImageViewResource(R.id.iv_icon, R.mipmap.ic_launcher);
        //更新通知栏记录
        remoteViews.setTextViewText(R.id.tv_number, number);
        remoteViews.setTextViewText(R.id.tv_area_name, areaName);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date());
        remoteViews.setTextViewText(R.id.tv_date, date);
        builder.setAutoCancel(true).
                setTicker("拦截来电" + number).
                setContent(remoteViews).
                setSmallIcon(R.mipmap.ic_launcher).
                setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
        mNotificationManager.notify(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //下次间隔多少秒开启服务
        //DebugLog.d("设置10秒后启动服务");
        //每隔1分钟启动服务，防止服务被系统杀死
        long triggerAtTime = SystemClock.elapsedRealtime() + 1 * 60 * 1000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadData() {
        //DebugLog.d("loadData线程开始运行");
        //加载黑名单号码
        loadBlackList();
        //加载白名单城市
        loadWhiteList();
        //加载所有联系人
        loadContacts();
    }

    /**
     * 加载白名单城市列表
     */
    private void loadWhiteList() {
        new Thread() {
            @Override
            public void run() {
                //DebugLog.d("加载白名单城市");
                mWhiteListAreaNameList = mHappyDbDao.getAllGrantAreaName();
            }
        }.start();
    }

    /**
     * 加载黑名单号码
     */
    private void loadBlackList() {
        new Thread() {
            @Override
            public void run() {
                //加载所有黑名单号码表达式
                List<String> tempList = mHappyDbDao.getAllBlackListNumber();
                //保存正则表达式
                mNumberRegexList = new ArrayList<>();
                for (String s : tempList) {
                    s = s.replace("*", "\\d+");//*解析为\\d*，多个数字
                    s = s.replace("?", "\\d");//?解析为\\d，一个数字
                    s = "^" + s + "$";
                    mNumberRegexList.add(s);
                }
            }
        }.start();
    }

    private void loadContacts() {
        new Thread() {
            @Override
            public void run() {
                //DebugLog.d("加载联系人开始");
                mContactList = ContactUtil.getAllContacts(TelService.this);
                //DebugLog.d("加载联系人结束");
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销来电监听器
        mTelephonyManager.listen(mTelListener, PhoneStateListener.LISTEN_NONE);
        //注销联系人表的观察者
        getContentResolver().unregisterContentObserver(mContactObserver);
        //注销黑名单表的观察者
        getContentResolver().unregisterContentObserver(mBlackListObserver);
        //注销白名单表的观察者
        getContentResolver().unregisterContentObserver(mWhiteListObserver);
//        DebugLog.d("onDestroy");
    }

    //来电回调
    class TelListener extends PhoneStateListener {

        private String mMobile_number_exp;
        private String mTel_number_exp;

        public TelListener() {
            //手机号码正则表达式
            mMobile_number_exp = "^1[34578]\\d{5,9}$";
            //固定电话正则表达式
            mTel_number_exp = "^0\\d{3,}$";
        }

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    //DebugLog.d("current_id:" + Thread.currentThread().getId());
                    //由于该回调方法运行在主线程，所以耗时操作需要另外开启一个线程来执行
                    new Thread() {
                        @Override
                        public void run() {
                            //DebugLog.d("child_id:" + Thread.currentThread().getId());
                            incomingProcess(incomingNumber);
                        }
                    }.start();
                    break;
            }
        }

        //来电拦截主要实现，分析来电号码是否需要挂断
        private void incomingProcess(String incomingNumber) {
            if (mNumberRegexList != null) {
                for (String exp : mNumberRegexList) {
                    if (incomingNumber.matches(exp)) {
                        //黑名单号码挂断电话，添加到拦截记录
                        endCall(incomingNumber, "黑名单号码");
                        return;
                    }
                }
            }
            //判断是否为联系人号码
            boolean allow_contacts = mSharedPreferences.getBoolean("allow_contacts", true);
            if (allow_contacts && mContactList != null && ContactUtil.existsNumber(mContactList, incomingNumber)) {
                //允许联系人的情况下判断是否为联系人
                return;
            }

            boolean endCall = false;
            String simpleCity = "";
            //判断是否在白名单中
            if (incomingNumber.matches(mMobile_number_exp)) {
                //DebugLog.d("查询来电号码 " + incomingNumber + " 的归属地");
                //手机号码来电
                //得到一个address数据库中的归属地，该归属地不统一
                //DebugLog.d("来电号码：" + incomingNumber);
                String addressAreaName = mAddressDao.getAreaNameByMobileNumber(incomingNumber.substring(0, 7));
                simpleCity = AreaNameUtil.getAvailAreaName(addressAreaName);
                //DebugLog.d(mWhiteListAreaNameList == null ? "mWhiteListAreaNameList is null" : "mWhiteListAreaNameList.size():" + mWhiteListAreaNameList.size());
                //白名单城市不包含这个城市就挂断
                if (mWhiteListAreaNameList != null)
                    endCall = !mWhiteListAreaNameList.contains(simpleCity);
                //DebugLog.d("手机号码归属地是：" + addressAreaName);
            } else if (incomingNumber.matches(mTel_number_exp)) {
                //固定电话来电
                //查询固定电话归属地列表，一个区号可能查询出多个城市的情况，所以返回值是一个城市名的List
                List<String> addressAreaNameList = mAddressDao.getAreaNameByTelNumber(incomingNumber);
                //该固定电话的号码是否在白名单城市中
                boolean contain = false;
                for (String s : addressAreaNameList) {
                    simpleCity = AreaNameUtil.getAvailAreaName(s);
                    if (mWhiteListAreaNameList.contains(simpleCity)) {
                        contain = true;
                        break;
                    }
                }
                endCall = !contain;
            }
            if (endCall)
                endCall(incomingNumber, simpleCity);
            else {
                //友盟统计未拦截的号码
                MobclickAgent.onEvent(TelService.this, "dontInterceptIncoming", incomingNumber);
            }
        }


        /**
         * 挂断电话
         *
         * @param incomingNumber    来电号码
         * @param whiteListAreaName 简写城市名，5个字符长度
         */
        private void endCall(final String incomingNumber, final String whiteListAreaName) {
            try {
                //注册通话记录观察者，清除来电号码
                mCallsContentObserver.setIncomingNumber(incomingNumber);
                getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, mCallsContentObserver);
                //DebugLog.d("准备拦截电话");
                requestEndCall();
                //DebugLog.d("电话被拦截");
                //保存拦截记录
                String totalAreaName = mDataDao.getTotalAreaName(whiteListAreaName);
                if (TextUtils.isEmpty(totalAreaName)) {
                    totalAreaName = "未知归属地";
                }
                //DebugLog.d("添加拦截记录：" + incomingNumber + "，" + totalAreaName);
                mHappyDbDao.addCall(incomingNumber, totalAreaName);
                //友盟统计拦截来电次数
                HashMap<String, String> map = new HashMap<>();
                map.put("incomingNumber", incomingNumber);
                map.put("city", totalAreaName);
                MobclickAgent.onEvent(TelService.this, "interceptIncoming", map);
                //判断是否需要显示拦截通知
                boolean showNotify = mSharedPreferences.getBoolean("show_notify", true);
                if (showNotify) {
                    //在通知栏显示拦截通知
                    showNotify(incomingNumber, totalAreaName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //系统来电记录被修改时清除已拦截的来电号码
    class CallsContentObserver extends ContentObserver {

        /**
         * onChange() will happen on the provider Handler.
         *
         * @param handler The handler to run {@link #onChange} on.
         */
        public CallsContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 设置来电号码，该来电号码将从通话记录中删除
         *
         * @param incomingNumber 来电号码
         */
        public void setIncomingNumber(String incomingNumber) {
            mIncomingNumber = incomingNumber;
        }

        private String mIncomingNumber = "";

        @Override
        public void onChange(boolean selfChange) {
            //DebugLog.d("收到通知——通话记录被修改");
            //DebugLog.d("thread_id:" + Thread.currentThread().getId());
            getContentResolver().unregisterContentObserver(this);
            //DebugLog.d("清除通话记录");
            getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?", new String[]{mIncomingNumber});
        }
    }

    /**
     * 请求挂断电话
     */
    private void requestEndCall() {
        try {
            Class cls = getClassLoader().loadClass("android.os.ServiceManager");
            Method method = cls.getDeclaredMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //联系人发生变更时重新读取联系人
    class ContactObserver extends ContentObserver {

        public ContactObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //当联系人表发生改变时重新加载联系人
            //DebugLog.d("联系人被修改，重新加载");
            loadContacts();
        }
    }

    //黑名单号码表发生变更时重新读取
    class BlackListObserver extends ContentObserver {

        public BlackListObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            loadBlackList();
        }
    }

    //白名单号码表发生变更时重新读取
    class WhiteListObserver extends ContentObserver {

        public WhiteListObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            loadWhiteList();
        }

    }


}
/////////////////////////////////////以下代码暂时没有用到////////////////////////////////////////////////////////
/////////////////////////////////////以下代码暂时没有用到////////////////////////////////////////////////////////
/////////////////////////////////////以下代码暂时没有用到////////////////////////////////////////////////////////

/**
 * 请求接通电话
 */
//    private void requestAnswerCall() {
//        try {
//            Class cls = getClassLoader().loadClass("android.os.ServiceManager");
//            Method method = cls.getDeclaredMethod("getService", String.class);
//            IBinder binder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
//            ITelephony telephony = ITelephony.Stub.asInterface(binder);
//            telephony.silenceRinger();//静音
//            telephony.answerRingingCall();//接通电话
//        } catch (Exception e) {
//            e.printStackTrace();
//            //模拟按键接听
//            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
//            sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
//
//            // froyo and beyond trigger on buttonUp instead of buttonDown
//            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
//            sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
//            new Thread(){
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000);
//                        //DebugLog.d("播放音乐");
//                        mediaPlayer.start();
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }.start();
//
//        }
//    }