# PassMan Android ProGuard Rules

# Keep all model classes
-keep class com.passman.android.data.model.** { *; }
-keep class com.passman.android.data.entity.** { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep crypto classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# Keep ZXing QR code
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Keep Lottie
-keep class com.airbnb.lottie.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# General optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose
