# Project ProGuard / R8 rules.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

# Google Mobile Ads Next Gen SDK
-keep class com.google.android.libraries.ads.mobile.sdk.** { *; }

# kotlinx.serialization: history rows store ScanContent / QrStyle as JSON.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.**$$serializer { *; }
-keepclassmembers class com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.** { *** Companion; }
-keepclasseswithmembers class com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Type-safe navigation routes are @Serializable objects/classes (covered above); keep their names for saved state.
-keepnames class com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.**

# Crashlytics readable stack traces
-keep public class * extends java.lang.Exception
