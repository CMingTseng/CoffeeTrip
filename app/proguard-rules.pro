# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/louis/Library/Android/sdk/tools/proguard/proguard-android.txt
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

-optimizations 5

-keep class tw.com.louis383.coffeefinder.model.entity.** { *; }
-keep class tw.com.louis383.coffeefinder.model.domain.** { *; }
-keep interface tw.com.louis383.coffeefinder.model.CoffeeTripService { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Google Play Service
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Android Support Library
-keep public class android.support.** { *; }

# Gson
-keepattributes *Annotation*

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp 3
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.*

