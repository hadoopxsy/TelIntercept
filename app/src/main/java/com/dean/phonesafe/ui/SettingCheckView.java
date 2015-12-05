package com.dean.phonesafe.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dean.phonesafe.R;


/**
 * Created by Administrator on 2015/10/12.
 */
public class SettingCheckView extends LinearLayout implements SlideSwitch.SlideListener {

    private OnCheckedChangeListener mOnCheckedChangeListener;
    private SlideSwitch mSlideSwitch;

    public SettingCheckView(Context context) {
        this(context, null);
    }

    public SettingCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ui_settingcheckview, this);
        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingCheckView);
        //读取布局配置中的标题设置
        String title = a.getString(R.styleable.SettingCheckView_title);
        //读取布局配置默认显示分隔线
        boolean allowDivider = a.getBoolean(R.styleable.SettingCheckView_allowDivider, true);
        boolean isOpen = a.getBoolean(R.styleable.SettingCheckView_isOpen, false);
        String desc = a.getString(R.styleable.SettingCheckView_desc);

        TextView tvTitle = (TextView) findViewById(R.id.tv_ui_settingcheckview_title);
        ImageView ivDivider = (ImageView) findViewById(R.id.iv_ui_settingcheckview_divider);
        TextView tvDesc = (TextView) findViewById(R.id.tv_ui_settingcheckview_desc);
        //用户没有填写描述时将描述框彻底隐藏，标题会自动居中
        if (TextUtils.isEmpty(desc)) {
            tvDesc.setVisibility(GONE);
        } else {
            tvDesc.setText(desc);
        }
        mSlideSwitch = (SlideSwitch) findViewById(R.id.ss_switch);
        mSlideSwitch.setSlideListener(this);
        mSlideSwitch.setState(isOpen);

        //设置标题
        tvTitle.setText(title);
        //控件布局设置中指定不显示分隔线
        if (!allowDivider)
            ivDivider.setVisibility(View.GONE);
        a.recycle();
    }

    public void setDescription(String text){
        TextView tvDesc = (TextView) findViewById(R.id.tv_ui_settingcheckview_desc);
        tvDesc.setText(text);
    }

    /**
     * 该选项是否已经被选中
     *
     * @return
     */
    public boolean isChecked() {
        return mSlideSwitch.getState();
    }


    /**
     * 设置选项为选中状态
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        mSlideSwitch.setState(checked);
    }

    /**
     * 设置选中后的回调方法
     *
     * @param listener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public void open() {
        if (mOnCheckedChangeListener != null)
            mOnCheckedChangeListener.onCheckedChanged(this, true);
    }

    @Override
    public void close() {
        if (mOnCheckedChangeListener != null)
            mOnCheckedChangeListener.onCheckedChanged(this, false);
    }

    //选项被点击后更改复选框状态，同时引发选中状态被更改事件
    //    @Override
    //    public void onClick(View v) {
    //        boolean db_checked=mSlideBar.getIsOpened();
    //        db_checked=!db_checked;
    //        mSlideBar.setIsOpened(db_checked);
    //        if (mOnCheckedChangeListener != null)
    //            mOnCheckedChangeListener.onCheckedChanged(this, db_checked);
    //    }
    //    @Override
    //    public void onClick(View v) {
    //        boolean db_checked = isChecked();
    //        db_checked = !db_checked;
    //        mCbStatus.setChecked(db_checked);
    //        if (mOnCheckedChangeListener != null)
    //            mOnCheckedChangeListener.onCheckedChanged(this, db_checked);
    //    }

    /**
     * 选中状态更改事件定义
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(SettingCheckView view, boolean isChecked);
    }
}
