package com.dean.phonesafe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dean.phonesafe.R;
import com.dean.phonesafe.db.DataDao;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 选择省份窗口
 */
public class SelectProvinceActivity extends CounterActivity {
    @Bind(R.id.lv_province)
    ListView mLvProvince;
    private DataDao mDataDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_province);
        ButterKnife.bind(this);
        mDataDao = DataDao.getInstance(this);
        MyAdapter adapter = new MyAdapter(this, R.layout.item_select_province, mDataDao.getProvinces());
        mLvProvince.setAdapter(adapter);
        mLvProvince.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String province = (String) mLvProvince.getAdapter().getItem(position);
                Intent intent = new Intent(SelectProvinceActivity.this, SelectCityActivity.class);
                intent.putExtra("province", province);
                startActivity(intent);
                overridePendingTransition(R.anim.next_in, R.anim.next_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.prev_in, R.anim.prev_out);
    }

    @OnClick(R.id.iv_back)
    public void clickBack(View view) {
        onBackPressed();
    }

    class MyAdapter extends ArrayAdapter<String> {
        int mResId;

        public MyAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mResId = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), mResId, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTvProvince.setText(getItem(position));
            return convertView;
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'item_select_province.xml'
     * for easy to all layout elements.
     *
     * @author ButterKnifeZelezny, plugin for Android Studio by Avast Developers (http://github.com/avast)
     */
    static class ViewHolder {
        @Bind(R.id.tv_province)
        TextView mTvProvince;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
