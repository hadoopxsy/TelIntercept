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
import com.dean.phonesafe.db.DataDao;
import com.dean.phonesafe.db.HappyDbDao;
import com.dean.phonesafe.utils.AreaNameUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class WhiteListActivity extends CounterActivity {

    @Bind(R.id.lv_white_list)
    ListView mLvWhiteList;
    @Bind(R.id.ll_no_record)
    LinearLayout mLlNoRecord;

    private List<String> mAreaNameList;
    private MyAdapter mAdapter;
    private HappyDbDao mHappyDbDao;
    private DataDao mDataDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_list);
        ButterKnife.bind(this);
        mHappyDbDao = HappyDbDao.getInstance(this);
        mDataDao = DataDao.getInstance(this);
    }

    class MyAdapter extends ArrayAdapter<String> {

        private int mResId;

        public MyAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mResId = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), mResId, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final String areaName = getItem(position);
            //点击删除
            viewHolder.mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String availAreaName = AreaNameUtil.getAvailAreaName(areaName);
                    AlertDialog.Builder builder = new AlertDialog.Builder(WhiteListActivity.this);
                    builder.setTitle("删除").setMessage("确定要删除 " + areaName + " 吗？").setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAreaNameList.remove(areaName);
                            //从数据库中删除城市
                            mHappyDbDao.deleteAreaName(AreaNameUtil.getAvailAreaName(areaName));
                            mAdapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("取消", null).show();
                }
            });
            viewHolder.mTvAreaName.setText(areaName);
            return convertView;
        }
    }

    //添加白名单
    @OnClick({R.id.iv_add,R.id.bt_add_city})
    public void clickAdd(View view) {
        Intent intent = new Intent(WhiteListActivity.this, SelectProvinceActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.next_in, R.anim.next_out);
    }

    //重新可见时刷新白名单城市列表
    @Override
    protected void onStart() {
        super.onStart();
        //不完整的临时城市名列表
        List<String> tempList = mHappyDbDao.getAllGrantAreaName();
        //根据不完整的城市名查询Data数据库获取完整的城市名列表供用户查看
        mAreaNameList = new ArrayList<>();
        for (String s : tempList) {
            //通过一个不完整的城市名得到一个完整的城市名
            mAreaNameList.add(mDataDao.getTotalAreaName(s));
        }
        //无记录时显示提示语
        mLlNoRecord.setVisibility(mAreaNameList.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        mAdapter = new MyAdapter(this, R.layout.item_white_list, mAreaNameList);
        mLvWhiteList.setAdapter(mAdapter);
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

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_white_list.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.tv_area_name)
        TextView mTvAreaName;
        @Bind(R.id.iv_delete)
        ImageView mIvDelete;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
