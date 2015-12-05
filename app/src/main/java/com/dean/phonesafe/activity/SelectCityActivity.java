package com.dean.phonesafe.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.dean.phonesafe.R;
import com.dean.phonesafe.db.DataDao;
import com.dean.phonesafe.db.HappyDbDao;
import com.dean.phonesafe.utils.AreaNameUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 选择城市窗口
 */
public class SelectCityActivity extends CounterActivity {
    @Bind(R.id.lv_city)
    ListView mLvCity;
    @Bind(R.id.tv_ok)
    TextView mTvOk;
    private String mProvince;
    private MyAdapter mAdapter;
    private List<City> mCityList;
    private HappyDbDao mHappyDbDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        ButterKnife.bind(this);
        //获取用户选中的省份
        mProvince = getIntent().getStringExtra("province");
        //查询省份下的所有城市
        DataDao dataDao = DataDao.getInstance(this);
        //数据库查询出来的城市（城市名为全称）列表
        List<String> cities = dataDao.getCities(mProvince);
        //包括是否选中状态的城市列表
        mCityList = new ArrayList<>();
        mHappyDbDao = HappyDbDao.getInstance(this);
        for (String c : cities) {
            City city = new City();
            city.cityName = c;
            //判断数据库是否已经存在该城市，存在则显示勾选状态
            city.db_checked = mHappyDbDao.existsArea(AreaNameUtil.getAvailAreaName(mProvince, city.cityName));
            //该城市当前的选中状态
            city.cur_checked = city.db_checked;
            mCityList.add(city);
        }
        mAdapter = new MyAdapter(this, R.layout.item_select_city, mCityList);
        mLvCity.setAdapter(mAdapter);
        mLvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = (City) mLvCity.getAdapter().getItem(position);
                city.cur_checked = !city.cur_checked;
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                viewHolder.mCbChecked.setChecked(city.cur_checked);
                mTvOk.setEnabled(true);
                mTvOk.setTextColor(0xff33a3dc);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    //点击完成，将选中的城市保存到数据库
    @OnClick(R.id.tv_ok)
    public void clickOK(View view) {
        for (City city : mCityList) {
            if (city.cur_checked == city.db_checked)
                continue;//用户对该城市没有操作过
            if (city.db_checked && !city.cur_checked) {
                //删除这条记录
                mHappyDbDao.deleteAreaName(AreaNameUtil.getAvailAreaName(mProvince, city.cityName));
            } else if (!city.db_checked && city.cur_checked) {
                //增加这条记录
                mHappyDbDao.addGrantArea(AreaNameUtil.getAvailAreaName(mProvince, city.cityName));
                //友盟统计添加白名单城市
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("city", mProvince + " " + city.cityName);
                MobclickAgent.onEvent(this, "whiteListCity", map);
            }
        }
        onBackPressed();
    }

    class MyAdapter extends ArrayAdapter<City> {
        private int mResId;

        public MyAdapter(Context context, int resource, List<City> objects) {
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
            City city = getItem(position);
            viewHolder.mTvCity.setText(city.cityName);
            viewHolder.mCbChecked.setChecked(city.cur_checked);
            return convertView;
        }
    }

    //动画形式关闭当前窗口
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.prev_in, R.anim.prev_out);
    }

    //点击取消=返回操作
    @OnClick(R.id.tv_cancel)
    public void clickCancel(View view) {
        onBackPressed();
    }


    class City {
        public String cityName;
        /**
         * 指示数据库是否存在这个城市名
         */
        public boolean db_checked;
        /**
         * 指示用户是否选中了它
         */
        public boolean cur_checked;
    }

    class ViewHolder {
        @Bind(R.id.tv_city)
        TextView mTvCity;
        @Bind(R.id.cb_checked)
        CheckBox mCbChecked;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
