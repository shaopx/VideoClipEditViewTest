# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Java\AndroidStudio\SDK/tools/proguard/proguard-android.txt
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
-optimizationpasses 5
-keepattributes Signature
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-dontoptimize
-microedition

-dontwarn android.os.**
-dontwarn java.nio.file.**
-dontwarn javax.naming.**
-dontwarn org.codehaus.**
-dontwarn junit.textui.**

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.app.Fragment


-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {*;}
-keep class * implements java.io.Serializable {*;}
#-keep class * implements java.lang.Runnable {*;}

# 1.9.0新加入的model没有完全实现parcel，所以会被混淆，暂时让这个包下面的所有model不被混淆
# 2.0.0版本的时候将新加入的类全部实现parcel
-keep class com.sogou.groupwenwen.model.layoutdata.**{*;}
# 3.1.0 版本工程拆分，部分model移动到net这个lib下面
-keep class com.sogo.model.**{*;}

-keep class org.apache.commons.codec.**
-keep interface org.apache.commons.codec.**
-keepclasseswithmembers class org.apache.commons.codec.** { <methods>; <fields>; }
-keepclasseswithmembers class org.apache.commons.codec.** { static <methods>; }
-keepclasseswithmembers class org.apache.commons.codec.** { static <fields>; }
-keepclasseswithmembers interface org.apache.commons.codec.** { <methods>; }
-keepclasseswithmembers interface org.apache.commons.codec.** { static <fields>; }

-keep class com.google.gson.**
-keep interface com.google.gson.**
-keepclasseswithmembers class com.google.gson.** { <methods>; <fields>; }
-keepclasseswithmembers class com.google.gson.** { static <methods>; }
-keepclasseswithmembers class com.google.gson.** { static <fields>; }
-keepclasseswithmembers interface com.google.gson.** { <methods>; }
-keepclasseswithmembers interface com.google.gson.** { static <fields>; }
-keep class com.google.**{*;}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

-keep class com.google.gdata.util.common.base.**
-keep interface com.google.gdata.util.common.base.**
-keepclasseswithmembers class com.google.gdata.util.common.base.** { <methods>; <fields>; }
-keepclasseswithmembers class com.google.gdata.util.common.base.** { static <methods>; }
-keepclasseswithmembers class com.google.gdata.util.common.base.** { static <fields>; }
-keepclasseswithmembers interface com.google.gdata.util.common.base.** { <methods>; }
-keepclasseswithmembers interface com.google.gdata.util.common.base.** { static <fields>; }

-keep class oauth.signpost.**
-keep interface oauth.signpost.**
-keepclasseswithmembers class oauth.signpost.** { <methods>; <fields>; }
-keepclasseswithmembers class oauth.signpost.** { static <methods>; }
-keepclasseswithmembers class oauth.signpost.** { static <fields>; }
-keepclasseswithmembers interface oauth.signpost.** { <methods>; }
-keepclasseswithmembers interface oauth.signpost.** { static <fields>; }



-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class android.content.pm.** { *;}

-keep class android.net.http.** {*;}
-keep class android.support.v4.** {*;}
-keep class android.support.v7.** {*;}
-dontwarn android.support.**
#-keep class com.squareup.okhttp.** {*;}
-keep class okhttp3.** {*;}
-keep class org.bouncycastle.** {*;}
-keep class org.apache.commons.** {*;}

-keep class org.eclipse.jdt.annotation.**{*;}
-dontwarn org.eclipse.jdt.annotation.**
-dontwarn javax.annotation.**

#=============================fresco begin ===========================
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keep @com.facebook.animated.gif.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}
-keep class com.facebook.imagepipeline.** { *; }
-keep class com.facebook.animated.gif.** {
    *;
}
-dontwarn okio.**
#-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
#=============================fresco end ===========================

#=======================tencent sdk begin====================
-keep class com.tencent.** {*;}
-keep class com.tencent.mm.sdk.** {
   *;
}
-dontwarn com.tencent.**
-keepclassmembers class ** {
    public void on*Event(...);
}
#=======================tencent sdk end====================
#=======================greenDao begin====================
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
#=======================greenDao end====================
#===================youmeng sdk begin====================
-keep public class com.sogou.groupwenwen.R$*{
public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#===================youmeng sdk end====================

#===================Tencent Bugly===================
-keep public class com.tencent.bugly.**{*;}

# 删除搜狗推送
#===================Sogou Push======================
#-keep public class com.sogou.udp.push.**{*;}
#-dontwarn com.sogou.udp.push.PushServiceReceiver
#-dontwarn com.sogou.udp.push.notification.**


#====================Sogou Passport==================
-keep public class com.sogou.passportsdk.**{*;}
-dontwarn com.sogou.passportsdk.**


#============================JSBridge==============================
-keep public class com.sogou.groupwenwen.hybrid.CoreBridge{*;}

#======================share QQ ============================
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
#==============================QQ Login SDK=========================================
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keep class oicq.wlogin_sdk.**{*;}
-keep class oicq.wlogin_sdk.request.**{*;}
-keep class oicq.wlogin_sdk.quicklogin.**{*;}
#======================================eventbus start======================================
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#======================================eventbus end========================================

#======================================ffmpeg4android_lib begin========================================
-dontwarn com.github.hiteshsondhi88.libffmpeg.**
-keep class com.github.hiteshsondhi88.libffmpeg.** {*;}
-keep class com.github.hiteshsondhi88.libffmpeg.exceptions.**{*;}
#======================================ffmpeg4android_lib end========================================

#这里com.xiaomi.mipushdemo.DemoMessageRreceiver改成app中定义的完整类名
-keep class com.sogou.groupwenwen.push.MiPushMessageReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

#======================================tinker use==================================
-keep public class com.sogou.groupwenwen.TinkerApplicationLike
-keep public class com.previewlibrary.enitity.ThumbViewInfo

#=======================glide====================================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
#=============================================================================

#======================retrofit=======================================================================
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
#===========================================================================================

#===================rxjava=====================================================================
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#===============================================================================

#=============================================================================
-dontwarn org.bytedeco.javacv.**
-dontwarn org.bytedeco.javacpp.**
-dontwarn org.apache.maven.**
-keep class org.bytedeco.javacv.** {*;}
-keep class org.bytedeco.javacpp.** {*;}
-keep class org.apache.maven.** {*;}

-dontwarn com.googlecode.**
-keep class com.googlecode.**{*;}
-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable

-keepattributes *Annotation*

# JavaCV
-keep @org.bytedeco.javacpp.annotation interface * {
    *;
}
-keep @org.bytedeco.javacpp.annotation.Platform public class *

-keepclasseswithmembernames class * {
    @org.bytedeco.* <fields>;
}

-keepclasseswithmembernames class * {
    @org.bytedeco.* <methods>;
}

-keepattributes EnclosingMethod
-keep @interface org.bytedeco.javacpp.annotation.*,javax.inject.*


-keepattributes *Annotation*, Exceptions, Signature, Deprecated, SourceFile, SourceDir, LineNumberTable, Synthetic, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault, InnerClasses

-keep class org.bytedeco.javacpp.** {*;}
-dontwarn java.awt.**
-dontwarn org.bytedeco.javacv.**
-dontwarn org.bytedeco.javacpp.**
#==========================================================================

#============================================================================
#ButterKnife混淆配置
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}
#=============================================================================

# 华为Push服务
-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}

-keep class com.huawei.gamebox.plugin.gameservice.**{*;}

-keep public class com.huawei.android.hms.agent.** extends android.app.Activity { public *; protected *; }
-keep interface com.huawei.android.hms.agent.common.INoProguard {*;}
-keep class * extends com.huawei.android.hms.agent.common.INoProguard {*;}

# 个推Push服务
-dontwarn com.igexin.**
-keep class com.igexin.** { *; }
-keep class org.json.** { *; }

-keep class android.support.v4.app.NotificationCompat { *; }
-keep class android.support.v4.app.NotificationCompat$Builder { *; }