package com.dean.phonesafe.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dean.phonesafe.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/11/27.
 */
public class TitleDescView extends LinearLayout {

    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.tv_desc)
    TextView mTvDesc;

    public TitleDescView(Context context) {
        this(context, null);
    }

    public TitleDescView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        View.inflate(context, R.layout.ui_title_desc, this);
        ButterKnife.bind(this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleDescView);
        String title=a.getString(R.styleable.TitleDescView_title);
        String desc=a.getString(R.styleable.TitleDescView_desc);
        mTvTitle.setText(title);
        mTvDesc.setText(desc);
    }

    /**
     * 设置左侧的标题文本
     * @param text
     */
    public void setTitle(String text){
        mTvTitle.setText(text);
    }

    /**
     * 设置右侧的描述文本
     * @param text
     */
    public void setDesc(String text){
        mTvDesc.setText(text);
    }
}
