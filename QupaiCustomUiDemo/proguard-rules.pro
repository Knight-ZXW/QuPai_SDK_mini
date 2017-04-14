# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/leaf_yyl/tools/android-sdk-linux/tools/proguard/proguard-android.txt
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
-keep class com.duanqu.**
-keepclassmembers class com.duanqu.** {
    *;
}
-dontwarn com.google.common.net.**
-dontwarn com.amap.api.**
-dontwarn net.jcip.annotations.**

-keepattributes Annotation,EnclosingMethod,Signature,InnerClasses
-dontwarn com.google.auto.factory.**
-ignorewarnings
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }