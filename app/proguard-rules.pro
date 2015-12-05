# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.android.internal.telephony.ITelephony
-keep class com.android.internal.telephony.ITelephony{*;}

#有米广告
-dontwarn net.youmi.android.**
-keep class net.youmi.android.** {
    *;
}

#ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#友盟服务
-dontwarn com.umeng.**
-dontwarn u.upd.**
-dontwarn org.apache.http.entity.mime.**
-dontwarn com.tencent.**
-dontwarn u.aly.**

-keep class com.umeng.** {*;}
-keep class u.upd.** {*;}
-keep class org.apache.http.entity.mime.** {*;}
-keep class com.tencent.** {*;}
-keep class u.aly.** {*;}
