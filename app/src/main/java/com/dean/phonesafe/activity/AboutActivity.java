package com.dean.phonesafe.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dean.phonesafe.BuildConfig;
import com.dean.phonesafe.R;
import com.dean.phonesafe.ui.TitleDescView;
import com.dean.phonesafe.utils.ToastUtil;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2015/11/27.
 */
public class AboutActivity extends CounterActivity {

    @Bind(R.id.td_setting_version)
    TitleDescView mTdSettingVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mTdSettingVersion.setDesc(BuildConfig.VERSION_NAME);
    }


    //点击标题中的返回
    @OnClick(R.id.iv_back)
    public void clickBack(View view) {
        onBackPressed();
    }

    //按下返回键
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.prev_in, R.anim.prev_out);
    }

    @OnClick(R.id.sav_setting_update)
    public void clickUpdate(View view) {
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(AboutActivity.this, updateInfo);
                        break;
                    case UpdateStatus.No: // has no update
//                        Toast.makeText(AboutActivity.this, "您已经是最新版本", Toast.LENGTH_SHORT).show();
                        ToastUtil.show(AboutActivity.this,"您已经是最新版本");
                        break;
                    case UpdateStatus.NoneWifi: // none wifi
                        Toast.makeText(AboutActivity.this, "请连接至wifi下再更新", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateStatus.Timeout: // time out
                        Toast.makeText(AboutActivity.this, "获取更新信息超时", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        //手动更新
        UmengUpdateAgent.forceUpdate(this);
    }

    @OnClick(R.id.sav_setting_share)
    public void clickShare(View view) {
        //从友盟动态获取分享时显示的标题、内容以及下载地址
        String targetUrl = OnlineConfigAgent.getInstance().getConfigParams(this, "shareTargetUrl");
        String content = OnlineConfigAgent.getInstance().getConfigParams(this, "shareContent");
        String title = OnlineConfigAgent.getInstance().getConfigParams(this, "shareTitle");
        UMImage umImage = new UMImage(this, R.mipmap.ic_launcher);
        // 首先在您的Activity中添加如下成员变量
        final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
        // 设置分享内容
        mController.setShareContent(content);

        // 添加短信
        SmsHandler smsHandler = new SmsHandler();
        smsHandler.addToSocialSDK();


        //参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, getString(R.string.qq_app_id), getString(R.string.qq_app_key));
        qqSsoHandler.addToSocialSDK();

        QQShareContent qqShareContent = new QQShareContent();
        //设置分享文字
        qqShareContent.setShareContent(content);
        //设置分享title
        qqShareContent.setTitle(title);
        //设置分享图片
        qqShareContent.setShareImage(umImage);
        //设置点击分享内容的跳转链接
        qqShareContent.setTargetUrl(targetUrl);
        mController.setShareMedia(qqShareContent);

        QZoneShareContent qzone = new QZoneShareContent();
        //设置分享文字
        qzone.setShareContent(content);
        //设置点击消息的跳转URL
        qzone.setTargetUrl(targetUrl);
        //设置分享内容的标题
        qzone.setTitle(title);
        //设置分享图片
        qzone.setShareImage(umImage);
        mController.setShareMedia(qzone);

        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, getString(R.string.qq_app_id), getString(R.string.qq_app_key));
        qZoneSsoHandler.addToSocialSDK();

        //微信分享
        String appId = getString(R.string.wx_app_id);
        String appSecret = getString(R.string.wx_app_secret);
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(this, appId, appSecret);
        wxHandler.addToSocialSDK();
        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(this, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();

        //设置微信好友分享内容
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        //设置分享文字
        weixinContent.setShareContent(content);
        //设置title
        weixinContent.setTitle(title);
        //设置分享内容跳转URL
        weixinContent.setTargetUrl(targetUrl);
        //设置分享图片
        weixinContent.setShareImage(umImage);
        mController.setShareMedia(weixinContent);

        //设置微信朋友圈分享内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(content);
        //设置朋友圈title
        circleMedia.setTitle(title);
        //朋友圈分享设置图片就不会显示内容
        //        circleMedia.setShareImage(umImage);
        circleMedia.setTargetUrl(targetUrl);
        mController.setShareMedia(circleMedia);

        //setWeiXin(mController);
        //        mController.getConfig().setPlatforms(SHARE_MEDIA.RENREN, SHARE_MEDIA.DOUBAN,
        //                SHARE_MEDIA.TENCENT,SHARE_MEDIA.SMS);
        mController.getConfig().removePlatform(SHARE_MEDIA.SINA);
        // 是否只有已登录用户才能打开分享选择页
        mController.openShare(this, false);
    }

    //去市场评分
    @OnClick(R.id.sav_setting_ping)
    public void clickPing(View view) {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.next_in, R.anim.next_out);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "啊哦~未找到应用市场", Toast.LENGTH_SHORT).show();
        }
    }
}
