package com.dean.phonesafe.activity;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dean.phonesafe.R;
import com.dean.phonesafe.db.HappyDbDao;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


/**
 * Created by Administrator on 2015/11/22.
 */
public class AddBlackListActivity extends CounterActivity {
    @Bind(R.id.tv_ok)
    TextView mTvOk;
    @Bind(R.id.et_number)
    EditText mEtNumber;
    private HappyDbDao mHappyDbDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_black_list);
        ButterKnife.bind(this);
        mHappyDbDao = HappyDbDao.getInstance(this);
        //添加一个自定义输入字符的过滤器
        mEtNumber.setFilters(new InputFilter[]{new NumberFilter()});
        //编辑框中按回车相当于点击完成按钮
        mEtNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mTvOk.isEnabled()) {
                    if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        clickOK(null);
                        return true;
                    }
                }
                return false;
            }
        });
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


    //编辑号码时将完成按钮设置为可点击状态
    @OnTextChanged(R.id.et_number)
    public void changedNumber(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(s)) {
            //号码不为空将完成按钮设置为可点击状态
            mTvOk.setEnabled(true);
            mTvOk.setTextColor(0xff33a3dc);
        } else {
            //号码为空将完成按钮设置为不可点击状态
            mTvOk.setEnabled(false);
            mTvOk.setTextColor(0x5533a3dc);
        }
    }


    //点击完成保存号码规则到数据库
    @OnClick(R.id.tv_ok)
    public void clickOK(View view) {
        //友盟统计添加黑名单号码
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("number", mEtNumber.getText().toString());
        MobclickAgent.onEvent(this, "blackListNumber", map);
        mHappyDbDao.addBlackListNumber(mEtNumber.getText().toString());
        onBackPressed();
    }

    //号码编辑框字符过滤器，只允许输入0~9*？?+字符
    class NumberFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (dest.length() > 0 && dest.charAt(0) == '+' && dstart == 0) {
                //+号左边不允许输入任何字符
                return "";
            }
            //去掉新输入的字符串中的非法字符
            String src = source.toString().replaceAll("[^0-9?？\\+\\*]", "");
            //替换掉新输入的字符串中的连续的*号为单*号，将不在开始位置的+号替换成空字符，同时将中文？号替换成英文?号
            src = src.replaceAll("\\*+", "*").replaceAll("(?<!^)\\+", "").replace("？", "?");
            if (src.length() > 0 && dest.length() > 0 && dstart - 1 >= 0 && dstart - 1 < dest.length() && src.charAt(0) == '*' && dest.charAt(dstart - 1) == '*') {
                //如果新字符串中的第一个字符和旧字符串的最后一个字符都是*的话就去掉新字符串中的第一个字符
                src = src.replaceAll("^\\*+", "");
            }
            if (src.length() > 0 && dest.length() > 0 && dstart >= 0 && dstart < dest.length() && src.charAt(src.length() - 1) == '*' && dest.charAt(dstart) == '*') {
                //如果新字符串中的最后一个字符和旧字符串的第一个字符都是*的话就去掉新字符串中的第一个字符
                src = src.replaceAll("\\*+$", "");
            }
            if (src.length() > 0 && src.charAt(0) == '+' && dstart > 0) {
                //新输入的字符串的最左边包含+号
                //新字符串输入的位置在旧字符串的第1个字符右边位置，去掉新字符串中的所有+号
                src = src.replaceAll("\\+", "");
            }
            //DebugLog.d(src);
            //总文本长度不超过20个字符
            if (src.length() > 0 && src.length() + dest.length() > 20) {
                int length = 20 - dest.length();
                if (length > 0 && length <= src.length()) {
                    src = src.substring(0, length);
                } else {
                    src = "";
                }
            }
            return src;
        }
    }

}

