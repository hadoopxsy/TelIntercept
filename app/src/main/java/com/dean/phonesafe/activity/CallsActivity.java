package com.dean.phonesafe.activity;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.dean.phonesafe.R;
import com.dean.phonesafe.db.HappyDbDao;
import com.dean.phonesafe.domain.Call;
import com.dean.phonesafe.service.TelService;
import com.dean.phonesafe.utils.ContactUtil;
import com.dean.phonesafe.utils.IntentUtil;
import com.dean.phonesafe.utils.ServiceUtil;
import com.dean.phonesafe.utils.ToastUtil;
import com.umeng.update.UmengUpdateAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
 让您远离诈骗广告传销电话
 本程序可以帮您拦截除白名单之外的城市来电，您可以将自己的城市以及亲朋好友的城市加入到白名单中，也可以将他们的手机号码加入到联系人中，以后只要不在白名单中的城市，或者不在联系人中的号码打来电话时可以实现自动拦截电话功能，您可以通过拦截记录看到被拦截的电话。该应用是绿色环保软件，不会开机自启，但需要读取联系人（允许联系人来电需要）、修改/写入联系人（删除已拦截的来电记录需要）、拨打电话权限（挂断电话需要打电话权限）、访问网络（查询版本更新需要）等以及其它权限（投放开屏广告时需要），使用本款程序时您需要开启拦截功能后拦截才会生效，最后希望您远离骚扰电话
 */
public class CallsActivity extends CounterActivity {
    @Bind(R.id.lv_calls)
    ListView mLvCalls;
    @Bind(R.id.ll_no_record)
    LinearLayout mLlNoRecord;
    @Bind(R.id.tv_no_record)
    TextView mTvNoRecord;
    @Bind(R.id.bt_setting)
    Button mBtSetting;
    private MyAdapter mAdapter;
    private List<Call> mCallList;
    private HappyDbDao mHappyDbDao;
    private long[] mHits;
    private ContactContentObserver mContactContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls);
        ButterKnife.bind(this);
        UmengUpdateAgent.update(this);
        mHappyDbDao = HappyDbDao.getInstance(this);
        //用于双击后退键退出程序
        mHits = new long[2];
        //初始化电话联系人观察者，白名单表变更时刷新窗口，窗口可见时注册观察者，窗口不可见时注销观察者
        mContactContentObserver = new ContactContentObserver(new Handler());
        mCallList = new ArrayList<>();
        mAdapter = new MyAdapter(this, R.layout.item_calls, mCallList);
        mLvCalls.setAdapter(mAdapter);
        mLvCalls.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                //显示弹出菜单
                openPopupMenu(view, position);
                return true;
            }
        });
    }

    //长按拦截的号码项时显示弹出菜单
    private void openPopupMenu(View view, final int position) {
        //弹出复制菜单
        PopupMenu popup = new PopupMenu(CallsActivity.this, view);
        //默认情况下不会显示菜单左侧的图标，下面利用反射机制显示快捷菜单左侧的图标
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object mHelper = field.get(popup);
            Method setForceShowIcon = mHelper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
            setForceShowIcon.invoke(mHelper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.calls_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = null;
                Call call = mCallList.get(position);
                switch (item.getItemId()) {
                    case R.id.menu_copy:
                        //复制号码到剪切板
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(call.getNumber());
                        ToastUtil.show(CallsActivity.this,"号码已复制");
                        break;
                    case R.id.menu_call:
                        //呼叫拦截的号码
                        intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + call.getNumber()));
                        startActivity(intent);
                        break;
                    case R.id.menu_contact:
                        if(ContactUtil.existsNumber(getApplicationContext(),call.getNumber())){
                            ToastUtil.show(CallsActivity.this,"该号码已经在联系人中");
                        }else {
                            //添加这个号码为联系人
                            intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            intent.putExtra(ContactsContract.Intents.Insert.PHONE, call.getNumber());

                            if (!IntentUtil.isIntentAvailable(getApplicationContext(), intent)) {
                                ToastUtil.show(CallsActivity.this, "无法添加联系人");
                            } else {
                                startActivity(intent);
                            }
                        }
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    //按菜单键跳到设置界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            clickSetting(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fillData();
        //用户停留在这个窗口时正有电话被拦截，需要更新列表，当窗口不可见时注销
        getContentResolver().registerContentObserver(HappyDbDao.CONTENT_URI_ADD_CALL, false, mContactContentObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //这个窗口不可见时，没有必要同步白名单表
        getContentResolver().unregisterContentObserver(mContactContentObserver);
    }

    class ContactContentObserver extends ContentObserver {

        /**
         * onChange() will happen on the provider Handler.
         *
         * @param handler The handler to run {@link #onChange} on.
         */
        public ContactContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            fillData();
        }
    }


    /**
     * 刷新拦截记录
     */
    private void fillData() {
        new AsyncTask<Void, Void, List<Call>>() {
            private ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                mProgressDialog = new ProgressDialog(CallsActivity.this);
                mProgressDialog.setMessage("努力加载中...");
                mProgressDialog.show();
            }

            @Override
            protected List<Call> doInBackground(Void... params) {
                //查询历史拦截记录
                mCallList.clear();
                mCallList.addAll(mHappyDbDao.getAllCall());
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                return mCallList;
            }

            @Override
            protected void onPostExecute(List<Call> calls) {
                //没有拦截记录时显示无记录提示
                mLlNoRecord.setVisibility(calls.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                if (calls.size() == 0) {
                    if (!ServiceUtil.existsService(CallsActivity.this, TelService.class)) {
                        //没有开启服务时提示“马上设置”
                        mTvNoRecord.setText("暂无拦截记录\n需要在设置中开启来电拦截");
                        mBtSetting.setVisibility(View.VISIBLE);
                    } else {
                        mTvNoRecord.setText("暂无拦截记录");
                        mBtSetting.setVisibility(View.INVISIBLE);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    class MyAdapter extends ArrayAdapter<Call> {

        private int mResId;

        public MyAdapter(Context context, int resource, List<Call> objects) {
            super(context, resource, objects);
            mResId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), mResId, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final Call call = getItem(position);
            viewHolder.mTvNumber.setText(call.getNumber());
            viewHolder.mTvDate.setText(call.getRelativeTimeSpanString());
            viewHolder.mTvAreaName.setText(call.getAreaName());
            viewHolder.mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CallsActivity.this);
                    builder.setTitle("删除").setMessage("确定要删除 " + call.getNumber() + " 吗？").setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCallList.remove(call);
                            //从数据库中删除城市
                            mHappyDbDao.deleteCall(call.getId());
                            mAdapter.notifyDataSetChanged();
                            //拦截记录为0时显示暂无拦截记录提示
                            mLlNoRecord.setVisibility(mCallList.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                        }
                    }).setNegativeButton("取消", null).show();
                }
            });
            return convertView;
        }
    }

    //打开设置窗口
    @OnClick({R.id.iv_setting, R.id.bt_setting})
    public void clickSetting(View view) {
        Intent intent = new Intent(CallsActivity.this, SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }


    @Override
    public void onBackPressed() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {
            super.onBackPressed();
        } else {
//            if (mToast == null) {
//                mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
//            }
//            mToast.setText("再按一次退出程序");
//            mToast.show();
            ToastUtil.show(this,"再按一次退出程序");
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_calls.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.tv_number)
        TextView mTvNumber;
        @Bind(R.id.tv_area_name)
        TextView mTvAreaName;
        @Bind(R.id.iv_delete)
        ImageView mIvDelete;
        @Bind(R.id.tv_date)
        TextView mTvDate;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
