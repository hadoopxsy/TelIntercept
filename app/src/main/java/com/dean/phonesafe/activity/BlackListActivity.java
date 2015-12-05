package com.dean.phonesafe.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dean.phonesafe.R;
import com.dean.phonesafe.db.HappyDbDao;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/11/22.
 */
public class BlackListActivity extends CounterActivity {
    @Bind(R.id.lv_black_list)
    ListView mLvBlackList;
    @Bind(R.id.ll_no_record)
    LinearLayout mLlNoRecord;
    private HappyDbDao mHappyDbDao;
    private MyAdapter mAdapter;
    private List<String> mNumberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHappyDbDao = HappyDbDao.getInstance(this);
        mNumberList = mHappyDbDao.getAllBlackListNumber();
        //没有黑名单号码时显示无记录提示语
        mLlNoRecord.setVisibility(mNumberList.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        mAdapter = new MyAdapter(this, R.layout.item_black_list, mNumberList);
        mLvBlackList.setAdapter(mAdapter);
    }

    //添加白名单
    @OnClick({R.id.iv_add,R.id.bt_add_number})
    public void clickAdd(View view) {
        Intent intent = new Intent(this, AddBlackListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }

    //后退
    @OnClick(R.id.iv_back)
    public void clickBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.prev_in, R.anim.prev_out);
    }

    class MyAdapter extends ArrayAdapter<String> {
        private int mResId;

        public MyAdapter(Context context, int resource, List<String> objects) {
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
            final String number_exp = getItem(position);
            viewHolder.mTvNumberExp.setText(number_exp);
            viewHolder.mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("删除").setMessage("您确定要删除 " + number_exp + " 吗？").setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除一个黑名单号码表达式，并且更新界面
                            if (mHappyDbDao.deleteBlackListNumber(number_exp)) {
                                mNumberList.remove(number_exp);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }).setNegativeButton("放弃", null);
                    builder.show();
                }
            });
            return convertView;
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_black_list.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.tv_number_exp)
        TextView mTvNumberExp;
        @Bind(R.id.iv_delete)
        ImageView mIvDelete;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
