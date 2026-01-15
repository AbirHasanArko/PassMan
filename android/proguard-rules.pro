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

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.awt.Component
-dontwarn java.awt.Container
-dontwarn java.awt.Dimension
-dontwarn java.awt.FlowLayout
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Image
-dontwarn java.awt.LayoutManager
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.RenderedImage
-dontwarn java.awt.image.WritableRaster
-dontwarn java.sql.JDBCType
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.spi.ImageInputStreamSpi
-dontwarn javax.imageio.spi.ImageOutputStreamSpi
-dontwarn javax.imageio.spi.ImageReaderSpi
-dontwarn javax.imageio.spi.ImageWriterSpi
-dontwarn javax.swing.Icon
-dontwarn javax.swing.ImageIcon
-dontwarn javax.swing.JFileChooser
-dontwarn javax.swing.JFrame
-dontwarn javax.swing.JLabel
-dontwarn javax.swing.JPanel
-dontwarn javax.swing.JTextArea
-dontwarn javax.swing.SwingUtilities
-dontwarn javax.swing.text.JTextComponent
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

#proguard
# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# ZXing (and zxing-android-embedded)
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Gson (often needed with data classes)
# If you have data/model classes in a specific package, e.g., com.passman.core.models
# it's a good practice to keep them.
-keep class com.passman.core.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
