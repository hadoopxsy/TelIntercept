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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/10/21.
 */
public class SettingArrowView extends LinearLayout {

    @Bind(R.id.tv_ui_settingchangeview_title)
    TextView mTvUiSettingchangeviewTitle;
    @Bind(R.id.iv_ui_settingview_divider)
    ImageView mIvUiSettingviewDivider;
    @Bind(R.id.tv_ui_settingchangeview_desc)
    TextView mTvUiSettingchangeviewDesc;
    @Bind(R.id.iv_arrow)
    ImageView mIvArrow;

    public SettingArrowView(Context context) {
        this(context, null);
    }

    public SettingArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ui_settingarrowview, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);
        //获取自定义属性信息
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingArrowView);
        //设置界面的标题
        String title = a.getString(R.styleable.SettingArrowView_title);
        //是否有分隔线
        boolean allowDivider = a.getBoolean(R.styleable.SettingArrowView_allowDivider, true);
        //标题下面的描述文字
        String desc = a.getString(R.styleable.SettingArrowView_desc);
        if (TextUtils.isEmpty(desc)) {
            //没有描述内容则隐藏描述文本框，使标题框自动居中显示
            mTvUiSettingchangeviewDesc.setVisibility(GONE);
        } else {
            mTvUiSettingchangeviewDesc.setText(desc);
        }
        //设置标题
        mTvUiSettingchangeviewTitle.setText(title);
        boolean hasArrow = a.getBoolean(R.styleable.SettingArrowView_hasArrow, true);
        if (!hasArrow) {
            //不显示箭头
            mIvArrow.setVisibility(INVISIBLE);
        }

        //获取自定义布局视图
        if (!allowDivider) {
            //隐藏分隔条
            mIvUiSettingviewDivider.setVisibility(GONE);
        }
        a.recycle();
    }

    /**
     * 设置标题文本
     * @param text
     */
    public void setTitle(String text){
        mTvUiSettingchangeviewTitle.setText(text);
    }
}
