# SecureOTP - Production ProGuard Rules

# Keep NotificationListenerService
-keep class com.otpextractor.secureotp.service.OtpListener { *; }
-keep class android.service.notification.NotificationListenerService { *; }

# Keep BroadcastReceiver
-keep class com.otpextractor.secureotp.receiver.BootReceiver { *; }

# Keep Application class
-keep class com.otpextractor.secureotp.OtpExtractorApp { *; }

# Keep utils
-keep class com.otpextractor.secureotp.utils.** { *; }

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(android.view.LayoutInflater);
    public static *** bind(android.view.View);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Kotlin
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Keep attributes
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*,SourceFile,LineNumberTable
