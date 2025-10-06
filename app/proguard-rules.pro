# Add project specific ProGuard rules here.
-keep class com.google.api.** { *; }
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
