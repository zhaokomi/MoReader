# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.mochen.reader.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep data classes for serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Compose classes
-keep class androidx.compose.** { *; }

# NanoHTTPD
-keep class fi.iki.elonen.** { *; }
-keep class org.nanohttpd.** { *; }

# EPUB library
-keep class nl.siegmann.epublib.** { *; }

# Jsoup
-keeppackagenames org.jsoup.nodes

# PDF Viewer
-keep class com.github.barteksc.** { *; }
