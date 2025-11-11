# NanoBanana ProGuard Rules
# Optimized for production builds with code shrinking enabled

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*, Signature, Exception

# Google AI / Firebase
-keep class com.google.ai.** { *; }
-keep class com.google.firebase.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep domain models (used in serialization and reflection)
-keep class com.yunho.nanobanana.domain.model.** { *; }

# Keep data classes used in API responses
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Dependency Injection
-keep class javax.inject.** { *; }
-keep class * implements javax.inject.Provider

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Aggressive optimizations
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove unused code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
